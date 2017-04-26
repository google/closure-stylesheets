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

import com.google.common.css.compiler.ast.CssKeyNode;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** @author fbenz@google.com (Florian Benz) */
@RunWith(JUnit4.class)
public class ProcessKeyframesTest extends NewFunctionalTestBase {
  private static final String TEST_CODE_FROM_TO = linesToString(
      "@-webkit-keyframes bounce {",
      "from { left: 0px; }",
      "to { left: 200px; }",
      "}");
  private static final String TEST_MOZ_CODE_FROM_TO = linesToString(
      "@-moz-keyframes bounce {",
      "from { left: 0px; }",
      "to { left: 200px; }",
      "}");
  private static final String TEST_CODE_FROM_TO_BAD_1 = linesToString(
      "@-webkit-keyframes bounce {",
      "from_ { left: 0px; }",
      "to { left: 200px; }",
      "}");
  private static final String TEST_CODE_FROM_TO_BAD_2 = linesToString(
      "@-webkit-keyframes bounce {",
      "from { left: 0px; }",
      "to_ { left: 200px; }",
      "}");
  private static final String TEST_CODE_FROM_TO_BAD_3 = linesToString(
      "@-webkit-keyframes bounce {",
      "from { left: 0px; }",
      "tP { left: 200px; }",
      "}");
  private static final String TEST_CODE_FROM_TO_BAD_4 = linesToString(
      "@-webkit-keyframes bounce {",
      "frBm { left: 0px; }",
      "to { left: 200px; }",
      "}");
  private static final String TEST_CODE_PERCENTAGE_1 = linesToString(
      "@-webkit-keyframes pulse {",
      "0% {}",
      "33.33% {}",
      "100% {}",
      "}");
  private static final String TEST_CODE_PERCENTAGE_2 = linesToString(
      "@-webkit-keyframes pulse {",
      "0.000000% {}",
      "100.0000000% {}",
      "}");
  private static final String TEST_CODE_PERCENTAGE_3 = linesToString(
      "@-webkit-keyframes pulse {",
      "0.8200000% {}",
      "012.003400000% {}",
      "98.0000000% {}",
      "}");
  private static final String TEST_CODE_PERCENTAGE_BAD_1 = linesToString(
      "@-webkit-keyframes pulse {",
      "0% {}",
      "33.33% {}",
      "101% {}",
      "}");
  private static final String TEST_CODE_PERCENTAGE_BAD_2 = linesToString(
      "@-webkit-keyframes pulse {",
      "0% {}",
      "33.33% {}",
      "100.12% {}",
      "}");
  
  private boolean keyframesAllowed;
  private boolean simplifyCss;
  private String compactPrintedResult;

  @Override
  protected void runPass() {
    new ProcessKeyframes(
        tree.getVisitController(), errorManager,
        keyframesAllowed, simplifyCss).runPass();
    CompactPrinter compactPrinterPass = new CompactPrinter(tree);
    compactPrinterPass.runPass();
    compactPrintedResult = compactPrinterPass.getCompactPrintedString();
  }

