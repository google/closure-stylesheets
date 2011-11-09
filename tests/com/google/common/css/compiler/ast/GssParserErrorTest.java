/*
 * Copyright 2009 Google Inc.
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

import com.google.common.css.SourceCode;

import junit.framework.TestCase;

/**
 * Unit tests for error handling of {@link GssParser}.
 *
 */

public class GssParserErrorTest extends TestCase {

  private CssTree parse(String gss) throws GssParserException {
    GssParser parser = new GssParser(new SourceCode("test", gss));
    return parser.parse();
  }

  private void testError(String gss, int lineNumber, int indexInLine,
                         String line, String caret) {
    try {
      parse(gss);
      fail();
    } catch (GssParserException e) {
      assertEquals(
           "Parse error in test at line " + lineNumber +
           " column " + indexInLine + ":\n" +
           line + "\n" + caret + "\n",
           e.getMessage());
    }
  }

  public void test1() {
    testError("a { exu7y&&rgx: url('http://test.com') }", 1, 10,
              "a { exu7y&&rgx: url('http://test.com') }",
              "         ^");
  }

  public void test2() {
    testError(
        "a {\n" +
        "    exu7y&&rgx: url('http://test.com')\n" +
        "  }", 2, 10,
        "    exu7y&&rgx: url('http://test.com')",
        "         ^");
  }

  public void test3() {
    testError("a { b: c,,}", 1, 10,
              "a { b: c,,}",
              "         ^");
  }

  public void test4() {
    testError("a", 1, 1,
              "a",
              "^");
  }

  public void test5() {
    testError("a { b: c;", 1, 9,
              "a { b: c;",
              "        ^");
  }

  public void test6() {
    testError("{}", 1, 1,
              "{}",
              "^");
  }

  public void test7() {
    testError("\na { b: c,,}", 2, 10,
              "a { b: c,,}",
              "         ^");
  }

  public void testBadToken1() {
    // Should be > not <.
    testError(".foo .bar<td {}", 1, 10,
              ".foo .bar<td {}",
              "         ^");
  }

  public void testBadToken2() {
    testError("\n<td {}", 2, 1,
              "<td {}",
              "^");
  }

  public void testBadToken3() {
    testError("<td {}", 1, 1,
              "<td {}",
              "^");
  }

  public void testBadWebkitKeyframes1() {
    testError("@-webkit-keyframes bounce {\n" +
        "  0 {\n" +
        "    left: 0px;\n" +
        "  }\n" +
        "  100% {\n" +
        "    left: 200px;\n" +
        "  }\n" +
        "}\n", 2, 4,
        "  0 {",
        "   ^");
  }

  public void testBadWebkitKeyframes2() {
    testError("@-webkit-keyframes bounce {\n" +
        "  2.2 {\n" +
        "    left: 0px;\n" +
        "  }\n" +
        "  100% {\n" +
        "    left: 200px;\n" +
        "  }\n" +
        "}\n", 2, 6,
        "  2.2 {",
        "     ^");
  }

  public void testBadWebkitKeyframes3() {
    testError("@-webkit-keyframes foo;", 1, 23,
        "@-webkit-keyframes foo;",
        "                      ^");
  }

  public void testBadPseudoNth1() {
    testError("div :nth-child(#id) { }", 1, 16,
        "div :nth-child(#id) { }",
        "               ^");
  }

  public void testBadPseudoNth2() {
    testError("div :nth-child(.class) { }", 1, 16,
        "div :nth-child(.class) { }",
        "               ^");
  }

  public void testBadPseudoNot1() {
    testError("div :not() { }", 1, 10,
        "div :not() { }",
        "         ^");
  }

  public void testBadPseudoNot2() {
    // :not can only take a simple selector as an argument.
    testError("div :not(div p) { }", 1, 14,
        "div :not(div p) { }",
        "             ^");
  }

  public void testBadMixinDefinition() {
    testError("@defmixin name($var) {}", 1, 16,
        "@defmixin name($var) {}",
        "               ^");
  }

  public void testBadGradient() {
    testError("div {"
        + "d:-invalid-gradient(bottom left, red 20px, yellow, green,"
        + "blue 90%);"
        + "}",
        1, 72,
        "div {d:-invalid-gradient(bottom left, red 20px, yellow, green,"
        +"blue 90%);}",
        "                                                                "
        + "       ^");
  }
}
