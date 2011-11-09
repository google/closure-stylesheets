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

import com.google.common.css.compiler.ast.CssCommentNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRefinerNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorListNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;

/**
 * Compiler pass that traverses the tree and marks as non flippable the nodes
 * that should not be BiDi flipped.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class MarkNonFlippableNodes extends DefaultTreeVisitor
    implements CssCompilerPass {

  private VisitController visitController;

  /**
   * String that matches the comment marking a rule that should not be
   * flipped.
   * TODO(oana): Expand this annotation and make it more flexible.
   */
  private static final String NOFLIP = "/* @noflip */";

  public MarkNonFlippableNodes(VisitController visitController) {
    this.visitController = visitController;
  }

  /**
   * Returns whether the NOFLIP comment has been found among the comments of the
   * node.
   */
  private boolean hasNoFlip(CssNode node) {
    for (CssCommentNode comment : node.getComments()) {
      if (comment.getValue().equals(NOFLIP)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean enterMediaRule(CssMediaRuleNode node) {
    if (hasNoFlip(node)) {
      node.setShouldBeFlipped(false);
    }

    return true;
  }

  @Override
  public boolean enterSelector(CssSelectorNode node) {
    if (hasNoFlip(node)){
      node.setShouldBeFlipped(false);
    }
    return true;
  }

  @Override
  public void leaveSelector(CssSelectorNode node) {
    for (CssRefinerNode refiner : node.getRefiners().getChildren()){
      if (hasNoFlip(refiner)) {
        node.setShouldBeFlipped(false);
        return;
      }
    }
  }

  @Override
  public boolean enterRuleset(CssRulesetNode node) {
    // A ruleset can be inside a media rule or inside the root so we need to
    // check both its parent and its grandparent.
    // TODO(oana): Add enter and leave methods for the blocks inside a media or
    //     conditional rule to avoid calling the grandparent here.
    boolean noFlip = hasNoFlip(node) || !node.getParent().getShouldBeFlipped()
        || !node.getParent().getParent().getShouldBeFlipped();
    if (noFlip) {
      node.setShouldBeFlipped(false);
    }
    return true;
  }

  @Override
  public void leaveRuleset(CssRulesetNode node) {
    CssSelectorListNode selectors = node.getSelectors();
    for (CssSelectorNode sel : selectors.childIterable()) {
      if (!sel.getShouldBeFlipped()) {
        node.setShouldBeFlipped(false);
        selectors.setShouldBeFlipped(false);
        return;
      }
    }
  }

  @Override
  public boolean enterConditionalBlock(CssConditionalBlockNode node) {
    if (hasNoFlip(node) || !node.getParent().getShouldBeFlipped()
        || !node.getParent().getParent().getShouldBeFlipped()) {
      node.setShouldBeFlipped(false);
    }
    return true;
  }

  @Override
  public boolean enterConditionalRule(CssConditionalRuleNode node) {
    if (hasNoFlip(node) || !node.getParent().getShouldBeFlipped()) {
      node.setShouldBeFlipped(false);
    }
    return true;
  }

  @Override
  public boolean enterDeclarationBlock(CssDeclarationBlockNode node) {
    if (hasNoFlip(node) || !node.getParent().getShouldBeFlipped()) {
      node.setShouldBeFlipped(false);
    }
    return true;
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode node) {
    if (hasNoFlip(node) || !node.getParent().getShouldBeFlipped()) {
      node.setShouldBeFlipped(false);
    }
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
