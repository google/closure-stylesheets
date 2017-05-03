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

package com.google.common.css.compiler.passes.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

import java.util.List;

/**
 * Base class for testing passes where the printed output has to be compared.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class PassesTestBase extends NewFunctionalTestBase {
  /**
   * Parses GSS source code and compares the expected output to the
   * {@link AstPrinter}.
   */
  @Override
  protected void testTreeConstruction(String sourceCode,
      String expectedOutput) {
    parseAndBuildTree(sourceCode);
    checkTreeDebugString(expectedOutput);
  }

  /**
   * Compares the expected output to the output of the {@link AstPrinter}.
   * GSS source code has to be parsed before calling this method.
   */
  @Override
  protected void checkTreeDebugString(String expected) {
    assertEquals(expected, AstPrinter.print(tree));
  }

  protected void checkRuleset(String expected, CssRulesetNode rule) {
    List<CssNode> blockChildren = ImmutableList.of((CssNode) rule);
    CssBlockNode block = new CssBlockNode(false, blockChildren);
    CssRootNode root = new CssRootNode(block);
    CssTree t = new CssTree(null, root);
    assertEquals(expected, AstPrinter.print(t));
  }
}
