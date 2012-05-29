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

import javax.annotation.Nullable;

/**
 * A CSS node containing a numeric value such as height, width, length,
 * or percentage values.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssNumericNode extends CssValueNode {

  /** The numeric value of the node. */
  private String numericPart;
  /** The unit for this numeric value. */
  private String unit;
  /** Constant value for the units field for a node without units. */
  public static String NO_UNITS = "";

  /**
   * Constructor of a numeric node.
   *
   * @param value
   * @param unit
   */
  public CssNumericNode(String value,
                        String unit) {
    this(value, unit, null);
  }

  /**
   * Constructor of a numeric node.
   *
   * @param value
   * @param unit
   * @param sourceCodeLocation
   */
  public CssNumericNode(String value,
                        String unit,
                        @Nullable SourceCodeLocation sourceCodeLocation) {
    super(null, sourceCodeLocation);
    this.numericPart = value;
    this.unit = unit;
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssNumericNode(CssNumericNode node) {
    super(node);
    this.numericPart = node.getNumericPart();
    this.unit = node.getUnit();
  }

  @Override
  public CssNumericNode deepCopy() {
    return new CssNumericNode(this);
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getNumericPart() {
    return numericPart;
  }

  public void setNumericPart(String numericPart) {
    this.numericPart = numericPart;
  }

  @Override
  public void setValue(String value) {
    throw new UnsupportedOperationException(  // COV_NF_LINE
        "Use setNumericPart and setUnit to update a NumericValue.");
  }

  @Override
  public String toString() {
    return getNumericPart() + getUnit();
  }
}
