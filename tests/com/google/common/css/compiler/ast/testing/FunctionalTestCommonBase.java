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

package com.google.common.css.compiler.ast.testing;

import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.VisitController;
import com.google.common.css.compiler.passes.UniformVisitor;
import com.google.common.truth.Truth;

/**
 * Utility methods for all the functional tests.
 *
 */
public abstract class FunctionalTestCommonBase extends AstUtilityTestCase {

  protected CssTree tree = null;

  /**
   * Utility method that given an input string parses and converts it.
   *
   * @param sourceCode A string representing the css input for the parser
   */
  protected abstract void parseAndBuildTree(String sourceCode);

  /**
   * Utility method to test if for a given css input, the output is as
   * expected.
   *
   * @param sourceCode
   * @param expectedOutput
   */
  protected void testTreeConstruction(
      String sourceCode,
      String expectedOutput) {

    parseAndBuildTree(sourceCode);
    runPass();
    checkTreeDebugString(expectedOutput);
  }

  /**
   * Utility method to compare the string representation of the tree against
   * the expected string.
   *
   * @param expected
   */
  protected void checkTreeDebugString(String expected) {
    Truth.assertThat(tree.getRoot().toString()).isEqualTo(expected);
  }

  protected boolean isEmptyBody() {
    return tree.getRoot().getBody().isEmpty();
  }

  protected CssNode getFirstActualNode() {
    return tree.getRoot().getBody().getChildAt(0);
  }

  protected <T> T findFirstNodeOf(final Class<T> clazz) {
    final Object[] holder = new Object[1];
    final VisitController vc = tree.getVisitController();
    vc.startVisit(
        UniformVisitor.Adapters.asVisitor(
            new UniformVisitor() {
              @Override
              public void enter(CssNode n) {
                if (clazz.isAssignableFrom(n.getClass())) {
                  holder[0] = n;
                  vc.stopVisit();
                }
              }

              @Override
              public void leave(CssNode node) {}
            }));
    return clazz.cast(holder[0]);
  }

  /**
   * Assuming that the first actual node is a {@link CssRulesetNode}, gets the
   * property value of the first declaration.
   *
   * @return The property value of the first declaration
   */
  protected CssPropertyValueNode getFirstPropertyValue() {
    return getPropertyValue(0);
  }

  /**
   * Assuming that the first actual node is a {@link CssRulesetNode}, gets the
   * property value of the declaration with the given index.
   *
   * @param i The index of the declaration
   * @return The property value for the declaration with the given index
   */
  protected CssPropertyValueNode getPropertyValue(int i) {
    CssRulesetNode ruleset = (CssRulesetNode) getFirstActualNode();
    CssNode decl = ruleset.getDeclarations().getChildAt(i);
    return ((CssDeclarationNode) decl).getPropertyValue();
  }

  /** Runs a compiler pass. */
  protected void runPass() {}
}
