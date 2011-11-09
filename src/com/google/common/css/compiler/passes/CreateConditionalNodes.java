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
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssAtRuleNode;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;
import java.util.Stack;

/**
 * A compiler pass that replaces each {@code @if}, {@code @elseif}, and
 * {@code @else} {@link CssUnknownAtRuleNode} with appropriate conditional
 * nodes ({@link CssConditionalBlockNode} and {@link CssConditionalRuleNode}).
 *
 */
public class CreateConditionalNodes extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;
  private Stack<CssConditionalBlockNode> stack =
      new Stack<CssConditionalBlockNode>();
  private CssConditionalBlockNode activeBlockNode = null;

  private static final String ifName = CssAtRuleNode.Type.IF.getCanonicalName();
  private static final String elseifName = CssAtRuleNode.Type.ELSEIF.getCanonicalName();
  private static final String elseName = CssAtRuleNode.Type.ELSE.getCanonicalName();

  public CreateConditionalNodes(MutatingVisitController visitController,
                                ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    String name = node.getName().getValue();
    if (name.equals(ifName)) {
      CssConditionalBlockNode condBlock = new CssConditionalBlockNode(node.getComments());
      condBlock.setSourceCodeLocation(node.getSourceCodeLocation());
      stack.push(condBlock);
    } else if (name.equals(elseifName) || name.equals(elseName)) {
      if (activeBlockNode == null) {
        errorManager.report(new GssError("@" + name + " without previous @" + ifName,
                                         node.getSourceCodeLocation()));
        visitController.removeCurrentNode();
        return false;
      }
      stack.push(activeBlockNode);
    }
    activeBlockNode = null;
    return true;
  }

  @Override
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    String name = node.getName().getValue();
    if (name.equals(ifName)
        || name.equals(elseifName)
        || name.equals(elseName)) {
      activeBlockNode = stack.pop();
      CssConditionalRuleNode conditionalNode = createConditionalRuleNode(node, name);
      activeBlockNode.addChildToBack(conditionalNode);
      updateLocation(activeBlockNode);
      if (name.equals(ifName)) {
        visitController.replaceCurrentBlockChildWith(
            Lists.newArrayList((CssNode) activeBlockNode), false);
      } else {
        visitController.removeCurrentNode();
        if (name.equals(elseName)) {
          activeBlockNode = null;
        }
      }
    }
  }

  @Override
  public boolean enterRuleset(CssRulesetNode node) {
    activeBlockNode = null;
    return false;
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    activeBlockNode = null;
    return true;
  }

  private CssConditionalRuleNode createConditionalRuleNode(
      CssUnknownAtRuleNode node, String name) {
    Preconditions.checkState(node.getBlock() == null
        || node.getBlock() instanceof CssBlockNode);
    CssBlockNode block = (CssBlockNode) node.getBlock();
    if (block == null) {
      errorManager.report(new GssError("@" + name + " without block",
                                       node.getSourceCodeLocation()));
    }

    List<CssValueNode> params = node.getParameters();
    CssBooleanExpressionNode condition = null;
    if (!name.equals(elseName)) {
      if (!params.isEmpty()) {
        if (params.size() > 1) {
          errorManager.report(new GssError("@" + name + " with too many parameters",
                                           node.getSourceCodeLocation()));
        }
        CssValueNode param = params.get(0);
        if (param instanceof CssBooleanExpressionNode) {
          condition = (CssBooleanExpressionNode) param;
        } else {
          condition = new CssBooleanExpressionNode(CssBooleanExpressionNode.Type.CONSTANT,
                                                   param.getValue(), param.getSourceCodeLocation());
        }
      } else {
        errorManager.report(new GssError("@" + name + " without condition",
                                         node.getSourceCodeLocation()));
      }
    } else {
      if (!params.isEmpty()) {
        errorManager.report(new GssError("@" + elseName + " with too many parameters",
                                         node.getSourceCodeLocation()));
      }
    }

    CssConditionalRuleNode rulenode = new CssConditionalRuleNode(
        CssAtRuleNode.Type.valueOf(name.toUpperCase()), node.getName(), condition, block);
    rulenode.setComments(node.getComments());
    rulenode.setSourceCodeLocation(node.getSourceCodeLocation());
    return rulenode;
  }

  /**
   * Assigns a new SourceCodeLocation to the BlockNode which starts at the
   * the BlockNode's first child's beginning location point and ends at the
   * last child's ending location point.
   */
  private void updateLocation(CssConditionalBlockNode blockNode){
    SourceCodeLocation firstLocation = blockNode.getSourceCodeLocation();
    SourceCodeLocation lastLocation = blockNode.getLastChild().getSourceCodeLocation();
    SourceCodeLocation mergedLocation = new SourceCodeLocation(
        firstLocation.getSourceCode(),
        firstLocation.getBeginCharacterIndex(),
        firstLocation.getBeginLineNumber(),
        firstLocation.getBeginIndexInLine(),
        lastLocation.getEndCharacterIndex(),
        lastLocation.getEndLineNumber(),
        lastLocation.getEndIndexInLine());
    blockNode.setSourceCodeLocation(mergedLocation);
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
