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

/**
 * A node representing a combinator of selectors in the AST.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssCombinatorNode extends CssNode {
  /**
   * Contains the list of all possible CSS combinators.
   */
  public enum Combinator {
    DESCENDANT(" "),
    CHILD(">"),
    ADJACENT_SIBLING("+"),
    GENERAL_SIBLING("~");

    private final String symbol;

    private Combinator(String symbol) {
      this.symbol = symbol;
    }

    public String getCanonicalName() {
      return symbol;
    }
  }

  /** Reference to a child selector. */
  private CssSelectorNode selector;
  /** Type of combinator. */
  private Combinator type;

  /**
   * Constructor of a combinator node.
   *
   * @param selector
   * @param type
   * @param sourceCodeLocation
   */
  public CssCombinatorNode(CssSelectorNode selector,
                           Combinator type,
                           SourceCodeLocation sourceCodeLocation) {
    super(sourceCodeLocation);
    this.selector = selector;
    becomeParentForNode(this.selector);
    this.type = type;
  }

  /**
   * Constructor of a combinator node.
   *
   * @param type
   * @param sourceCodeLocation
   */
  public CssCombinatorNode(Combinator type,
                           SourceCodeLocation sourceCodeLocation) {
    this(null, type, sourceCodeLocation);
  }
  
  /**
   * Copy constructor.
   * 
   * @param node
   */
  public CssCombinatorNode(CssCombinatorNode node) {
    this(
        node.getSelector().deepCopy(),
        node.getCombinatorType(),
        node.getSourceCodeLocation());
  }

  @Override
  public CssCombinatorNode deepCopy() {
    return new CssCombinatorNode(this);
  }
  
  public CssSelectorNode getSelector() {
    return selector;
  }

  public void setSelector(CssSelectorNode selector) {
    if (this.selector != null) {
      removeAsParentOfNode(this.selector);
    }
    this.selector = selector;
    becomeParentForNode(this.selector);
  }

  public Combinator getCombinatorType() {
    return type;
  }

  @Override
  public String toString() {
    return type.getCanonicalName() + selector;
  }
}
