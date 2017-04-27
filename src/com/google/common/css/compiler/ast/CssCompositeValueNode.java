/*
 * Copyright 2009 Google Inc.
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

import com.google.common.base.Joiner;
import com.google.common.css.SourceCodeLocation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A node that contains a list of value nodes that together represent one logical value. For
 * example, font-family alternatives like {@code Arial, Helvetica, Sans-serif} are logically one
 * value in the {@code font} declaration. Likewise the font size specification {@code 12pt/14pt}
 * contains two values (the font size and line height) that are really logically one value.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssCompositeValueNode extends CssValueNode {
  private final List<CssValueNode> values;
  private final Operator operator;
  private final boolean hasParenthesis;

  /** Recognized operators. */
  public enum Operator {
    SPACE(" "),
    COMMA(","),
    SLASH("/"),
    EQUALS("="),
    /**
     * Must be space-separated to distinguish from unary plus. See
     * https://www.w3.org/TR/css3-values/#calc-syntax.
     */
    ADD(" + "),
    /**
     * Must be space-separated to distinguish from unary minus. See
     * https://www.w3.org/TR/css3-values/#calc-syntax.
     */
    SUB(" - "),
    MULT("*"),
    DIV("/"),
    UNKNOWN(null);

    private final String operatorName;

    Operator(String operatorName) {
      this.operatorName = operatorName;
    }

    public String getOperatorName() {
      return operatorName;
    }

    public static Operator valueOf(char c) {
      switch (c) {
        case ',':
          return COMMA;
        case '/':
          return SLASH;
        case '=':
          return EQUALS;
        default:
          return UNKNOWN;
      }
    }

    /**
     * For debugging only.
     * TODO(oana): Get rid of the toString method from all enums.
     */
    @Override
    public String toString() {
      return getOperatorName();
    }
  }


  /**
   * Constructor of a composite value node.
   *
   * @param compositeValues List of composite values
   * @param operator Operator that connects the values
   * @param sourceCodeLocation The location of the code
   */
  CssCompositeValueNode(
      List<CssValueNode> compositeValues,
      Operator operator,
      boolean hasParenthesis,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(null, sourceCodeLocation);
    this.operator = operator;
    this.values = new ArrayList<>(compositeValues);
    becomeParentForNodes(values);
    this.hasParenthesis = hasParenthesis;
  }

  /**
   * Constructor of a composite value node.
   *
   * @param compositeValues List of composite values
   * @param operator Operator that connects the values
   * @param sourceCodeLocation The location of the code
   */
  public CssCompositeValueNode(
      List<CssValueNode> compositeValues,
      Operator operator,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    this(compositeValues, operator, false, sourceCodeLocation);
  }

  /**
   * Copy constructor for a composite value node.
   *
   * @param node The composite node to copy
   */
  public CssCompositeValueNode(CssCompositeValueNode node) {
    this(
        deepCopyCompositeValues(node.getValues()),
        node.getOperator(),
        node.hasParenthesis(),
        node.getSourceCodeLocation());
  }

  private static List<CssValueNode> deepCopyCompositeValues(
      List<CssValueNode> compositeValues) {
    List<CssValueNode> copy = new ArrayList<>(compositeValues.size());
    for (CssValueNode child : compositeValues) {
      copy.add(child.deepCopy());
    }
    return copy;
  }

  @Override
  public CssCompositeValueNode deepCopy() {
    return new CssCompositeValueNode(this);
  }

  public List<CssValueNode> getValues() {
    return values;
  }

  /** Adds a value to the list of values. */
  void addValue(CssValueNode value) {
    values.add(value);
    becomeParentForNode(value);
  }

  public Operator getOperator() {
    return operator;
  }

  public boolean hasParenthesis() {
    return hasParenthesis;
  }

  @Override
  public String toString() {
    String value = Joiner.on(operator.getOperatorName()).join(values);
    return hasParenthesis ? "(" + value + ")" : value;
  }
}
