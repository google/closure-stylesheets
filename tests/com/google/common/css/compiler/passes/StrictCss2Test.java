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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link StrictCss2}.
 *
 * @author fbenz@google.com (Florian Benz)
 */
@RunWith(JUnit4.class)
public class StrictCss2Test extends NewFunctionalTestBase {
  private String compactPrintedResult;

  @Override
  protected void runPass() {
    new StrictCss2(tree.getVisitController(), errorManager).runPass();
    CompactPrinter compactPrinterPass = new CompactPrinter(tree);
    compactPrinterPass.runPass();
    compactPrintedResult = compactPrinterPass.getCompactPrintedString();
  }

  @Test
  public void testPseudoClass1() throws Exception {
    parseAndRun("div :first-child {}");
    assertThat(compactPrintedResult).isEqualTo("div :first-child{}");
  }

  @Test
  public void testPseudoClass2() throws Exception {
    parseAndRun("div :link {}");
    assertThat(compactPrintedResult).isEqualTo("div :link{}");
  }

  @Test
  public void testPseudoClass3() throws Exception {
    parseAndRun("div :visited {}");
    assertThat(compactPrintedResult).isEqualTo("div :visited{}");
  }

  @Test
  public void testPseudoClass4() throws Exception {
    parseAndRun("div :focus {}");
    assertThat(compactPrintedResult).isEqualTo("div :focus{}");
  }

  @Test
  public void testPseudoElement1() throws Exception {
    parseAndRun("p :first-line {}");
    assertThat(compactPrintedResult).isEqualTo("p :first-line{}");
  }

  @Test
  public void testPseudoElement2() throws Exception {
    parseAndRun("h1 :first-letter {}");
    assertThat(compactPrintedResult).isEqualTo("h1 :first-letter{}");
  }

  @Test
  public void testPseudoElement3() throws Exception {
    parseAndRun("div :after {}");
    assertThat(compactPrintedResult).isEqualTo("div :after{}");
  }

  @Test
  public void testPseudoElement4() throws Exception {
    parseAndRun("div :before {}");
    assertThat(compactPrintedResult).isEqualTo("div :before{}");
  }

  @Test
  public void testAttributeSelector1() throws Exception {
    parseAndRun("span[class=example]{}");
    assertThat(compactPrintedResult).isEqualTo("span[class=example]{}");
  }

  @Test
  public void testAttributeSelector2() throws Exception {
    parseAndRun("a[rel~='copyright']{}");
    assertThat(compactPrintedResult).isEqualTo("a[rel~='copyright']{}");
  }

  @Test
  public void testAttributeSelector3() throws Exception {
    parseAndRun("*[lang|=\"en\"]{}");
    assertThat(compactPrintedResult).isEqualTo("*[lang|=\"en\"]{}");
  }

  @Test
  public void testPseudoClassNthBad1() throws Exception {
    parseAndRun("ul :nth-child(5n+3) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassNthBad2() throws Exception {
    parseAndRun("ol :nth-last-child(5) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassNthBad3() throws Exception {
    parseAndRun("p :nth-of-type(odd) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassNthBad4() throws Exception {
    parseAndRun("div :nth-last-of-type(-2n-3) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassBad1() throws Exception {
    parseAndRun("div :none {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassBad2() throws Exception {
    parseAndRun("div :not(h1) {}", false,
        StrictCss2.UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoElementBad() throws Exception {
    parseAndRun("div ::none {}", false,
        StrictCss2.NEW_PESUDO_ELEMENTS_NOT_ALLOWED_ERROR_MESSAGE);
  }

  @Test
  public void testAtrributeSelectorBad1() throws Exception {
    parseAndRun("a[rel*='copyright']{}",
        StrictCss2.FORBIDDEN_ATTRIBUTE_COMPARER_ERROR_MESSAGE);
  }

  @Test
  public void testAtrributeSelectorBad2() throws Exception {
    parseAndRun("a[rel$='copyright']{}",
        StrictCss2.FORBIDDEN_ATTRIBUTE_COMPARER_ERROR_MESSAGE);
  }

  @Test
  public void testAtrributeSelectorBad3() throws Exception {
    parseAndRun("a[rel^='copyright']{}",
        StrictCss2.FORBIDDEN_ATTRIBUTE_COMPARER_ERROR_MESSAGE);
  }

  @Test
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

  @Test
  public void testInvalidLengthUnit() throws Exception {
    parseAndRun(
        "a {" +
        "  width: 1p;" +
        "}",
        StrictCss2.INVALID_UNIT_PREFIX + "p");
  }
}
