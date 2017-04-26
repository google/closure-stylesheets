/*
 * Copyright 2012 Google Inc.
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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Functions;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@code CssStringNode}
 *
 */
@RunWith(JUnit4.class)
public class CssStringNodeTest extends TestCase {
  @Test
  public void testCssValueNodeRoundtrip() throws Exception {
    String v = "ordinary";
    for (CssStringNode.Type t : CssStringNode.Type.values()) {
      CssStringNode n = new CssStringNode(t, v);
      n.setValue(v);
      assertThat(n.getValue()).isEqualTo(v);
    }
  }

  @Test
  public void testCssValueNodeFixedPoint() throws Exception {
    // This test doesn't care if setValue/getValue work in terms of
    // CSS or abstract values, but we just want to make sure that
    // eventually what we set is what we get.
    // Here's a value that must be escaped in CSS. It ensures that
    // we exercise whatever codec is in operation behind these
    // accessors.
    String v = "\\\\\"\"";
    CssStringNode n = new CssStringNode(
        CssStringNode.Type.DOUBLE_QUOTED_STRING, v);
    String v2 = n.getValue();
    n.setValue(v2);
    assertThat(n.getValue()).isEqualTo(v2);
  }

  @Test
  public void testConcreteRoundtrip() throws Exception {
    // This value is safe for verbatim inclusion in either kind of
    // string literal, and for each kind it includes:
    //   (a) an escape sequence that denotes a character that must
    // be escaped in literals of that kind.
    //   (b) a character (possibly as part of an escape sequence)
    // that must be escaped in literals of that kind
    // Also, it includes weird escape sequences that are unlikely
    // to occur together in sane usage, which helps demonstrate
    // normalization.
    String v = "\\'\\\"\\041zA\\0000411\\41 1";
    //          ^single quote
    //             ^double quote
    //                 ^weird A
    //                      ^z
    //                       ^A
    //                        ^weird A, second variation
    //                                ^1
    //                                 ^A with escape code delimiter
    //                                      ^1
    for (CssStringNode.Type t : CssStringNode.Type.values()) {
      CssStringNode n = new CssStringNode(t, v);
      n.setConcreteValue(v);
      assertThat(n.getConcreteValue()).isEqualTo(v);
    }
  }

  @Test
  public void testShortEscaper() throws Exception {
    for (String[] io : new String[][] {
        {"", ""},
        {"a", "a"},
        {"¤", "\\a4"},
        {"ξ", "\\3be"},
        {"ξe", "\\3be e"},
        {"ξx", "\\3bex"},
        {"唐", "\\5510"},
        {"𠍱", "\\20371"},
        {new String(
            new byte[] {(byte) 0xf0, (byte) 0xa0, (byte) 0x8d, (byte) 0xb1},
            UTF_8),
         "\\20371"}}) {
      assertThat(CssStringNode.SHORT_ESCAPER.apply(io[0])).isEqualTo(io[1]);
    }
    // Six-hexadecimal-digit codepoints aren't allowed
    // leading-zeros-padding. This is also an interesting case because
    // Java chars and Strings are defined in terms of UTF-16, which
    // represents codepoints in this range as surrogate pairs.  Let's
    // use UTF-8 to specify the input because it's simpler:
    byte[] puabUtf8 = {(byte) 0xf4, (byte) 0x80, (byte) 0x80, (byte) 0x80};
    assertThat(CssStringNode.SHORT_ESCAPER.apply(new String(puabUtf8, UTF_8)))
        .isEqualTo("\\100000");
    assertThat(CssStringNode.SHORT_ESCAPER.apply(String.format("%sa", new String(puabUtf8, UTF_8))))
        .isEqualTo("\\100000a");
  }

  @Test
  public void testInsertsIgnoredWhitespaceAfterEscape() throws Exception {
    // When parsing, we always discard zero or one whitespace after an
    // escape sequence.
    // See http://www.w3.org/TR/CSS2/syndata.html#characters
    // and http://www.w3.org/TR/css3-syntax/#consume-an-escaped-code-point
    String stringTemplate = "%s following (%s)";
    String cssTemplate = "%s  following (%s)";

    for (CssStringNode.Type type : CssStringNode.Type.values()) {
      // We produce escape sequences in three cases:
      // (1) newline
      assertThat(
              CssStringNode.escape(
                  type,
                  CssStringNode.HTML_ESCAPER,
                  String.format(stringTemplate, "\n", type.getClass().getName())))
          .isEqualTo(String.format(cssTemplate, "\\00000a", type.getClass().getName()));

      // (2) no CSS literal representation exists
      assertThat(
              CssStringNode.escape(
                  type,
                  CssStringNode.SHORT_ESCAPER,
                  String.format(stringTemplate, "¤", type.getClass().getName())))
          .isEqualTo(String.format(cssTemplate, "\\a4", type.getClass().getName()));

      // (3) HTML/SGML special character when using the HTML_ESCAPER
      assertThat(
              CssStringNode.escape(
                  type,
                  CssStringNode.HTML_ESCAPER,
                  String.format(stringTemplate, "<", type.getClass().getName())))
          .isEqualTo(String.format(cssTemplate, "\\00003c", type.getClass().getName()));
    }
  }

