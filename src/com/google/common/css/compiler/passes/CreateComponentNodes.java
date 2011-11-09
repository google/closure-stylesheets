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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssAtRuleNode;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssComponentNode;
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
 * A compiler pass that transforms each well-formed {@code @component} or
 * {@code @abstract_component} {@link CssUnknownAtRuleNode} into a {@link CssComponentNode}.
 *
 * The syntax for components is as follows:
 * <code>
 * {@literal @}(abstract_)?component LITERAL (extends LITERAL)? { ... }
 * </code>
 *
 */
public class CreateComponentNodes extends DefaultTreeVisitor
    implements CssCompilerPass {

  private static final String componentName = CssAtRuleNode.Type.COMPONENT.getCanonicalName();
  private static final String abstractComponentName =
      CssAtRuleNode.Type.ABSTRACT_COMPONENT.getCanonicalName();

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;

  public CreateComponentNodes(MutatingVisitController visitController, ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    String name = node.getName().getValue();
    if (name.equals(componentName) || name.equals(abstractComponentName)) {
      if (!node.getType().hasBlock()) {
        reportError("@" + name + " without block", node);
        return;
      }
      List<CssValueNode> params = node.getParameters();
      int paramSize = params.size();
      if (paramSize == 0) {
        reportError("@" + name + " without name", node);
        return;
      }
      CssNode nameNode = params.get(0);
      if (!(nameNode instanceof CssLiteralNode)) {
        reportError("@" + name + " without a valid literal as name", node);
        return;
      }
      CssLiteralNode parentNode = null;
      if (paramSize == 1) {
        // OK
      } else if (paramSize == 3) {
        CssNode extendNode = params.get(1);
        if (!(extendNode instanceof CssLiteralNode)
            || !((CssLiteralNode) extendNode).getValue().equals("extends")) {
          reportError("@" + name + " with invalid second parameter (expects 'extends')", node);
          return;
        }
        CssNode parentCssNode = params.get(2);
        if (!(parentCssNode instanceof CssLiteralNode)) {
          reportError("@" + name + " with invalid literal as parent name", node);
          return;
        }
        parentNode = (CssLiteralNode) parentCssNode;
      } else {
        reportError("@" + name + " with invalid number of parameters", node);
        return;
      }
      Preconditions.checkState(node.getBlock() instanceof CssBlockNode);
      CssComponentNode comp = new CssComponentNode(
          (CssLiteralNode) nameNode,
          parentNode,
          name.equals(abstractComponentName),
          (CssBlockNode) node.getBlock());
      comp.setComments(node.getComments());
      comp.setSourceCodeLocation(node.getSourceCodeLocation());
      visitController.replaceCurrentBlockChildWith(Lists.newArrayList((CssNode) comp), false);
    }
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
