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
 * A {@link CssRefinerNode} implementation that represents an attribute
 * selector. For example: {@code [hreflang|="en"]}
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssAttributeSelectorNode extends CssRefinerNode {
  /** Indicates which kind of attribute selector is used. */
  private MatchType matchType;
  private String attributeName;
  private CssValueNode value;

  /**
   * Determines how the given value has to match the value of the attribute so
   * that the attribute is selected.
   */
  public enum MatchType {
    ANY(""),                // [attr]
    EXACT("="),             // [attr=val]
    ONE_WORD("~="),         // [attr~=val]
    EXACT_OR_DASH("|="),    // [attr|=val]
    PREFIX("^="),           // [attr^=val]
    SUFFIX("$="),           // [attr$=val]
    CONTAINS("*=");         // [attr*=val]

    private final String symbol;

    private MatchType(String symbol) {
      this.symbol = symbol;
    }

    public String getSymbol() {
      return symbol;
    }
  }

  public CssAttributeSelectorNode(MatchType matchType, String attributeName,
      CssValueNode value, SourceCodeLocation sourceCodeLocation) {
    super(Refiner.ATTRIBUTE, "", sourceCodeLocation);
    this.matchType = matchType;
    this.attributeName = attributeName;
    this.value = value;
  }

  protected CssAttributeSelectorNode(CssAttributeSelectorNode node) {
    this(node.matchType, node.attributeName, node.value,
        node.getSourceCodeLocation());
  }

  @Override
  public CssAttributeSelectorNode deepCopy() {
    return new CssAttributeSelectorNode(this);
  }

  public MatchType getMatchType() {
    return matchType;
  }

  public String getMatchSymbol() {
    return matchType.getSymbol();
  }

  public String getAttributeName() {
    return attributeName;
  }

  public CssValueNode getValue() {
    return value;
  }

  @Override
  public Specificity getSpecificity() {
    // c++ (c = the number of class selectors, attributes selectors,
    // and pseudo-classes in the selector)
    return new Specificity(0, 1, 0);
  }
}
