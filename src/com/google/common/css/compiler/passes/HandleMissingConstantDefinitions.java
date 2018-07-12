/*
 * Copyright 2013 Google Inc.
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

import com.google.common.base.Preconditions;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.VisitController;

/**
 * Compiler pass that throws an error for any {@link CssConstantReferenceNode}
 * that do not have a corresponding {@link CssDefinitionNode}.
 */
public final class HandleMissingConstantDefinitions extends DefaultTreeVisitor
    implements CssCompilerPass {

  static final String ERROR_MESSAGE =
      "Unknown use of a @def constant. Make sure that the constant is included "
      + "in the compilation unit.";

  private final VisitController visitController;
  private final ErrorManager errorManager;
  private final ConstantDefinitions definitions;

  public HandleMissingConstantDefinitions(
      VisitController visitController,
      ErrorManager errorManager,
      ConstantDefinitions definitions) {
    this.visitController = Preconditions.checkNotNull(visitController);
    this.errorManager = Preconditions.checkNotNull(errorManager);
    this.definitions = Preconditions.checkNotNull(definitions);
  }

  @Override
  public void leaveValueNode(CssValueNode node) {
    checkValueNode(node);
  }

  @Override
  public void leaveArgumentNode(CssValueNode node) {
    checkValueNode(node);
  }

  private void checkValueNode(CssValueNode node) {
    if (node instanceof CssConstantReferenceNode
        && !definitions.getConstants().keySet().contains(node.getValue())) {
      errorManager.report(new GssError(
          ERROR_MESSAGE, node.getSourceCodeLocation()));
    }
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
