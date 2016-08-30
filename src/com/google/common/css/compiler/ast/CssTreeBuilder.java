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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode.Type;
import com.google.common.css.compiler.ast.CssCompositeValueNode.Operator;
import com.google.common.css.compiler.ast.CssFunctionNode.Function;

import java.util.Arrays;
import java.util.List;

/**
 * Use this to build one {@link CssTree} object.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssTreeBuilder implements
    CssParserEventHandler,
    CssParserEventHandler.ImportHandler,
    CssParserEventHandler.MediaHandler,
    CssParserEventHandler.ExpressionHandler,
    CssParserEventHandler.BooleanExpressionHandler {

  enum State {
    BEFORE_DOCUMENT_START,
    BEFORE_MAIN_BODY,
    INSIDE_IMPORT_RULE,
    INSIDE_MAIN_BODY,
    INSIDE_MEDIA_RULE,
    INSIDE_BLOCK,
    INSIDE_DECLARATION_BLOCK,
    INSIDE_PROPERTY_EXPRESSION,
    INSIDE_EXPRESSION_AFTER_OPERATOR,
    INSIDE_CONDITIONAL_BLOCK,
    BEFORE_BOOLEAN_EXPRESSION,
    INSIDE_BOOLEAN_EXPRESSION,
    INSIDE_DEFINITION,
    INSIDE_COMMENT,
    DONE_BUILDING;
  }

  private CssTree tree = null;
  private boolean treeIsConstructed = false;

  // TODO(user): Use Collections.asLifoQueue(new ArrayDeque()) for openBlocks
  private List<CssAbstractBlockNode> openBlocks = null;
  private List<CssConditionalBlockNode> openConditionalBlocks = null;
  private CssDeclarationBlockNode declarationBlock = null;
  private CssDeclarationNode declaration = null;
  private CssDefinitionNode definition = null;
  private List<CssCommentNode> comments = null;
  private CssRulesetNode ruleset = null;
  private CssImportRuleNode importRule = null;
  private CssMediaRuleNode mediaRule = null;
  private StateStack stateStack = new StateStack(State.BEFORE_DOCUMENT_START);

  public CssTreeBuilder() {
  }

  //TODO(oana): Maybe add a generic utility class for Stack than can be used in
  // DefaultVisitController too.
  @VisibleForTesting
  static class StateStack {
    private List<State> stack;

    StateStack(State initialState) {
      stack = Lists.newArrayList(initialState);
    }

    void push(State state) {
      stack.add(state);
    }

    void pop() {
      stack.remove(stack.size() - 1);
    }

    void transitionTo(State newState) {
      pop();
      push(newState);
    }

    boolean isIn(State... states) {
      return Arrays.asList(states).contains(
          stack.get(stack.size() - 1));
    }

    int size() {
      return stack.size();
    }
  }

  private void startMainBody() {
    if (stateStack.isIn(State.BEFORE_MAIN_BODY)) {
      stateStack.transitionTo(State.INSIDE_MAIN_BODY);
    }
  }

  private CssAbstractBlockNode getEnclosingBlock() {
    return openBlocks.get(openBlocks.size() - 1);
  }

  private void pushEnclosingBlock(CssAbstractBlockNode block) {
    openBlocks.add(block);
  }

  private void popEnclosingBlock() {
    openBlocks.remove(openBlocks.size() - 1);
  }

  private void endConditionalRuleChain() {
    if (!stateStack.isIn(State.INSIDE_CONDITIONAL_BLOCK)) {
      return;
    }
    Preconditions.checkState(!openConditionalBlocks.isEmpty());

    CssConditionalBlockNode conditionalBlock = openConditionalBlocks.remove(
        openConditionalBlocks.size() - 1);

    stateStack.pop();

    Preconditions.checkState(stateStack.isIn(
        State.INSIDE_BLOCK,
        State.INSIDE_MAIN_BODY,
        State.INSIDE_MEDIA_RULE,
        State.INSIDE_CONDITIONAL_BLOCK));

    getEnclosingBlock().addChildToBack(conditionalBlock);
  }

  private CssConditionalBlockNode getEnclosingConditonalBlock() {
    return openConditionalBlocks.get(openConditionalBlocks.size() - 1);
  }

  private void appendToCurrentExpression(CssValueNode node) {
    if (stateStack.isIn(State.INSIDE_DEFINITION)) {
      Preconditions.checkState(definition != null);

      definition.addChildToBack(node);
    } else if (stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION)) {
      Preconditions.checkState(declaration != null);

      declaration.getPropertyValue().addChildToBack(node);
    } else if (stateStack.isIn(State.INSIDE_EXPRESSION_AFTER_OPERATOR)) {
      stateStack.pop();

      if (stateStack.isIn(State.INSIDE_DEFINITION)) {
        Preconditions.checkState(definition != null);

        CssCompositeValueNode compositeNode =
            (CssCompositeValueNode) definition.getLastChild();
        compositeNode.addValue(node);
      } else if (stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION)) {
        Preconditions.checkState(declaration != null);

        CssCompositeValueNode compositeNode =
            (CssCompositeValueNode) declaration.getPropertyValue()
                .getLastChild();
        compositeNode.addValue(node);
      }
    }
  }

  private CssFunctionNode getFunctionFromCurrentExpression() {
    if (stateStack.isIn(State.INSIDE_DEFINITION)) {
      Preconditions.checkState(definition != null);
      return (CssFunctionNode) definition.getLastChild();
    } else if (stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION)) {
      Preconditions.checkState(declaration != null);
      int size = declaration.getPropertyValue().numChildren();
      return ((CssFunctionNode) declaration.getPropertyValue()
          .getChildAt(size - 1));
    } else {
      return null;
    }
  }

  @Override
  public void onDocumentStart(SourceCode sourceCode) {
    Preconditions.checkState(stateStack.isIn(State.BEFORE_DOCUMENT_START));

    Preconditions.checkNotNull(sourceCode);
    Preconditions.checkState(tree == null);
    tree = new CssTree(sourceCode);
    Preconditions.checkState(openBlocks == null);
    openBlocks = Lists.newArrayList();
    openBlocks.add(tree.getRoot().getBody());
    Preconditions.checkState(openConditionalBlocks == null);
    openConditionalBlocks = Lists.newArrayList();
    Preconditions.checkState(comments == null);
    comments = Lists.newArrayList();

    stateStack.transitionTo(State.BEFORE_MAIN_BODY);
  }

  @Override
  public void onDocumentEnd() {
    startMainBody();
    endConditionalRuleChain();
    Preconditions.checkState(stateStack.isIn(State.INSIDE_MAIN_BODY));

    Preconditions.checkState(openBlocks.size() == 1);
    Preconditions.checkState(openBlocks.get(0) == tree.getRoot().getBody());
    openBlocks = null;
    Preconditions.checkState(openConditionalBlocks.size() == 0);
    openConditionalBlocks = null;

    treeIsConstructed = true;

    stateStack.transitionTo(State.DONE_BUILDING);
    Preconditions.checkState(stateStack.size() == 1);
  }

  @VisibleForTesting
  public CssTree getTree() {
    Preconditions.checkState(stateStack.isIn(State.DONE_BUILDING));

    Preconditions.checkState(treeIsConstructed);
    return tree;
  }

  @Override
  public ImportHandler onImportRuleStart() {
    Preconditions.checkState(stateStack.isIn(State.BEFORE_MAIN_BODY));
    Preconditions.checkState(importRule == null);

    stateStack.push(State.INSIDE_IMPORT_RULE);

    importRule = new CssImportRuleNode(comments);
    comments.clear();

    return this;
  }

  @Override
  public void appendImportParameter(ParserToken parameter) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_IMPORT_RULE));

    CssValueNode importParameter = new CssLiteralNode(
        parameter.getToken(), parameter.getSourceCodeLocation());
    importRule.addChildToBack(importParameter);
  }

  @Override
  public void onImportRuleEnd() {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_IMPORT_RULE));
    stateStack.pop();

    tree.getRoot().getImportRules().addChildToBack(importRule);

    Preconditions.checkState(importRule != null);
    importRule = null;
  }

  @Override
  public MediaHandler onMediaRuleStart() {
    startMainBody();
    endConditionalRuleChain();

    Preconditions.checkState(stateStack.isIn(State.INSIDE_MAIN_BODY));
    Preconditions.checkState(mediaRule == null);

    stateStack.push(State.INSIDE_MEDIA_RULE);

    mediaRule = new CssMediaRuleNode(comments);
    comments.clear();

    pushEnclosingBlock(mediaRule.getBlock());

    return this;
  }

  @Override
  public void appendMediaParameter(ParserToken parameter) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_MEDIA_RULE));

    CssValueNode mediaParameter = new CssLiteralNode(
        parameter.getToken(), parameter.getSourceCodeLocation());
    mediaRule.addChildToBack(mediaParameter);
  }

  @Override
  public void onMediaRuleEnd() {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_MEDIA_RULE));
    stateStack.pop();

    mediaRule.setBlock(getEnclosingBlock());
    popEnclosingBlock();
    getEnclosingBlock().addChildToBack(mediaRule);

    Preconditions.checkState(mediaRule != null);
    mediaRule = null;
  }

  @Override
  public ExpressionHandler onDefinitionStart(ParserToken definitionName) {
    startMainBody();
    endConditionalRuleChain();
    Preconditions.checkState(
        stateStack.isIn(
            State.INSIDE_MAIN_BODY,
            State.INSIDE_MEDIA_RULE,
            State.INSIDE_BLOCK));
    Preconditions.checkState(definition == null);

    stateStack.push(State.INSIDE_DEFINITION);

    CssLiteralNode name = new CssLiteralNode(
        definitionName.getToken(), definitionName.getSourceCodeLocation());
    definition = new CssDefinitionNode(name, comments);
    comments.clear();

    return this;
  }

  @Override
  public void onDefinitionEnd() {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_DEFINITION));
    stateStack.pop();

    Preconditions.checkState(definition != null);
    getEnclosingBlock().addChildToBack(definition);
    definition = null;
  }

  @Override
  public void onCommentStart(ParserToken commentToken) {
    // Comments can be anywhere in the file, so there is not requirement for the
    // state.
    stateStack.push(State.INSIDE_COMMENT);

    comments.add(new CssCommentNode(commentToken.getToken(),
        commentToken.getSourceCodeLocation()));
  }

  @Override
  public void onCommentEnd() {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_COMMENT));
    stateStack.pop();
  }

  @Override
  public void onRulesetStart(CssSelectorListNode selectorList) {
    startMainBody();
    endConditionalRuleChain();
    Preconditions.checkState(
        stateStack.isIn(State.INSIDE_MAIN_BODY, State.INSIDE_BLOCK,
                  State.INSIDE_MEDIA_RULE));

    Preconditions.checkState(ruleset == null);
    ruleset = new CssRulesetNode(comments);
    ruleset.setSourceCodeLocation(selectorList.getSourceCodeLocation());
    ruleset.setSelectors(selectorList);
    comments.clear();

    Preconditions.checkState(declarationBlock == null);
    declarationBlock = ruleset.getDeclarations();

    stateStack.push(State.INSIDE_DECLARATION_BLOCK);
  }

  @Override
  public void onRulesetEnd() {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_DECLARATION_BLOCK));

    Preconditions.checkState(declarationBlock != null);
    Preconditions.checkState(ruleset != null);
    Preconditions.checkState(declarationBlock == ruleset.getDeclarations());

    declarationBlock = null;

    stateStack.pop();

    Preconditions.checkState(stateStack.isIn(
        State.INSIDE_DECLARATION_BLOCK,
        State.INSIDE_MAIN_BODY,
        State.INSIDE_BLOCK,
        State.INSIDE_MEDIA_RULE));

    getEnclosingBlock().addChildToBack(ruleset);
    ruleset = null;
  }

  @Override
  public ExpressionHandler onDeclarationStart(ParserToken propertyName, boolean hasStarHack) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_DECLARATION_BLOCK));

    CssPropertyNode name = new CssPropertyNode(
        propertyName.getToken(),
        propertyName.getSourceCodeLocation());
    Preconditions.checkState(declaration == null);
    declaration = new CssDeclarationNode(name, comments);
    declaration.setStarHack(hasStarHack);
    comments.clear();

    stateStack.push(State.INSIDE_PROPERTY_EXPRESSION);
    return this;
  }

  @Override
  public void onDeclarationEnd() {
    stateStack.pop();

    Preconditions.checkState(stateStack.isIn(State.INSIDE_DECLARATION_BLOCK));

    Preconditions.checkState(declaration != null);
    declarationBlock.addChildToBack(declaration);
    declaration = null;
  }

  @Override
  public void onLiteral(ParserToken expressionToken) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION,
        State.INSIDE_DEFINITION, State.INSIDE_EXPRESSION_AFTER_OPERATOR));

    CssLiteralNode expression = new CssLiteralNode(
        expressionToken.getToken(), expressionToken.getSourceCodeLocation());

    appendToCurrentExpression(expression);
  }

  @Override
  public void onOperator(ParserToken expressionToken) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION,
        State.INSIDE_DEFINITION, State.INSIDE_EXPRESSION_AFTER_OPERATOR));

    // Make sure that the string we are passing as operator actually has one char
    Preconditions.checkArgument(expressionToken.getToken().length() == 1);

    // We are going to change the state unless it's a space operator
    if (!" ".equals(expressionToken.getToken())) {
      // We may need to construct the corresponding composite node if the last
      // one in the list is not a composite node or if it is not based on the
      // same operator
      CssValueNode lastChild = null;
      if (stateStack.isIn(State.INSIDE_DEFINITION)) {
        Preconditions.checkState(definition != null);
        lastChild = definition.removeLastChild();
      } else if (stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION)) {
        Preconditions.checkState(declaration != null);
        lastChild = declaration.getPropertyValue().removeLastChild();
      }

      if (!(lastChild instanceof CssCompositeValueNode)
          || ((CssCompositeValueNode) lastChild).getOperator().toString().equals(
              expressionToken.getToken())) {
        CssCompositeValueNode node = new CssCompositeValueNode(
            Lists.newArrayList(lastChild),
            Operator.valueOf(expressionToken.getToken().charAt(0)),
            null);
        appendToCurrentExpression(node);
      } else if (lastChild != null) {
        appendToCurrentExpression(lastChild);
      }

      stateStack.push(State.INSIDE_EXPRESSION_AFTER_OPERATOR);
    }
  }

  @Override
  public void onPriority(ParserToken priority) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION,
        State.INSIDE_DEFINITION, State.INSIDE_EXPRESSION_AFTER_OPERATOR));

    CssPriorityNode expressionPriority = new CssPriorityNode(
        CssPriorityNode.PriorityType.IMPORTANT,
        priority.getSourceCodeLocation());

    appendToCurrentExpression(expressionPriority);
  }

  @Override
  public void onColor(ParserToken color) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION,
        State.INSIDE_DEFINITION, State.INSIDE_EXPRESSION_AFTER_OPERATOR));

    CssHexColorNode expression = new CssHexColorNode(
        color.getToken(), color.getSourceCodeLocation());

    appendToCurrentExpression(expression);
  }

  @Override
  public void onNumericValue(ParserToken numericValue, ParserToken unit) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION,
        State.INSIDE_DEFINITION, State.INSIDE_EXPRESSION_AFTER_OPERATOR));

    CssValueNode expression = new CssNumericNode(
        numericValue.getToken(),
        unit != null ? unit.getToken() : CssNumericNode.NO_UNITS,
        numericValue.getSourceCodeLocation());

    appendToCurrentExpression(expression);
  }

  @Override
  public void onReference(ParserToken reference) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION,
        State.INSIDE_DEFINITION, State.INSIDE_EXPRESSION_AFTER_OPERATOR));

    CssConstantReferenceNode expression = new CssConstantReferenceNode(
        reference.getToken(), reference.getSourceCodeLocation());

    appendToCurrentExpression(expression);
  }

  @Override
  public void onFunction(ParserToken constant) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION,
        State.INSIDE_DEFINITION, State.INSIDE_EXPRESSION_AFTER_OPERATOR));

    Function f = Function.byName(constant.getToken());
    CssFunctionNode expression;
    if (f != null) {
      expression = new CssFunctionNode(f, constant.getSourceCodeLocation());
    } else {
      expression = new CssCustomFunctionNode(
          constant.getToken() /* gssFunctionName */,
          constant.getSourceCodeLocation());
    }
    appendToCurrentExpression(expression);
  }

  @Override
  public void onFunctionArgument(ParserToken term) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION,
        State.INSIDE_DEFINITION, State.INSIDE_EXPRESSION_AFTER_OPERATOR));

    CssValueNode expression = new CssLiteralNode(
        term.getToken(), term.getSourceCodeLocation());

    getFunctionFromCurrentExpression().getArguments()
        .addChildToBack(expression);
  }


  @Override
  public void onReferenceFunctionArgument(ParserToken term) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_PROPERTY_EXPRESSION,
        State.INSIDE_DEFINITION, State.INSIDE_EXPRESSION_AFTER_OPERATOR));

    CssConstantReferenceNode expression = new CssConstantReferenceNode(
        term.getToken(), term.getSourceCodeLocation());

    getFunctionFromCurrentExpression().getArguments()
        .addChildToBack(expression);
  }

  @Override
  public BooleanExpressionHandler onConditionalRuleStart(
      CssAtRuleNode.Type type, ParserToken ruleName) {
    startMainBody();
    if (type == CssAtRuleNode.Type.IF) {
      endConditionalRuleChain();
      Preconditions.checkState(stateStack.isIn(
          State.INSIDE_MEDIA_RULE,
          State.INSIDE_MAIN_BODY,
          State.INSIDE_BLOCK));
    } else {
      Preconditions.checkState(stateStack.isIn(
          State.INSIDE_MEDIA_RULE,
          State.INSIDE_MAIN_BODY,
          State.INSIDE_BLOCK,
          State.INSIDE_CONDITIONAL_BLOCK));
    }

    if (stateStack.isIn(State.INSIDE_CONDITIONAL_BLOCK)) {
      Preconditions.checkState(!openConditionalBlocks.isEmpty());
    } else {
      CssConditionalBlockNode conditionalBlock =
          new CssConditionalBlockNode(comments);
      comments.clear();
      openConditionalBlocks.add(conditionalBlock);
      stateStack.push(State.INSIDE_CONDITIONAL_BLOCK);
    }

    CssLiteralNode name = new CssLiteralNode(
        ruleName.getToken(), ruleName.getSourceCodeLocation());
    CssConditionalRuleNode conditionalRule =
        new CssConditionalRuleNode(type, name);
    pushEnclosingBlock(conditionalRule.getBlock());

    if (type != CssAtRuleNode.Type.ELSE) {
      stateStack.push(State.BEFORE_BOOLEAN_EXPRESSION);
      return this;
    } else {
      stateStack.push(State.INSIDE_BLOCK);
      return null;
    }
  }

  @Override
  public void onConditionalRuleEnd() {
    endConditionalRuleChain();
    Preconditions.checkState(stateStack.isIn(State.INSIDE_BLOCK));

    CssConditionalRuleNode conditionalRule =
        (CssConditionalRuleNode) getEnclosingBlock().getParent();
    getEnclosingConditonalBlock().addChildToBack(conditionalRule);
    CssAtRuleNode.Type type = conditionalRule.getType();
    popEnclosingBlock();

    stateStack.pop();

    if (type == CssAtRuleNode.Type.ELSE) {
      endConditionalRuleChain();
    }
    Preconditions.checkState(stateStack.isIn(
        State.INSIDE_BLOCK,
        State.INSIDE_MAIN_BODY,
        State.INSIDE_MEDIA_RULE,
        State.INSIDE_CONDITIONAL_BLOCK));
  }

  @Override
  public void onBooleanExpressionStart() {
    Preconditions.checkState(stateStack.isIn(State.BEFORE_BOOLEAN_EXPRESSION));
    stateStack.transitionTo(State.INSIDE_BOOLEAN_EXPRESSION);
  }

  @Override
  public Object onConstant(ParserToken constantName) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_BOOLEAN_EXPRESSION));

    return new CssBooleanExpressionNode(CssBooleanExpressionNode.Type.CONSTANT,
        constantName.getToken(), constantName.getSourceCodeLocation());
  }

  @Override
  public Object onUnaryOperator(Type operator, ParserToken operatorToken,
      Object operand) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_BOOLEAN_EXPRESSION));

    return new CssBooleanExpressionNode(operator, operatorToken.getToken(),
        (CssBooleanExpressionNode) operand,
        operatorToken.getSourceCodeLocation());
  }

  @Override
  public Object onBinaryOperator(Type operator, ParserToken operatorToken,
      Object leftOperand, Object rightOperand) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_BOOLEAN_EXPRESSION));

    return new CssBooleanExpressionNode(operator, operatorToken.getToken(),
        (CssBooleanExpressionNode) leftOperand,
        (CssBooleanExpressionNode) rightOperand,
        operatorToken.getSourceCodeLocation());
  }

  @Override
  public void onBooleanExpressionEnd(Object topOperand) {
    Preconditions.checkState(stateStack.isIn(State.INSIDE_BOOLEAN_EXPRESSION));

    CssConditionalRuleNode conditionalRule =
      (CssConditionalRuleNode) getEnclosingBlock().getParent();
    conditionalRule.setCondition((CssBooleanExpressionNode) topOperand);

    stateStack.transitionTo(State.INSIDE_BLOCK);
  }
}
