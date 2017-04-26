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

package com.google.common.css.compiler.passes;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link HandleUnknownAtRuleNodes}.
 *
 */
@RunWith(JUnit4.class)
public class HandleUnknownAtRuleNodesTest extends NewFunctionalTestBase {

  private final String errorMessage = HandleUnknownAtRuleNodes.unknownAtRuleErrorMessage;

  private final String testCode = "@foo a b c {.x {y: z}\n@bar {}\n@baz X;}";
  private final String testCodePrettyPrintedResult = linesToString(
      "@foo a b c {",
      "  .x {",
      "    y: z;",
      "  }",
      "  @bar {",
      "  }",
      "  @baz X;",
      "}",
      "");
  private final String testCodeCompactPrintedResult = "@foo a b c{.x{y:z}@bar{}@baz X;}";

  private boolean report;
  private boolean remove;
  private String prettyPrintedResult;
  private String compactPrintedResult;

  @Override
  protected void runPass() {
    new HandleUnknownAtRuleNodes(
        tree.getMutatingVisitController(), errorManager,
        Sets.<String>newHashSet("-custom-at-rule"),
        report, remove).runPass();
    PrettyPrinter prettyPrinterPass = new PrettyPrinter(tree.getVisitController());
    prettyPrinterPass.runPass();
    prettyPrintedResult = prettyPrinterPass.getPrettyPrintedString();
    CompactPrinter compactPrinterPass = new CompactPrinter(tree);
    compactPrinterPass.runPass();
    compactPrintedResult = compactPrinterPass.getCompactPrintedString();
  }

  @Test
  public void testReportRemove() throws Exception {
    report = true;
    remove = true;
    parseAndRun(testCode, errorMessage);
    assertThat(prettyPrintedResult).isEmpty();
    assertThat(compactPrintedResult).isEmpty();
  }

  @Test
  public void testReportDoNotRemove() throws Exception {
    report = true;
    remove = false;
    parseAndRun(testCode, errorMessage, errorMessage, errorMessage);
    assertThat(prettyPrintedResult).isEqualTo(testCodePrettyPrintedResult);
    assertThat(compactPrintedResult).isEqualTo(testCodeCompactPrintedResult);
  }

  @Test
  public void testDoNotReportRemove() throws Exception {
    report = false;
    remove = true;
    parseAndRun(testCode);
    assertThat(prettyPrintedResult).isEmpty();
    assertThat(compactPrintedResult).isEmpty();
  }

  @Test
  public void testDoNotReportDoNotRemove() throws Exception {
    report = false;
    remove = false;
    parseAndRun(testCode);
    assertThat(prettyPrintedResult).isEqualTo(testCodePrettyPrintedResult);
    assertThat(compactPrintedResult).isEqualTo(testCodeCompactPrintedResult);
  }

  @Test
  public void testDoNotReportDoNotRemoveMedia() throws Exception {
    report = true;
    remove = true;
    parseAndRun("@media print { .A { margin: 0; } }");
    assertThat(prettyPrintedResult)
        .isEqualTo(linesToString("@media print {", "  .A {", "    margin: 0;", "  }", "}", ""));
    assertThat(compactPrintedResult).isEqualTo("@media print{.A{margin:0}}");
  }

  @Test
  public void testDoNotReportDoNotRemoveMediaWithUnknown() throws Exception {
    report = true;
    remove = true;
    parseAndRun("@media print { @foo { .A { margin: 0; } } }", errorMessage);
    assertThat(prettyPrintedResult).isEqualTo(linesToString("@media print {", "}", ""));
    assertThat(compactPrintedResult).isEqualTo("@media print{}");
  }

  @Test
  public void testDoNotReportDoNotRemoveCustomAtRule() throws Exception {
    report = true;
    remove = true;
    parseAndRun("@-custom-at-rule print { }");
    assertThat(compactPrintedResult).isEqualTo("@-custom-at-rule print{}");
  }
}
