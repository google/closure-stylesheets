/*
 * Copyright 2012 Google Inc.
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

import com.google.common.css.compiler.ast.CssAttributeSelectorNode;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCombinatorNode;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssComponentNode;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssIdSelectorNode;
import com.google.common.css.compiler.ast.CssImportBlockNode;
import com.google.common.css.compiler.ast.CssImportRuleNode;
import com.google.common.css.compiler.ast.CssKeyListNode;
import com.google.common.css.compiler.ast.CssKeyNode;
import com.google.common.css.compiler.ast.CssKeyframeRulesetNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssMixinNode;
import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNodesListNode;
import com.google.common.css.compiler.ast.CssPageRuleNode;
import com.google.common.css.compiler.ast.CssPageSelectorNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssPseudoClassNode;
import com.google.common.css.compiler.ast.CssPseudoElementNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorListNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;

/**
 * Build up an s-expression corresponding to the AST for
 * debugging purposes.
 */
public class SExprPrinter implements CssTreeVisitor {

  public StringBuilder sb;

  public SExprPrinter(StringBuilder sb) {
    this.sb = sb;
  }

  public SExprPrinter() {
    this(new StringBuilder());
  }

  private void enter(CssNode node) {
    sb.append(
        String.format("(%s@%d", node.getClass().getName(), node.hashCode()));
  }

  private void trim(StringBuilder sb, String waste) {
    if (sb.length() == 0) {
      return;
    }
    int expectedPosition = sb.length() - waste.length();
    if (sb.indexOf(waste, expectedPosition) != -1) {
      sb.delete(expectedPosition, sb.length());
    }
  }

  private void leave(CssNode node) {
    trim(sb, " ");
    sb.append(")");
  }

  public boolean enterConditionalRule(CssConditionalRuleNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssConditionalRuleNode}'s sub trees */
  public void leaveConditionalRule(CssConditionalRuleNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssImportRuleNode}'s sub trees */
  public boolean enterImportRule(CssImportRuleNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssImportRuleNode}'s sub trees */
  public void leaveImportRule(CssImportRuleNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssMediaRuleNode}'s sub trees */
  public boolean enterMediaRule(CssMediaRuleNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssMediaRuleNode}'s sub trees */
  public void leaveMediaRule(CssMediaRuleNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssPageRuleNode}'s sub trees */
  public boolean enterPageRule(CssPageRuleNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssPageRuleNode}'s sub trees */
  public void leavePageRule(CssPageRuleNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssPageSelectorNode}'s sub trees */
  public boolean enterPageSelector(CssPageSelectorNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssPageSelectorNode}'s sub trees */
  public void leavePageSelector(CssPageSelectorNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssFontFaceNode}'s sub trees */
  public boolean enterFontFace(CssFontFaceNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssFontFaceNode}'s sub trees */
  public void leaveFontFace(CssFontFaceNode node) {
    leave(node);
  }

