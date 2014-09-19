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

/**
 * A node representing an @-webkit-keyframes rule.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssKeyframesNode extends CssAtRuleNode implements ChunkAware {
  /** The chunk this selector belongs to. */
  private Object chunk;

  public CssKeyframesNode(CssLiteralNode name) {
    super(Type.UNKNOWN_BLOCK, name, new CssBlockNode());
  }

  public CssKeyframesNode(CssKeyframesNode node) {
    super(node);
  }

  /**
   * Used by CreateVendorPrefixedKeyframes to copy an existing keyframes node
   * to create a browser specific duplicate having different name.
   */
  public CssKeyframesNode(CssLiteralNode name, CssKeyframesNode node) {
    super(node);
    setName(name);
  }

  @Override
  public CssKeyframesNode deepCopy() {
    return new CssKeyframesNode(this);
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
    output += "@" + getName().toString() + getParameters().toString();

    if (getBlock() != null) {
      output += "{" + getBlock().toString() + "}";
    }

    return output;
  }

  @Override
  public CssBlockNode getBlock() {
    // The type is ensured by the constructor.
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
