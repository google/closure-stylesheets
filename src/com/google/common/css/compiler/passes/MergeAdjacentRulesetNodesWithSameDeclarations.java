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

import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRefinerNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.ast.SkippingTreeVisitor;

import java.util.Iterator;

/**
 * Compiler pass that merges adjacent ruleset nodes that have the same selector.
 * 
 * @author oana@google.com (Oana Florescu)
 */
public class MergeAdjacentRulesetNodesWithSameDeclarations
    extends SkippingTreeVisitor 
    implements CssCompilerPass {

  private final CssTree tree;
  private final MutatingVisitController visitController;

  public MergeAdjacentRulesetNodesWithSameDeclarations(CssTree tree) {
    this(tree, false);
  }

  public MergeAdjacentRulesetNodesWithSameDeclarations(CssTree tree,
        boolean skipping) {
    super(skipping);
    this.tree = tree;
    this.visitController = tree.getMutatingVisitController();
  }

  @Override
  public boolean enterTree(CssRootNode root) {
    tree.resetRulesetNodesToRemove();
    return true;
  }

  @Override
  public boolean enterBlock(CssBlockNode block) {
    if (block.numChildren() <= 1) {
      return true; // There is nothing to merge
    }

    Iterator<CssNode> iterator = block.getChildIterator();
    CssNode node;
    CssRulesetNode ruleToMergeTo = null;

    while (iterator.hasNext()) {
      node = iterator.next();

      if (!(node instanceof CssRulesetNode) ||
          hasProblematicSelectors((CssRulesetNode) node) ||
          !canModifyRuleset((CssRulesetNode) node)) {
        // Node is unmergeable. Skip the current node and make it act as a barrier to merging by
        // clearing the current merge candidate.
        ruleToMergeTo = null;
        continue;
      }

      CssRulesetNode currentRule = (CssRulesetNode) node;

      if (ruleToMergeTo != null &&
          ruleToMergeTo.getDeclarations().toString().equals(
              currentRule.getDeclarations().toString())) {
        for (CssSelectorNode decl : currentRule.getSelectors().childIterable()) {
          ruleToMergeTo.addSelector(decl);
        }
        tree.getRulesetNodesToRemove().addRulesetNode(currentRule);
      } else {
        ruleToMergeTo = currentRule;
      }
    }

    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

  /**
   * Determines if the rule has selectors that not all browsers understand and
   * may cause issues when merging.
   *
   * <p>For example, the ::-ms-clear pseudo element can invalid an entire ruleset.  Browsers other
   * than IE10+ will ignore this entire rule:
   * <pre>
   * #foo,input::-ms-clear{display:none}
   * </pre>
   *
   * <p>Note that this issue occurs with any unrecognized pseudo element. For example, ::foo
   * will break the rest of the selectors, whereas ::before will not (since it is recognized).
   */
  private boolean hasProblematicSelectors(CssRulesetNode rule) {
    for (CssSelectorNode selector : rule.getSelectors().childIterable()) {
      for (CssRefinerNode refiner : selector.getRefiners().childIterable()) {
        if (refiner.getRefinerType() == CssRefinerNode.Refiner.PSEUDO_ELEMENT) {
          // Unrecognized names in pseudo selectors can invalidate the whole rule.
          return true;
        }
      }
    }
    return false;
  }
}
