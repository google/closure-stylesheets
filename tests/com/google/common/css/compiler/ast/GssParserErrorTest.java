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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.css.SourceCode;

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for error handling of {@link GssParser}.
 *
 * @author fbenz@google.com (Florian Benz)
 */

public class GssParserErrorTest extends TestCase {

  private boolean reuseGssParser = false;

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
    testError("@defmixin name($#%$var) {}", 1, 16,
        "@defmixin name($#%$var) {}",
        "               ^");
  }

  public void testBadGradient() {
    testError("div {"
        + "d:-invalid-gradient(bottom left, red 20px, yellow, green,"
        + "blue 90%);"
        + "}",
        1, 72,
        "div {d:-invalid-gradient(bottom left, red 20px, yellow, green,blue 90%);}",
        "                                                                       ^");
  }

  public void testInvalidSpaceInArgumentList() {
    // The parser marks the error at the semicolon because this is the token immediately following
    // the last successfully-consumed production. This is not ideal because the error occurs within
    // the argument list, but we validate the argument list after it is successfully parsed by the
    // grammar.
    testError("div { transform:rotate(180 deg); }",
        1, 32,
        "div { transform:rotate(180 deg); }",
        "                               ^");
    testError("div { background: rgba(255,0,0 1); }",
        1, 34,
        "div { background: rgba(255,0,0 1); }",
        "                                 ^");
  }

  /**
   * Tests for error handling below
   */

  private void testErrorHandling(String input, String expected, String... errors)
      throws GssParserException {
    List<GssParserException> handledErrors = new ArrayList<>();
    CssTree tree = parse(input, true, handledErrors);
    List<String> errorMessages = new ArrayList<>();
    for (GssParserException e : handledErrors) {
      errorMessages.add(e.getMessage());
    }
    assertNotNull(tree);
    CssRootNode root = tree.getRoot();
    assertNotNull(root);
    assertThat(errorMessages).containsExactly((Object[]) errors).inOrder();
    assertEquals(expected, root.toString());
  }

  public void testDeclarationErrorHandling() throws GssParserException {
    testErrorHandling("a { b: c,,; d: e }", "[[a]{[d:[e]]}]",
        "Parse error in test at line 1 column 10:\n"
        + "a { b: c,,; d: e }\n"
        + "         ^\n");
    testErrorHandling("a { b: c: d; e: f }", "[[a]{[e:[f]]}]",
        "Parse error in test at line 1 column 10:\n"
        + "a { b: c: d; e: f }\n"
        + "         ^\n");
    testErrorHandling("a { b: c; @at d: e; f: g }", "[[a]{[b:[c], f:[g]]}]",
        "Parse error in test at line 1 column 17:\n"
        + "a { b: c; @at d: e; f: g }\n"
        + "                ^\n");
  }

  public void testSelectorErrorHandling() throws GssParserException {
    testErrorHandling("a>>b { b: c } d { e: f }", "[[d]{[e:[f]]}]",
        "Parse error in test at line 1 column 2:\n"
        + "a>>b { b: c } d { e: f }\n"
        + " ^\n");
    testErrorHandling("a @ b { c: d } e {}", "[[e]{[]}]",
        "Parse error in test at line 1 column 3:\n"
        + "a @ b { c: d } e {}\n"
        + "  ^\n");
    // No error; braces within quoted string are correctly parsed
    testErrorHandling("a{b:\"{,}\"}", "[[a]{[b:[\"{,}\"]]}]");
  }

  public void testAtRuleErrorHandling() throws GssParserException {
    testErrorHandling("@a b (,,); c { d: e }", "[[c]{[d:[e]]}]",
        "Parse error in test at line 1 column 7:\n"
        + "@a b (,,); c { d: e }\n"
        + "      ^\n");
    testErrorHandling("@a { b,,{} c { d:: e; f: g } } h { i: j }",
        "[@a[]{[[c]{[f:[g]]}]}, [h]{[i:[j]]}]",
        "Parse error in test at line 1 column 8:\n"
        + "@a { b,,{} c { d:: e; f: g } } h { i: j }\n"
        + "       ^\n",
        "Parse error in test at line 1 column 18:\n"
        + "@a { b,,{} c { d:: e; f: g } } h { i: j }\n"
        + "                 ^\n");
    testErrorHandling("@a (b;) { c {} } d {}", "[[d]{[]}]",
        "Parse error in test at line 1 column 6:\n"
        + "@a (b;) { c {} } d {}\n"
        + "     ^\n");
    testErrorHandling("@a (b:c[]) { d[;}] {} e {} } f {}", "[[f]{[]}]",
        "Parse error in test at line 1 column 8:\n"
        + "@a (b:c[]) { d[;}] {} e {} } f {}\n"
        + "       ^\n");
    testErrorHandling("a { @b { c, {} d {} } e: f }", "[[a]{[@b[]{[]}, e:[f]]}]",
        "Parse error in test at line 1 column 11:\n"
        + "a { @b { c, {} d {} } e: f }\n"
        + "          ^\n");
    testErrorHandling("a[b=] { c {} } d {}", "[[d]{[]}]",
        "Parse error in test at line 1 column 5:\n"
        + "a[b=] { c {} } d {}\n"
        + "    ^\n");
  }

  public void testMatchingBraces() throws GssParserException {
    // Inner closed block ignored
    testErrorHandling("a{ b{} } c{}", "[[a]{[]}, [c]{[]}]",
        "Parse error in test at line 1 column 5:\n"
        + "a{ b{} } c{}\n"
        + "    ^\n");
    // Inner nested blocks ignored as well
    testErrorHandling("a{([b])} c{}", "[[c]{[]}]",
        "Parse error in test at line 1 column 3:\n"
        + "a{([b])} c{}\n"
        + "  ^\n");
    // Unmatched left brace consume until EOF
    testErrorHandling("a{([b)]} c{}", "[]",
        "Parse error in test at line 1 column 3:\n"
        + "a{([b)]} c{}\n"
        + "  ^\n");
    // Unmatched right brace ignored
    testErrorHandling("a{ (}) } b{}", "[[b]{[]}]",
        "Parse error in test at line 1 column 4:\n"
        + "a{ (}) } b{}\n"
        + "   ^\n");
  }

  public void testErrorRecoveryWithInvalidArgumentList() throws GssParserException {
    testErrorHandling("div { transform:rotate(180 deg); }", "[[div]{[]}]",
        "Parse error in test at line 1 column 32:\n"
        + "div { transform:rotate(180 deg); }\n"
        + "                               ^\n");
  }

  // When porting this test to Junit4, please make necessary change to this
  // method to make sure all the rest test cases are invoked here.
  public void testAllCasesWithReuseableParser() throws Exception {
    // Call all other test cases in one method to make sure the same thread
    // local parser is reused.
    reuseGssParser = true;
    for (Method m : GssParserErrorTest.class.getDeclaredMethods()) {
      if (m.getName().startsWith("test")
          && !m.getName().equals("testAllCasesWithReuseableParser")
          && m.getParameterTypes().length == 0) {
        // Run each test twice to run each test with a used parser.
        m.invoke(this);
        m.invoke(this);
      }
    }
  }

  private CssTree parse(String gss, boolean shouldHandleError,
      List<GssParserException> handledErrors)
      throws GssParserException {
    CssTree tree;
    if (reuseGssParser) {
      GssParser parser = new GssParser(new SourceCode("test", gss));
      tree = parser.parse(shouldHandleError);
      handledErrors.addAll(parser.getHandledErrors());
    } else {
      PerThreadGssParser parser = new PerThreadGssParser();
      tree = parser.parse(new SourceCode("test", gss), shouldHandleError);
      handledErrors.addAll(parser.getHandledErrors());
    }
    return tree;
  }

  private CssTree parse(String gss) throws GssParserException {
    return parse(gss, false, new ArrayList<GssParserException>());
  }
}
