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

import com.google.common.css.SourceCode;

/**
 * A handler for parse events in the spirit of SAC and SAX.
 *
 */
// TODO(user): Perhaps rename these to *Listener, as *Handler is a bit
//     inappropriate.
public interface CssParserEventHandler {

  void onDocumentStart(SourceCode sourceCode);
  void onDocumentEnd();

  // TODO(user): This should return a SelectorsListBuilder, similar to what
  //     onDeclarationStart does.
  void onRulesetStart(CssSelectorListNode selectorList);
  void onRulesetEnd();

  void onCommentStart(ParserToken comment);
  void onCommentEnd();

  /**
   * Builds an expression.
   */
  public interface ExpressionHandler {
    // TODO(oana): Maybe change the parameters into something like
    //     ParserToken operator and CssValueNode term.
    void onLiteral(ParserToken expression);
    void onOperator(ParserToken expression);
    void onPriority(ParserToken priority);
    void onColor(ParserToken color);
    void onNumericValue(ParserToken numericValue, ParserToken unit);
    void onReference(ParserToken reference);
    void onFunction(ParserToken constant);
    void onFunctionArgument(ParserToken term);
    void onReferenceFunctionArgument(ParserToken term);

  }
  ExpressionHandler onDeclarationStart(ParserToken propertyName, boolean hasStarHack);
  void onDeclarationEnd();
  ExpressionHandler onDefinitionStart(ParserToken definitionName);
  void onDefinitionEnd();

  /**
   * Builds a boolean expression.
   */
  public interface BooleanExpressionHandler {
    void onBooleanExpressionStart();

    Object onConstant(ParserToken constantName);

    Object onUnaryOperator(CssBooleanExpressionNode.Type operator,
        ParserToken operatorToken, Object operand);

    Object onBinaryOperator(CssBooleanExpressionNode.Type operator,
        ParserToken operatorToken, Object leftOperand, Object rightOperand);

    void onBooleanExpressionEnd(Object topOperand);
  }
  BooleanExpressionHandler onConditionalRuleStart(CssAtRuleNode.Type type,
      ParserToken ruleName);
  void onConditionalRuleEnd();

  /**
   * Builds an import.
   */
  public interface ImportHandler {
    void appendImportParameter(ParserToken parameter);
  }
  ImportHandler onImportRuleStart();
  void onImportRuleEnd();

  /**
   * Builds a media rule.
   */
  public interface MediaHandler {
    void appendMediaParameter(ParserToken parameter);
  }
  MediaHandler onMediaRuleStart();
  void onMediaRuleEnd();

  // TODO(user): Add events for unrecognized/unknown at rules.
}
