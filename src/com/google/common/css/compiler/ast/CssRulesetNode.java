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

import java.util.List;

import javax.annotation.Nullable;

/**
 * A node representing a style rule.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssRulesetNode extends CssNode {
  /** List of selectors of this style rule. */
  private CssSelectorListNode selectors;
  /** The block of declarations inside this style rule. */
  private CssDeclarationBlockNode declarations;

  /**
   * Constructor of a ruleset node.
   *
   * @param declarations
   * @param comments
   */
  public CssRulesetNode(CssDeclarationBlockNode declarations,
                 @Nullable List<CssCommentNode> comments) {
    super(null, comments, null);
    Preconditions.checkNotNull(declarations);
    this.declarations = declarations;
    becomeParentForNode(this.declarations);
    this.selectors = new CssSelectorListNode();
    becomeParentForNode(this.selectors);
  }

  /**
   * Constructor of a ruleset node.
   *
   * @param declarations
   */
  public CssRulesetNode(CssDeclarationBlockNode declarations) {
    this(declarations, null);
  }

  /**
   * Constructor of a ruleset node.
   */
  public CssRulesetNode() {
    this(new CssDeclarationBlockNode());
  }

  /**
   * Constructor of a ruleset node.
   *
   * @param comments
   */
  public CssRulesetNode(List<CssCommentNode> comments) {
    this(new CssDeclarationBlockNode(), comments);
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssRulesetNode(CssRulesetNode node) {
    this(node.getDeclarations().deepCopy());
    this.setSourceCodeLocation(node.getSourceCodeLocation());
    this.setComments(node.getComments());
    this.selectors = node.getSelectors().deepCopy();
  }

  @Override
  public CssRulesetNode deepCopy() {
    return new CssRulesetNode(this);
  }

  public CssSelectorListNode getSelectors() {
    return selectors;
  }

  public void setSelectors(CssSelectorListNode selectors) {
    Preconditions.checkNotNull(selectors);
    removeAsParentOfNode(this.selectors);
    this.selectors = selectors;
    becomeParentForNode(this.selectors);
  }

  public CssDeclarationBlockNode getDeclarations() {
    return declarations;
  }

  public void addDeclaration(CssNode declaration) {
    declarations.addChildToBack(declaration);
  }

  public void addSelector(CssSelectorNode selector) {
    selectors.addChildToBack(selector);
  }

  /**
   * For debugging only.
   */
  @Override
  public String toString() {
    String output = "";
    if (!getComments().isEmpty()) {
      output = getComments().toString();
    }
    output += selectors.toString() + "{" + declarations.toString() + "}";

    return output;
  }
}
