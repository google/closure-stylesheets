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
import com.google.common.css.SourceCodeLocation;

import javax.annotation.Nullable;

/**
 * A node representing a boolean expression to evaluate.
 *
 */
// TODO(user): It might be better for each operator to have a list of
//     operands, not just the left and right operand. Thus, A || B || C would be
//     represented as a single operator node and a list of three constant nodes
//     (similar to what the old tree structure does, but with explicit lists).
//     This might make the code larger but has the potential of faster
//     compilation (important for Gmail). This structure might also ease
//     constructing a canonical form of the expression and simplifying
//     expressions (such as EXPR || !EXPR).
public class CssBooleanExpressionNode extends CssValueNode {
  /**
   * Boolean expression node types. The types are given in the order of their
   * precedence, "!" having the highest priority.
   */
  public enum Type {
    CONSTANT(null),
    NOT("!"),
    AND("&&"),
    OR("||");

    public static final String TRUE_CONSTANT = "TRUE";
    public static final String FALSE_CONSTANT = "FALSE";

    private final String operatorString;

    private Type(@Nullable String operatorString) {
      this.operatorString = operatorString;
    }

    public String getOperatorString() {
      return operatorString;
    }

    public boolean isConstant() {
      return this == CONSTANT;
    }

    public boolean isOperator() {
      return !isConstant();
    }

    public boolean isBinaryOperator() {
      Preconditions.checkArgument(this.isOperator());
      return this != NOT;
    }

    public boolean isUnaryOperator() {
      Preconditions.checkArgument(this.isOperator());
      return this == NOT;
    }

    /**
     * Lower numbers are lower priority.
     */
    public int getPriority() {
      return -this.ordinal();
    }
    /**
     * For debugging only.
     */
    @Override
    public String toString() {
      return getOperatorString();
    }
  }

  private final Type type;
  private final CssBooleanExpressionNode left;
  private final CssBooleanExpressionNode right;

  /**
   * Constructor for a boolean expression node.
   *
   * @param type Type of node
   * @param value Value of node
   * @param left Left expression node
   * @param right Right expression node
   * @param sourceCodeLocation The location of the source code
   */
  public CssBooleanExpressionNode(Type type, String value,
      @Nullable CssBooleanExpressionNode left,
      @Nullable CssBooleanExpressionNode right,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(value, sourceCodeLocation);
    this.type = type;
    this.left = left;
    this.right = right;
    becomeParentForNode(this.left);
    becomeParentForNode(this.right);
    Preconditions.checkArgument(isValidExpressionTree());
  }

  /**
   * Constructor for a boolean expression node.
   *
   * @param type Type of node
   * @param value Value of node
   * @param left Left expression node
   * @param sourceCodeLocation The location of the source code
   */
  // TODO(oana): Maybe we want to change the constructor to build the right
  // child instead of the left ona for the unary operators.
  public CssBooleanExpressionNode(Type type, String value,
      @Nullable CssBooleanExpressionNode left,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    this(type, value, left, null, sourceCodeLocation);
  }

  /**
   * Constructor for a boolean expression node.
   *
   * @param type
   * @param value
   * @param sourceCodeLocation
   */
  public CssBooleanExpressionNode(Type type, String value,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    this(type, value, null, null, sourceCodeLocation);
  }

  /**
   * Constructor for a boolean expression node.
   *
   * @param type Type of node
   * @param value Value of node
   */
  public CssBooleanExpressionNode(Type type, String value) {
    this(type, value, null, null, null);
  }

  /**
   * Copy constructor.
   */
  public CssBooleanExpressionNode(CssBooleanExpressionNode node) {
    super(node);
    this.type = node.getType();
    if (node.getLeft() != null) {
      this.left = new CssBooleanExpressionNode(node.getLeft());
      becomeParentForNode(this.left);
    } else {
      this.left = null;
    }
    if (node.getRight() != null) {
      this.right = new CssBooleanExpressionNode(node.getRight());
      becomeParentForNode(this.right);
    } else {
      this.right = null;
    }
  }

  @Override
  public CssBooleanExpressionNode deepCopy() {
    return new CssBooleanExpressionNode(this);
  }

  public Type getType() {
    return type;
  }

  public CssBooleanExpressionNode getLeft() {
    return left;
  }

  public CssBooleanExpressionNode getRight() {
    return right;
  }

  /**
   * Checks if the expression tree is valid.
   */
  public boolean isValidExpressionTree() {
    if (getType().isConstant()) {
      return true;
    } else if (!getType().isOperator()) {
      return getLeft() == null && getRight() == null;
    } else if (getType().isBinaryOperator()) {
      return getLeft() != null && getRight() != null;
    } else if (getType().isUnaryOperator()) {
      return getLeft() != null && getRight() == null;
    } else {
      // assert false
      return false;
    }
  }

  /**
   * For debugging only.
   */
  private void appendChildExpression(StringBuilder sb,
      CssBooleanExpressionNode child) {
    if (child.getType().getPriority() >= getType().getPriority()) {
      sb.append(child.toString());
    } else {
      sb.append("(" + child.toString() + ")");
    }
  }

  /**
   * For debugging only.
   */
  @Override
  public String toString() {
    if (!getType().isOperator()) {
      return getValue();
    } else if (getType().isBinaryOperator()) {
      StringBuilder sb = new StringBuilder();
      appendChildExpression(sb, getLeft());
      sb.append(" " + this.getType().getOperatorString() + " ");
      appendChildExpression(sb, getRight());
      return sb.toString();
    } else if (getType().isUnaryOperator()) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getType().getOperatorString());
      appendChildExpression(sb, getLeft());
      return sb.toString();
    } else {
      // assert false;
      return null;
    }
  }
}
