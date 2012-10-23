/*
 * Copyright 2011 Google Inc.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssAtRuleNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssMixinNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;

/**
 * A compiler pass that transforms matching {@link CssUnknownAtRuleNode} instances
 * into mixins or mixin definitions.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CreateMixins extends DefaultTreeVisitor
    implements CssCompilerPass {

  @VisibleForTesting
  static final String NO_BLOCK_ERROR_MESSAGE =
    "This @-rule has to have a block";
  @VisibleForTesting
  static final String BLOCK_ERROR_MESSAGE =
    "This @-rule is not allowed to have a block";
  @VisibleForTesting
  static final String ONLY_DECLARATION_BLOCK_ERROR_MESSAGE =
    "Only declaration blocks are allowed for this @-rule";
  @VisibleForTesting
  static final String INVALID_PARAMETERS_ERROR_MESSAGE =
    "This @-rule has invalid parameters";

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;

  public CreateMixins(MutatingVisitController visitController,
      ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    String mixinName = CssAtRuleNode.Type.MIXIN.getCanonicalName();
    String mixinDefinitionName = CssAtRuleNode.Type.DEFMIXIN.getCanonicalName();

    if (node.getName().getValue().equals(mixinName)) {
      createMixin(node);
    } else if (node.getName().getValue().equals(mixinDefinitionName)) {
      createMixinDefinition(node);
    }
    return true;
  }

  private void createMixin(CssUnknownAtRuleNode node) {
    if (node.getBlock() != null) {
      reportError(BLOCK_ERROR_MESSAGE, node);
      return;
    }
    List<CssValueNode> params = node.getParameters();
    if (params.size() != 1 || !(params.get(0) instanceof CssFunctionNode)) {
      reportError(INVALID_PARAMETERS_ERROR_MESSAGE, node);
      return;
    }
    CssFunctionNode function = (CssFunctionNode) params.get(0);
    String name = function.getFunctionName();
    CssFunctionArgumentsNode arguments = function.getArguments();
    CssMixinNode mixin = new CssMixinNode(name,
        arguments, node.getSourceCodeLocation());
    mixin.setComments(node.getComments());
    visitController.replaceCurrentBlockChildWith(
        Lists.newArrayList(mixin),
        true /* visitTheReplacementNodes */);
  }

  private void createMixinDefinition(CssUnknownAtRuleNode node) {
    if (node.getBlock() == null) {
      reportError(NO_BLOCK_ERROR_MESSAGE, node);
      return;
    }
    if (!(node.getBlock() instanceof CssDeclarationBlockNode)) {
      reportError(ONLY_DECLARATION_BLOCK_ERROR_MESSAGE, node);
      return;
    }
    List<CssValueNode> params = node.getParameters();
    if (params.size() != 1 || !(params.get(0) instanceof CssFunctionNode)) {
      reportError(INVALID_PARAMETERS_ERROR_MESSAGE, node);
      return;
    }
    CssDeclarationBlockNode block = (CssDeclarationBlockNode) node.getBlock();
    CssFunctionNode function = (CssFunctionNode) params.get(0);
    String name = function.getFunctionName();
    CssFunctionArgumentsNode arguments = function.getArguments();
    CssMixinDefinitionNode mixinDefinition = new CssMixinDefinitionNode(name,
        arguments, block, node.getSourceCodeLocation());
    mixinDefinition.setComments(node.getComments());
    visitController.replaceCurrentBlockChildWith(
        Lists.newArrayList(mixinDefinition),
        true /* visitTheReplacementNodes */);
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
