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

import com.google.common.css.SourceCodeLocation;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * A node representing a CSS property, such as background or padding.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssPropertyNode extends CssValueNode {
  private final Property property;

  /**
   * Creates a property node with the specified value.
   */
  public CssPropertyNode(String value) {
    this(value, null);
  }

  /**
   * Creates a property node with the specified value and source code location.
   */
  public CssPropertyNode(
      String value, @Nullable SourceCodeLocation sourceCodeLocation) {
    super(value, sourceCodeLocation);
    String propertyName = value.toLowerCase();
    this.property = Property.byName(propertyName);
  }

  /**
   * Creates a property node by deep-copying the specified property node.
   */
  public CssPropertyNode(CssPropertyNode node) {
    super(node);
    this.property = node.property;
  }

  @Override
  public CssPropertyNode deepCopy() {
    return new CssPropertyNode(this);
  }

  public String getProperty() {
    return property.getName();
  }

  public boolean isStandardProperty() {
    return property.isRecognizedProperty();
  }

  public boolean hasPositionDependentValues() {
    return property.hasPositionalParameters();
  }

  /**
   * Returns the set of shorthand properties related to this property, or the
   * empty set if this property is not standard.
   *
   * <p>For example, {@code border-left-style} has {@code border},
   * {@code border-left} and {@code border-style} as shorthands.
   */
  public Set<String> getShorthands() {
    return property.getShorthands();
  }

  /**
   * Gets the partition of this property. All properties with the same partition
   * share a common shorthand. A non-standard property is its own single
   * partition.
   * <p>
   * For example, {@code padding}, {@code padding-bottom}, {@code padding-left},
   * {@code padding-right}, {@code padding-top} are all in the {@code padding}
   * partition. As another example, {@code z-index} is its own single partition.
   *
   * @return a string representing the partition
   */
  public String getPartition() {
    return property.getPartition();
  }

  @Override
  public String toString() {
    return property.getName();
  }
}
