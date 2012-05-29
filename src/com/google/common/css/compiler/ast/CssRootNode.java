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

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

/**
 * The root of a node.
 *
 *
 */
public class CssRootNode extends CssNode {
  private CssAtRuleNode charsetRule = null;
  private CssImportBlockNode importRules;

  // TODO(user): Support this.
  // http://www.w3.org/TR/css3-namespace/
  // private List<CssAtRuleNode> namespaceRules;

  // TODO(user): Support this.
  // http://disruptive-innovations.com/zoo/cssvariables/
  // private List<CssAtRuleNode> variablesRules;

  // Contains style rules, media rules, conditional rules, etc.
  private final CssBlockNode body;

  public CssRootNode(CssBlockNode body) {
    Preconditions.checkNotNull(body);
    Preconditions.checkArgument(!body.isEnclosedWithBraces());
    this.body = body;
    this.importRules = new CssImportBlockNode();
    becomeParentForNode(this.body);
    becomeParentForNode(this.importRules);
  }

  public CssRootNode() {
    this(new CssBlockNode(false /* isEnclosedWithBraces */));
  }

  public CssRootNode(CssRootNode node) {
    this(node.getBody().deepCopy());
    this.setParent(node.getParent());
    // TODO(oana): When charset rules are added to the tree, a deep copy of it
    // must be created here.
    this.importRules = node.getImportRules().deepCopy();
  }

  @Override
  public CssRootNode deepCopy() {
    return new CssRootNode(this);
  }

  @Override
  void removeParent() {
    Preconditions.checkState(false,
        "You cannot remove the parent node of a tree root.");
  }

  @Override
  void setParent(CssNode parent) {
    Preconditions.checkState(parent == null,
        "You cannot set a non-null parent node of a tree root.");
  }

  public CssAtRuleNode getCharsetRule() {
    return this.charsetRule;
  }

  void setCharsetRule(@Nullable CssAtRuleNode charsetRule) {
    removeAsParentOfNode(this.charsetRule);
    this.charsetRule = charsetRule;
    becomeParentForNode(this.charsetRule);
  }

  public CssImportBlockNode getImportRules() {
    return importRules;
  }

  void setImportRules(@Nullable CssImportBlockNode importRules) {
    removeAsParentOfNode(this.importRules);
    this.importRules = importRules;
    becomeParentForNode(this.importRules);
  }

  public CssBlockNode getBody() {
    return body;
  }

  /**
   * Debugging only.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (getCharsetRule() != null) {
      sb.append(getCharsetRule().toString());
    }
    if (!getImportRules().isEmpty()) {
      sb.append(getImportRules().toString());
    }
    sb.append(getBody().toString());
    return sb.toString();
  }
}
