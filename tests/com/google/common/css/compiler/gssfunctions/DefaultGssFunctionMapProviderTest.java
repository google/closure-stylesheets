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

package com.google.common.css.compiler.gssfunctions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import com.google.common.css.compiler.passes.PrettyPrinter;
import com.google.common.css.compiler.passes.ResolveCustomFunctionNodes;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link DefaultGssFunctionMapProviderTest} is a unit test for
 * {@link DefaultGssFunctionMapProvider}, which is used to provide the default
 * functions for <a href="http://code.google.com/p/closure-stylesheets/">Closure
 * Stylesheets</a>.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class DefaultGssFunctionMapProviderTest extends NewFunctionalTestBase {

  /**
   * Normalizes the compiled CSS to a pretty-printed form that can be compared
   * with the result of {@link #normalizeExpectedCss(String)}.
   */
  private String getCompiledCss() {
    PrettyPrinter prettyPrinterPass = new PrettyPrinter(tree
        .getVisitController());
    prettyPrinterPass.runPass();
    return prettyPrinterPass.getPrettyPrintedString();
  }

  /**
   * Normalizes the expected CSS to a pretty-printed form that can be compared
   * with the result of {@link #getCompiledCss()}.
   */
  private static String normalizeExpectedCss(String expectedCss)
      throws GssParserException {
    List<SourceCode> inputs = ImmutableList.of(
        new SourceCode("expectedCss", expectedCss));
    CssTree tree = new GssParser(inputs).parse();
    PrettyPrinter prettyPrinterPass = new PrettyPrinter(tree
        .getVisitController());
    prettyPrinterPass.runPass();
    return prettyPrinterPass.getPrettyPrintedString();
  }

  /**
   * Takes a string of GSS to compile and the CSS that should be produced as a
   * result of compilation. The compiled GSS and expected CSS will be normalized
   * to a pretty-printed format for comparison, so it is not necessary to format
   * either by hand.
   */
  private void test(String inputGss, String expectedCss)
      throws GssParserException {
    parseAndRun(inputGss);
    assertEquals(normalizeExpectedCss(expectedCss), getCompiledCss());
  }

  @Override
  protected void runPass() {
    Map<String, GssFunction> functionMap = new DefaultGssFunctionMapProvider()
        .get();
    final boolean allowUnknownFunctions = false;
    final Set<String> allowedNonStandardFunctions = ImmutableSet.of();
    new ResolveCustomFunctionNodes(
        tree.getMutatingVisitController(), errorManager, functionMap,
        allowUnknownFunctions, allowedNonStandardFunctions).runPass();
  }

  public void testAddPercentagesMissingUnit() throws GssParserException {
    parseAndRun("A { width: add(5%, 10); }",
        "Size must be 0 or have a unit; was: 10");
  }

  public void testAddPercentagesZeroSize() throws GssParserException {
    test("A { width: add(5%, 0, 10%); }",
        " A { width: 15%; }");
    test("A { width: add(5%, 0%, 10%); }",
        " A { width: 15%; }");
    test("A { width: add(5%, -0, 10%); }",
        " A { width: 15%; }");
    test("A { width: add(5%, -0%, 10%); }",
        " A { width: 15%; }");
  }

  public void testAddPercentagesImplicitUnit() throws GssParserException {
    test("A { width: add(5%, 10%); }",
        " A { width: 15%; }");
  }

  public void testAddNegativeValues() throws GssParserException {
    test("A { width: add(5%, 10%, -25%, 50%); }",
        " A { width: 40%; }");
  }
}
