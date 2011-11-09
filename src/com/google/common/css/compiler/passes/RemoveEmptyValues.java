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
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssPriorityNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

/**
 * Compiler pass that removes empty values and their containing declaration
 * nodes if they become empty.
 *
 */
public class RemoveEmptyValues extends DefaultTreeVisitor
    implements CssCompilerPass {

  /**
   * Special "empty" value that can cause declarations to be removed if they
   * have only this value.
   */
  private static final String EMPTY = "empty";

  private final MutatingVisitController visitController;

  public RemoveEmptyValues(MutatingVisitController visitController) {
    this.visitController = visitController;
  }

  private static boolean isEmpty(CssValueNode node) {
    return EMPTY.equals(node.getValue());
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    return false;
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    if (isEmpty(node)) {
      visitController.removeCurrentNode();
    }
    return true;
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode node) {
    CssPropertyValueNode propertyValue = node.getPropertyValue();
    if (propertyValue.isEmpty()
        || (propertyValue.numChildren() == 1
            && propertyValue.getChildAt(0) instanceof CssPriorityNode)) {
      visitController.removeCurrentNode();
    }
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
