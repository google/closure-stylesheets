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

import java.util.List;

/**
 * A list of values for a property in a declaration.
 *
 * @author oana@google.com (Oana Florescu)
 *
 */
public class CssPropertyValueNode extends CssNodesListNode<CssValueNode> {
  public CssPropertyValueNode() {
    super(false);
  }

  public CssPropertyValueNode(List<CssValueNode> valueNodesList) {
    super(false, valueNodesList, null);
  }

  public CssPropertyValueNode(CssPropertyValueNode node) {
    super(node);
  }

  @Override
  public CssPropertyValueNode deepCopy() {
    return new CssPropertyValueNode(this);
  }
}
