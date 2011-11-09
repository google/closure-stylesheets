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

import com.google.common.css.SourceCodeLocation;

/**
 * Node corresponding to a hex color.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssHexColorNode extends CssValueNode {

  /**
   * Constructor of a color node.
   *
   * @param color
   */
  public CssHexColorNode(String color) {
    super(color, null);
  }

  /**
   * Constructor of a color node.
   *
   * @param color
   * @param sourceCodeLocation
   */
  public CssHexColorNode(String color,
                         SourceCodeLocation sourceCodeLocation) {
    super(color, sourceCodeLocation);
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssHexColorNode(CssHexColorNode node) {
    super(node);
  }
  
  @Override
  public CssHexColorNode deepCopy() {
    return new CssHexColorNode(this);
  }
  
  @Override
  public String toString() {
    return getValue().toLowerCase();
  }
}
