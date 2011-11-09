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

import java.util.List;

/**
 * A list of nodes that are arguments of a function.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssFunctionArgumentsNode extends CssNodesListNode<CssValueNode> {

  /**
   * Constructor of a node that contains the arguments of a function.
   */
  public CssFunctionArgumentsNode() {
    super(false);
  }

  /**
   * Constructor of a node with list of arguments to initialize with.
   */
  public CssFunctionArgumentsNode(List<CssValueNode> valueNodesList) {
    super(false, valueNodesList, null);
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssFunctionArgumentsNode(CssFunctionArgumentsNode node) {
    super(false);
    for (CssValueNode arg : node.childIterable()) {
      this.addChildToBack(arg.deepCopy());
    }
  }

  @Override
  public CssFunctionArgumentsNode deepCopy() {
    return new CssFunctionArgumentsNode(this);
  }
}
