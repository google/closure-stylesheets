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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.CssAtRuleNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssProvideNode;
import com.google.common.css.compiler.ast.CssRequireNode;
import com.google.common.css.compiler.ast.CssStringNode;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;
import java.util.Set;

/**
 * {@link CheckDependencyNodes} ensures that {@code @provide} nodes appear
 * before {@code @require} nodes.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class CheckDependencyNodes extends DefaultTreeVisitor
    implements CssCompilerPass{

  private static final String provideName = CssAtRuleNode.Type.PROVIDE
      .getCanonicalName();
  private static final String requireName = CssAtRuleNode.Type.REQUIRE
      .getCanonicalName();
  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;

  /**
   * A linked collection is used so the dependencies can be iterated in the
   * order in which they were provided.
   */
  private Set<String> provides = Sets.newLinkedHashSet();

  public CheckDependencyNodes(MutatingVisitController visitController,
      ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    String atRuleName = node.getName().getValue();
    CssNode dependencyNode;
    if (provideName.equals(atRuleName)) {
      CssLiteralNode arg = extractArgument(node);
      if (arg != null) {
        dependencyNode = createProvideNode(node, arg);
      } else {
        return false;
      }
    } else if (requireName.equals(atRuleName)) {
      CssLiteralNode arg = extractArgument(node);
      if (arg != null) {
        dependencyNode = createRequireNode(node, arg);
      } else {
        return false;
      }
    } else {
      return true;
    }

    visitController.replaceCurrentBlockChildWith(
        Lists.newArrayList(dependencyNode),
        false /* visitTheReplacementNodes */);
    return true;
  }

  private CssLiteralNode extractArgument(CssUnknownAtRuleNode node) {
    String atRuleName = node.getName().getValue();
    if (node.getType().hasBlock()) {
      reportError("@" + atRuleName + " with block", node);
      return null;
    }
    List<CssValueNode> params = node.getParameters();
    if (params.isEmpty()) {
      reportError("@" + atRuleName + " without name", node);
      return null;
    }
    CssNode nameNode = params.get(0);
    if (!(nameNode instanceof CssStringNode)) {
      reportError("@" + atRuleName + " without a quoted string as name", node);
      return null;
    }
    CssStringNode nameStringNode = (CssStringNode) nameNode;
    return new CssLiteralNode(nameStringNode.getValue());
  }

  private CssProvideNode createProvideNode(CssUnknownAtRuleNode node,
      CssLiteralNode provideArgument) {
    //TODO(user): Make CssProvideNode & CssRequireNode consistent with other at-rule nodes.
    // Pass the @name as CssLiteralNode and the namespace as CssValueNode.
    CssProvideNode provideNode = new CssProvideNode(
        provideArgument,
        node.getComments(),
        node.getSourceCodeLocation());
    String provide = provideNode.getProvide();
    if (provides.contains(provide)) {
      reportError("Duplicate provide for: " + provide, node);
    }
    provides.add(provide);
    return provideNode;
  }

  private CssRequireNode createRequireNode(CssUnknownAtRuleNode node,
      CssLiteralNode requireArgument) {
    CssRequireNode requireNode = new CssRequireNode(
        requireArgument,
        node.getComments(),
        node.getSourceCodeLocation());
    String require = requireNode.getRequire();
    if (!provides.contains(require)) {
      reportError("Missing provide for: " + require, node);
    }
    return requireNode;
  }

  private void reportError(String message, CssNode node) {
    errorManager.report(new GssError(message, node.getSourceCodeLocation()));
    visitController.removeCurrentNode();
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

  @VisibleForTesting
  List<String> getProvidesInOrder() {
    return ImmutableList.copyOf(provides);
  }
}
