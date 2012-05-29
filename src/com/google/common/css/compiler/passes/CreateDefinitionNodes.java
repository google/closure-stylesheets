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

import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssAtRuleNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;

/**
 * A compiler pass that transforms each well-formed {@code @def}
 * {@link CssUnknownAtRuleNode} to a {@link CssDefinitionNode}.
 *
 */
public class CreateDefinitionNodes extends DefaultTreeVisitor
    implements CssCompilerPass {

  private static final String defName =
      CssAtRuleNode.Type.DEF.getCanonicalName();
  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;

  public CreateDefinitionNodes(
      MutatingVisitController visitController, ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    if (node.getName().getValue().equals(defName)) {
      if (node.getType().hasBlock()) {
        reportError("@" + defName + " with block", node);
        return false;
      }
      List<CssValueNode> params = node.getParameters();
      if (params.isEmpty()) {
        reportError("@" + defName + " without name", node);
        return false;
      }
      CssNode nameNode = params.get(0);
      if (!(nameNode instanceof CssLiteralNode)) {
        reportError("@" + defName + " without a valid literal as name", node);
        return false;
      }

      CssLiteralNode defNameNode = (CssLiteralNode) nameNode;
      String defName = defNameNode.getValue();
      if (!CssConstantReferenceNode.isDefinitionReference(defName)) {
        // TODO(nicksantos): Update ErrorManager to support warnings as
        // well as errors.
        GssError error = new GssError(
            String.format(
                "WARNING for invalid @def name %s. We will ignore this.",
                defName),
            defNameNode.getSourceCodeLocation());
        System.err.println(error.format());

        // Create a definition node anyway, so that the compiler doesn't crash.
      }
      CssDefinitionNode def = new CssDefinitionNode(
          params.subList(1, params.size()),
          (CssLiteralNode) nameNode,
          node.getComments(),
          node.getSourceCodeLocation());
      visitController.replaceCurrentBlockChildWith(
          Lists.newArrayList((CssNode) def), false);
      return false;
    }
    return true;
  }

  private void reportError(String message, CssNode node) {
    errorManager.report(new GssError(message, node.getSourceCodeLocation()));
    visitController.removeCurrentNode();
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
