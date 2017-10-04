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
 * Unit tests for {@link StrictCss3}.
 *
 * @author fbenz@google.com (Florian Benz)
 */
@RunWith(JUnit4.class)
public class StrictCss3Test extends NewFunctionalTestBase {
  private String compactPrintedResult;

  @Override
  protected void runPass() {
    new StrictCss3(tree.getVisitController(), errorManager).runPass();
    CompactPrinter compactPrinterPass = new CompactPrinter(tree);
    compactPrinterPass.runPass();
    compactPrintedResult = compactPrinterPass.getCompactPrintedString();
  }

  @Test
  public void testPseudoClass1() throws Exception {
    parseAndRun("div :root {}");
    assertThat(compactPrintedResult).isEqualTo("div :root{}");
  }

  @Test
  public void testPseudoClass2() throws Exception {
    parseAndRun("div :last-child {}");
    assertThat(compactPrintedResult).isEqualTo("div :last-child{}");
  }

  @Test
  public void testPseudoClass3() throws Exception {
    parseAndRun("div :empty {}");
    assertThat(compactPrintedResult).isEqualTo("div :empty{}");
  }

  @Test
  public void testPseudoClass4() throws Exception {
    parseAndRun("div :checked {}");
    assertThat(compactPrintedResult).isEqualTo("div :checked{}");
  }

  @Test
  public void testPseudoElement1() throws Exception {
    parseAndRun("p ::first-line {}");
    assertThat(compactPrintedResult).isEqualTo("p ::first-line{}");
  }

  @Test
  public void testPseudoElement2() throws Exception {
    parseAndRun("h1 ::first-letter {}");
    assertThat(compactPrintedResult).isEqualTo("h1 ::first-letter{}");
  }

  @Test
  public void testPseudoElement3() throws Exception {
    parseAndRun("div ::after {}");
    assertThat(compactPrintedResult).isEqualTo("div ::after{}");
  }

  @Test
  public void testPseudoElement4() throws Exception {
    parseAndRun("div ::before {}");
    assertThat(compactPrintedResult).isEqualTo("div ::before{}");
  }

  @Test
  public void testPseudoClassNth1() throws Exception {
    parseAndRun("ul :nth-child(5n+3) {}");
    assertThat(compactPrintedResult).isEqualTo("ul :nth-child(5n+3){}");
  }

  @Test
  public void testPseudoClassNth2() throws Exception {
    parseAndRun("ol :nth-last-child(5) {}");
    assertThat(compactPrintedResult).isEqualTo("ol :nth-last-child(5){}");
  }

  @Test
  public void testPseudoClassNth3() throws Exception {
    parseAndRun("p :nth-of-type(odd) {}");
    assertThat(compactPrintedResult).isEqualTo("p :nth-of-type(odd){}");
  }

  @Test
  public void testPseudoClassNth4() throws Exception {
    parseAndRun("div :nth-last-of-type(-2n-3) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-last-of-type(-2n-3){}");
  }

  @Test
  public void testPseudoClassBad1() throws Exception {
    parseAndRun("div :none {}", false,
        StrictCss3.UNSUPPORTED_PESUDO_CLASS_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassBad2() throws Exception {
    parseAndRun("div :first-line {}", false,
        StrictCss3.UNSUPPORTED_PESUDO_CLASS_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassBad3() throws Exception {
    parseAndRun("div :after {}", false,
        StrictCss3.UNSUPPORTED_PESUDO_CLASS_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoElementBad() throws Exception {
    parseAndRun("div ::none {}", false,
        StrictCss3.UNSUPPORTED_PESUDO_ELEMENT_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassNthBad1() throws Exception {
    parseAndRun("div :none(2n) {}", false,
        StrictCss3.UNSUPPORTED_PESUDO_CLASS_NTH_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassNthBad2() throws Exception {
    parseAndRun("div :bad(odd) {}", false,
        StrictCss3.UNSUPPORTED_PESUDO_CLASS_NTH_ERROR_MESSAGE);
  }

  @Test
  public void testPseudoClassNthBad3() throws Exception {
    parseAndRun("div :nth-last-of-type {}", false,
        StrictCss3.MISSING_FUNCTION_PESUDO_CLASS_NTH_ERROR_MESSAGE);
  }

  @Test
  public void testValidLengthUnits() throws Exception {
    parseAndRun(
        "a {" +
        "  width: 0;" +
        "  width: 1em;" +
        "  width: 1ex;" +
        "  width: 1ch;" +
        "  width: 1rem;" +
        "  width: 1vw;" +
        "  width: 1vh;" +
        "  width: 1vm;" +
        "  width: 1%;" +
        "  width: 1in;" +
        "  width: 1cm;" +
        "  width: 1mm;" +
        "  width: 1pt;" +
        "  width: 1pc;" +
        "  width: 1px;" +
        "  color: #fff;" +
        "  transform: rotate(90deg);" +
        "  transform: rotate(2rad);" +
        "  transform: rotate(2turn);" +
        "  transform: rotate(2grad);" +
        "  transition-duration: 1s;" +
        "  transition-duration: 1ms;" +
        "  voice-pitch: 1Hz;" +
        "  voice-pitch: 1kHz;" +
        "}");
  }

  @Test
  public void testInvalidLengthUnit() throws Exception {
    parseAndRun(
        "a {" +
        "  width: 1p;" +
        "}",
        StrictCss3.INVALID_UNIT_PREFIX + "p");
  }


  @Test
  public void testBorderValidation() throws Exception {
    parseAndRun(
        "a {" +
        "  border-width: 1px medium 4px;" +
        "}");
  }

  @Test
  public void testBorderValidation2() throws Exception {
    parseAndRun(
        "a {" +
        "  border-width: 1px medium 4xx;" +
        "}",
        StrictCss3.INVALID_UNIT_PREFIX + "xx");
  }

  @Test
  public void testBorderValidation3() throws Exception {
    parseAndRun(
        "a {" +
        "  border-width: 1xx medium 4px;" +
        "}",
        StrictCss3.INVALID_UNIT_PREFIX + "xx");
  }
}
