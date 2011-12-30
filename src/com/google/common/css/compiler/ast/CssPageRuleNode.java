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
 * A node representing an @page rule.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssPageRuleNode extends CssAtRuleNode {

  /**
   * Constructor of a page rule.
   */
  public CssPageRuleNode(List<CssCommentNode> comments,
      CssDeclarationBlockNode block) {
    super(CssAtRuleNode.Type.PAGE, new CssLiteralNode("page"), block,
        comments);
  }

  /**
   * Copy constructor.
   */
  public CssPageRuleNode(CssPageRuleNode node) {
    super(node);
  }

  @Override
  public CssNode deepCopy() {
    return new CssPageRuleNode(this);
  }

  @Override
  public CssDeclarationBlockNode getBlock() {
    // The type is ensured by the constructor.
    return (CssDeclarationBlockNode) super.getBlock();
  }
}
