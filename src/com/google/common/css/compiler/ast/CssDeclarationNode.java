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

  /** Constructor of a node representing a CSS declaration. */
  public CssDeclarationNode(CssPropertyNode propertyName) {
    this(propertyName, new CssPropertyValueNode(), null /* sourceCodeLocation */);
  }

  /** Constructor of a node representing a CSS declaration. */
  public CssDeclarationNode(
      CssPropertyNode propertyName, @Nullable SourceCodeLocation sourceCodeLocation) {
    this(propertyName, new CssPropertyValueNode(), sourceCodeLocation);
  }

  /** Constructor of a node representing a CSS declaration. */
  public CssDeclarationNode(CssPropertyNode propertyName, List<CssCommentNode> comments) {
    this(propertyName, comments, null /* sourceCodeLocation */);
  }

  /** Constructor of a node representing a CSS declaration. */
  public CssDeclarationNode(
      CssPropertyNode propertyName,
      List<CssCommentNode> comments,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    this(propertyName, new CssPropertyValueNode(), comments, sourceCodeLocation);
  }

  /** Constructor of a node representing a CSS declaration. */
  public CssDeclarationNode(
      CssPropertyNode propertyName,
      CssPropertyValueNode propertyValue,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    this(propertyName, propertyValue, null, sourceCodeLocation);
  }

  /** Constructor of a node representing a CSS declaration. */
  public CssDeclarationNode(
      CssPropertyNode propertyName,
      CssPropertyValueNode propertyValue,
      @Nullable List<CssCommentNode> comments,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    this(propertyName, propertyValue, comments, sourceCodeLocation, false);
  }

  /** Constructor of a node representing a CSS declaration. */
  public CssDeclarationNode(
      CssPropertyNode propertyName,
      CssPropertyValueNode propertyValue,
      @Nullable List<CssCommentNode> comments,
      @Nullable SourceCodeLocation sourceCodeLocation,
      boolean hasStarHack) {
    super(null, comments, sourceCodeLocation);
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
    becomeParentForNode(this.propertyName);
    becomeParentForNode(this.propertyValue);
    this.setStarHack(hasStarHack);
  }

  /** Copy constructor. */
  public CssDeclarationNode(CssDeclarationNode node) {
    this(
        node.getPropertyName().deepCopy(),
        node.getPropertyValue().deepCopy(),
        node.getComments(),
        node.getSourceCodeLocation(),
        node.hasStarHack());
  }

  public CssDeclarationNode(CssPropertyNode propertyNode,
      CssPropertyValueNode cssPropertyValueNode) {
    this(propertyNode, cssPropertyValueNode, null /* sourceCodeLocation */);
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
