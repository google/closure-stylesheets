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

import com.google.common.base.Joiner;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.FunctionalTestBase;

/**
 * Base class for compact printer tests.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public abstract class AbstractCompactPrinterTest extends FunctionalTestBase {
  
  protected boolean preserveMarkedComments; 
  
  @Override
  protected void setUp() throws Exception {
    preserveMarkedComments = false;
    super.setUp();
  }

  protected void assertCompactPrintedResult(String expected, String source) {
    parseAndBuildTree(source);

    assertCompactPrintedResult(expected, tree);

    assertCompactPrintedResult(expected, newTree);
  }

  protected void assertNewCompactPrintedResult(String expected, String source) {
    buildTreeWithNewParser(source);
    runPassesOnNewTree();
    assertCompactPrintedResult(expected, newTree);
  }

  protected CssTree parseStyleSheet(String sourceCode) {
    // NOTE(reinerp): We don't use the old parser, because it can't parse
    // @keyframes rules correctly: it expects the animation name to be enclosed
    // in double-quotes - see com.google.common.css.CssParser.scanKeyframes.
    buildTreeWithNewParser(sourceCode);
    runPassesOnNewTree();
    return newTree;
  }

  protected String lines(String... lines) {
    return Joiner.on("\n").join(lines);
  }

  private void assertCompactPrintedResult(String expected,
      CssTree treeToCheck) {
    CompactPrinter pass = new CompactPrinter(treeToCheck);
    pass.setPreserveMarkedComments(preserveMarkedComments);
    pass.runPass();
    assertEquals(expected, pass.getCompactPrintedString());
  }
}
