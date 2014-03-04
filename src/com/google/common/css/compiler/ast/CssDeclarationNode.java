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

import java.util.List;

import javax.annotation.Nullable;

/**
 * A node representing a CSS declaration in the abstract syntax tree.
 * For example: <code>background: red</code>
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssDeclarationNode extends CssNode {
  /** The node representing the property. */
  private CssPropertyNode propertyName;
  /** The value given to the property. */
  private CssPropertyValueNode propertyValue;
  /**
   * This will be set to true if this declaration node has a "star-hack"
   * (has a star before the property name).
   */
  private boolean hasStarHack;

  /**
   * Constructor of a node representing a CSS declaration.
   *
   * @param propertyName
   */
  public CssDeclarationNode(CssPropertyNode propertyName) {
    this(propertyName, new CssPropertyValueNode());
  }

  /**
   * Constructor of a node representing a CSS declaration.
   *
   * @param propertyName
   * @param comments
   */
  public CssDeclarationNode(CssPropertyNode propertyName,
                            List<CssCommentNode> comments) {
    this(propertyName, new CssPropertyValueNode(), comments);
  }

  /**
   * Constructor of a node representing a CSS declaration.
   *
   * @param propertyName
   * @param propertyValue
   */
  public CssDeclarationNode(CssPropertyNode propertyName,
                            CssPropertyValueNode propertyValue) {
    this(propertyName, propertyValue, null);
  }

  /**
   * Constructor of a node representing a CSS declaration.
   *
   * @param propertyName
   * @param propertyValue
   * @param comments
   */
  public CssDeclarationNode(CssPropertyNode propertyName,
                            CssPropertyValueNode propertyValue,
                            @Nullable List<CssCommentNode> comments) {
    this(propertyName, propertyValue, comments, false);
  }

  /**
   * Constructor of a node representing a CSS declaration.
   *
   * @param propertyName
   * @param propertyValue
   * @param comments
   * @param hasStarHack
   */
  public CssDeclarationNode(CssPropertyNode propertyName,
                            CssPropertyValueNode propertyValue,
                            @Nullable List<CssCommentNode> comments,
                            boolean hasStarHack) {
    super(null, comments, null);
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
    becomeParentForNode(this.propertyName);
    becomeParentForNode(this.propertyValue);
    this.setStarHack(hasStarHack);
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssDeclarationNode(CssDeclarationNode node) {
    this(
        node.getPropertyName().deepCopy(),
        node.getPropertyValue().deepCopy(),
        node.getComments(),
        node.hasStarHack());
  }

  @Override
  public CssDeclarationNode deepCopy() {
    return new CssDeclarationNode(this);
  }

  public CssPropertyNode getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(CssPropertyNode propertyName) {
    Preconditions.checkNotNull(propertyName);
    removeAsParentOfNode(this.propertyName);
    this.propertyName = propertyName;
    becomeParentForNode(this.propertyName);
  }

  public CssPropertyValueNode getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue(CssPropertyValueNode propertyValue) {
    Preconditions.checkNotNull(propertyValue);
    removeAsParentOfNode(this.propertyValue);
    this.propertyValue = propertyValue;
    becomeParentForNode(this.propertyValue);
  }

  /**
   * Sets the hasStarHack attribute to the given value.
   *
   * @param hasStarHack
   */
  public void setStarHack(boolean hasStarHack) {
    this.hasStarHack = hasStarHack;
  }

  /**
   * Returns the value of this node's {@code hasStarHack} attribute.
   *
   * @return {@code true} if this node's {@code hasStarHack} is set to true.
   */
  public boolean hasStarHack() {
    return hasStarHack;
  }

  /**
   * For debugging only.
   */
  @Override
  public String toString() {
    String output = "";
    if (!getComments().isEmpty()) {
      output = getComments().toString();
    }
    if (hasStarHack()) {
      output = "*";
    }
    output += getPropertyName().getValue() + ":" + getPropertyValue();

    return output;
  }
}
