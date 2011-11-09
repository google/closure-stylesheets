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

package com.google.common.css.compiler.passes;

import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

/**
 * Unit tests for {@link StrictCss2}.
 *
 */
public class StrictCss2Test extends NewFunctionalTestBase {
  private String compactPrintedResult;

  @Override
  protected void runPass() {
    new StrictCss2(tree.getMutatingVisitController(), errorManager).runPass();
    CompactPrinter compactPrinterPass = new CompactPrinter(tree);
    compactPrinterPass.runPass();
    compactPrintedResult = compactPrinterPass.getCompactPrintedString();
  }

  public void testPseudoClass1() throws Exception {
    parseAndRun("div :first-child {}");
    assertEquals("div :first-child{}", compactPrintedResult);
  }

  public void testPseudoClass2() throws Exception {
    parseAndRun("div :link {}");
    assertEquals("div :link{}", compactPrintedResult);
  }

  public void testPseudoClass3() throws Exception {
    parseAndRun("div :visited {}");
    assertEquals("div :visited{}", compactPrintedResult);
  }

  public void testPseudoClass4() throws Exception {
    parseAndRun("div :focus {}");
    assertEquals("div :focus{}", compactPrintedResult);
  }

  public void testPseudoElement1() throws Exception {
    parseAndRun("p :first-line {}");
    assertEquals("p :first-line{}", compactPrintedResult);
  }

  public void testPseudoElement2() throws Exception {
    parseAndRun("h1 :first-letter {}");
    assertEquals("h1 :first-letter{}", compactPrintedResult);
  }

  public void testPseudoElement3() throws Exception {
    parseAndRun("div :after {}");
    assertEquals("div :after{}", compactPrintedResult);
  }

  public void testPseudoElement4() throws Exception {
    parseAndRun("div :before {}");
    assertEquals("div :before{}", compactPrintedResult);
  }

  public void testAtrributeSelector1() throws Exception {
    parseAndRun("span[class=example]{}");
    assertEquals("span[class=example]{}", compactPrintedResult);
  }

  public void testAtrributeSelector2() throws Exception {
    parseAndRun("a[rel~='copyright']{}");
    assertEquals("a[rel~='copyright']{}", compactPrintedResult);
  }

  public void testAtrributeSelector3() throws Exception {
    parseAndRun("*[lang|=\"en\"]{}");
    assertEquals("*[lang|=\"en\"]{}", compactPrintedResult);
  }

  public void testPseudoClassNthBad1() throws Exception {
    parseAndRun("ul :nth-child(5n+3) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  public void testPseudoClassNthBad2() throws Exception {
    parseAndRun("ol :nth-last-child(5) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  public void testPseudoClassNthBad3() throws Exception {
    parseAndRun("p :nth-of-type(odd) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  public void testPseudoClassNthBad4() throws Exception {
    parseAndRun("div :nth-last-of-type(-2n-3) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  public void testPseudoClassBad1() throws Exception {
    parseAndRun("div :none {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  public void testPseudoClassBad2() throws Exception {
    parseAndRun("div :not(h1) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  public void testPseudoElementBad() throws Exception {
    parseAndRun("div ::none {}", false,
        StrictCss2.NEW_PESUDO_ELEMENTS_NOT_ALLOWED_ERROR_MESSAGE);
  }

  public void testAtrributeSelectorBad1() throws Exception {
    parseAndRun("a[rel*='copyright']{}",
        StrictCss2.FORBIDDEN_ATTRIBUTE_COMPARER_ERROR_MESSAGE);
  }

  public void testAtrributeSelectorBad2() throws Exception {
    parseAndRun("a[rel$='copyright']{}",
        StrictCss2.FORBIDDEN_ATTRIBUTE_COMPARER_ERROR_MESSAGE);
  }

  public void testAtrributeSelectorBad3() throws Exception {
    parseAndRun("a[rel^='copyright']{}",
        StrictCss2.FORBIDDEN_ATTRIBUTE_COMPARER_ERROR_MESSAGE);
  }

  public void testValidLengthUnits() throws Exception {
    parseAndRun(
        "a {" +
        "  width: 0;" +
        "  width: 1em;" +
        "  width: 1ex;" +
        "  width: 1%;" +
        "  width: 1in;" +
        "  width: 1cm;" +
        "  width: 1mm;" +
        "  width: 1pt;" +
        "  width: 1pc;" +
        "  width: 1px;" +
        "}");
  }

  public void testInvalidLengthUnit() throws Exception {
    parseAndRun(
        "a {" +
        "  width: 1p;" +
        "}",
        StrictCss2.INVALID_UNIT_PREFIX + "p");
  }
}
