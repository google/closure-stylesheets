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
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;

/**
 * Compiler pass that traverses the tree and marks as default the value nodes 
 * in a definition that has the corresponding annotation.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class MarkDefaultDefinitions extends DefaultTreeVisitor
    implements CssCompilerPass {

  private VisitController visitController;

  /**
   * String that matches the comment marking a definition as a default value.
   */
  private static final String DEFAULT = "/* @default */";

  public MarkDefaultDefinitions(VisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode definition) {
    boolean isDefault = definition.hasComment(DEFAULT);
    for (CssValueNode param : definition.getParameters()) {
      if (param.hasComment(DEFAULT)) {
        isDefault = true;
        break;
      }
    }
    if (isDefault) {
      for (CssValueNode node : definition.getParameters()) {
        node.setIsDefault(true);
      }
    }
    return false;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
