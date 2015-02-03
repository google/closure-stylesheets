/*
 * Copyright 2008 Google Inc.
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

package com.google.common.css.compiler.ast;

/**
 * Visits everything and does nothing.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class DefaultTreeVisitor implements CssTreeVisitor {

  @Override
  public boolean enterTree(CssRootNode root) {
    return true;
  }

  @Override
  public void leaveTree(CssRootNode root) {}

  @Override
  public boolean enterImportBlock(CssImportBlockNode block) {
    return true;
  }

  @Override
  public void leaveImportBlock(CssImportBlockNode block) {}

  @Override
  public boolean enterBlock(CssBlockNode block) {
    return true;
  }

  @Override
  public void leaveBlock(CssBlockNode block) {}

  @Override
  public boolean enterConditionalBlock(CssConditionalBlockNode block) {
    return true;
  }

  @Override
  public void leaveConditionalBlock(CssConditionalBlockNode block) {}

  @Override
  public boolean enterDeclarationBlock(CssDeclarationBlockNode block) {
    return true;
  }

  @Override
  public void leaveDeclarationBlock(CssDeclarationBlockNode block) {}

  @Override
  public boolean enterRuleset(CssRulesetNode ruleset) {
    return true;
  }

  @Override
  public void leaveRuleset(CssRulesetNode ruleset) {}

  @Override
  public boolean enterSelectorBlock(CssSelectorListNode block) {
    return true;
  }

  @Override
  public void leaveSelectorBlock(CssSelectorListNode block) {}

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    return true;
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode declaration) {}

  @Override
  public boolean enterSelector(CssSelectorNode selector) {
    return true;
  }

  @Override
  public void leaveSelector(CssSelectorNode selector) {}

  @Override
  public boolean enterPropertyValue(CssPropertyValueNode propertyValue) {
    return true;
  }

  @Override
  public void leavePropertyValue(CssPropertyValueNode propertyValue) {}

  @Override
  public boolean enterCompositeValueNode(CssCompositeValueNode value) {
    return true;
  }

  @Override
  public void leaveCompositeValueNode(CssCompositeValueNode value) {}

  @Override
  public boolean enterValueNode(CssValueNode value) {
    return true;
  }

  @Override
  public void leaveValueNode(CssValueNode value) {}

  @Override
  public boolean enterCompositeValueNodeOperator(CssCompositeValueNode parent) {
    return true;
  }

  @Override
  public void leaveCompositeValueNodeOperator(CssCompositeValueNode parent) {}

  @Override
  public boolean enterFunctionNode(CssFunctionNode value) {
    return true;
  }

  @Override
  public void leaveFunctionNode(CssFunctionNode value) {}

  @Override
  public boolean enterArgumentNode(CssValueNode value) {
    return true;
  }

  @Override
  public void leaveArgumentNode(CssValueNode value) {}

  @Override
  public boolean enterCombinator(CssCombinatorNode combinator) {
    return true;
  }

  @Override
  public void leaveCombinator(CssCombinatorNode combinator) {}

  @Override
  public boolean enterConditionalRule(CssConditionalRuleNode node) {
    return true;
  }

  @Override
  public void leaveConditionalRule(CssConditionalRuleNode node) {}

  @Override
  public boolean enterImportRule(CssImportRuleNode node) {
    return true;
  }

  @Override
  public void leaveImportRule(CssImportRuleNode node) {}

  @Override
  public boolean enterMediaRule(CssMediaRuleNode node) {
    return true;
  }

  @Override
  public void leaveMediaRule(CssMediaRuleNode node) {}

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    return true;
  }

  @Override
  public void leaveDefinition(CssDefinitionNode node) {}

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    return true;
  }

  @Override
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {}

  @Override
  public boolean enterMediaTypeListDelimiter(
      CssNodesListNode<? extends CssNode> node) {
    return true;
  }

  @Override
  public void leaveMediaTypeListDelimiter(
      CssNodesListNode<? extends CssNode> node) {}

  @Override
  public boolean enterComponent(CssComponentNode node) {
    return true;
  }

  @Override
  public void leaveComponent(CssComponentNode node) {}

  @Override
  public void visit(CssNode node) {}

  @Override
  public boolean enterKeyframesRule(CssKeyframesNode node) {
    return true;
  }

  @Override
  public void leaveKeyframesRule(CssKeyframesNode node) {}

  @Override
  public boolean enterKey(CssKeyNode node) {
    return true;
  }

  @Override
  public void leaveKey(CssKeyNode node) {}

  @Override
  public boolean enterKeyBlock(CssKeyListNode block) {
    return true;
  }

  @Override
  public void leaveKeyBlock(CssKeyListNode block) {}

  @Override
  public boolean enterKeyframeRuleset(CssKeyframeRulesetNode node) {
    return true;
  }

  @Override
  public void leaveKeyframeRuleset(CssKeyframeRulesetNode node) {}

  @Override
  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    return true;
  }

  @Override
  public void leaveMixinDefinition(CssMixinDefinitionNode node) {}

  @Override
  public boolean enterMixin(CssMixinNode node) {
    return true;
  }

  @Override
  public void leaveMixin(CssMixinNode node) {}

  @Override
  public boolean enterPageRule(CssPageRuleNode node) {
    return true;
  }

  @Override
  public void leavePageRule(CssPageRuleNode node) {}

  @Override
  public boolean enterPageSelector(CssPageSelectorNode node) {
    return true;
  }

  @Override
  public void leavePageSelector(CssPageSelectorNode node) {}

  @Override
  public boolean enterFontFace(CssFontFaceNode node) {
    return true;
  }

  @Override
  public void leaveFontFace(CssFontFaceNode node) {}

  @Override
  public boolean enterAttributeSelector(
      CssAttributeSelectorNode attributeSelector) {
    return true;
  }

  @Override
  public void leaveAttributeSelector(
      CssAttributeSelectorNode attributeSelector) {}

  @Override
  public boolean enterClassSelector(CssClassSelectorNode classSelector) {
    return true;
  }

  @Override
  public void leaveClassSelector(CssClassSelectorNode classSelector) {}

  @Override
  public boolean enterIdSelector(CssIdSelectorNode idSelector) {
    return true;
  }

  @Override
  public void leaveIdSelector(CssIdSelectorNode idSelector) {}

  @Override
  public boolean enterPseudoClass(CssPseudoClassNode pseudoClass) {
    return true;
  }

  @Override
  public void leavePseudoClass(CssPseudoClassNode pseudoClass) {}

  @Override
  public boolean enterPseudoElement(CssPseudoElementNode pseudoElement) {
    return true;
  }

  @Override
  public void leavePseudoElement(CssPseudoElementNode pseudoElement) {}

  @Override
  public boolean enterProvideNode(CssProvideNode node) {
    return true;
  }

  @Override
  public void leaveProvideNode(CssProvideNode node) {}

  @Override
  public boolean enterRequireNode(CssRequireNode node) {
    return true;
  }

  @Override
  public void leaveRequireNode(CssRequireNode node) {}

  @Override
  public boolean enterForLoop(CssForLoopRuleNode node) {
    return true;
  }

  @Override
  public void leaveForLoop(CssForLoopRuleNode node) {}
}
