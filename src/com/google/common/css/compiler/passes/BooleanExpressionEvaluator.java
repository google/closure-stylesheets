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

package com.google.common.css.compiler.passes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode.Type;

import java.util.Set;

/**
 * An evaluator for boolean expressions. The evaluation returns a new boolean
 * expression node corresponding to true, false, or to the condition if that was
 * not recognized.
 *
 */
public class BooleanExpressionEvaluator {
  final CssBooleanExpressionNode expression;
  final Set<String> trueConditions;
  final Set<String> falseConditions;
  final boolean theRestAreUnknown;

  /**
   * Evaluates a boolean expression given a set of true conditions. Conditions
   * that are not in the set are assumed to be false.
   */
  public BooleanExpressionEvaluator(CssBooleanExpressionNode expression,
      Set<String> trueConditions) {
    this(expression, trueConditions, ImmutableSet.<String>of(),
        false /* the rest of the conditions are assumed known */);
    Preconditions.checkArgument(!trueConditions.contains(null));
  }

  /**
   * Evaluates a boolean expression given a set of true conditions and a set of
   * false conditions. Conditions that are not in any of the sets are assumed to
   * be unknown and are left in the resulting boolean expression.
   */
  public BooleanExpressionEvaluator(CssBooleanExpressionNode expression,
      Set<String> trueConditions, Set<String> falseConditions) {
    this(expression, trueConditions, falseConditions,
        true /* the rest of the conditions are assumed not known */);
    Preconditions.checkArgument(!trueConditions.contains(null));
    Preconditions.checkArgument(!falseConditions.contains(null));
  }

  private BooleanExpressionEvaluator(CssBooleanExpressionNode expression,
      Set<String> trueConditions, Set<String> falseConditions,
      boolean theRestAreUnknown) {
    Preconditions.checkArgument(falseConditions.isEmpty() ||
        !theRestAreUnknown);
    Preconditions.checkArgument(
        !trueConditions.contains(Type.TRUE_CONSTANT) &&
        !trueConditions.contains(Type.FALSE_CONSTANT) &&
        !falseConditions.contains(Type.TRUE_CONSTANT) &&
        !falseConditions.contains(Type.FALSE_CONSTANT));
    this.expression = expression;
    this.trueConditions =
      Sets.union(trueConditions, ImmutableSet.of(Type.TRUE_CONSTANT));
    this.falseConditions =
      Sets.union(falseConditions, ImmutableSet.of(Type.FALSE_CONSTANT));
    this.theRestAreUnknown = theRestAreUnknown;
  }

  /**
   * Evaluates the boolean expression associated with this evaluator.
   */
  public CssBooleanExpressionNode evaluate() {
    Object result = evaluateTree(expression);
    if (result instanceof Boolean) {
      return new CssBooleanExpressionNode(Type.CONSTANT,
          (Boolean) result ? Type.TRUE_CONSTANT : Type.FALSE_CONSTANT);
    }
    return (CssBooleanExpressionNode) result;
  }

  /**
   * Evaluates the tree corresponding to a boolean expression node.
   */
  private Object evaluateTree(CssBooleanExpressionNode node) {

    if (node.getType().isConstant()) {
      String constantName = node.getValue();
      if (trueConditions.contains(constantName)) {
        return Boolean.TRUE;
      }
      if (falseConditions.contains(constantName)) {
        return Boolean.FALSE;
      }
      if (theRestAreUnknown) {
        // Copy of this constant node.
        return new CssBooleanExpressionNode(node.getType(), node.getValue(),
            node.getSourceCodeLocation());
      } else {
        return Boolean.FALSE;
      }
    }

    // If we are here it means that the expression has operators.
    if (node.getType().isUnaryOperator()) {
      return evaluateTreeWithUnaryOperator(node);
    } else {
      // assert node.getType().isBinaryOperator();
      return evaluateTreeWithBinaryOperator(node);
    }
  }

  /**
   * Evaluates the tree corresponding to a boolean expression node with an unary
   * operator.
   */
  private Object evaluateTreeWithUnaryOperator(CssBooleanExpressionNode node) {
    // For a unary operator we only need to evaluate the left operand.
    // assert node.getType() == Type.NOT;
    Object operand = evaluateTree(node.getLeft());
    if (operand instanceof Boolean) {
      Boolean boolResult = (Boolean) operand;
      return !boolResult;
    }
    // Return a tree for "!operand".
    CssBooleanExpressionNode operandResult =
      (CssBooleanExpressionNode) operand;
    return new CssBooleanExpressionNode(node.getType(), node.getValue(),
        operandResult, node.getSourceCodeLocation());
  }

  /**
   * Evaluates the tree corresponding to a boolean expression node with a binary
   * operator.
   */
   private Object evaluateTreeWithBinaryOperator(
       CssBooleanExpressionNode node) {
    // For a binary operator we need to evaluate both left and right operands.
    Object leftOperand = evaluateTree(node.getLeft());
    if (leftOperand instanceof Boolean) {
      Boolean leftBoolResult = (Boolean) leftOperand;
      if (leftBoolResult == true && node.getType() == Type.OR) {
        return Boolean.TRUE;
      }
      if (leftBoolResult == false && node.getType() == Type.AND) {
        return Boolean.FALSE;
      }
    }

    Object rightOperand = evaluateTree(node.getRight());
    if (rightOperand instanceof Boolean) {
        Boolean rightBoolResult = (Boolean) rightOperand;
        if (leftOperand instanceof Boolean) {
          if (node.getType() == Type.AND) {
            return (Boolean) leftOperand && (Boolean) rightOperand;
          } else {
            // assert node.getType() == Type.OR;
            return (Boolean) leftOperand || (Boolean) rightOperand;
          }
        } else {
          if (rightBoolResult == true && node.getType() == Type.OR) {
            return Boolean.TRUE;
          }
          if (rightBoolResult == false && node.getType() == Type.AND) {
            return Boolean.FALSE;
          }
          // We either have (left && true) or (left || false).
          return leftOperand;
        }
    } else {
      if (leftOperand instanceof Boolean) {
        // In this case the result is dictated by the right operand, as we can
        // only have (TRUE && right) or (FALSE || right).
        // assert leftOperand.equals(Boolean.TRUE) && node.getType() == Type.AND ||
        //       leftOperand.equals(Boolean.FALSE) && node.getType() == Type.OR;
        return rightOperand;
      } else {
        // Return a tree for "leftOperand operator rightOperand".
        CssBooleanExpressionNode leftResult =
          (CssBooleanExpressionNode) leftOperand;
        CssBooleanExpressionNode rightResult =
          (CssBooleanExpressionNode) rightOperand;
        return new CssBooleanExpressionNode(node.getType(), node.getValue(),
            leftResult, rightResult, node.getSourceCodeLocation());
      }
    }
  }
}
