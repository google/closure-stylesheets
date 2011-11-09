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

import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorListNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.ast.SkippingTreeVisitor;

import java.util.List;

/**
 * Compiler pass that splits ruleset nodes into multiple ruleset nodes by
 * selector and declaration.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class SplitRulesetNodes extends SkippingTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;

  public SplitRulesetNodes(MutatingVisitController visitController) {
    this(visitController, false);
  }

  public SplitRulesetNodes(MutatingVisitController visitController,
        boolean skipping) {
    super(skipping);
    this.visitController = visitController;
  }

  @Override
  public boolean enterRuleset(CssRulesetNode node) {
    boolean canModifyRuleset = canModifyRuleset(node);
    if (canModifyRuleset) {
      List<CssNode> replacementNodes = Lists.newArrayList();

      CssSelectorListNode selectors = node.getSelectors();
      CssDeclarationBlockNode declarations = node.getDeclarations();

      for (CssSelectorNode sel : selectors.childIterable()) {
        for (CssNode child : declarations.childIterable()) {
          CssRulesetNode ruleset = new CssRulesetNode();
          ruleset.addDeclaration(child.deepCopy());
          ruleset.addSelector(sel.deepCopy());

          replacementNodes.add(ruleset);
        }
      }

      visitController.replaceCurrentBlockChildWith(
          replacementNodes,
          false);
    }
    return canModifyRuleset;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

}
