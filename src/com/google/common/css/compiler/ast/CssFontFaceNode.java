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

import javax.annotation.Nullable;

/**
 * {@link CssFontFaceNode} corresponds to a {@code @font-face} declaration in
 * CSS.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class CssFontFaceNode extends CssAtRuleNode implements ChunkAware {

  private Object chunk;

  public CssFontFaceNode() {
    this((List<CssCommentNode>)null);
  }

  public CssFontFaceNode(@Nullable List<CssCommentNode> comments) {
    this(comments, null);
  }

  public CssFontFaceNode(@Nullable List<CssCommentNode> comments,
      @Nullable CssDeclarationBlockNode block) {
    super(CssAtRuleNode.Type.FONT_FACE, new CssLiteralNode("font-face"), block,
        comments);
  }

  /** Copy constructor so this can be cloned by {@link #deepCopy()}. */
  private CssFontFaceNode(CssFontFaceNode node) {
    super(node);
    this.chunk = node.getChunk();
  }

  @Override
  public CssDeclarationBlockNode getBlock() {
    // The type is ensured by the constructor.
    return (CssDeclarationBlockNode) super.getBlock();
  }

  @Override
  public CssFontFaceNode deepCopy() {
    return new CssFontFaceNode(this);
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
