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
import com.google.common.css.compiler.ast.CssPseudoClassNode;
import com.google.common.css.compiler.ast.CssPseudoElementNode;
import com.google.common.css.compiler.ast.CssRefinerNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.Set;

/**
 * This compiler pass enforces that only correct CSS level 3 is used. Be aware
 * that there is no final specification yet and that the draft is distributed
 * over several documents.
 *
 * <p>The official W3C drafts used here:
 * <a href="http://www.w3.org/TR/css3-selectors/">Selectors Level 3</a>,
 * <a href="http://www.w3.org/TR/css3-ui/">CSS3 Basic User Interface Module</a>
 *
 * <p>Wikipedia gives a good overview:
 * <a href="http://en.wikipedia.org/wiki/Comparison_of_layout_engines_(Cascading_Style_Sheets)#Selectors">
 * Comparison of layout engines (Cascading Style Sheets)</a>
 *
 * <p>TODO(fbenz): The ProcessRefiners and ProcessKeyframes passes should
 * run before.
 *
 * @author fbenz@google.com (Florian Benz)
 *
 */
public class StrictCss3 extends StrictCssBase {
  private static final ImmutableSet<String> PSEUDO_CLASSES = ImmutableSet.of(
      "root", "first-child", "last-child", "first-of-type", "last-of-type",
      "only-child", "only-of-type", "empty", "link", "visited", "active",
      "hover", "focus", "target", "enabled", "disabled", "checked",
      "indeterminate", "default", "valid", "invalid", "in-range",
      "out-of-range", "required", "optional", "read-only", "read-write");
  private static final ImmutableSet<String> PSEUDO_CLASSES_NTH =
    ImmutableSet.of("nth-child", "nth-last-child", "nth-of-type",
        "nth-last-of-type");
  private static final ImmutableSet<String> PSEUDO_ELEMENTS = ImmutableSet.of(
      "first-line", "first-letter", "before", "after", "value", "choices",
      "repeat-item", "repeat-index");

  /**
   * Length units.
   * See Section 5 of
   * http://www.w3.org/TR/css3-values/#lengths
   */
  private static final Set<String> UNITS = Sets.newHashSet(
      "",

      // Relative measures
      "em",
      "ex",
      "%",
      "ch",
      "rem",
      "vw",
      "vh",
      "vm",

      // Absolute measures
      "in",
      "cm",
      "mm",
      "pt",
      "pc",
      "px",

      // angles
      "deg",
      "grad",
      "rad",
      "turn",

      // time
      "s",
      "ms",

      // frequency
      "hz",
      "khz");

  @VisibleForTesting
  static final String UNSUPPORTED_PESUDO_CLASS_ERROR_MESSAGE =
      "An unsupported pseudo-class is used.";
  @VisibleForTesting
  static final String UNSUPPORTED_PESUDO_CLASS_NTH_ERROR_MESSAGE =
      "An unsupported pseudo-class for the nth-pattern is used.";
  @VisibleForTesting
  static final String UNSUPPORTED_PESUDO_ELEMENT_ERROR_MESSAGE =
      "An unsupported pseudo-element is used.";
  @VisibleForTesting
  static final String MISSING_FUNCTION_PESUDO_CLASS_NTH_ERROR_MESSAGE =
      "A pseudo-class for the nth-pattern is used without an argument.";

  public StrictCss3(MutatingVisitController visitController,
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

  @Override
  public boolean enterPseudoClass(CssPseudoClassNode node) {
    switch (node.getFunctionType()) {
      case NONE:
        return checkNonFunctionPseudoClass(node);
      case NTH:
        return checkNthFunctionPseudoClass(node);
    }
    return true;
  }

  /**
   * Checks whether the pseudo-class is in the list of standard pseudo-classes
   * for CSS level 3. Note that this means that pseudo-elements, like
   * {@code after}, that are normally accepted using the pseudo-class style for
   * backwards compatibility are rejected.
   */
  private boolean checkNonFunctionPseudoClass(CssRefinerNode refiner) {
    if (PSEUDO_CLASSES_NTH.contains(refiner.getRefinerName())) {
      errorManager.report(new GssError(
          MISSING_FUNCTION_PESUDO_CLASS_NTH_ERROR_MESSAGE,
          refiner.getSourceCodeLocation()));
      return false;
    }

    if (!PSEUDO_CLASSES.contains(refiner.getRefinerName())) {
      reportUnsupported(refiner, UNSUPPORTED_PESUDO_CLASS_ERROR_MESSAGE,
          PSEUDO_CLASSES);
      return false;
    }
    return true;
  }

  private boolean checkNthFunctionPseudoClass(CssRefinerNode refiner) {
    // If name is an nth function pseudo class, then it should be of the form:
    // "nth-child(".
    String name = refiner.getRefinerName();

    if (!name.endsWith("(")
        || !PSEUDO_CLASSES_NTH.contains(name.substring(0, name.length() - 1))) {
      reportUnsupported(refiner, UNSUPPORTED_PESUDO_CLASS_NTH_ERROR_MESSAGE,
          PSEUDO_CLASSES_NTH);
      return false;
    }
    return true;
  }

  /**
   * Ensures that only pseudo-elements valid in CSS 3 are used.
   */
  @Override
  public boolean enterPseudoElement(CssPseudoElementNode node) {
    if (!PSEUDO_ELEMENTS.contains(node.getRefinerName())) {
      reportUnsupported(node, UNSUPPORTED_PESUDO_ELEMENT_ERROR_MESSAGE,
          PSEUDO_ELEMENTS);
      return false;
    }
    return true;
  }
}
