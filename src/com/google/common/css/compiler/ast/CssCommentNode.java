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
 * Node corresponding to a comment.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssCommentNode extends CssValueNode {

  /**
   * Constructor of a comment node.
   *
   * @param comment
   * @param sourceCodeLocation
   */
  public CssCommentNode(String comment, SourceCodeLocation sourceCodeLocation) {
    super(comment, sourceCodeLocation);
  }

  /**
   * Copy constructor.
   * 
   * @param node
   */
  public CssCommentNode(CssCommentNode node) {
    super(node);
  }
  
  @Override
  public CssCommentNode deepCopy() {
    return new CssCommentNode(this);
  }
}
