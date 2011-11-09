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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.ast.Proxiable;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Compiler pass that replaces the constant references with the right values.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class ReplaceConstantReferences extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;
  private final ConstantDefinitions constantDefinitions;
  private final boolean removeDefs;
  private final ErrorManager errorManager;
  private final boolean allowUndefinedConstants;

  /**
   * This constructor is only used by other projects.
   * It should not be used in new code.
   */
  public ReplaceConstantReferences(CssTree tree,
      @Nullable ConstantDefinitions constantDefinitions) {
    this(tree, constantDefinitions, true /* removeDefs */,
        null /* errorManager*/, true /* allowUndefinedConstants */);
  }

  /**
   * This constructor is only used by other projects.
   * It should not be used in new code.
   */
  public ReplaceConstantReferences(CssTree tree,
      @Nullable ConstantDefinitions constantDefinitions, boolean removeDefs) {
    this(tree, constantDefinitions, removeDefs, null /* errorManager*/,
        true /* allowUndefinedConstants */);
  }

  public ReplaceConstantReferences(CssTree tree,
      @Nullable ConstantDefinitions constantDefinitions, boolean removeDefs,
      ErrorManager errorManager, boolean allowUndefinedConstants) {
    Preconditions.checkArgument(allowUndefinedConstants
        || errorManager != null);
    this.visitController = tree.getMutatingVisitController();
    this.constantDefinitions = constantDefinitions;
    this.removeDefs = removeDefs;
    this.errorManager = errorManager;
    this.allowUndefinedConstants = allowUndefinedConstants;
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    if (removeDefs) {
      visitController.removeCurrentNode();
    }
    return !removeDefs;
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    if (node instanceof CssConstantReferenceNode) {
      replaceConstantReference((CssConstantReferenceNode) node);
    }
    return true;
  }

  @Override
  public boolean enterArgumentNode(CssValueNode node) {
    return enterValueNode(node);
  }

  @VisibleForTesting
  void replaceConstantReference(CssConstantReferenceNode node) {
    if (constantDefinitions == null) {
      return;
    }

    CssDefinitionNode constantNode =
        constantDefinitions.getConstantDefinition(node.getValue());

    if (constantNode == null) {
      if (!allowUndefinedConstants) {
        errorManager.report(new GssError("GSS constant not defined: "
            + node.getValue(), node.getSourceCodeLocation()));
      }
      return;
    }

    List<CssValueNode> params = constantNode.getParameters();
    List<CssValueNode> temp = Lists.newArrayListWithCapacity(params.size());
    for (CssValueNode n : params) {
      if (n instanceof Proxiable<?>) {
        @SuppressWarnings("unchecked")
        Proxiable<CssValueNode> proxiable = (Proxiable<CssValueNode>) n;
        temp.add(proxiable.createProxy());
      } else {
        temp.add(n.deepCopy());
      }
    }
    // The composite value is used so that we can store nodes with different
    // separators in one another. visitController.replaceCurrentBlockChildWith
    // will unwrap the value if it can in the current context.
    CssCompositeValueNode tempNode = new CssCompositeValueNode(
        temp, CssCompositeValueNode.Operator.SPACE,
        constantNode.getSourceCodeLocation());
    visitController.replaceCurrentBlockChildWith(
        Lists.newArrayList(tempNode), true);
  }

  @Override
  public void runPass() {
    // Replace the original custom function node with a proxy to stop
    // propagation of changes of the original node to nodes that proxy it.
    if (constantDefinitions != null) {
      for (String constantName : constantDefinitions.getConstantsNames()) {
        CssDefinitionNode node =
            constantDefinitions.getConstantDefinition(constantName);
        List<CssValueNode> params = node.getParameters();
        for (int i = 0; i < params.size(); i++) {
          CssValueNode n = params.get(i);
          if (n instanceof Proxiable<?>) {
            @SuppressWarnings("unchecked")
            Proxiable<CssValueNode> proxiable = (Proxiable<CssValueNode>) n;
            node.replaceChildAt(i, ImmutableList.of(proxiable.createProxy()));
          }
        }
      }
    }
    visitController.startVisit(this);
  }
}
