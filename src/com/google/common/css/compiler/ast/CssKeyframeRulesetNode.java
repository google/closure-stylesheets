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

import com.google.common.base.Preconditions;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A node representing a keyframe style rule.
 * This is used inside a Webkit keyframes rule to represent the style of 
 * one or more points (specified by the keys) in the animation.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssKeyframeRulesetNode extends CssNode {
  /** List of keys. */
  private CssKeyListNode keys;
  /** The block of declarations inside this style rule. */
  private CssDeclarationBlockNode declarations;

  /**
   * Constructor of a keyframe ruleset node.
   *
   * @param declarations
   * @param comments
   */
  public CssKeyframeRulesetNode(CssDeclarationBlockNode declarations,
      @Nullable List<CssCommentNode> comments) {
    super(null, comments, null);
    Preconditions.checkNotNull(declarations);
    this.declarations = declarations;
    becomeParentForNode(this.declarations);
    this.keys = new CssKeyListNode();
    becomeParentForNode(this.keys);
  }

  /**
   * Constructor of a keyframe ruleset node.
   *
   * @param declarations
   */
  public CssKeyframeRulesetNode(CssDeclarationBlockNode declarations) {
    this(declarations, null);
  }

  /**
   * Constructor of a keyframe ruleset node.
   */
  public CssKeyframeRulesetNode() {
    this(new CssDeclarationBlockNode());
  }

  /**
   * Constructor of a keyframe ruleset node.
   *
   * @param comments
   */
  public CssKeyframeRulesetNode(List<CssCommentNode> comments) {
    this(new CssDeclarationBlockNode(), comments);
  }

  /**
   * Copy constructor.
   * 
   * @param node
   */
  public CssKeyframeRulesetNode(CssKeyframeRulesetNode node) {
    this(node.getDeclarations().deepCopy());
    this.setComments(node.getComments());
    this.keys = node.getKeys().deepCopy();
  }
  
  @Override
  public CssKeyframeRulesetNode deepCopy() {
    return new CssKeyframeRulesetNode(this);
  }
  
  public CssKeyListNode getKeys() {
    return keys;
  }

  void setKeys(CssKeyListNode keys) {
    Preconditions.checkNotNull(keys);
    removeAsParentOfNode(this.keys);
    this.keys = keys;
    becomeParentForNode(this.keys);
  }

  public CssDeclarationBlockNode getDeclarations() {
    return declarations;
  }

  public void addDeclaration(CssDeclarationNode declaration) {
    declarations.addChildToBack(declaration);
  }
  
  public void addKey(CssKeyNode key) {
    keys.addChildToBack(key);
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
    output += keys.toString() + "{" + declarations.toString() + "}";

    return output;
  }
}
