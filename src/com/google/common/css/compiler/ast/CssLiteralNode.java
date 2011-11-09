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

import com.google.common.css.SourceCodeLocation;

import javax.annotation.Nullable;

/**
 * A node that holds a CSS literal. Catch-all class for things that don't yet
 * have their own {@link CssValueNode} subtype in the tree representation.
 *
 * <p>This is meant to represent a leaf node so it lacks child nodes.
 *
 */
public class CssLiteralNode extends CssValueNode {

  public CssLiteralNode(String value,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(value, sourceCodeLocation);
  }

  public CssLiteralNode(String value) {
    this(value, null);
  }

  public CssLiteralNode(CssLiteralNode node) {
    super(node);
  }

  @Override
  public CssLiteralNode deepCopy() {
    return new CssLiteralNode(this);
  }
}