  /**
   * @return {@code true} if the contents of the rule should be visited,
   *     false otherwise. {@link #leaveDefinition(CssDefinitionNode)}
   *     will still be called.
   */
  public boolean enterDefinition(CssDefinitionNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssDefinitionNode}'s sub trees */
  public void leaveDefinition(CssDefinitionNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssUnknownAtRuleNode}'s sub trees */
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssUnknownAtRuleNode}'s sub trees */
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    leave(node);
  }

  /** Called between adjacent nodes in a media type list */
  public boolean enterMediaTypeListDelimiter(
      CssNodesListNode<? extends CssNode> node) {
    enter(node);
    return true;
  }

  /** Called between adjacent nodes in a media type list */
  public void leaveMediaTypeListDelimiter(
      CssNodesListNode<? extends CssNode> node) {
    leave(node);
  }

  /** Called before visiting a {@code CssComponentNode}'s sub trees */
  public boolean enterComponent(CssComponentNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssComponentNode}'s sub trees */
  public void leaveComponent(CssComponentNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssKeyframesNode}'s sub trees */
  public boolean enterKeyframesRule(CssKeyframesNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssKeyframesNode}'s sub trees */
  public void leaveKeyframesRule(CssKeyframesNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssMixinDefinitionNode}'s sub trees */
  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssMixinDefinitionNode}'s sub trees */
  public void leaveMixinDefinition(CssMixinDefinitionNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssMixinNode}'s sub trees */
  public boolean enterMixin(CssMixinNode node) {
    enter(node);
    return true;
  }

  /** Called after visiting a {@code CssMixinNode}'s sub trees */
  public void leaveMixin(CssMixinNode node) {
    leave(node);
  }

  /** Called before visiting a {@code CssRootNode}'s sub trees */
  public boolean enterTree(CssRootNode root) {
    enter(root);
    return true;
  }

  /** Called after visiting a {@code CssRootNode}'s sub trees */
  public void leaveTree(CssRootNode root) {
    leave(root);
  }

  /** Called before visiting a {@code CssImportBlockNode}'s sub trees */
  public boolean enterImportBlock(CssImportBlockNode block) {
    enter(block);
    return true;
  }

  /** Called after visiting a {@code CssImportBlockNode}'s sub trees */
  public void leaveImportBlock(CssImportBlockNode block) {
    leave(block);
  }

  /** Called before visiting a {@code CssBlockNode}'s sub trees */
  public boolean enterBlock(CssBlockNode block) {
    enter(block);
    return true;
  }

  /** Called after visiting a {@code CssBlockNode}'s sub trees */
  public void leaveBlock(CssBlockNode block) {
    leave(block);
  }

  /** Called before visiting a {@code CssConditionalBlockNode}'s sub trees */
  public boolean enterConditionalBlock(CssConditionalBlockNode block) {
    enter(block);
    return true;
  }

  /** Called after visiting a {@code CssConditionalBlockNode}'s sub trees */
  public void leaveConditionalBlock(CssConditionalBlockNode block) {
    leave(block);
  }

  /** Called before visiting a {@code CssDeclarationBlockNode}'s sub trees */
  public boolean enterDeclarationBlock(CssDeclarationBlockNode block) {
    enter(block);
    return true;
  }

  /** Called after visiting a {@code CssDeclarationBlockNode}'s sub trees */
  public void leaveDeclarationBlock(CssDeclarationBlockNode block) {
    leave(block);
  }

  /**
   * Called before visiting a {@code CssRulesetNode}'s sub trees.
   *
   * @return whether ruleset children should be visited
   */
  public boolean enterRuleset(CssRulesetNode ruleset) {
    enter(ruleset);
    return true;
  }

  /** Called after visiting a {@code CssRulesetNode}'s sub trees */
  public void leaveRuleset(CssRulesetNode ruleset) {
    leave(ruleset);
  }

  /** Called before visiting a {@code CssSelectorListNode}'s sub trees */
  public boolean enterSelectorBlock(CssSelectorListNode block) {
    enter(block);
    return true;
  }

  /** Called after visiting a {@code CssSelectorListNode}'s sub trees */
  public void leaveSelectorBlock(CssSelectorListNode block) {
    leave(block);
  }

  /** Called before visiting a {@code CssDeclarationNode}'s sub trees */
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    enter(declaration);
    return true;
  }

  /** Called after visiting a {@code CssDeclarationNode}'s sub trees */
  public void leaveDeclaration(CssDeclarationNode declaration) {
    leave(declaration);
  }

  /** Called before visiting a {@code CssSelectorNode}'s sub trees */
  public boolean enterSelector(CssSelectorNode selector) {
    enter(selector);
    return true;
  }

  /** Called after visiting a {@code CssSelectorNode}'s sub trees */
  public void leaveSelector(CssSelectorNode selector) {
    leave(selector);
  }

  /** Called before visiting a {@code CssClassSelectorNode}'s sub trees */
  public boolean enterClassSelector(CssClassSelectorNode classSelector) {
    enter(classSelector);
    return true;
  }

  /** Called after visiting a {@code CssClassSelectorNode}'s sub trees */
  public void leaveClassSelector(CssClassSelectorNode classSelector) {
    leave(classSelector);
  }

  /** Called before visiting a {@code CssIdSelectorNode}'s sub trees */
  public boolean enterIdSelector(CssIdSelectorNode idSelector) {
    enter(idSelector);
    return true;
  }

  /** Called after visiting a {@code CssIdSelectorNode}'s sub trees */
  public void leaveIdSelector(CssIdSelectorNode idSelector) {
    leave(idSelector);
  }

  /** Called before visiting a {@code CssPseudoClassNode}'s sub trees */
  public boolean enterPseudoClass(CssPseudoClassNode pseudoClass) {
    enter(pseudoClass);
    return true;
  }

  /** Called after visiting a {@code CssPseudoClassNode}'s sub trees */
  public void leavePseudoClass(CssPseudoClassNode pseudoClass) {
    leave(pseudoClass);
  }

  /** Called before visiting a {@code CssPseudoElementNode}'s sub trees */
  public boolean enterPseudoElement(CssPseudoElementNode pseudoElement) {
    enter(pseudoElement);
    return true;
  }

  /** Called after visiting a {@code CssPseudoElementNode}'s sub trees */
  public void leavePseudoElement(CssPseudoElementNode pseudoElement) {
    leave(pseudoElement);
  }

  /** Called before visiting a {@code CssAttributeSelectorNode}'s sub trees */
  public boolean enterAttributeSelector(CssAttributeSelectorNode attributeSelector) {
    enter(attributeSelector);
    return true;
  }

  /** Called after visiting a {@code CssAttributeSelectorNode}'s sub trees */
  public void leaveAttributeSelector(CssAttributeSelectorNode attributeSelector) {
    leave(attributeSelector);
  }

  /** Called before visiting a {@code CssPropertyValueNode}'s sub trees */
  public boolean enterPropertyValue(CssPropertyValueNode propertyValue) {
    enter(propertyValue);
    return true;
  }

  /** Called after visiting a {@code CssPropertyValueNode}'s sub trees */
  public void leavePropertyValue(CssPropertyValueNode propertyValue) {
    leave(propertyValue);
  }

  /** Called before visiting a {@code CssValueNode} that is a
      {@code CssCompositeValueNode} */
  public boolean enterCompositeValueNode(CssCompositeValueNode value) {
    enter(value);
    sb.append(value.toString() + " ");
    return true;
  }

  /** Called after visiting a {@code CssValueNode} that is a
      {@code CssCompositeValueNode} */
  public void leaveCompositeValueNode(CssCompositeValueNode value) {
    leave(value);
  }

  /** Called before visiting a {@code CssValueNode} that is not a
      {@code CssCompositeValueNode} */
  public boolean enterSimpleValueNode(CssValueNode value) {
    enter(value);
    sb.append(value.toString() + " ");
    return true;
  }

  /** Called after visiting a {@code CssValueNode} that is not a
      {@code CssCompositeValueNode} */
  public void leaveSimpleValueNode(CssValueNode value) {
    leave(value);
  }

  /** Called between values in a {@code CssCompositeValueNode} */
  public boolean enterCompositeValueNodeOperator(CssCompositeValueNode parent) {
    enter(parent);
    return true;
  }

  /** Called between values in a {@code CssCompositeValueNode} */
  public void leaveCompositeValueNodeOperator(CssCompositeValueNode parent) {
    leave(parent);
  }

  /** Called before visiting a {@code CssFunctionNode}'s sub trees */
  public boolean enterFunctionNode(CssFunctionNode value) {
    enter(value);
    return true;
  }

  /** Called after visiting a {@code CssFunctionNode}'s sub trees. */
  public void leaveFunctionNode(CssFunctionNode value) {
    leave(value);
  }

  /** Called before visiting a {@code CssFunctionNode}'s sub trees */
  public boolean enterArgumentNode(CssValueNode value) {
    enter(value);
    return true;
  }

  /** Called after visiting a {@code CssFunctionNode}'s sub trees. */
  public void leaveArgumentNode(CssValueNode value) {
    leave(value);
  }

  /** Called before visiting a {@code CssCombinatorNode}'s sub trees */
  public boolean enterCombinator(CssCombinatorNode combinator) {
    enter(combinator);
    return true;
  }

  /** Called after visiting a {@code CssCombinatorNode}'s sub trees */
  public void leaveCombinator(CssCombinatorNode combinator) {
    leave(combinator);
  }

  /** Called before visiting a {@code CssKeyNode}'s sub trees */
  public boolean enterKey(CssKeyNode key) {
    enter(key);
    return true;
  }

  /** Called after visiting a {@code CssKeyNode}'s sub trees */
  public void leaveKey(CssKeyNode key) {
    leave(key);
  }

  /** Called before visiting a {@code CssKeyListNode}'s sub trees */
  public boolean enterKeyBlock(CssKeyListNode block) {
    enter(block);
    return true;
  }

  /** Called after visiting a {@code CssKeyListNode}'s sub trees */
  public void leaveKeyBlock(CssKeyListNode block) {
    leave(block);
  }

  /** Called before visiting a {@code CssKeyframeRulesetNode}'s sub trees */
  public boolean enterKeyframeRuleset(CssKeyframeRulesetNode key) {
    enter(key);
    return true;
  }

  /** Called after visiting a {@code CssKeyframeRulesetNode}'s sub trees */
  public void leaveKeyframeRuleset(CssKeyframeRulesetNode key) {
    leave(key);
  }

  public boolean enterValueNode(CssValueNode n) {
    enter(n);
    return true;
  }

  public void leaveValueNode(CssValueNode n) {
    leave(n);
  }

  /** Traverse the (sub) tree starting at {@code node} */
  public void visit(CssNode node) {}

  public static String print(CssTree t) {
    StringBuilder sb = new StringBuilder();
    t.getVisitController().startVisit(new SExprPrinter(sb));
    return sb.toString();
  }
}
