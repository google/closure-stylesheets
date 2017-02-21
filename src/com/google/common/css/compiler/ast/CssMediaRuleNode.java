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
 * A node representing an @media rule.
 *
 * @author oana@google.com (Oana Florescu)
 *
 */
public class CssMediaRuleNode extends CssAtRuleNode implements ChunkAware {

  /** The chunk this selector belongs to. */
  private Object chunk;

  /**
   * Constructor of a media rule.
   */
  public CssMediaRuleNode() {
    super(CssAtRuleNode.Type.MEDIA, new CssLiteralNode("media"));
  }

  /**
   * Constructor of a media rule.
   */
  public CssMediaRuleNode(List<CssCommentNode> comments) {
    super(CssAtRuleNode.Type.MEDIA, new CssLiteralNode("media"), comments);
  }

  /**
   * Constructor of a media rule.
   */
  public CssMediaRuleNode(List<CssCommentNode> comments, CssBlockNode block) {
    super(CssAtRuleNode.Type.MEDIA, new CssLiteralNode("media"), block,
        comments);
  }

  /**
   * Copy constructor.
   */
  public CssMediaRuleNode(CssMediaRuleNode node) {
    super(node);

    this.chunk = node.getChunk();
  }

  @Override
  public CssMediaRuleNode deepCopy() {
    return new CssMediaRuleNode(this);
  }

  @Override
  public CssBlockNode getBlock() {
    // This type is ensured by the constructor.
    return (CssBlockNode) super.getBlock();
  }

  @Override
  public void setChunk(Object chunk) {
    this.chunk = chunk;
  }

  @Override
  public Object getChunk() {
    return chunk;
  }
}
