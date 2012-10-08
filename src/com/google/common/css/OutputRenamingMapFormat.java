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
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
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
   * Writes the mapping as JSON, passed as an argument to
   * {@code goog.setCssNameMapping()}. Designed for use with the Closure
   * Library in compiled mode.
   */
  CLOSURE_COMPILED("goog.setCssNameMapping(%s);\n"),

  /**
   * Before writing the mapping as CLOSURE_COMPILED, split the css name maps
   * by hyphens and write out each piece individually. see
   * {@code CLOSURE_COMPILED}
   */
  CLOSURE_COMPILED_SPLIT_HYPHENS {
    @Override
    public void writeRenamingMap(Map<String, String> renamingMap,
        PrintWriter renamingMapWriter)  {
      Map<String, String> newSplitRenamingMap = Maps.newHashMap();
      for (Map.Entry<String, String> entry : renamingMap.entrySet()) {
        Iterator<String> parts =
            HYPHEN_SPLITTER.split(entry.getKey()).iterator();
        Iterator<String> partsNew =
            HYPHEN_SPLITTER.split(entry.getValue()).iterator();
        while(parts.hasNext() && partsNew.hasNext()) {
          newSplitRenamingMap.put(parts.next(), partsNew.next());
        }
        if (parts.hasNext()) {
          throw new AssertionError("Not all parts of the original class " +
              "name were output. Class: " + entry.getKey() + " Next Part:" +
              parts.next());
        }
        if (partsNew.hasNext()) {
          throw new AssertionError("Not all parts of the renamed class were " +
              "output. Class: " + entry.getKey() + " Renamed Class: " +
              entry.getValue() + " Next Part:" + partsNew.next());
        }
      }
      OutputRenamingMapFormat.CLOSURE_COMPILED.writeRenamingMap(
          newSplitRenamingMap, renamingMapWriter);
    }
  },

  /**
   * Writes the mapping as JSON, assigned to the global JavaScript variable
   * {@code CLOSURE_CSS_NAME_MAPPING}. Designed for use with the Closure
   * Library in uncompiled mode.
   */
  CLOSURE_UNCOMPILED("CLOSURE_CSS_NAME_MAPPING = %s;\n"),

  /**
   * Writes the mapping as JSON.
   */
  JSON,

  /**
   * Writes the mapping in a .properties file format, such that it can be read
   * by {@link Properties}.
   */
  PROPERTIES {
    @Override
    public void writeRenamingMap(Map<String, String> renamingMap,
        PrintWriter renamingMapWriter)  {
      // We write the properties directly rather than using
      // Properties#store() because it is impossible to suppress the timestamp
      // comment: http://goo.gl/6hsrN. As noted on the Stack Overflow thread,
      // the timestamp results in unnecessary diffs between runs. Further, those
      // who are using a language other than Java to parse this file should not
      // have to worry about adding support for comments. 
      for (Map.Entry<String, String> entry : renamingMap.entrySet()) {
        renamingMapWriter.format("%s=%s\n", entry.getKey(), entry.getValue());
      }
    }
  },

  /**
   * This is the current default behavior for output maps. Still used for
   * legacy reasons.
   */
  JSCOMP_VARIABLE_MAP {
    @Override
    public void writeRenamingMap(Map<String, String> renamingMap,
        PrintWriter renamingMapWriter)  {
      for (Map.Entry<String, String> entry : renamingMap.entrySet()) {
        renamingMapWriter.format("%s:%s\n", entry.getKey(), entry.getValue());
      }
    }
  };

  // Splitter used for CLOSURE_COMPILED_SPLIT_HYPHENS format.
  private static final Splitter HYPHEN_SPLITTER = Splitter.on("-");

  private final String formatString;

  private OutputRenamingMapFormat(String formatString) {
    Preconditions.checkNotNull(formatString);
    this.formatString = formatString;
  }

  private OutputRenamingMapFormat() {
    this("%s");
  }

  /**
   * @see com.google.common.css.compiler.commandline.DefaultCommandLineCompiler
   *     #writeRenamingMap(Map, PrintWriter)
   */
  public void writeRenamingMap(Map<String, String> renamingMap,
      PrintWriter renamingMapWriter) {
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
}
