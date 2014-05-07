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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Functions;

import junit.framework.TestCase;

/**
 * Unit tests for {@code CssStringNode}
 *
 */
public class CssStringNodeTest extends TestCase {
  public void testCssValueNodeRoundtrip() throws Exception {
    String v = "ordinary";
    for (CssStringNode.Type t : CssStringNode.Type.values()) {
      CssStringNode n = new CssStringNode(t, v);
      n.setValue(v);
      assertEquals(v, n.getValue());
    }
  }

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
    assertEquals(v2, n.getValue());
  }

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
      assertEquals(v, n.getConcreteValue());
    }
  }

  public void testShortEscaper() throws Exception {
    for (String[] io : new String[][] {
        {"", ""},
        {"a", "a"},
        // Under ant, we can't have supplementary characters in String literals
        // {"¤", "\\a4"},
        {"\u00a4", "\\a4"},
        // {"ξ", "\\3be"},
        {"\u03be", "\\3be"},
        // {"ξe", "\\3be e"},
        {"\u03bee", "\\3be e"},
        // {"ξx", "\\3bex"},
        {"\u03bex", "\\3bex"},
        // {"唐", "\\5510"},
        {"\u5510", "\\5510"},
        //{"𠍱", "\\20371"}
        {new String(
            new byte[] {(byte) 0xf0, (byte) 0xa0, (byte) 0x8d, (byte) 0xb1},
            UTF_8),
         "\\20371"}}) {
      assertEquals(io[1], CssStringNode.SHORT_ESCAPER.apply(io[0]));
    }
    // Six-hexadecimal-digit codepoints aren't allowed
    // leading-zeros-padding and must not generate a trailing whitespace
    // delimiter. This is also an interesting case because Java chars and
    // Strings are defined in terms of UTF-16, which represents codepoints
    // in this range as surrogate pairs.
    // Let's use UTF-8 to specify the input because it's simpler:
    byte[] puabUtf8 = {(byte) 0xf4, (byte) 0x80, (byte) 0x80, (byte) 0x80};
    assertEquals("\\100000",
                 CssStringNode.SHORT_ESCAPER.apply(
                     new String(puabUtf8, UTF_8)));
    assertEquals("\\100000a",
                 CssStringNode.SHORT_ESCAPER.apply(
                     String.format("%sa",
                                   new String(puabUtf8, UTF_8))));
  }

  public void testHtmlEscaper() throws Exception {
    for (String[] io : new String[][] {
        {"", ""},
        {"a", "a"},
        // Under ant, we can't have supplementary characters in String literals
        // {"¤", "\\0000a4"},
        {"\u00a4", "\\0000a4"},
        // {"ξ", "\\0003be"},
        {"\u03be", "\\0003be"},
        // {"ξe", "\\0003bee"},
        {"\u03bee", "\\0003bee"},
        // {"ξx", "\\0003bex"},
        {"\u03bex", "\\0003bex"},
        // {"唐", "\\005510"},
        {"\u5510", "\\005510"},
        //{"𠍱", "\\020371"}
        {new String(
            new byte[] {(byte) 0xf0, (byte) 0xa0, (byte) 0x8d, (byte) 0xb1},
            UTF_8),
         "\\020371"}}) {
      assertEquals(io[1], CssStringNode.HTML_ESCAPER.apply(io[0]));
    }
    assertEquals("\\000026", CssStringNode.HTML_ESCAPER.apply("&"));
    assertEquals("\\00003c", CssStringNode.HTML_ESCAPER.apply("<"));
    assertEquals("\\00003e", CssStringNode.HTML_ESCAPER.apply(">"));
    assertEquals("\\000022", CssStringNode.HTML_ESCAPER.apply("\""));
    assertEquals("\\000027", CssStringNode.HTML_ESCAPER.apply("'"));
  }

  public void testEscape() throws Exception {
    for (String[] io : new String[][] {
        {"", ""},
        {"a", "a"},
        {"\\", "\\\\"},
        {"19\\3=6", "19\\\\3=6"},
        {"say \"hello\"", "say \\\"hello\\\""},
        {"say 'goodbye'", "say 'goodbye'"}}) {
      assertEquals(
          io[1],
          CssStringNode.escape(
              CssStringNode.Type.DOUBLE_QUOTED_STRING,
              Functions.<String>identity(), io[0]));
    }
    assertEquals(
        "say \\'goodbye\\'",
        CssStringNode.escape(
            CssStringNode.Type.SINGLE_QUOTED_STRING,
            Functions.<String>identity(), "say 'goodbye'"));
  }

  public void testUnescape() throws Exception {
    for (String[] io : new String[][] {
        {"", ""},
        {"a", "a"},
        {"\\\\", "\\"},
        {"\\\"", "\""},
        {"\\41", "A"},
        {"\\41 ", "A"},
        {"\\41  ", "A "},
        {"abc\\000041 ", "abcA"},
        {"abc\\000041  ", "abcA "},
        {"\\41x", "Ax"},
        // {"\\0000a4", "¤"},
        {"\\0000a4", "\u00a4"},
        // {"\\0003be", "ξ"},
        {"\\0003be", "\u03be"},
        // {"\\0003bee", "ξe"},
        {"\\0003bee", "\u03bee"},
        // {"\\3be e", "ξe"},
        {"\\3be e", "\u03bee"},
        // {"\\0003be", "ξ"},
        {"\\0003be", "\u03be"},
        // {"\\3be z", "ξz"},
        {"\\3be z", "\u03bez"},
        // {"\\005510", "唐"},
        {"\\005510", "\u5510"},
        // {"\\020371", "𠍱"}
        {"\\020371", new String(
            new byte[] {(byte) 0xf0, (byte) 0xa0, (byte) 0x8d, (byte) 0xb1},
            UTF_8)}}) {
      assertEquals(io[1], CssStringNode.unescape(io[0]));
    }

    // Now let's look at a character that requires a max-length escape
    // code in CSS and use of surrogate pairs in the JVM
    byte[] puabUtf8 = {(byte) 0xf4, (byte) 0x80, (byte) 0x80, (byte) 0x80};
    assertEquals(new String(puabUtf8, UTF_8),
                 CssStringNode.unescape("\\100000"));
    assertEquals(new String(puabUtf8, UTF_8) + "a",
                 CssStringNode.unescape("\\100000a"));

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

  public void testCopyCtor() {
    CssStringNode a = new CssStringNode(
        CssStringNode.Type.DOUBLE_QUOTED_STRING, "foo");
    a.setConcreteValue("\\0066oobar");
    CssStringNode b = new CssStringNode(a);
    assertEquals("\\0066oobar", a.getConcreteValue());
    assertEquals(a.getConcreteValue(), b.getConcreteValue());
    assertEquals(a.getValue(), b.getValue());
  }

  public void testStringCannotDirectlyContainNewline() {
    // See http://www.w3.org/TR/CSS2/syndata.html#strings
    CssStringNode a = new CssStringNode(
        CssStringNode.Type.SINGLE_QUOTED_STRING, "line1\nline2");
    assertTrue(
        "We should support the Java String representation of newlines.",
        a.getValue().contains("\n"));
    assertFalse(
        "If we set a Java newline, it should be escaped in the"
        + " generated concrete value.",
        a.getConcreteValue().contains("\n"));
    assertFalse(
        "If we ask for CSS markup, we should esceape newlines per the"
        + " CSS spec.",
        a.toString(CssStringNode.HTML_ESCAPER).contains("\n"));
  }
}
