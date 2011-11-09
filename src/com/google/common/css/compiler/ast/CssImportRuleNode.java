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
 * A node representing an @import rule.
 *
 * @author oana@google.com (Oana Florescu)
 *
 */
public class CssImportRuleNode extends CssAtRuleNode {
  /**
   * Constructor of an import rule.
   */
  public CssImportRuleNode() {
    super(CssAtRuleNode.Type.IMPORT, new CssLiteralNode("import"));
  }

  /**
   * Constructor of an import rule.
   *
   * @param comments
   */
  public CssImportRuleNode(List<CssCommentNode> comments) {
    super(CssAtRuleNode.Type.IMPORT, new CssLiteralNode("import"), comments);
  }
  
  /**
   * Copy constructor.
   * 
   * @param node
   */
  public CssImportRuleNode(CssImportRuleNode node) {
    super(node);
  }
  
  @Override
  public CssImportRuleNode deepCopy() {
    return new CssImportRuleNode(this);
  }
}