  @Test
  public void testHtmlEscaper() throws Exception {
    for (String[] io : new String[][] {
        {"", ""},
        {"a", "a"},
        {"¤", "\\0000a4"},
        {"ξ", "\\0003be"},
        {"ξe", "\\0003bee"},
        {"ξx", "\\0003bex"},
        {"唐", "\\005510"},
        {"𠍱", "\\020371"},
        {new String(
            new byte[] {(byte) 0xf0, (byte) 0xa0, (byte) 0x8d, (byte) 0xb1},
            UTF_8),
         "\\020371"}}) {
      assertThat(CssStringNode.HTML_ESCAPER.apply(io[0])).isEqualTo(io[1]);
    }
    assertThat(CssStringNode.HTML_ESCAPER.apply("&")).isEqualTo("\\000026");
    assertThat(CssStringNode.HTML_ESCAPER.apply("<")).isEqualTo("\\00003c");
    assertThat(CssStringNode.HTML_ESCAPER.apply(">")).isEqualTo("\\00003e");
    assertThat(CssStringNode.HTML_ESCAPER.apply("\"")).isEqualTo("\\000022");
    assertThat(CssStringNode.HTML_ESCAPER.apply("'")).isEqualTo("\\000027");
  }

  @Test
  public void testEscape() throws Exception {
    for (String[] io : new String[][] {
        {"", ""},
        {"a", "a"},
        {"\\", "\\\\"},
        {"19\\3=6", "19\\\\3=6"},
        {"say \"hello\"", "say \\\"hello\\\""},
        {"say 'goodbye'", "say 'goodbye'"}}) {
      assertThat(
              CssStringNode.escape(
                  CssStringNode.Type.DOUBLE_QUOTED_STRING, Functions.<String>identity(), io[0]))
          .isEqualTo(io[1]);
    }
    assertThat(
            CssStringNode.escape(
                CssStringNode.Type.SINGLE_QUOTED_STRING,
                Functions.<String>identity(),
                "say 'goodbye'"))
        .isEqualTo("say \\'goodbye\\'");
  }

  @Test
  public void testUnescape() throws Exception {
    for (String[] io : new String[][] {
        {"", ""},
        {"a", "a"},
        {"\\\\", "\\"},
        {"\\\"", "\""},
        {"\\41", "A"},
        {"\\41 ", "A"},
        {"\\41  ", "A "},
        // The spec requires us to discard a whitespace following an
        // escape sequence, even when the escape sequence is as long
        // as possible and hence there is no ambiguity about where it
        // ends.
        {"abc\\000041 ", "abcA"},
        {"abc\\000041  ", "abcA "},
        {"\\41x", "Ax"},
        {"\\0000a4", "¤"},
        {"\\0003be", "ξ"},
        {"\\0003bee", "ξe"},
        {"\\3be e", "ξe"},
        {"\\0003be", "ξ"},
        {"\\3be z", "ξz"},
        {"\\005510", "唐"},
        {"\\020371", "𠍱"},
        {"\\020371", new String(
            new byte[] {(byte) 0xf0, (byte) 0xa0, (byte) 0x8d, (byte) 0xb1},
            UTF_8)}}) {
      assertThat(CssStringNode.unescape(io[0])).isEqualTo(io[1]);
    }

    // Now let's look at a character that requires a max-length escape
    // code in CSS and use of surrogate pairs in the JVM
    byte[] puabUtf8 = {(byte) 0xf4, (byte) 0x80, (byte) 0x80, (byte) 0x80};
    assertThat(CssStringNode.unescape("\\100000")).isEqualTo(new String(puabUtf8, UTF_8));
    assertThat(CssStringNode.unescape("\\100000a")).isEqualTo(new String(puabUtf8, UTF_8) + "a");

    // Here's an escape sequence denoting a code point beyond the Java
    // char/String repertoire. According to CSS 2.1, we can replace the
    // escape.
    int bigCodePoint = Character.MAX_CODE_POINT + 1;
    assertEquals(
        // replacement character
        "\ufffd",
        CssStringNode.unescape(
            String.format("\\%x", bigCodePoint)));
  }

  @Test
  public void testCopyCtor() {
    CssStringNode a = new CssStringNode(
        CssStringNode.Type.DOUBLE_QUOTED_STRING, "foo");
    a.setConcreteValue("\\0066oobar");
    CssStringNode b = new CssStringNode(a);
    assertThat(a.getConcreteValue()).isEqualTo("\\0066oobar");
    assertThat(b.getConcreteValue()).isEqualTo(a.getConcreteValue());
    assertThat(b.getValue()).isEqualTo(a.getValue());
  }

  @Test
  public void testStringCannotDirectlyContainNewline() {
    // See http://www.w3.org/TR/CSS2/syndata.html#strings
    CssStringNode a = new CssStringNode(
        CssStringNode.Type.SINGLE_QUOTED_STRING, "line1\nline2");
    assertWithMessage("We should support the Java String representation of newlines.")
        .that(a.getValue().contains("\n"))
        .isTrue();
    assertWithMessage(
            "If we set a Java newline, it should be escaped in the" + " generated concrete value.")
        .that(a.getConcreteValue().contains("\n"))
        .isFalse();
    assertWithMessage("If we ask for CSS markup, we should escape newlines per the" + " CSS spec.")
        .that(a.toString(CssStringNode.HTML_ESCAPER).contains("\n"))
        .isFalse();
    assertWithMessage("Escaping a new line shouldn't affect the left hand side")
        .that(a.toString(CssStringNode.HTML_ESCAPER).startsWith("'line1"))
        .isTrue();
    assertWithMessage("Escaping a new line shouldn't affect the right-hand side")
        .that(a.toString(CssStringNode.HTML_ESCAPER).endsWith("line2'"))
        .isTrue();
  }
}
