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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

/**
 * A node representing a conditional rule such as @if or @else.
 *
 */
public class CssConditionalRuleNode extends CssAtRuleNode {
  /**
   * Constructor of a conditional rule.
   *
   * @param type Type of the rule
   * @param name Name of the rule
   */
  public CssConditionalRuleNode(Type type, CssLiteralNode name) {
    super(type, name);
    Preconditions.checkArgument(this.getType().isConditional());
  }

  /**
   * Constructor of a conditional rule.
   *
   * @param type Type of the rule
   * @param name Name of the rule
   * @param condition The condition of the rule
   * @param block A block
   */
  public CssConditionalRuleNode(Type type, CssLiteralNode name,
                                @Nullable CssBooleanExpressionNode condition,
                                CssAbstractBlockNode block) {
    super(type, name, block);
    Preconditions.checkArgument(this.getType().isConditional());
    if (condition != null) {
      setCondition(condition);
    }
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssConditionalRuleNode(CssConditionalRuleNode node) {
    super(node);
    if (node.getCondition() != null) {
      this.setCondition(node.getCondition().deepCopy());
    }
  }

  @Override
  public CssConditionalRuleNode deepCopy() {
    return new CssConditionalRuleNode(this);
  }

  @Override
  void setParent(CssNode parent) {
    Preconditions.checkArgument(parent instanceof CssConditionalBlockNode);
    super.setParent(parent);
  }

  /**
   * Can only be called after the node is completely built (meaning it has
   * the condition set).
   */
  public CssBooleanExpressionNode getCondition() {
    if (getType() == Type.ELSE) {
      Preconditions.checkState(getParametersCount() == 0);
      return null;
    }
    Preconditions.checkState(getParametersCount() == 1);
    return (CssBooleanExpressionNode) this.getParameters().get(0);
  }

  void setCondition(CssBooleanExpressionNode condition) {
    Preconditions.checkState(getType() != Type.ELSE);
    Preconditions.checkState(getParametersCount() <= 1);
    this.setParameters(ImmutableList.<CssValueNode>of(condition));
  }

  @Override
  public CssAbstractBlockNode getBlock() {
    return super.getBlock();
  }

  // TODO(user): Make sure that the parameters list is made up of a single
  //      CssBooleanExpressionNode.
}
