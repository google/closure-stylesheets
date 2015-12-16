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
 * A {@link CssRefinerNode} implementation that represents a selector for a
 * class. For example: {@code .classy}
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssClassSelectorNode extends CssRefinerNode {
  /** Specifies the kind or absence of a component scoping prefix. */
  public static enum ComponentScoping {
    /** The classname has no prefix. */
    DEFAULT,
    /** The classname has a % prefix, to force scoping. */
    FORCE_SCOPED,
    /** The classname has a ^ prefix, to prevent scoping. */
    FORCE_UNSCOPED
  }

  private final ComponentScoping scoping;

  public CssClassSelectorNode(String refinerName, ComponentScoping scoping,
      SourceCodeLocation sourceCodeLocation) {
    super(Refiner.CLASS, refinerName, sourceCodeLocation);
    this.scoping = scoping;
  }

  public CssClassSelectorNode(String refinerName, SourceCodeLocation sourceCodeLocation) {
    this(refinerName, ComponentScoping.DEFAULT, sourceCodeLocation);
  }

  protected CssClassSelectorNode(CssClassSelectorNode node) {
    this(node.refinerName, node.scoping, node.getSourceCodeLocation());
    this.setComments(node.getComments());
  }

  /** Returns the kind or absence of a component scoping prefix. */
  public ComponentScoping getScoping() {
    return scoping;
  }

  @Override
  public CssClassSelectorNode deepCopy() {
    return new CssClassSelectorNode(this);
  }

  @Override
  public Specificity getSpecificity() {
    // c++ (c = the number of class selectors, attributes selectors,
    // and pseudo-classes in the selector)
    return new Specificity(0, 1, 0);
  }
}
