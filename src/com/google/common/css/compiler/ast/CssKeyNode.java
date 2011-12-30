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

import com.google.common.css.SourceCodeLocation;

import javax.annotation.Nullable;

/**
 * A node representing a key (for keyframes) in the AST.
 * A key is used to represent a position in an animation and can be a expressed
 * with percentage ranging from 0% to 100%, or by <code>from</code> (= 0%), or
 * <code>to</code> (= 100%).
 * For example: <code>0%, 33.3% { ... }</code>
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssKeyNode extends CssNode implements ChunkAware {
  /** Value of the key held by this node. */
  private String keyValue;
  /** The chunk this key belongs to. */
  private Object chunk;

  /**
   * Constructor of a key node.
   *
   * @param keyValue
   * @param sourceCodeLocation
   */
  public CssKeyNode(@Nullable String keyValue,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(sourceCodeLocation);
    this.keyValue = keyValue;
  }

  /**
   * Constructor of a key node.
   *
   * @param keyValue
   */
  public CssKeyNode(String keyValue) {
    this(keyValue, null);
  }

  /**
   * Copy-constructor of a key node.
   *
   * @param node
   */
  public CssKeyNode(CssKeyNode node) {
    this(node.getKeyValue(), node.getSourceCodeLocation());
    this.chunk = node.getChunk();
  }

  @Override
  public CssKeyNode deepCopy() {
    return new CssKeyNode(this);
  }

  public void setKeyValue(String keyValue) {
    this.keyValue = keyValue;
  }

  public String getKeyValue() {
    return keyValue;
  }

  @Override
  public void setChunk(Object chunk) {
    this.chunk = chunk;
  }

  @Override
  public Object getChunk() {
    return chunk;
  }

  /**
   * For debugging only.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (keyValue != null) {
      sb.append(keyValue);
    }
    return sb.toString();
  }
}
