/*
 * Copyright 2014 Google Inc.
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
 * A CSS node that holds a mathematical expression. The expression is stored in the value of this
 * node as a whitespace-normalized string. In particular, this means:
 * <ul>
 * <li>No {@code @def} substitution.
 * <li>No unit validation. For example, {@code calc(10s + 5px)}, {@code calc(5foo * 1bar)},
 *     etc. compile without error.
 * <li>No constant folding. For example, {@code calc(5px + 5px)} and {@code calc(5px/0)} are
 *     output verbatim.
 * </ul>
 *
 * <p>See http://www.w3.org/TR/css3-values/#calc
 *
 */
public class CssMathNode extends CssValueNode {

  public CssMathNode(String contents) {
    super(contents, null);
  }

  public CssMathNode(String contents, @Nullable SourceCodeLocation sourceCodeLocation) {
    super(contents, sourceCodeLocation);
  }

  public CssMathNode deepCopy() {
    return new CssMathNode(this.getValue());
  }
}
