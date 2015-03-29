/*
 * Copyright 2015 Google Inc.
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
 * A node that represent a unicode range.
 *
 * <p>See <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/unicode-range"></a> for details.
 */
public class CssUnicodeRangeNode extends CssValueNode {

  public CssUnicodeRangeNode(String value, @Nullable SourceCodeLocation sourceCodeLocation) {
    super(value, sourceCodeLocation);
  }

  public CssUnicodeRangeNode(String value) {
    this(value, null);
  }

  public CssUnicodeRangeNode(CssUnicodeRangeNode node) {
    super(node);
  }

  @Override
  public CssUnicodeRangeNode deepCopy() {
    return new CssUnicodeRangeNode(this);
  }
}
