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
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

/**
 * A compiler pass that transforms each upper-cased {@link CssLiteralNode} to
 * a {@link CssConstantReferenceNode}.
 *
 */
public class CreateConstantReferences extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;
  private CssNode globalScope;
  private CssNode currentScope;

  public CreateConstantReferences(MutatingVisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public void leaveValueNode(CssValueNode node) {
    if (!(node instanceof CssLiteralNode)
        || !CssConstantReferenceNode.isDefinitionReference(node.getValue())) {
      return;
    }
    CssConstantReferenceNode ref =
        new CssConstantReferenceNode(node.getValue(),
            node.getSourceCodeLocation());
    visitController.replaceCurrentBlockChildWith(ImmutableList.of(ref), false);
    CssNode scope = determineScope(ref);
    ref.setScope(scope);
  }

  @Override
  public void leaveArgumentNode(CssValueNode node) {
    leaveValueNode(node);
  }

  @Override
  public boolean enterTree(CssRootNode node) {
    globalScope = node;
    return true;
  }

  @Override
  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    currentScope = node;
    return true;
  }

  @Override
  public void leaveMixinDefinition(CssMixinDefinitionNode node) {
    currentScope = globalScope;
  }

  /**
   * Returns the scope of the given reference.
   */
  private CssNode determineScope(CssConstantReferenceNode ref) {
    if (currentScope instanceof CssMixinDefinitionNode) {
      CssMixinDefinitionNode mixinDef = (CssMixinDefinitionNode) currentScope;
      // Search for a matching argument.
      for (CssValueNode arg : mixinDef.getArguments().getChildren()) {
        if (arg.getValue().equals(ref.getValue())) {
          return currentScope;
        }
      }
    }
    return globalScope;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
