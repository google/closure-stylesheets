/*
 * Copyright 2011 Google Inc.
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
 *  A list of nodes that contains only keys (for keyframes).
 *  This list is used to apply the same style to different positions in an
 *  animation.
 *  For example: <code>0%, 33.3%</code>
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssKeyListNode extends CssNodesListNode<CssKeyNode> {

  public CssKeyListNode() {
    super(false);
  }

  public CssKeyListNode(CssKeyListNode node) {
    super(node);
  }
  
  @Override
  public CssKeyListNode deepCopy() {
    return new CssKeyListNode(this);
  }
}
