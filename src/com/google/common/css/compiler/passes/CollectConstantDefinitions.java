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
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;

/**
 * A pass that collects the constant definitions inside the tree.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CollectConstantDefinitions extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final VisitController visitController;
  private final ConstantDefinitions constantDefinitions;

  public CollectConstantDefinitions(CssTree tree) {
    this.visitController = tree.getVisitController();
    this.constantDefinitions = new ConstantDefinitions();
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    constantDefinitions.addConstantDefinition(node);
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

  public ConstantDefinitions getConstantDefinitions() {
    return constantDefinitions;
  }
}
