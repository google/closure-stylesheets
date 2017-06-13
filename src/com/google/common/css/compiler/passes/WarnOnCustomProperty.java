/*
 * Copyright 2017 Google Inc.
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
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.VisitController;

/**
 * A compiler pass that warns when custom properties declarations and references are encountered,
 * since they are not yet supported by all the major browsers.
 */
public class WarnOnCustomProperty extends DefaultTreeVisitor implements CssCompilerPass {

  private static final String VAR_FUNCTION_NAME = "var";

  static final String DECLARATION_WARNING_MSG = "Custom property declaration encountered. "
      + "Please note that custom properties do not work with all major browsers, and their use is "
      + "discouraged.";

  static final String REFERENCE_WARNING_MSG = "Custom property reference encountered. "
      + "Please note that custom properties do not work with all major browsers, and their use "
      + "is discouraged.";

  private final ErrorManager errorManager;

  private final VisitController visitController;

  public WarnOnCustomProperty(VisitController visitController, ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    if (declaration.isCustomDeclaration()) {
      errorManager.reportWarning(
          new GssError(
              DECLARATION_WARNING_MSG,
              declaration.getSourceCodeLocation()));
    }

    return super.enterDeclaration(declaration);
  }

  @Override
  public boolean enterFunctionNode(CssFunctionNode value) {
    if (value.getFunctionName().equals(VAR_FUNCTION_NAME)) {
      errorManager.reportWarning(
          new GssError(REFERENCE_WARNING_MSG, value.getSourceCodeLocation()));
    }

    return super.enterFunctionNode(value);
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
