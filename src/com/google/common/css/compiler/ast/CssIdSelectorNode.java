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
import com.google.common.css.compiler.ast.CssSelectorNode.Specificity;

/**
 * A {@link CssRefinerNode} implementation that represents a selector for an id.
 * For example: {@code #id}
 *
 */
public class CssIdSelectorNode extends CssRefinerNode {
  public CssIdSelectorNode(String refinerName,
      SourceCodeLocation sourceCodeLocation) {
    super(Refiner.ID, refinerName, sourceCodeLocation);
  }

  protected CssIdSelectorNode(CssIdSelectorNode node) {
    this(node.refinerName, node.getSourceCodeLocation());
  }

  @Override
  public CssIdSelectorNode deepCopy() {
    return new CssIdSelectorNode(this);
  }

  @Override
  public Specificity getSpecificity() {
    // b++ (b = the number of ID selectors in the selector)
    return new Specificity(1, 0, 0);
  }
}
