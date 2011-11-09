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
import com.google.common.collect.Lists;
import com.google.common.css.SourceCodeLocation;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A node that contains a list of value nodes that together represent one logical
 * value. For example, font-family alternatives like {@code Arial, Helvetica,
 * Sans-sherif} are logically one value in the {@code font} declaration. Likewise
 * the font size specification {@code 12pt/14pt} contains two values (the font
 * size and line height) that are really logically one value.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssCompositeValueNode extends CssValueNode {
  private List<CssValueNode> values;
  private Operator operator;

  /**
   * Contains the list of recognized operators.
   */
  public enum Operator {
    SPACE(" "),
    COMMA(","),
    SLASH("/"),
    EQUALS("="),
    UNKNOWN(null);

    private final String operatorName;

    private Operator(String operatorName) {
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
  public CssCompositeValueNode(
      List<CssValueNode> compositeValues,
      Operator operator,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(null, sourceCodeLocation);
    this.operator = operator;
    // TODO(dgajda): Values should be treated as children and have their parent
    //     property set, current implementation is buggy and does not do it.
    this.values = Lists.newArrayList(compositeValues);
  }

  /**
   * Copy constructor for a composite value node.
   *
   * @param node The composite node to copy
   */
  public CssCompositeValueNode(CssCompositeValueNode node) {
    this(node.getValues(), node.getOperator(), node.getSourceCodeLocation());
  }

  @Override
  public CssCompositeValueNode deepCopy() {
    return new CssCompositeValueNode(this);
  }

  public List<CssValueNode> getValues() {
    return values;
  }

  /**
   * Adds a value to the list of values.
   *
   * @param value The value to be added to the list
   */
  public void addValue(CssValueNode value) {
    values.add(value);
  }

  public Operator getOperator() {
    return operator;
  }

  @Override
  public String getValue() {
    return this.toString();
  }

  @Override
  public String toString() {
    return Joiner.on(operator.getOperatorName()).join(values);
  }
}
