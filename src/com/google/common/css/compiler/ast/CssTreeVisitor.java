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
 * Visitor interface for CSS abstract syntax trees.
 *
 */
public interface CssTreeVisitor extends AtRuleHandler {

  /** Called before visiting a {@code CssRootNode}'s sub trees */
  boolean enterTree(CssRootNode root);

  /** Called after visiting a {@code CssRootNode}'s sub trees */
  void leaveTree(CssRootNode root);

  /** Called before visiting a {@code CssImportBlockNode}'s sub trees */
  boolean enterImportBlock(CssImportBlockNode block);

  /** Called after visiting a {@code CssImportBlockNode}'s sub trees */
  void leaveImportBlock(CssImportBlockNode block);

  /** Called before visiting a {@code CssBlockNode}'s sub trees */
  boolean enterBlock(CssBlockNode block);

  /** Called after visiting a {@code CssBlockNode}'s sub trees */
  void leaveBlock(CssBlockNode block);

  /** Called before visiting a {@code CssConditionalBlockNode}'s sub trees */
  boolean enterConditionalBlock(CssConditionalBlockNode block);

  /** Called after visiting a {@code CssConditionalBlockNode}'s sub trees */
  void leaveConditionalBlock(CssConditionalBlockNode block);

  /** Called before visiting a {@code CssDeclarationBlockNode}'s sub trees */
  boolean enterDeclarationBlock(CssDeclarationBlockNode block);

  /** Called after visiting a {@code CssDeclarationBlockNode}'s sub trees */
  void leaveDeclarationBlock(CssDeclarationBlockNode block);

  /**
   * Called before visiting a {@code CssRulesetNode}'s sub trees.
   *
   * @return whether ruleset children should be visited
   */
  boolean enterRuleset(CssRulesetNode ruleset);

  /** Called after visiting a {@code CssRulesetNode}'s sub trees */
  void leaveRuleset(CssRulesetNode ruleset);

  /** Called before visiting a {@code CssSelectorListNode}'s sub trees */
  boolean enterSelectorBlock(CssSelectorListNode block);

  /** Called after visiting a {@code CssSelectorListNode}'s sub trees */
  void leaveSelectorBlock(CssSelectorListNode block);

  /** Called before visiting a {@code CssDeclarationNode}'s sub trees */
  boolean enterDeclaration(CssDeclarationNode declaration);

  /** Called after visiting a {@code CssDeclarationNode}'s sub trees */
  void leaveDeclaration(CssDeclarationNode declaration);

  /** Called before visiting a {@code CssSelectorNode}'s sub trees */
  boolean enterSelector(CssSelectorNode selector);

  /** Called after visiting a {@code CssSelectorNode}'s sub trees */
  void leaveSelector(CssSelectorNode selector);

  /** Called before visiting a {@code CssClassSelectorNode}'s sub trees */
  boolean enterClassSelector(CssClassSelectorNode classSelector);

  /** Called after visiting a {@code CssClassSelectorNode}'s sub trees */
  void leaveClassSelector(CssClassSelectorNode classSelector);

  /** Called before visiting a {@code CssIdSelectorNode}'s sub trees */
  boolean enterIdSelector(CssIdSelectorNode idSelector);

  /** Called after visiting a {@code CssIdSelectorNode}'s sub trees */
  void leaveIdSelector(CssIdSelectorNode idSelector);

  /** Called before visiting a {@code CssPseudoClassNode}'s sub trees */
  boolean enterPseudoClass(CssPseudoClassNode pseudoClass);

  /** Called after visiting a {@code CssPseudoClassNode}'s sub trees */
  void leavePseudoClass(CssPseudoClassNode pseudoClass);

  /** Called before visiting a {@code CssPseudoElementNode}'s sub trees */
  boolean enterPseudoElement(CssPseudoElementNode pseudoElement);

  /** Called after visiting a {@code CssPseudoElementNode}'s sub trees */
  void leavePseudoElement(CssPseudoElementNode pseudoElement);

  /** Called before visiting a {@code CssAttributeSelectorNode}'s sub trees */
  boolean enterAttributeSelector(CssAttributeSelectorNode attributeSelector);

  /** Called after visiting a {@code CssAttributeSelectorNode}'s sub trees */
  void leaveAttributeSelector(CssAttributeSelectorNode attributeSelector);

  /** Called before visiting a {@code CssPropertyValueNode}'s sub trees */
  boolean enterPropertyValue(CssPropertyValueNode propertyValue);

  /** Called after visiting a {@code CssPropertyValueNode}'s sub trees */
  void leavePropertyValue(CssPropertyValueNode propertyValue);

  /** Called before visiting a {@code CssValueNode} that is a
      {@code CssCompositeValueNode} */
  boolean enterCompositeValueNode(CssCompositeValueNode value);

  /** Called after visiting a {@code CssValueNode} that is a
      {@code CssCompositeValueNode} */
  void leaveCompositeValueNode(CssCompositeValueNode value);

  /** Called before visiting a {@code CssValueNode} that is not a
      {@code CssCompositeValueNode} */
  boolean enterValueNode(CssValueNode value);

  /** Called after visiting a {@code CssValueNode} that is not a
      {@code CssCompositeValueNode} */
  void leaveValueNode(CssValueNode value);

  /** Called between values in a {@code CssCompositeValueNode} */
  boolean enterCompositeValueNodeOperator(CssCompositeValueNode parent);

  /** Called between values in a {@code CssCompositeValueNode} */
  void leaveCompositeValueNodeOperator(CssCompositeValueNode parent);

  /** Called before visiting a {@code CssFunctionNode}'s sub trees */
  boolean enterFunctionNode(CssFunctionNode value);

  /** Called after visiting a {@code CssFunctionNode}'s sub trees. */
  void leaveFunctionNode(CssFunctionNode value);

  /** Called before visiting a {@code CssFunctionNode}'s sub trees */
  boolean enterArgumentNode(CssValueNode value);

  /** Called after visiting a {@code CssFunctionNode}'s sub trees. */
  void leaveArgumentNode(CssValueNode value);

  /** Called before visiting a {@code CssCombinatorNode}'s sub trees */
  boolean enterCombinator(CssCombinatorNode combinator);

  /** Called after visiting a {@code CssCombinatorNode}'s sub trees */
  void leaveCombinator(CssCombinatorNode combinator);

  /** Called before visiting a {@code CssKeyNode}'s sub trees */
  boolean enterKey(CssKeyNode key);

  /** Called after visiting a {@code CssKeyNode}'s sub trees */
  void leaveKey(CssKeyNode key);

  /** Called before visiting a {@code CssKeyListNode}'s sub trees */
  boolean enterKeyBlock(CssKeyListNode block);

  /** Called after visiting a {@code CssKeyListNode}'s sub trees */
  void leaveKeyBlock(CssKeyListNode block);

  /** Called before visiting a {@code CssKeyframeRulesetNode}'s sub trees */
  boolean enterKeyframeRuleset(CssKeyframeRulesetNode key);

  /** Called after visiting a {@code CssKeyframeRulesetNode}'s sub trees */
  void leaveKeyframeRuleset(CssKeyframeRulesetNode key);

  /** Called before visiting a {@code CssForLoopRuleNode}'s sub trees */
  boolean enterForLoop(CssForLoopRuleNode node);

  /** Called after visiting a {@code CssForLoopRuleNode}'s sub trees */
  void leaveForLoop(CssForLoopRuleNode node);
}
