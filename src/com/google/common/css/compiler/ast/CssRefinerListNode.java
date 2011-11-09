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

/**
 * A list of nodes that are refiners of a selector.
 * 
 * @author oana@google.com (Oana Florescu)
 */
public class CssRefinerListNode extends CssNodesListNode<CssRefinerNode> {

  /**
   * Constructor of a list of refiner nodes.
   */
  public CssRefinerListNode() {
    super(false);
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssRefinerListNode(CssRefinerListNode node) {
    super(node);
  }
  
  @Override
  public CssRefinerListNode deepCopy() {
    return new CssRefinerListNode(this);
  }
}
