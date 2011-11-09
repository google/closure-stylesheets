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
import com.google.common.collect.ImmutableSet;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssSelectorNode.Specificity;

/**
 * A {@link CssRefinerNode} implementation that represents a pseudo-class.
 * For example: {@code :visited}, {@code :nth-child(2n)}
 *
 */
public class CssPseudoClassNode extends CssRefinerNode {
  private static final ImmutableSet<String> COMPATIBILITY_PSEUDO_ELEMENTS =
    ImmutableSet.of("first-line", "first-letter", "before", "after");

  /** Indicates if the refiner is a function and if so which one. */
  private FunctionType functionType;
  /** Argument of the function if the refiner is a function else empty */
  private String argument;
  /** Selector for the ':not' function */
  private CssSelectorNode notSelector;

  /**
   * Determines if the pseudo-class is a function and if so which one.
   */
  public enum FunctionType {
    NONE,
    LANG,
    // TODO(user): make the arguments for nth-functions real nodes
    NTH,
    NOT;
    // No support for 'any' at the moment because it is relatively new and
    // subject to changes (e.g. introduced in Gecko 2).
  }

  /**
   * Constructor for non-function pseudo-classes like {@code :link} or
   * {@code :visited}
   */
  public CssPseudoClassNode(String name,
      SourceCodeLocation sourceCodeLocation) {
    this(FunctionType.NONE, name, null /* argument */, null /* notSelector */,
        sourceCodeLocation);
  }

  /**
   * Constructor for function pseudo-classes except the negation function like
   * {@code :lang(en)} or {@code :nth-child(2n)}.
   */
  public CssPseudoClassNode(FunctionType functionType, String name,
      String argument, SourceCodeLocation sourceCodeLocation) {
    this(functionType, name, argument, null /* notSelector */,
        sourceCodeLocation);
    Preconditions.checkArgument(
        functionType != FunctionType.NOT && functionType != FunctionType.NONE);
  }

  /**
   * Constructor for the negation function pseudo-class.
   */
  public CssPseudoClassNode(String name, CssSelectorNode notSelector,
      SourceCodeLocation sourceCodeLocation) {
    this(FunctionType.NOT, name, null /* argument */, notSelector,
        sourceCodeLocation);
  }

  public CssPseudoClassNode(CssPseudoClassNode node) {
    this(node.functionType, node.refinerName, node.argument, node.notSelector,
        node.getSourceCodeLocation());
  }

  private CssPseudoClassNode(FunctionType functionType, String name,
      String argument, CssSelectorNode notSelector, SourceCodeLocation
      sourceCodeLocation) {
    super(Refiner.PSEUDO_CLASS, name, sourceCodeLocation);
    this.functionType = functionType;
    this.argument = argument;
    this.notSelector = notSelector;
  }

  @Override
  public CssPseudoClassNode deepCopy() {
    return new CssPseudoClassNode(this);
  }

  public FunctionType getFunctionType() {
    return functionType;
  }

  public String getArgument() {
    return argument;
  }

  public void setArgument(String argument) {
    this.argument = argument;
  }

  public CssSelectorNode getNotSelector() {
    return notSelector;
  }

  /**
   * Returns the specificity of this pseudo-class or an old pseudo-element
   * starting with a single colon.
   *
   * <p>The :: notation for pseudo-elements is introduced in CSS level 3 in
   * order to establish a discrimination between pseudo-classes and
   * pseudo-elements.
   * "For compatibility with existing style sheets, user agents must also
   * accept the previous one-colon notation for pseudo-elements introduced
   * in CSS levels 1 and 2 (namely, :first-line, :first-letter, :before and
   * :after). This compatibility is not allowed for the new pseudo-elements
   * introduced in this specification."
   * http://www.w3.org/TR/css3-selectors/#target-pseudo
   *
   * <p>The negation pseudo-class itself does not influence the specificity.
   * However, selectors inside the negation pseudo-class are counted like
   * any other. Thus, {@code red.level} has the same specificity as
   * {@code *:not(red.level)}.
   */
  @Override
  public Specificity getSpecificity() {
    if (COMPATIBILITY_PSEUDO_ELEMENTS.contains(refinerName)) {
      // d++ (d = the number of type selectors and pseudo-elements in
      // the selector)
      return new Specificity(0, 0, 1);
    } else {
      if (functionType == FunctionType.NOT) {
        return notSelector.getSpecificity();
      } else {
        // c++ (c = the number of class selectors, attributes selectors,
        // and pseudo-classes in the selector)
        return new Specificity(0, 1, 0);
      }
    }
  }

  @Override
  public String toString() {
    // TODO(user): toString should not be used to print a node. However,
    // some tests rely on it. This should be fixed.
    StringBuilder sb = new StringBuilder();
    sb.append(refinerType.getPrefix());
    sb.append(refinerName);
    switch (functionType) {
      case NONE:
        sb.append(refinerType.getSuffix());
        break;
      case LANG:
        sb.append(argument);
        break;
      case NTH:
        sb.append(argument);
        break;
      case NOT:
        sb.append(notSelector.toString());
        break;
    }
    if (functionType != FunctionType.NONE) {
      sb.append(")");
    }
    return sb.toString();
  }
}
