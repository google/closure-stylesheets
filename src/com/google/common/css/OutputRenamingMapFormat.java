/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.css;


import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.escape.CharEscaperBuilder;
import com.google.common.escape.Escaper;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Defines the values for the --output-renaming-map-format flag in Closure
 * Stylesheets.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public enum OutputRenamingMapFormat {
  /**
   * Reads/Writes the mapping as JSON, passed as an argument to
   * {@code goog.setCssNameMapping()}. Designed for use with the Closure
   * Library in compiled mode.
   */
  CLOSURE_COMPILED("goog.setCssNameMapping(%s);\n"),

  /**
   * Reads/Writes the mapping as JSON, passed as an argument to
   * {@code goog.setCssNameMapping()} using the 'BY_WHOLE' mapping style.
   * Designed for use with the Closure Library in compiled mode where the CSS
   * name substitutions are taken as-is, which allows, e.g., using
   * {@code SimpleSubstitutionMap} with class names containing hyphens.
   */
  CLOSURE_COMPILED_BY_WHOLE("goog.setCssNameMapping(%s, 'BY_WHOLE');\n"),

  /**
   * Before writing the mapping as CLOSURE_COMPILED, split the css name maps by hyphens and write
   * out each piece individually. see {@code CLOSURE_COMPILED}
   */
  CLOSURE_COMPILED_SPLIT_HYPHENS("goog.setCssNameMapping(%s);\n") {
    @Override
    public void writeRenamingMap(Map<String, String> renamingMap, Writer renamingMapWriter)
        throws IOException {
      super.writeRenamingMap(splitEntriesOnHyphens(renamingMap), renamingMapWriter);
    }
  },

  /**
   * Reads/Writes the mapping as JSON, assigned to the global JavaScript variable
   * {@code CLOSURE_CSS_NAME_MAPPING}. Designed for use with the Closure
   * Library in uncompiled mode.
   */
  CLOSURE_UNCOMPILED("CLOSURE_CSS_NAME_MAPPING = %s;\n"),

  /**
   * Reads/Writes the mapping as JSON.
   */
  JSON,

  /**
   * Reads/Writes the mapping from/in a .properties file format, such that it can be read
   * by {@link Properties}.
   */
  PROPERTIES {
    @Override
    public void writeRenamingMap(Map<String, String> renamingMap, Writer renamingMapWriter)
        throws IOException {
      writeOnePerLine('=', renamingMap, renamingMapWriter);
      // We write the properties directly rather than using
      // Properties#store() because it is impossible to suppress the timestamp
      // comment: http://goo.gl/6hsrN. As noted on the Stack Overflow thread,
      // the timestamp results in unnecessary diffs between runs. Further, those
      // who are using a language other than Java to parse this file should not
      // have to worry about adding support for comments.
    }

    @Override
    void readMapInto(
        BufferedReader in, ImmutableMap.Builder<? super String, ? super String> builder)
        throws IOException {
      readOnePerLine('=', in, builder);
    }
  },

  /**
   * This is the current default behavior for output maps. Still used for
   * legacy reasons.
   */
  JSCOMP_VARIABLE_MAP {
    @Override
    public void writeRenamingMap(Map<String, String> renamingMap, Writer renamingMapWriter)
        throws IOException {
      writeOnePerLine(':', renamingMap, renamingMapWriter);
    }

    @Override
    void readMapInto(
        BufferedReader in, ImmutableMap.Builder<? super String, ? super String> builder)
        throws IOException {
      readOnePerLine(':', in, builder);
    }
  };

  private final String formatString;

  private OutputRenamingMapFormat(String formatString) {
    Preconditions.checkNotNull(formatString);
    this.formatString = formatString;
  }

  private OutputRenamingMapFormat() {
    this("%s");
  }

  /**
   * Writes the renaming map.
   *
   * @see com.google.common.css.compiler.commandline.DefaultCommandLineCompiler
   *     #writeRenamingMap(Map, PrintWriter)
   */
  public void writeRenamingMap(Map<String, String> renamingMap, Writer renamingMapWriter)
      throws IOException {
    // Build up the renaming map as a JsonObject.
    JsonObject properties = new JsonObject();
    for (Map.Entry<String, String> entry : renamingMap.entrySet()) {
      properties.addProperty(entry.getKey(), entry.getValue());
    }

    // Write the JSON wrapped in this output format's formatString.
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    renamingMapWriter.write(String.format(formatString,
        gson.toJson(properties)));
  }

  /**
   * Like {@writeRenamingMap(java.util.Map, java.io.Writer)} but does not throw when writes fail.
   */
  public final void writeRenamingMap(
      Map<String, String> renamingMap, PrintWriter renamingMapWriter) {
    try {
      writeRenamingMap(renamingMap, (Writer) renamingMapWriter);
    } catch (IOException ex) {
      throw (AssertionError) new AssertionError("IOException from PrintWriter").initCause(ex);
    }
  }

  /**
   * Reads the output of {@link #writeRenamingMap} so a renaming map can be reused from one compile
   * to another.
   */
  public ImmutableMap<String, String> readRenamingMap(Reader in) throws IOException {
    String subsitutionMarker = "%s";
    int formatStringSubstitutionIndex = formatString.indexOf(subsitutionMarker);
    Preconditions.checkState(formatStringSubstitutionIndex >= 0, formatString);

    String formatPrefix = formatString.substring(0, formatStringSubstitutionIndex);
    String formatSuffix =
        formatString.substring(formatStringSubstitutionIndex + subsitutionMarker.length());

    // GSON's JSONParser does not stop reading bytes when it sees a bracket that
    // closes the value.
    // We read the whole input in, then strip prefixes and suffixes and then parse
    // the rest.
    String content = CharStreams.toString(in);

    content = content.trim();
    formatPrefix = formatPrefix.trim();
    formatSuffix = formatSuffix.trim();

    if (!content.startsWith(formatPrefix)
        || !content.endsWith(formatSuffix)
        || content.length() < formatPrefix.length() + formatSuffix.length()) {
      throw new IOException("Input does not match format " + formatString + " : " + content);
    }

    content = content.substring(formatPrefix.length(), content.length() - formatSuffix.length());

    ImmutableMap.Builder<String, String> b = ImmutableMap.builder();
    BufferedReader br = new BufferedReader(new StringReader(content));
    readMapInto(br, b);
    requireEndOfInput(br);

    return b.build();
  }

  /**
   * Reads the mapping portion of the formatted output.
   *
   * <p>This default implementation works for formats that substitute a JSON mapping from rewritten
   * names to originals into their format string, and may be overridden by formats that do something
   * different.
   */
  void readMapInto(BufferedReader in, ImmutableMap.Builder<? super String, ? super String> builder)
      throws IOException {
    JsonElement json = new JsonParser().parse(in);
    for (Map.Entry<String, JsonElement> e : json.getAsJsonObject().entrySet()) {
      builder.put(e.getKey(), e.getValue().getAsString());
    }
  }

  /**
   * Raises an IOException if there are any non-space characters on in, and consumes the remaining
   * characters on in.
   */
  private static void requireEndOfInput(BufferedReader in) throws IOException {
    for (int ch; (ch = in.read()) >= 0; ) {
      if (!Character.isSpace((char) ch)) {
        throw new IOException("Expected end of input, not '" + escape((char) ch) + "'");
      }
    }
  }

  private static final Escaper ESCAPER =
      new CharEscaperBuilder()
          .addEscape('\t', "\\t")
          .addEscape('\n', "\\n")
          .addEscape('\r', "\\r")
          .addEscape('\\', "\\\\")
          .addEscape('\'', "\\'")
          .toEscaper();

  private static String escape(char ch) {
    return ESCAPER.escape(new String(new char[] {ch}));
  }

  /** Splitter used for CLOSURE_COMPILED_SPLIT_HYPHENS format. */
  private static final Splitter HYPHEN_SPLITTER = Splitter.on("-");

  /**
   * <code>{ "foo-bar": "f-b" }</code> => <code>{ "foo": "f", "bar": "b" }</code>.
   *
   * @see SplittingSubstitutionMap
   */
  private static Map<String, String> splitEntriesOnHyphens(Map<String, String> renamingMap) {
    Map<String, String> newSplitRenamingMap = Maps.newLinkedHashMap();
    for (Map.Entry<String, String> entry : renamingMap.entrySet()) {
      Iterator<String> keyParts = HYPHEN_SPLITTER.split(entry.getKey()).iterator();
      Iterator<String> valueParts = HYPHEN_SPLITTER.split(entry.getValue()).iterator();
      while (keyParts.hasNext() && valueParts.hasNext()) {
        String keyPart = keyParts.next();
        String valuePart = valueParts.next();
        String oldValuePart = newSplitRenamingMap.put(keyPart, valuePart);
        // Splitting by part to make a simple map shouldn't involve mapping two old names
        // to the same new name.  It's ok the other way around, but the part relation should
        // be a partial function.
        Preconditions.checkState(oldValuePart == null || oldValuePart.equals(valuePart));
      }
      if (keyParts.hasNext()) {
        throw new AssertionError(
            "Not all parts of the original class "
                + "name were output. Class: "
                + entry.getKey()
                + " Next Part:"
                + keyParts.next());
      }
      if (valueParts.hasNext()) {
        throw new AssertionError(
            "Not all parts of the renamed class were "
                + "output. Class: "
                + entry.getKey()
                + " Renamed Class: "
                + entry.getValue()
                + " Next Part:"
                + valueParts.next());
      }
    }
    return newSplitRenamingMap;
  }

  private static void writeOnePerLine(
      char separator, Map<String, String> renamingMap, Writer renamingMapWriter)
      throws IOException {
    for (Map.Entry<String, String> entry : renamingMap.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      Preconditions.checkState(key.indexOf(separator) < 0);
      Preconditions.checkState(key.indexOf('\n') < 0);
      Preconditions.checkState(value.indexOf('\n') < 0);

      renamingMapWriter.write(key);
      renamingMapWriter.write(separator);
      renamingMapWriter.write(value);
      renamingMapWriter.write('\n');
    }
  }

  private static void readOnePerLine(
      char separator,
      BufferedReader in,
      ImmutableMap.Builder<? super String, ? super String> builder)
      throws IOException {
    for (String line; (line = in.readLine()) != null; ) {
      int eq = line.indexOf(separator);
      if (eq < 0 && !line.isEmpty()) {
        throw new IOException("Line is missing a '" + separator + "': " + line);
      }
      builder.put(line.substring(0, eq), line.substring(eq + 1));
    }
  }
}
