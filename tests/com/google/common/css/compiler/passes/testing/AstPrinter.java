/*
 * Copyright 2011 Google Inc.
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

package com.google.common.css.compiler.passes.testing;

import com.google.common.css.compiler.ast.CssBooleanExpressionNode;
import com.google.common.css.compiler.ast.CssCommentNode;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssImportRuleNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorListNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.passes.CompactPrinter;

/**
 * A {@link CompactPrinter} extension that adds square brackets to make the
 * construction of the AST more obvious.
 *
 * <p> This printer is useful to ensure that the AST is constructed in the
 * expected way. Thus, it avoids that simply returning the source code is
 * regarded as valid.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class AstPrinter extends CompactPrinter {
  /**
   * Uses the AstPrinter to print the tree.
   */
  public static String print(CssTree tree) {
    AstPrinter astPrinter = new AstPrinter(tree);
    astPrinter.runPass();
    return astPrinter.getCompactPrintedString();
  }

  public AstPrinter(CssTree tree) {
    super(tree);
  }

  @Override
  public boolean enterTree(CssRootNode root) {
    buffer.append('[');
    return super.enterTree(root);
  }

  @Override
  public void leaveTree(CssRootNode root) {
    super.leaveTree(root);
    buffer.append(']');
  }

  @Override
  public boolean enterDeclarationBlock(CssDeclarationBlockNode block) {
    super.enterDeclarationBlock(block);
    buffer.append('[');
    return true;
  }

  @Override
  public void leaveDeclarationBlock(CssDeclarationBlockNode block) {
    buffer.append(']');
    super.leaveDeclarationBlock(block);
  }

  @Override
  public boolean enterSelectorBlock(CssSelectorListNode block) {
    buffer.append('[');
    return super.enterSelectorBlock(block);
  }

  @Override
  public void leaveSelectorBlock(CssSelectorListNode block) {
    super.leaveSelectorBlock(block);
    buffer.append(']');
  }

  @Override
  public boolean enterPropertyValue(CssPropertyValueNode propertyValue) {
    buffer.append('[');
    return super.enterPropertyValue(propertyValue);
  }

  @Override
  public void leavePropertyValue(CssPropertyValueNode propertyValue) {
    super.leavePropertyValue(propertyValue);
    buffer.deleteLastCharIfCharIs(' ');
    buffer.append(']');
  }

  @Override
  public boolean enterCompositeValueNode(CssCompositeValueNode value) {
    buffer.append('[');
    return super.enterCompositeValueNode(value);
  }

  @Override
  public void leaveCompositeValueNode(CssCompositeValueNode value) {
    super.leaveCompositeValueNode(value);
    buffer.deleteLastCharIfCharIs(' ');
    buffer.append(']');
  }

  @Override
  public boolean enterValueNode(CssValueNode value) {
    buffer.append('[');
    return super.enterValueNode(value);
  }

  @Override
  public void leaveValueNode(CssValueNode value) {
    super.leaveValueNode(value);
    buffer.deleteLastCharIfCharIs(' ');
    buffer.append(']');
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    buffer.append("@def ");
    buffer.append(node.getName());
    buffer.append(" [");
    return true;
  }

  @Override
  public void leaveDefinition(CssDefinitionNode node) {
    buffer.deleteLastCharIfCharIs(' ');
    buffer.append("];");
  }

  @Override
  public boolean enterConditionalBlock(CssConditionalBlockNode node) {
    return true;
  }

  @Override
  public boolean enterConditionalRule(CssConditionalRuleNode node) {
    buffer.append(node.getType());
    buffer.append('[');
    for (CssValueNode value : node.getParameters()) {
      appendValue(value);
      buffer.append(' ');
    }
    buffer.deleteLastCharIfCharIs(' ');
    buffer.append(']').append('{');
    return true;
  }

  @Override
  public void leaveConditionalRule(CssConditionalRuleNode node) {
    buffer.append('}');
  }

  @Override
  public boolean enterRuleset(CssRulesetNode node) {
    appendComments(node);
    return super.enterRuleset(node);
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode node) {
    appendComments(node);
    return super.enterDeclaration(node);
  }

  @Override
  public boolean enterMediaRule(CssMediaRuleNode node) {
    appendComments(node);
    return super.enterMediaRule(node);
  }

  @Override
  public boolean enterImportRule(CssImportRuleNode node) {
    appendComments(node);
    return super.enterImportRule(node);
  }

  /**
   * This method appends the representation of a value but does not cover
   * all cases at the moment.
   */
  private void appendValue(CssValueNode node) {
    if (node instanceof CssBooleanExpressionNode) {
      appendBooleanExpression((CssBooleanExpressionNode) node);
    } else {
      buffer.append(node.getValue());
    }
  }

  private void appendBooleanExpression(CssBooleanExpressionNode node) {
    if (!node.getType().isOperator()) {
      buffer.append(node.getValue());
    } else if (node.getType().isBinaryOperator()) {
      appendBooleanChildExpression(node, node.getLeft());
      buffer.append(' ');
      buffer.append(node.getType().getOperatorString());
      buffer.append(' ');
      appendBooleanChildExpression(node, node.getRight());
    } else if (node.getType().isUnaryOperator()) {
      buffer.append(node.getType().getOperatorString());
      appendBooleanChildExpression(node, node.getLeft());
    }
  }

  private void appendBooleanChildExpression(CssBooleanExpressionNode node,
      CssBooleanExpressionNode child) {
    if (child.getType().getPriority() >= node.getType().getPriority()) {
      appendBooleanExpression(child);
    } else {
      buffer.append('(');
      appendBooleanExpression(child);
      buffer.append(')');
    }
  }

  private void appendComments(CssNode node) {
    for (CssCommentNode c : node.getComments()) {
      buffer.append('[');
      buffer.append(c.getValue());
      buffer.append(']');
    }
  }
}
