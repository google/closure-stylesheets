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

import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.Set;

/**
 * Base class for compiler passes enforcing a specific CSS level.
 *
 */
public abstract class StrictCssBase extends DefaultTreeVisitor
    implements CssCompilerPass {
  private static final String SUPPORTED_LIST_PREFIX = "\nsupported names: ";
  private static final String SUPPORTED_LIST_SUFFIX = "\n";

  static final String INVALID_UNIT_PREFIX = "Invalid unit: ";

  protected final MutatingVisitController visitController;
  protected final ErrorManager errorManager;

  public StrictCssBase(MutatingVisitController visitController,
      ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  abstract Set<String> getValidCssUnits();

  /**
   * Checks numeric nodes for valid units.
   */
  @Override
  public boolean enterValueNode(CssValueNode node) {
    if (node instanceof CssNumericNode) {
      // NOTE(nicksantos): At some point, we should attach type information
      // to the AST so that we can validate by the type of unit.
      String unit = ((CssNumericNode) node).getUnit();
      if (!getValidCssUnits().contains(unit)) {
        errorManager.report(
            new GssError(INVALID_UNIT_PREFIX + unit,
                node.getSourceCodeLocation()));
        return false;
      }
    }
    return true;
  }

  /**
   * Adds a list of supported names to the error message and then reports
   * the error.
   */
  protected void reportUnsupported(CssNode node, String message,
      Set<String> supportedNames) {
    StringBuilder sb = new StringBuilder();
    sb.append(message);
    sb.append(SUPPORTED_LIST_PREFIX);
    for (String name : supportedNames) {
      sb.append(name);
      sb.append(", ");
    }
    sb.append(SUPPORTED_LIST_SUFFIX);
    errorManager.report(new GssError(sb.toString(),
        node.getSourceCodeLocation()));
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
