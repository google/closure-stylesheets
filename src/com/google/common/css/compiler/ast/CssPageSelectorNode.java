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

import java.util.List;

/**
 * A node representing a page selector @-rule that can be used inside an
 * @page rule.
 *
 * <p>For example: {@code @bottom-left-corner { ... } }
 *
 */
public class CssPageSelectorNode extends CssAtRuleNode {

  /**
   * Constructor of a page selector.
   */
  public CssPageSelectorNode(CssAtRuleNode.Type type,
      List<CssCommentNode> comments, CssDeclarationBlockNode block) {
    super(type, new CssLiteralNode(type.getCanonicalName()), block,
        comments);
  }

  /**
   * Copy constructor.
   */
  public CssPageSelectorNode(CssPageSelectorNode node) {
    super(node);
  }

  @Override
  public CssNode deepCopy() {
    return new CssPageSelectorNode(this);
  }

  @Override
  public CssDeclarationBlockNode getBlock() {
    // The type is ensured by the constructor.
    return (CssDeclarationBlockNode) super.getBlock();
  }
}
