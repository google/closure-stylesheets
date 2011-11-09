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

package com.google.common.css.compiler.passes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.CssAttributeSelectorNode;
import com.google.common.css.compiler.ast.CssAttributeSelectorNode.MatchType;
import com.google.common.css.compiler.ast.CssCombinatorNode;
import com.google.common.css.compiler.ast.CssCombinatorNode.Combinator;
import com.google.common.css.compiler.ast.CssPseudoClassNode;
import com.google.common.css.compiler.ast.CssPseudoElementNode;
import com.google.common.css.compiler.ast.CssRefinerNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.Set;

/**
 * This compiler pass enforces that only correct CSS 2.1 is used.
 * See {@link "http://www.w3.org/TR/CSS21/"} for the specification.
 *
 */
public class StrictCss2 extends StrictCssBase {
  private static final ImmutableSet<String> PSEUDO_CLASSES_OR_ELEMENT =
      ImmutableSet.of("first-child", "link", "visited", "active", "hover",
          "focus", "first-line", "first-letter", "before", "after");
  private static final ImmutableSet<Combinator>
      ALLOWED_COMBINATORS = ImmutableSet.of(Combinator.DESCENDANT,
          Combinator.CHILD, Combinator.ADJACENT_SIBLING);
  private static final ImmutableSet<MatchType>
      ALLOWED_ATTRIBUTE_SELECTORS = ImmutableSet.of(MatchType.ANY,
          MatchType.EXACT, MatchType.EXACT_OR_DASH, MatchType.ONE_WORD);

  /**
   * Units.
   * See Section 4.3.2 of
   * http://www.w3.org/TR/CSS2/syndata.html#value-def-length
   */
  private static final Set<String> UNITS = Sets.newHashSet(
      "",

      // Relative measures
      "em",
      "ex",
      "%",

      // Absolute measures
      "in",
      "cm",
      "mm",
      "pt",
      "pc",
      "px");

  @VisibleForTesting
  static final String UNSUPPORTED_COMBINATOR_ERROR_MESSAGE =
      "An unsupported combinator is used.";
  @VisibleForTesting
  static final String UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE =
      "An unsupported pseudo-class or pseudo-element is used.";
  @VisibleForTesting
  static final String NEW_PESUDO_ELEMENTS_NOT_ALLOWED_ERROR_MESSAGE =
      "Pseudo-elements starting with '::' are not allowed in CSS 2.1.";
  @VisibleForTesting
  static final String FORBIDDEN_ATTRIBUTE_COMPARER_ERROR_MESSAGE =
      "An operator for matching attributes not allowed in CSS 2.1 is used.";

  public StrictCss2(MutatingVisitController visitController,
      ErrorManager errorManager) {
    super(visitController, errorManager);
  }

  /**
   * Idenitifies valid units of 'width', 'height', etc.
   */
  @Override
  Set<String> getValidCssUnits() {
    return UNITS;
  }

  /**
   * Ensures that the combinator '~' (introduced in CSS 3) is not used.
   */
  @Override
  public boolean enterCombinator(CssCombinatorNode combinator) {
    if (!ALLOWED_COMBINATORS.contains(combinator.getCombinatorType())) {
      errorManager.report(new GssError(UNSUPPORTED_COMBINATOR_ERROR_MESSAGE,
          combinator.getSourceCodeLocation()));
      return false;
    }
    return true;
  }

  /**
   * Ensures that only pseudo-classes valid in CSS 2.1 are used. Especially, the
   * the new pseudo-classes introduced in CSS 3 are not valid.
   */
  @Override
  public boolean enterPseudoClass(CssPseudoClassNode node) {
    switch (node.getFunctionType()) {
      case NONE:
        return checkNonFunctionPseudoClass(node);
      case NTH:
      case NOT:
        reportUnsupported(node,
            UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE,
            PSEUDO_CLASSES_OR_ELEMENT);
        return false;
    }
    return true;
  }

  /**
   * Ensures that only pseudo-classes and pseudo-elements listed in the
   * specification are used.
   */
  private boolean checkNonFunctionPseudoClass(CssRefinerNode refiner) {
     if (!PSEUDO_CLASSES_OR_ELEMENT.contains(refiner.getRefinerName())) {
       reportUnsupported(refiner,
           UNSUPPORTED_PESUDO_CLASS_OR_ELEMENT_ERROR_MESSAGE,
           PSEUDO_CLASSES_OR_ELEMENT);
       return false;
     }
     return true;
  }

  /**
   * Ensures that the new pseudo-element notation (::) is not used, because
   * the notation has been introduced in CSS 3.
   */
  @Override
  public boolean enterPseudoElement(CssPseudoElementNode node) {
    errorManager.report(new GssError(
        NEW_PESUDO_ELEMENTS_NOT_ALLOWED_ERROR_MESSAGE,
        node.getSourceCodeLocation()));
    return false;
  }

  /**
   * Ensures that attribute selectors are valid. If the equal sign is preceded
   * by a special character, only '~' and '|' are allowed.
   * Valid CSS 2.1 examples: {@code [att]}, {@code [att=val]},
   * {@code [att~=val]}, {@code [att|=val]}
   */
  @Override
  public boolean enterAttributeSelector(CssAttributeSelectorNode node) {
    if (!ALLOWED_ATTRIBUTE_SELECTORS.contains(node.getMatchType())) {
      errorManager.report(new GssError(
          FORBIDDEN_ATTRIBUTE_COMPARER_ERROR_MESSAGE,
          node.getSourceCodeLocation()));
      return false;
    }
    return true;
  }
}
