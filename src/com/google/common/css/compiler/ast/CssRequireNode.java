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
import com.google.common.css.SourceCodeLocation;

import java.util.List;

/**
 * {@link CssRequireNode} corresponds to a {@code @require} declaration in GSS.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class CssRequireNode extends CssAtRuleNode implements ChunkAware {

  private final String require;

  private Object chunk;

  public CssRequireNode(CssLiteralNode name, List<CssCommentNode> comments,
      SourceCodeLocation sourceCodeLocation) {
    super(CssAtRuleNode.Type.REQUIRE,
        Preconditions.checkNotNull(name),
        Preconditions.checkNotNull(comments));
    Preconditions.checkNotNull(sourceCodeLocation);
    setSourceCodeLocation(sourceCodeLocation);
    this.require = name.getValue();
  }

  /** Copy constructor so this can be cloned by {@link #deepCopy()}. */
  private CssRequireNode(CssRequireNode node) {
    this(node.getName(), node.getComments(), node.getSourceCodeLocation());
  }

  public String getRequire() {
    return require;
  }

  @Override
  public CssRequireNode deepCopy() {
    return new CssRequireNode(this);
  }

  @Override
  public Object getChunk() {
    return chunk;
  }

  @Override
  public void setChunk(Object chunk) {
    this.chunk = chunk;
  }
}
