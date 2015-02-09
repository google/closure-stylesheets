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

import com.google.common.css.compiler.ast.CssForLoopRuleNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssLoopVariableNode;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

/**
 * Unit tests for {@link CreateForLoopNodes}.
 */
public class CreateForLoopNodesTest extends NewFunctionalTestBase {

  @Override
  protected void runPass() {
    CreateForLoopNodes pass = new CreateForLoopNodes(
        tree.getMutatingVisitController(), errorManager);
    pass.runPass();
  }

  public void testCreateLoopNodeWithNumbers() throws Exception {
    parseAndRun("@for $i from 1 to 3 step 2 {}");
    assertTrue(getFirstActualNode() instanceof CssForLoopRuleNode);
    CssForLoopRuleNode loop = (CssForLoopRuleNode) getFirstActualNode();
    assertEquals("1", loop.getFrom().toString());
    assertEquals("3", loop.getTo().toString());
    assertEquals("2", loop.getStep().toString());
  }

  public void testCreateLoopNodeWithVariables() throws Exception {
    parseAndRun("@for $i from $x to $y step $z {}");
    assertTrue(getFirstActualNode() instanceof CssForLoopRuleNode);
    CssForLoopRuleNode loop = (CssForLoopRuleNode) getFirstActualNode();
    assertTrue(loop.getFrom() instanceof CssLoopVariableNode);
    assertEquals("$x", loop.getFrom().getValue());
    assertTrue(loop.getTo() instanceof CssLoopVariableNode);
    assertEquals("$y", loop.getTo().getValue());
    assertTrue(loop.getStep() instanceof CssLoopVariableNode);
    assertEquals("$z", loop.getStep().getValue());
  }

  public void testCreateLoopNodeWithConstants() throws Exception {
    parseAndRun("@for $i from X to Y step Z {}");
    assertTrue(getFirstActualNode() instanceof CssForLoopRuleNode);
    CssForLoopRuleNode loop = (CssForLoopRuleNode) getFirstActualNode();
    assertTrue(loop.getFrom() instanceof CssLiteralNode);
    assertEquals("X", loop.getFrom().getValue());
    assertTrue(loop.getTo() instanceof CssLiteralNode);
    assertEquals("Y", loop.getTo().getValue());
    assertTrue(loop.getStep() instanceof CssLiteralNode);
    assertEquals("Z", loop.getStep().getValue());
  }

  public void testLoopWithoutBlock() throws Exception {
    parseAndRun("@for $i from 1 to 2;", "@for with no block");
  }

  public void testLoopWithoutAllParameters() throws Exception {
    parseAndRun("@for $i from 1 to {}", CreateForLoopNodes.SYNTAX_ERROR);
  }

  public void testLoopWithoutBadVariablePattern() throws Exception {
    parseAndRun("@for i from 1 to 2 {}", CreateForLoopNodes.SYNTAX_ERROR);
  }

  public void testLoopThatOverridesVariable() throws Exception {
    parseAndRun(linesToString(
        "@for $i from 1 to 2 {",
        "  @for $i from 4 to 6 {",
        "  }",
        "}"),
        CreateForLoopNodes.OVERRIDE_VARIABLE_NAME);
  }
}
