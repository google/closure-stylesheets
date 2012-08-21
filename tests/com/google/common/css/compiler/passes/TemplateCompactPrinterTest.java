/*
 * Copyright 2010 Google Inc.
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


/**
 * Unit tests for {@link TemplateCompactPrinter}.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class TemplateCompactPrinterTest extends ChunkCompactPrinterTest {

  @Override
  public void testChunkOutput() {
    char rS = TemplateCompactPrinter.RULE_START;
    char rE = TemplateCompactPrinter.RULE_END;

    setupTestTree();

    assertTemplateOutput("foo",
        rS + "foo{}" + rE
        + rS + "a{}" + rE
        + rS + "a#a{}" + rE
        + rS + "a#a b{}" + rE
        + rS + "b+i{}" + rE
        + "@media print{" + rS + "foo{}" + rE + "}");
    assertTemplateOutput("bar",
        rS + ".bar{}" + rE
        + rS + "b{}" + rE
        + rS + "b#b{}" + rE
        + rS + "b>i+em{}" + rE
        + "@keyframes my-animation{" + rS + "0%{}" + rE + "}");
    assertTemplateOutput("baz",
        rS + "hr,i{}" + rE
        + rS + "i{}" + rE
        + rS + "hr{}" + rE
        + rS + "i,hr{}" + rE
        + rS + "a i{}" + rE
        + rS + "a+i{}" + rE);
  }

  private void assertTemplateOutput(
      String chunk, String expected) {
    TemplateCompactPrinter<String> printer =
        new TemplateCompactPrinter<String>(newTree, chunk);
    printer.runPass();
    assertEquals(expected, printer.getCompactPrintedString());
  }
}
