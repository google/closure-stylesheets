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
 * A CSS node that holds a value of some sort. This is the base class for all
 * the nodes in the abstract syntax tree that have a value.
 *
 * TODO(oana): Maybe de-emphasize the value aspect, allow value to be null, and
 * rename this as CssTermNode.
 *
 */
public abstract class CssValueNode extends CssNode {

  /** The value contained by the node. */
  private String value;

  /** Annotation to show whether this is a default value or not. */
  private boolean isDefault;

  /**
   * Constructor of a node that contains a value.
   *
   * @param value
   * @param sourceCodeLocation
   */
  public CssValueNode(@Nullable String value,
                      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(sourceCodeLocation);
    this.value = value;
    this.isDefault = false;
  }

  /**
   * Constructor of a node that contains a value.
   *
   * @param value
   */
  public CssValueNode(String value) {
    this(value, null);
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssValueNode(CssValueNode node) {
    this(node.getValue(), node.getSourceCodeLocation());
    this.isDefault = node.getIsDefault();
  }

  @Override
  public abstract CssValueNode deepCopy();

  public String getValue() {
    return this.value;
  }

  /**
   * Subclasses should perform additional consistency checks. For example, a
   * boolean expression node will not allow setting this as boolean expression
   * trees are immutable.
   */
  public void setValue(String value) {
    Preconditions.checkNotNull(value);
    Preconditions.checkArgument(!value.isEmpty());
    this.value = value;
  }

  public void setIsDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public boolean getIsDefault() {
    return isDefault;
  }

  /**
   * Use for debugging only.
   */
  @Override
  public String toString() {
    return getValue();
  }
}
