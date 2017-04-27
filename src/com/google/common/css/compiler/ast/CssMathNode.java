/*
 * Copyright 2014 Google Inc.
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
import java.util.List;

/**
 * A CSS node that holds a mathematical expression.
 *
 * <p>See http://www.w3.org/TR/css3-values/#calc
 */
public final class CssMathNode extends CssCompositeValueNode {

  private CssMathNode(
      CssValueNode operand1,
      CssCompositeValueNode.Operator operator,
      CssValueNode operand2,
      boolean hasParenthesis) {
    super(ImmutableList.of(operand1, operand2), operator, hasParenthesis, null);
  }

  public static CssValueNode createFromOperandsAndOperators(
      List<CssValueNode> operands,
      List<CssCompositeValueNode.Operator> operators,
      boolean hasParenthesis) {
    Preconditions.checkArgument(
        operands.size() == operators.size() + 1,
        "There should be one more operands than operators");
    if (operators.size() == 0) {
      return operands.get(0);
    }
    if (operators.size() == 1) {
      return new CssMathNode(operands.get(0), operators.get(0), operands.get(1), hasParenthesis);
    } else {
      return new CssMathNode(
          operands.get(0),
          operators.get(0),
          createFromOperandsAndOperators(
              operands.subList(1, operands.size()), operators.subList(1, operators.size()), false),
          hasParenthesis);
    }
  }

  @Override
  public CssMathNode deepCopy() {
    CssValueNode operand1 = getValues().get(0);
    CssValueNode operand2 = getValues().get(1);
    return new CssMathNode(operand1, getOperator(), operand2, hasParenthesis());
  }
}
