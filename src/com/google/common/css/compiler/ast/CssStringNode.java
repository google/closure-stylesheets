/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.common.css.compiler.ast;

import com.google.common.base.Function;
import com.google.common.css.SourceCodeLocation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Node corresponding to a string value.
 * <p>
 * TODO(user): refactor the encoding/decoding logic out of this
 * class, thereby restoring the architectural integrity of the CssNode
 * type hierarchy; all these classes should be value types with
 * no elaborate behaviors.
 * <p>
 * This node represents both a CSS fragment and the abstract value
 * represented by that concrete syntax. For example, the following
 * three declarations mean precisely the same thing in CSS:
 * {@code
 *   .warning { content-before: 'warning:'; }
 *   .warning { content-before: "warning:"; }
 *   .warning { content-before: 'war\06e ing:"; }
 * }
 * <p>
 * Some clients care about concrete CSS syntax. For high-fidelity
 * roundtrip CSS processing it is necessary to preserve the original
 * author's choice of quote character. On the other hand, some clients
 * care about abstract values. For purposes of machine translation or
 * typeface resolution, we are uninterested in the differences that
 * distinguish the cases shown above; in these applications we would
 * like to deal in terms of the simple Java String {@code warning:}.
 * <p>
 * Java's {@code Character} and {@code String} classes represent
 * values in the UTF-16 encoding; some codepoints are represented by
 * surrogate pairs of {@code Character}s. CSS escape sequences are
 * designed without a particular encoding in mind; there are CSS
 * escape sequences that correspond to a single Unicode character
 * and multiple Java {@code Character}s.
 * <p>
 * Java's {@code Character} repertoire is a strict subset of the
 * codepoints that can be represented in CSS. When decoding CSS
 * escape sequences, this class substitutes the Unicode replacement
 * character for characters that cannot be represented in Java
 * {@code Strings}, as permitted by
 * http://www.w3.org/TR/CSS2/syndata.html#characters
 *
 */
public class CssStringNode extends CssValueNode {

  private static final String LINE_BREAK_PATTERN_STRING =
    "(?:\\n|\\r\\n|\\r|\\f)";

  private static final Pattern ESCAPE_CHAR_STRING_CONTINUATION_PATTERN =
    Pattern.compile("\\\\" + LINE_BREAK_PATTERN_STRING);
  private static final Pattern ESCAPE_CHAR_NOT_SPECIAL =
    Pattern.compile("\\\\([^0-9a-fA-F\\n\\r\\f])");
  private static final Pattern ESCAPE_CHAR_HARD_TO_TYPE =
    Pattern.compile("\\\\([0-9a-fA-F]{1,6})(\\r\\n|[ \\t\\r\\n\\f])?");

  /** The pattern for HTML markup special characters */
  private static final Pattern HTML_PATTERN =
    Pattern.compile("[<>\"&']");

  private static final Pattern LINE_BREAK_PATTERN =
      Pattern.compile("\\\\" + LINE_BREAK_PATTERN_STRING);

  private static final Pattern HEX_PATTERN =
      Pattern.compile("[0-9a-fA-F]+");

  private static final Pattern WIDE_NONASCII_PATTERN =
      Pattern.compile("\\P{ASCII}");

  private final Type type;

  /**
   * The characters between the quotes in concrete CSS syntax.  Some
   * clients will want exact control and high fidelity for values in
   * these nodes. Some will expect an AST to disregard unimportant
   * detail and provide convenient access to a normalized
   * representation of the stylesheet. This field stores a verbatim
   * snippet of CSS corresponding to this node.
   */
  private String concreteValue;

  /**
   * Constructor of a string node.
   *
   * @param type CSS provides multiple syntax alternatives for strings;
   *     which was used for this term?
   * @param value the Java String representation of this string (not its
   *     concrete CSS syntax)
   * @param location The location in source code corresponding to this node
   */
  public CssStringNode(Type type, SourceCodeLocation location) {
    super("", location);
    setConcreteValue(location.getSourceCode().getFileContents()
                     .substring(location.getBeginCharacterIndex()
                                // for the quote
                                + 1,
                                // we end on the quote, so no need to adjust
                                location.getEndCharacterIndex()));
    this.type = type;
  }

  /**
   * Constructor of a string node.
   *
   * @param type CSS provides multiple syntax alternatives for strings;
   *     which was used for this term?
   * @param value the Java String representation of this string (not its
   *     concrete CSS syntax)
   */
  public CssStringNode(Type type, String value) {
    super(value, /* location */ null);
    this.type = type;
    setValue(value);
  }

  /**
   * Copy constructor.
   */
  public CssStringNode(CssStringNode node) {
    super(node);
    type = node.type;
    this.concreteValue = node.getConcreteValue();
  }

  public Type getType() {
    return type;
  }

  /**
   * Specifies the characters that should appear between the quotes
   * when this node is written as CSS, and updates this node's value
   * accordingly.
   * <p>
   * For example, the Java method invocation: {@code
   *   n.setConcreteValue("hi\\\"");
   * }
   * could result in the CSS: {@code
   *   p { content-after: "hi\""; }
   * } or perhaps {@code
   *   p { content-after: 'hi\"'; }
   * }, depending on the {@code CssStringNode.Type} of {@code n} and
   * the {@code CssTree} in which it occurs, but it would never
   * result in {@code
   *   p { content-after: "hi\000022"; }
   * }
   */
  public String setConcreteValue(String concreteValue) {
    this.concreteValue = concreteValue;
    super.setValue(unescape(concreteValue));
    return concreteValue;
  }

  /**
   * Retrieves the characters that should appear between the quotes
   * when this node is written in concrete CSS syntax.
   */
  public String getConcreteValue() {
    return concreteValue;
  }

  /**
   * Establishes a value for this node by conservatively escaping
   * {@code value} and delegating to {@link #setConcreteValue} to
   * maintain consistency between the {@link #value} and the
   * {@link #concreteValue}.
   * <p>
   * This function stores a normalized representation of the given
   * {@code value}; if you want to work in more exact terms, try
   * {@link setConcreteValue}.
   * <p>
   * For example, the Java snippet: {@code
   *   n.setValue("Senator's Response")
   * } could result in the CSS snippet: {@code
   *   p { content-before: "Senator's Response"; }
   * }
   * or {@code
   *   p { content-before: 'Senator\'s Response'; }
   * }
   * or {@code
   *   p { content-before: 'Senator\27 s Response'; }
   * }, depending on the {@code CssStringNode.Type} of {@code n} and
   * the {@code CssTree} in which it occurs and the choice of the
   * {@code CssTreeVisitor} that renders the output.
   * <p>
   * Note that the {@code value} parameter here will normally not
   * begin or end with a quotation mark.
   */
  @Override
  public void setValue(String value) {
    setConcreteValue(escape(type, HTML_ESCAPER, value));
  }

  @Override
  public CssStringNode deepCopy() {
    return new CssStringNode(this);
  }

  @Override
  public String toString() {
    return type.toString(getValue(), SHORT_ESCAPER);
  }

  /**
   * Determines the canonical Java String representation of a value encoded
   * in CSS syntax.
   *
   * @param escaped whatever lies between the quotes (excluding the quotes
   *     themselves).
   */
  public static String unescape(String escaped) {
    String result =
        ESCAPE_CHAR_STRING_CONTINUATION_PATTERN.matcher(escaped).replaceAll("");
    result = ESCAPE_CHAR_NOT_SPECIAL.matcher(result).replaceAll("$1");
    Matcher unicode = ESCAPE_CHAR_HARD_TO_TYPE.matcher(result);
    StringBuffer sb = new StringBuffer();
    while (unicode.find()) {
      // ESCAPE_CHAR_HARD_TO_TYPE recognizes trailing whitespace, which is only
      // sometimes part of the escape sequence.
      String trailer = "";
      if (unicode.group(2) != null && unicode.group(2).length() > 0) {
        // Divide the string into three parts: prefix whitespace suffix.
        // A trailing space is part of the escape sequence when the
        // prefix has fewer than 6 characters and the first character of the
        // suffix matches [0-9a-fA-F].
        if (unicode.group(1).length() < 6
            && result.length() >= unicode.end()
            && HEX_PATTERN.matcher(result.substring(unicode.end(),
                                                    unicode.end() + 1))
                .matches()) {
          // let the whitespace be replaced as part of the escape sequence
        } else {
          // use the whitespace in the replacement following the escape
          // sequence interpretation.
          trailer = unicode.group(2);
        }
      }
      // CSS allows us to substitute characters above 0x110000. Java
      // requires us to stay at or below MAX_CODE_POINT. If we are
      // allowed to substitute, and Java requires us to substitute,
      // then we substitute. Otherwise: (a) everything's fine without
      // substitution or (b) CSS does not permit a substitution we
      // need to make for Java's happiness or (c) CSS allows a
      // substitution but we don't need it. For (a) and (c) we don't
      // substitute and that's fine. For (b) we don't substitute,
      // probably that will produce an exception below, and then we'll
      // know it's worth thinking about that case some more.
      int codepoint = Integer.parseInt(unicode.group(1), 16);
      if (codepoint > 0x10FFFF && codepoint > Character.MAX_CODE_POINT) {
        // CSS allows us to substitute, and Java requires us not to use the
        // character we were given, so here is a character specifically
        // for replacements:
        codepoint = 0xfffd;
        // TODO(user): this would be a good spot for a warning.
      }
      String replacement =
          codepoint == 0 ? "" : new String(Character.toChars(codepoint));
      unicode.appendReplacement(
          sb, replacement + trailer);
    }
    unicode.appendTail(sb);
    result = sb.toString();
    return result;
  }

  /**
   * Encodes a CSS term denoting {@code raw}. In general, there are multiple
   * representations in CSS of the same value; we allow clients to influence
   * this choice through @{code discretionaryEscaper}.
   *
   * @see #HTML_ESCAPER
   * @see #SHORT_ESCAPER
   */
  public static String escape(
      Type type, Function<? super String, String> discretionaryEscaper,
      String raw) {
    String result = raw.replaceAll(
        // the Java String encoding of the regex encoding of a slash
        "\\\\",
        // the Java String encoding of a regex replacement encoding of a
        // CSS-escaped slash
        "\\\\\\\\");
    result = LINE_BREAK_PATTERN.matcher(result).replaceAll("\\\n");
    result = discretionaryEscaper.apply(result);
    result = type.escapeForDelimiters(result);
    return result;
  }

  /**
   * Represents this node's value in CSS syntax that is also safe for
   * inclusion in HTML attribute values and element contents. This is a
   * a good choice when you want defense in depth against client code that
   * fails to escape things properly.
   */
  public static final Function<String, String> HTML_ESCAPER =
      new Function<String, String>() {
    public String apply(String input) {
      return paranoidEscapeChars(WIDE_NONASCII_PATTERN,
                                 paranoidEscapeChars(HTML_PATTERN, input));
    }
  };

  /**
   * Replaces characters of questionable safety in {@code context} by
   * CSS escape sequences that are safe for DOUBLE_QUOTED_STRING and
   * SINGLE_QUOTED_STRING nodes and also in HTML attribute values and
   * element content. This implementation's code is especially simple
   * in hopes of improving safety.
   *
   * @param banned a {@code Pattern} matching strings of length one
   *     that should be escaped in the output.
   * @param context a {@code String} input potentially containing
   *     codepoints that are {@code banned}.
   */
  private static String paranoidEscapeChars(Pattern banned, String context) {
    StringBuffer sb = new StringBuffer();
    Matcher markup = banned.matcher(context);
    while (markup.find()) {
      String match = markup.group(0);
      assert(
          // We don't insert characters from whole cloth
          match.length() > 0
          // Our replacement accounts for the entire banned snippet,
          // which is one codepoint but potentially multiple UTF-16
          // Java Characters.
          && match.length() == match.offsetByCodePoints(0, 1));
      markup.appendReplacement(
          sb,
          String.format("\\\\%06x", markup.group(0).codePointAt(0)));
    }
    markup.appendTail(sb);
    return sb.toString();
  }

  /**
   * Replaces characters that have no literal representation in CSS with
   * their escape codes. This implementation compromises computational
   * efficiency in order to produce the shortest possible output for each
   * replaced character. This is a good choice for readability.
   */
  public static final Function<String, String> SHORT_ESCAPER =
      new Function<String, String>() {
    public String apply(String input) {
      StringBuffer sb = new StringBuffer();
      Matcher m = WIDE_NONASCII_PATTERN.matcher(input);
      while (m.find()) {
        String match = m.group(0);
        assert(
            // We don't insert characters from whole cloth
            match.length() > 0
            // Our replacement accounts for the entire banned snippet,
            // which is one codepoint but potentially multiple UTF-16
            // Java Characters.
            && match.length() == match.offsetByCodePoints(0, 1));
        // Escape codes can have up to 6 digits. We are allowed to pad with
        // 0s on the left. If we have fewer than 6 digits and the escape code
        // appears immediately to a hexadecimal digit, then we must add a
        // whitespace character after our digits.
        // Adding the space never results in longer CSS than adding zero
        // padding, and sometimes it shortens our output, so we never pad
        // with zeroes.
        String hexDigits = String.format("%x", match.codePointAt(0));
        String trailer =
            (input.length() <= m.end() || hexDigits.length() == 6
             || !HEX_PATTERN.matcher(
                 input.subSequence(m.end(), m.end() + 1)).matches()) ? "" : " ";
        m.appendReplacement(
            sb, String.format("\\\\%s%s", hexDigits, trailer));
      }
      m.appendTail(sb);
      return sb.toString();
    }
  };

  /**
   * Generates a CSS snippet representing this node.
   * This may differ in semantically unimportant ways from the snippet
   * from which this node was originally parsed.
   * <p>
   * You might reasonably {@code n.setConcreteValue(n.toString(ESC))}
   * because that will not change what you get from {@code n.getValue()}.
   * But it is probably an error to write
   * {@code n.setValue(n.toString(ESC))}; you can pump the string
   * to unbounded length by putting the latter snippet in the body
   * of a loop.
   *
   * @return a {@code String} corresponding to this node's
   * abstract value, but suitable for inclusion in CSS.
   * @see #getValue
   * @see #getConcreteValue
   */
  public String toString(
      Function<? super String, String> discretionaryEscaper) {
    return this.type.toString(this.getValue(), discretionaryEscaper);
  }

  /**
   * CSS syntax permits strings to be expressed either using
   * single-quotes or double-quotes.
   */
  public enum Type {
    /* double-quoted string */
    DOUBLE_QUOTED_STRING("\""),
    /* single-quoted string */
    SINGLE_QUOTED_STRING("'");

    public final String delimiter;
    public final String format;

    Type(String delimiter) {
      this.delimiter = delimiter;
      this.format = String.format("%s%%s%s", delimiter, delimiter);
    }

    public String toString(
        String value, Function<? super String, String> discretionaryEscaper) {
      return String.format(format, escape(this, discretionaryEscaper, value));
    }

    /**
     * Escape delimiters found in input so that they will not begin
     * or end new lexemes.
     */
    public String escapeForDelimiters(String input) {
      return input.replaceAll(
          delimiter,
          // Java String literal encoding of a regex-replacement encoding of
          // a slash used to cancel the meaning of the special CSS character
          // (the delimiter) that follows.
          "\\\\" + delimiter);
    }
  }
}