  protected void runCompactRepresentation(String expected, String value) {
    CssKeyNode node = new CssKeyNode(value);
    ProcessKeyframes process = new ProcessKeyframes(
        null, errorManager, true, true);
    if (!value.equals("from") && !value.equals("to")) {
      float percentage = Float.parseFloat(value.substring(0,
          value.length() - 1));
      process.compactRepresentation(node, percentage);
    } else {
      process.compactRepresentation(node, -1);
    }
    String actual = node.getKeyValue();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testFromTo() throws Exception {
    keyframesAllowed = true;
    simplifyCss = false;
    parseAndRun(TEST_CODE_FROM_TO);
    assertThat(compactPrintedResult)
        .isEqualTo("@-webkit-keyframes bounce{from{left:0px}to{left:200px}}");
  }

  @Test
  public void testMozFromTo() throws Exception {
    keyframesAllowed = true;
    simplifyCss = false;
    parseAndRun(TEST_MOZ_CODE_FROM_TO);
    assertThat(compactPrintedResult)
        .isEqualTo("@-moz-keyframes bounce{from{left:0px}to{left:200px}}");
  }


  @Test
  public void testDeactivated() throws Exception {
    keyframesAllowed = false;
    simplifyCss = false;
    parseAndRun(TEST_CODE_FROM_TO,
        ProcessKeyframes.KEYFRAMES_NOT_ALLOWED_ERROR_MESSAGE);
    assertThat(compactPrintedResult)
        .isEqualTo("@-webkit-keyframes bounce{from{left:0px}to{left:200px}}");
  }

  @Test
  public void testFromToSimplified() throws Exception {
    keyframesAllowed = true;
    simplifyCss = true;
    parseAndRun(TEST_CODE_FROM_TO);
    assertThat(compactPrintedResult)
        .isEqualTo("@-webkit-keyframes bounce{0%{left:0px}to{left:200px}}");
  }

  @Test
  public void testFromToBad1() throws Exception {
    keyframesAllowed = true;
    simplifyCss = false;
    parseAndRun(TEST_CODE_FROM_TO_BAD_1,
        ProcessKeyframes.INVALID_NUMBER_ERROR_MESSAGE);
  }

  @Test
  public void testFromToBad2() throws Exception {
    keyframesAllowed = true;
    simplifyCss = false;
    parseAndRun(TEST_CODE_FROM_TO_BAD_2,
        ProcessKeyframes.INVALID_NUMBER_ERROR_MESSAGE);
  }

  @Test
  public void testFromToBad3() throws Exception {
    keyframesAllowed = true;
    simplifyCss = false;
    parseAndRun(TEST_CODE_FROM_TO_BAD_3,
        ProcessKeyframes.INVALID_NUMBER_ERROR_MESSAGE);
  }

  @Test
  public void testFromToBad4() throws Exception {
    keyframesAllowed = true;
    simplifyCss = false;
    parseAndRun(TEST_CODE_FROM_TO_BAD_4,
        ProcessKeyframes.INVALID_NUMBER_ERROR_MESSAGE);
  }

  @Test
  public void testPercentage1() throws Exception {
    keyframesAllowed = true;
    simplifyCss = false;
    parseAndRun(TEST_CODE_PERCENTAGE_1);
    assertThat(compactPrintedResult).isEqualTo("@-webkit-keyframes pulse{0%{}33.33%{}100%{}}");
  }

  @Test
  public void testPercentage2() throws Exception {
    keyframesAllowed = true;
    simplifyCss = false;
    parseAndRun(TEST_CODE_PERCENTAGE_2);
    assertThat(compactPrintedResult)
        .isEqualTo("@-webkit-keyframes pulse{0.000000%{}100.0000000%{}}");
  }

  @Test
  public void testPercentageSimplified1() throws Exception {
    keyframesAllowed = true;
    simplifyCss = true;
    parseAndRun(TEST_CODE_PERCENTAGE_1);
    assertThat(compactPrintedResult).isEqualTo("@-webkit-keyframes pulse{0%{}33.33%{}to{}}");
  }

  @Test
  public void testPercentageSimplified2() throws Exception {
    keyframesAllowed = true;
    simplifyCss = true;
    parseAndRun(TEST_CODE_PERCENTAGE_3);
    assertThat(compactPrintedResult).isEqualTo("@-webkit-keyframes pulse{.82%{}12.0034%{}98%{}}");
  }

  @Test
  public void testPercentageBad1() throws Exception {
    keyframesAllowed = true;
    simplifyCss = true;
    parseAndRun(TEST_CODE_PERCENTAGE_BAD_1,
        ProcessKeyframes.WRONG_KEY_VALUE_ERROR_MESSAGE);
  }

  @Test
  public void testPercentageBad2() throws Exception {
    keyframesAllowed = true;
    simplifyCss = true;
    parseAndRun(TEST_CODE_PERCENTAGE_BAD_2,
        ProcessKeyframes.WRONG_KEY_VALUE_ERROR_MESSAGE);
  }

  @Test
  public void testCompactRepresentation() throws Exception {
    runCompactRepresentation("to", "to");
    runCompactRepresentation("to", "100%");
    runCompactRepresentation("to", "100.00000%");
    runCompactRepresentation("0%", "from");
    runCompactRepresentation("0%", "0%");
    runCompactRepresentation("0%", "0.0%");
    runCompactRepresentation("11%", "11.%");
    runCompactRepresentation("12%", "0012%");
    runCompactRepresentation("13.45%", "13.45%");
    runCompactRepresentation("14.05%", "14.05%");
    runCompactRepresentation("15.02%", "0015.02%");
    runCompactRepresentation(".16%", ".16%");
    runCompactRepresentation(".0017%", ".001700%");
    runCompactRepresentation(".12345679%", ".1234567890123456789%");
    runCompactRepresentation(".1%", ".1%");
    runCompactRepresentation("1234.5679%", "1234.567890123456789%");
  }
}
