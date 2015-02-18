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
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssKeyNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.VisitController;

/**
 * Compiler pass which ensures that @keyframes rules are only allowed if
 * they are enabled. In addition this pass checks if the keys are between
 * 0% and 100%. If CSS simplification is enabled, "from" is replaced by "0%"
 * and "100%" is replaced by "to".
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class ProcessKeyframes extends DefaultTreeVisitor
    implements CssCompilerPass {
  @VisibleForTesting
  static final String KEYFRAMES_NOT_ALLOWED_ERROR_MESSAGE =
    "a @keyframes rule occured but the option for it is disabled";
  @VisibleForTesting
  static final String WRONG_KEY_VALUE_ERROR_MESSAGE =
    "the value of the key is not between 0% and 100%";
  static final String INVALID_NUMBER_ERROR_MESSAGE =
    "the value of the key is invalid (not 'from', 'to', or 'XXX.XXX%')";

  private final VisitController visitController;
  private final ErrorManager errorManager;
  private final boolean keyframesAllowed;
  private final boolean simplifyCss;

  public ProcessKeyframes(VisitController visitController,
      ErrorManager errorManager,
      boolean keyframesAllowed,
      boolean simplifyCss) {
    this.visitController = visitController;
    this.errorManager = errorManager;
    this.keyframesAllowed = keyframesAllowed;
    this.simplifyCss = simplifyCss;
  }

  @Override
  public boolean enterKeyframesRule(CssKeyframesNode node) {
    if (!keyframesAllowed) {
      errorManager.report(new GssError(KEYFRAMES_NOT_ALLOWED_ERROR_MESSAGE,
          node.getSourceCodeLocation()));
    }
    return keyframesAllowed;
  }

  @Override
  public boolean enterKey(CssKeyNode node) {
    if (!keyframesAllowed) {
      return false;
    }
    String value = node.getKeyValue();
    float percentage = -1;
    if (value.contains("%")) {
      try {
        // parse to a float by excluding '%'
        percentage = Float.parseFloat(value.substring(0, value.length() - 1));
      } catch (NumberFormatException e) {
        // should not happen if the generated parser works correctly
        errorManager.report(new GssError(INVALID_NUMBER_ERROR_MESSAGE,
            node.getSourceCodeLocation()));
        return false;
      }
      if (!checkRangeOfPercentage(node, percentage)) {
        return false;
      }
    } else {
      if (!value.equals("from") && !value.equals("to")) {
        errorManager.report(new GssError(INVALID_NUMBER_ERROR_MESSAGE,
            node.getSourceCodeLocation()));
        return false;
      }
    }
    if (simplifyCss) {
      compactRepresentation(node, percentage);
    }
    return true;
  }

  /**
   * Checks if the percentage is between 0% and 100% inclusive.
   *
   * @param node The {@link CssKeyNode} to get the location in case of an error
   * @param percentage The value represented as a float
   * @return Returns true if there is no error
   */
  private boolean checkRangeOfPercentage(CssKeyNode node, float percentage) {
    // check whether the percentage is between 0% and 100%
    if (percentage < 0 || percentage > 100) {
      errorManager.report(new GssError(WRONG_KEY_VALUE_ERROR_MESSAGE,
          node.getSourceCodeLocation()));
      return false;
    }
    return true;
  }

  /**
   * Shortens the representation of the key.
   *
   * @param node The {@link CssKeyNode} where the percentage belongs to.
   * @param percentage The value represented as a float
   */
  @VisibleForTesting
  void compactRepresentation(CssKeyNode node, float percentage) {
    if (node.getKeyValue().equals("from")) {
      node.setKeyValue("0%");
    } else if (percentage == 100) {
      node.setKeyValue("to");
    } else if (percentage != -1) {
      String percentageStr = Float.toString(percentage);
      if (0 < percentage && percentage < 1) {
        // eliminate an unnecessary leading 0
        percentageStr = percentageStr.substring(1, percentageStr.length());
      }
      // eliminate a trailing zero like in 0.0
      percentageStr = percentageStr.replaceAll("0+$", "");
      if (percentageStr.endsWith(".")) {
        // if the number ends with '.' then eliminate that too
        percentageStr = percentageStr.substring(0, percentageStr.length() - 1);
      }
      node.setKeyValue(percentageStr + "%");
    }
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
