/*
 * Copyright 2015 Google Inc.
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

import com.google.common.collect.ImmutableMap;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssForLoopRuleNode;
import com.google.common.css.compiler.passes.testing.PassesTestBase;

import java.util.Map;

/**
 * Unit tests for {@link EvaluateCompileConstants}.
 */
public class EvaluateCompileConstantsTest extends PassesTestBase {

  private static final Map<String, Integer> CONSTANTS =
      ImmutableMap.of("FOO", 2, "BAR", 7, "BAZ", 3);

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
    new CreateForLoopNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new EvaluateCompileConstants(tree.getMutatingVisitController(), CONSTANTS).runPass();
  }

  public void testLoopParametersReplacement() throws Exception {
    parseAndRun("@for $i from FOO to BAR step BAZ {}");
    assertTrue(getFirstActualNode() instanceof CssForLoopRuleNode);
    CssForLoopRuleNode loop = (CssForLoopRuleNode) getFirstActualNode();
    assertEquals("2", loop.getFrom().toString());
    assertEquals("7", loop.getTo().toString());
    assertEquals("3", loop.getStep().toString());
  }

  public void testValueInDefinitionReplacement() throws Exception {
    parseAndRun("@def X FOO;");
    assertTrue(getFirstActualNode() instanceof CssDefinitionNode);
    CssDefinitionNode definition = (CssDefinitionNode) getFirstActualNode();
    assertEquals(1, definition.getChildren().size());
    assertEquals("2", definition.getChildAt(0).toString());
  }

  public void testValueInArgumentReplacement() throws Exception {
    parseAndRun("@def X f(BAR);");
    assertTrue(getFirstActualNode() instanceof CssDefinitionNode);
    CssDefinitionNode definition = (CssDefinitionNode) getFirstActualNode();
    assertEquals(1, definition.getChildren().size());
    assertEquals("f(7)", definition.getChildAt(0).toString());
  }
}
