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

import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 * Functional tests for {@link RemoveEmptyValues}.
 *
 */
public class RemoveEmptyValuesFunctionalTest extends PassesTestBase {

  public void testRemoveEmptyValues1() {
    testTreeConstruction(linesToString(
        "@def COLOR empty;",
        "@def PADDING 1px;",
        ".CSS_RULE {",
        "  color: COLOR;",
        "  padding: PADDING;",
        "}"),
        "[[.CSS_RULE]{[padding:[[1px]];]}]");
  }

  public void testRemoveEmptyValues2() {
    testTreeConstruction(linesToString(
        "@def COLOR empty;",
        "@def PADDING 1px;",
        ".CSS_RULE {",
        "  color: COLOR !important;",
        "  padding: PADDING;",
        "}"),
        "[[.CSS_RULE]{[padding:[[1px]];]}]");
  }

  public void testRemoveEmptyValues3() {
    testTreeConstruction(linesToString(
        "@def A empty;",
        "@def B A A;",
        "@def C A 1px;",
        ".CSS_RULE {",
        "  color: A B !important;",
        "  padding: C;",
        "}"),
        "[[.CSS_RULE]{[padding:[[1px]];]}]");
  }

  public void testRemoveEmptyValues4() {
    testTreeConstruction(linesToString(
        "@def A empty;",
        "@def B A A;",
        "@def C A B;",
        ".CSS_RULE {",
        "  color: A B !important;",
        "  padding: C;",
        "}"),
        "[]");
  }

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController())
        .runPass();

    CollectConstantDefinitions collectConstantDefinitionsPass =
        new CollectConstantDefinitions(tree);
    collectConstantDefinitionsPass.runPass();

    new ReplaceConstantReferences(
        tree, collectConstantDefinitionsPass.getConstantDefinitions(),
        true /* removeDefs */, errorManager,
        true /* allowUndefinedConstants */).runPass();

    new RemoveEmptyValues(tree.getMutatingVisitController()).runPass();

    new SplitRulesetNodes(tree.getMutatingVisitController()).runPass();

    new MarkRemovableRulesetNodes(tree).runPass();

    new EliminateUselessRulesetNodes(tree).runPass();

    new MergeAdjacentRulesetNodesWithSameSelector(tree).runPass();

    new MergeAdjacentRulesetNodesWithSameDeclarations(tree).runPass();
  }
}
