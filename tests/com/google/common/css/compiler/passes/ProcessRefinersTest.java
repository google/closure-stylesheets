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

/** @author fbenz@google.com (Florian Benz) */
@RunWith(JUnit4.class)
public class ProcessRefinersTest extends NewFunctionalTestBase {
  private boolean simplifyCss;
  private String compactPrintedResult;

  @Override
  protected void runPass() {
    new ProcessRefiners(tree.getMutatingVisitController(), errorManager,
        simplifyCss).runPass();
    CompactPrinter compactPrinterPass = new CompactPrinter(tree);
    compactPrinterPass.runPass();
    compactPrintedResult = compactPrinterPass.getCompactPrintedString();
  }

  protected void runParseA(int expected, String argument) {
    ProcessRefiners processRefiners = new ProcessRefiners(null, errorManager,
        simplifyCss);
    int indexOfN = argument.indexOf('n');
    int actual = processRefiners.parseA(argument, indexOfN);
    assertThat(actual).isEqualTo(expected);
  }

  protected void runParseB(int expected, String argument) {
    ProcessRefiners processRefiners = new ProcessRefiners(null, errorManager,
        simplifyCss);
    int indexOfN = argument.indexOf('n');
    int actual = processRefiners.parseB(argument, indexOfN);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNthCompact1() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child(+n) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
  }

  @Test
  public void testNthCompact2() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child(+1n) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
  }

  @Test
  public void testNthCompact3() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child(1n) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
  }

  @Test
  public void testNthCompact4() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child(+n+0) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
  }

  @Test
  public void testNthCompact5() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( +2n+5 ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(2n+5){}");
  }

  @Test
  public void testNthCompact6() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( n-7 ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(n-7){}");
  }

  @Test
  public void testNthCompact7() throws Exception {
    simplifyCss = true;
    parseAndRun("#id :nth-child( 2 ) {}");
    assertThat(compactPrintedResult).isEqualTo("#id :nth-child(2){}");
  }

  @Test
  public void testNthCompact8() throws Exception {
    simplifyCss = true;
    parseAndRun(".class :nth-child( -3 ) {}");
    assertThat(compactPrintedResult).isEqualTo(".class :nth-child(-3){}");
  }

  @Test
  public void testNthCompact9() throws Exception {
    simplifyCss = true;
    parseAndRun("* :nth-child( odd ) {}");
    assertThat(compactPrintedResult).isEqualTo("* :nth-child(odd){}");
  }

  @Test
  public void testNthCompact10() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( even ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(2n){}");
  }

  @Test
  public void testNthCompact11() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( +3 ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(3){}");
  }

  @Test
  public void testNthCompact12() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( 0 ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(0){}");
  }

  @Test
  public void testNthCompact13() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( 0n ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(0){}");
  }

  @Test
  public void testNthCompact14() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( 0n+0 ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(0){}");
  }

  @Test
  public void testNthCompact15() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( 0 n - 1 ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(-1){}");
  }

  @Test
  public void testNthCompact16() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( 1 n - 0 ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
  }

  @Test
  public void testNthCompact17() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( -1 n - 0 ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(-1n){}");
  }

  @Test
  public void testNthCompact18() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( -n ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(-1n){}");
  }

  @Test
  public void testNthCompact19() throws Exception {
    simplifyCss = true;
    parseAndRun("div :nth-child( -n+5 ) {}");
    assertThat(compactPrintedResult).isEqualTo("div :nth-child(-1n+5){}");
  }

  @Test
  public void testNthBad1() throws Exception {
    simplifyCss = false;
    parseAndRun("div :nth-child(1.1) {}",
        ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
  }

  @Test
  public void testNthBad2() throws Exception {
    simplifyCss = false;
    parseAndRun("div :nth-child(n+2.3) {}",
        ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
  }

  @Test
  public void testNthBad3() throws Exception {
    simplifyCss = false;
    parseAndRun("div :nth-child(m+7) {}",
        ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
  }

  @Test
  public void testNthBad4() throws Exception {
    simplifyCss = false;
    parseAndRun("div :nth-child(oddy) {}",
        ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
  }

  @Test
  public void testNthBad5() throws Exception {
    simplifyCss = false;
    parseAndRun("div :nth-child(_even) {}",
        ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
  }

  @Test
  public void testNotBad1() throws Exception {
    simplifyCss = false;
    parseAndRun("div :not(:not(*)) {}",
        ProcessRefiners.INVALID_NOT_SELECTOR_ERROR_MESSAGE);
  }

  @Test
  public void testNotBad2() throws Exception {
    simplifyCss = false;
    parseAndRun("div :not(::first-line) {}",
        ProcessRefiners.INVALID_NOT_SELECTOR_ERROR_MESSAGE);
  }

  @Test
  public void testNotBad3() throws Exception {
    simplifyCss = false;
    parseAndRun("div :not(.A.B) {}",
        ProcessRefiners.INVALID_NOT_SELECTOR_ERROR_MESSAGE);
  }

  @Test
  public void testNotWithComment() throws Exception {
    simplifyCss = true;
    parseAndRun("div:not(.A /* C */) {}");
    assertThat(compactPrintedResult).isEqualTo("div:not(.A){}");
  }


  @Test
  public void testParseA() throws Exception {
    runParseA(0, "2");
    runParseA(0, "-3");
    runParseA(1, "n");
    runParseA(1, "+n");
    runParseA(-1, "-n");
    runParseA(2, "2n");
    runParseA(3, "+3n");
    runParseA(-2, "-2n");
    runParseA(42, "42n+3");
    runParseA(-23, "-23n-67");
    runParseA(-23, "-23n-0");
    runParseA(-23, "-23n+0");
  }

  @Test
  public void testParseB() throws Exception {
    runParseB(2, "2");
    runParseB(-3, "-3");
    runParseB(0, "n");
    runParseB(0, "+n");
    runParseB(0, "-n");
    runParseB(0, "2n");
    runParseB(0, "+3n");
    runParseB(0, "-2n");
    runParseB(3, "42n+3");
    runParseB(-67, "-23n-67");
    runParseB(0, "-23n+0");
    runParseB(0, "-21n-0");
  }

  @Test
  public void testLang() throws Exception {
    simplifyCss = true;
    parseAndRun("div :lang(en) {}");
    assertThat(compactPrintedResult).isEqualTo("div :lang(en){}");
  }
}
