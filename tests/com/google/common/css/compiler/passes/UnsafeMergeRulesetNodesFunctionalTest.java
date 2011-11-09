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

import com.google.common.css.compiler.ast.FunctionalTestBase;

/**
 * Functional tests for {@link UnsafeMergeRulesetNodes}.
 *
 */
public class UnsafeMergeRulesetNodesFunctionalTest extends FunctionalTestBase {
  private boolean byPartition = false;

  public void testSorting() {
    testEachTreeConstruction(
        "c, a, b {x: 1px;}",
        "[[a, b, c]{[x:[1px]]}]");
  }

  public void testGroupByDeclarations() {
    testEachTreeConstruction(
        linesToString(
          "c {x: 2px;}",
          "a, b {x: 2px; y: 3px}"),
        "[[a, b, c]{[x:[2px]]}, "
        + "[a, b]{[y:[3px]]}]");
  }

  public void testPropertyPartition() {
    testEachTreeConstruction(
        linesToString(
          "c {padding: 1px;}",
          "a {padding: 1px; padding-right: 3px; padding-left:  2px;}",
          "b {padding: 1px; padding-left: 2px;  padding-right: 3px;}"),
        // Not grouped by partition.
        "[[a, b, c]{[padding:[1px]]}, "
        + "[a, b]{[padding-left:[2px]]}, "
        + "[a, b]{[padding-right:[3px]]}]",
        // Grouped by partition.
        "[[c]{[padding:[1px]]}, "
        + "[a, b]{[padding:[1px], padding-left:[2px], padding-right:[3px]]}]");
  }

  public void testBorderAlwaysPartitioned() {
    testEachTreeConstruction(
        linesToString(
          "a {border-left: red;}",
          "b {border-color: red; border-left: blue;}",
          "c {border-left: red; border-color: blue;}"),
        // The border property is always grouped by partition
        // because ordering might matter.
        "[[b]{[border-color:[red], border-left:[blue]]}, "
        + "[a]{[border-left:[red]]}, "
        + "[c]{[border-left:[red], border-color:[blue]]}]");
  }

  private void testEachTreeConstruction(String input, String output) {
    testEachTreeConstruction(input, output, output);
  }

  private void testEachTreeConstruction(String input, String output1, String output2) {
    byPartition = false;
    testTreeConstruction(input, output1);
    byPartition = true;
    testTreeConstruction(input, output2);
  }

  @Override
  protected void runPass() {
    new SplitRulesetNodes(tree.getMutatingVisitController(), true).runPass();

    new MarkRemovableRulesetNodes(tree, true).runPass();

    new EliminateUselessRulesetNodes(tree).runPass();

    new UnsafeMergeRulesetNodes(tree, byPartition, true).runPass();
  }
}
