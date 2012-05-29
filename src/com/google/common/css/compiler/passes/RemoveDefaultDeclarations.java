/*
 * Copyright 2009 Google Inc.
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
import com.google.common.css.compiler.ast.CssPriorityNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

/**
 * Compiler pass that removes declaration nodes that have all the property
 * values marked as default.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class RemoveDefaultDeclarations extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;

  private boolean canRemoveDefaultValue = false;

  public RemoveDefaultDeclarations(MutatingVisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    if (canRemoveDefaultValue && node.getIsDefault()) {
      visitController.removeCurrentNode();
    }
    return true;
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode node) {
    boolean removeDeclaration = true;
    canRemoveDefaultValue =
        !node.getPropertyName().getProperty().hasPositionalParameters();
    // If any of the DeclarationNode's values is a priority node (marked with 
    // "!important" in the css) then none of the values should be 
    // removed. Otherwise remove the default values.
    for (CssValueNode value : node.getPropertyValue().childIterable()) {
      if (value instanceof CssPriorityNode) {
        removeDeclaration = false;
        canRemoveDefaultValue = false;
        break;
      }
      if (!value.getIsDefault()) {
        removeDeclaration = false;
      }
    }
    if (removeDeclaration) {
      visitController.removeCurrentNode();
    }
    return true;
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode node) {
    canRemoveDefaultValue = false;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
