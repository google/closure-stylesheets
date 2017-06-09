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

import com.google.common.collect.ImmutableList;
import com.google.common.css.SubstitutionMap;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssIdSelectorNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

/**
 * Compiler pass that does CSS class renaming given a renaming map.
 * 
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
public class CssClassRenaming extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;
  private final SubstitutionMap cssClassRenamingMap;
  private final SubstitutionMap elementIdMap;

  public CssClassRenaming(MutatingVisitController visitController,
      SubstitutionMap cssClassRenamingMap, SubstitutionMap elementIdMap) {
    this.visitController = visitController;
    this.cssClassRenamingMap = cssClassRenamingMap;
    this.elementIdMap = elementIdMap;
  }

  @Override
  public boolean enterClassSelector(CssClassSelectorNode node) {
    if (cssClassRenamingMap == null) {
      return true;
    }
    String substitution = cssClassRenamingMap.get(node.getRefinerName());
    if (substitution == null) {
      return true;
    }
    CssClassSelectorNode classSelector = 
        new CssClassSelectorNode(substitution, node.getSourceCodeLocation());
    visitController.replaceCurrentBlockChildWith(
        ImmutableList.of(classSelector), false /* visitTheReplacementNodes */);
    return true;
  }

  @Override
  public boolean enterIdSelector(CssIdSelectorNode node) {
    if (elementIdMap == null) {
      return true;
    }
    String substitution = elementIdMap.get(node.getRefinerName());
    if (substitution == null) {
      return true;
    }
    CssIdSelectorNode idSelector = 
        new CssIdSelectorNode(substitution, node.getSourceCodeLocation());
    visitController.replaceCurrentBlockChildWith(
        ImmutableList.of(idSelector), false /* visitTheReplacementNodes */);
    return true;
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode node) {
    if (cssClassRenamingMap == null) {
      return;
    }
    String name = node.getPropertyName().getPropertyName();
    if (!name.startsWith("--")) {
      return;
    }
    String substitution = cssClassRenamingMap.get(name);
    if (substitution == null) {
      return;
    }
    CssPropertyValueNode value = node.getPropertyValue();
    if (value != null) {
      value = new CssPropertyValueNode(value);
    }
    CssDeclarationNode declaration = new CssDeclarationNode(
        new CssPropertyNode(substitution, node.getPropertyName().getSourceCodeLocation()), value);
    visitController.replaceCurrentBlockChildWith(ImmutableList.of(declaration), false);
    return;
  }

  @Override
  public boolean enterArgumentNode(CssValueNode value) {
    if (!(value instanceof CssLiteralNode && value.getValue().startsWith("--"))) {
      return true;
    }
    if (cssClassRenamingMap == null) {
      return true;
    }
    String substitution = cssClassRenamingMap.get(value.getValue());
    if (substitution == null) {
      return true;
    }
    CssLiteralNode literal = new CssLiteralNode(substitution, value.getSourceCodeLocation());
    visitController.replaceCurrentBlockChildWith(ImmutableList.of(literal), false);
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
