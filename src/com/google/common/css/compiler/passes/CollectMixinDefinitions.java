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
import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Compiler pass that collects all mixin definitions and afterwards provides a
 * mapping of mixin definition names to {@link CssMixinDefinitionNode}s.
 *
 * <p>In addition, this pass checks that no two definitions with the same name
 * exist and that no definition exits that has arguments with the same name
 * exist.
 *
 * <p>{@link CreateConstantReferences} has to run before.
 *
 */
public class CollectMixinDefinitions extends DefaultTreeVisitor
    implements CssCompilerPass {
  @VisibleForTesting
  static final String DUPLICATE_MIXIN_DEFINITION_NAME_ERROR_MESSAGE =
    "A mixin definition name occured more than once";
  @VisibleForTesting
  static final String DUPLICATE_ARGUMENT_NAME_ERROR_MESSAGE =
    "The mixin definition has arguments with the same name";
  @VisibleForTesting
  static final String INVALID_ARGUMENT_ERROR_MESSAGE =
    "The mixin definition has invalid arguments";
  @VisibleForTesting
  static final String INVALID_BLOCK_ERROR_MESSAGE =
    "The mixin definition is not placed on the top level";

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;
  private final Map<String, CssMixinDefinitionNode> definitions;

  public CollectMixinDefinitions(MutatingVisitController visitController,
      ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
    this.definitions = new HashMap<String, CssMixinDefinitionNode>();
  }

  @Override
  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    if (!node.getParent().getParent().getClass().equals(CssRootNode.class)) {
      errorManager.report(new GssError(INVALID_BLOCK_ERROR_MESSAGE,
          node.getSourceCodeLocation()));
      return false;
    }
    if (definitions.containsKey(node.getDefinitionName())) {
      errorManager.report(new GssError(
          DUPLICATE_MIXIN_DEFINITION_NAME_ERROR_MESSAGE,
          node.getSourceCodeLocation()));
      return false;
    }
    if (!checkArguments(node)) {
      return false;
    }
    definitions.put(node.getDefinitionName(), node);
    return true;
  }

  @Override
  public void leaveMixinDefinition(CssMixinDefinitionNode node) {
    visitController.removeCurrentNode();
  }

  /**
   * Checks that every argument has a unique name and that the argument only
   * consists of capital letters and digits.
   */
  private boolean checkArguments(CssMixinDefinitionNode node) {
    Set<String> arguments = Sets.newHashSetWithExpectedSize(
        node.getArguments().numChildren());
    for (CssValueNode arg : node.getArguments().getChildren()) {
      boolean isSeparator = ",".equals(arg.getValue());
      if (!isSeparator
          && !CssConstantReferenceNode.isDefinitionReference(arg.getValue())) {
        errorManager.report(new GssError(INVALID_ARGUMENT_ERROR_MESSAGE,
            node.getSourceCodeLocation()));
        return false;
      }
      if (arguments.contains(arg.getValue())) {
        errorManager.report(new GssError(DUPLICATE_ARGUMENT_NAME_ERROR_MESSAGE,
            node.getSourceCodeLocation()));
        return false;
      }
      if (!isSeparator) {
        arguments.add(arg.getValue());
      }
    }
    return true;
  }

  public Map<String, CssMixinDefinitionNode> getDefinitions() {
    return definitions;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
