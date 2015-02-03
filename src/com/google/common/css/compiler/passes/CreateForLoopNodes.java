/*
 * Copyright 2015 Google Inc.
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
import com.google.common.css.compiler.ast.CssForLoopRuleNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssLoopVariableNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.Stack;
import java.util.regex.Pattern;

/**
 * A compiler pass that replaces each {@code @for} with a {@link CssForLoopRuleNode}.
 */
public class CreateForLoopNodes extends DefaultTreeVisitor implements CssCompilerPass {

  @VisibleForTesting
  static final String SYNTAX_ERROR = "Invalid syntax for @for rule. Expected: "
      + "@for <IDENTIFIER> from <CONST|NUMBER> to <CONST|NUMBER>) [step <CONST|NUMBER>]";

  @VisibleForTesting
  static final String ILLEGAL_VARIABLE_NAME = "Illegal variable name.";

  @VisibleForTesting
  static final String OVERRIDE_VARIABLE_NAME = "Overriding existing variable name.";

  private static final String FOR_NAME = CssAtRuleNode.Type.FOR.getCanonicalName();
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$[a-zA-Z_]\\w*");
  private static final String FROM_KEYWORD = "from";
  private static final String TO_KEYWORD = "to";
  private static final String STEP_KEYWORD = "step";

  private static final int VARIABLE_INDEX = 0;
  private static final int FROM_KEYWORD_INDEX = 1;
  private static final int FROM_VALUE_INDEX = 2;
  private static final int TO_KEYOWRD_INDEX = 3;
  private static final int TO_VALUE_INDEX = 4;
  private static final int STEP_KEYOWRD_INDEX = 5;
  private static final int STEP_VALUE_INDEX = 6;

  private static final int ARGUMENT_COUNT_WITHOUT_STEP = 5;
  private static final int ARGUMENT_COUNT_WITH_STEP = 7;

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;
  private final Stack<String> variables = new Stack<>();

  private int uniqueLoopId = 0;

  public CreateForLoopNodes(MutatingVisitController visitController, ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    if (!node.getName().getValue().equals(FOR_NAME)) {
      return true;
    }

    if (!node.getType().hasBlock()) {
      reportError("@" + FOR_NAME + " with no block", node);
      return false;
    }

    if (node.getChildren().size() != ARGUMENT_COUNT_WITHOUT_STEP
        && node.getChildren().size() != ARGUMENT_COUNT_WITH_STEP) {
      reportError(SYNTAX_ERROR, node);
      return false;
    }
    if (!(node.getChildAt(VARIABLE_INDEX) instanceof CssLoopVariableNode)
        || !(node.getChildAt(FROM_KEYWORD_INDEX) instanceof CssLiteralNode)
        || !FROM_KEYWORD.equals(node.getChildAt(FROM_KEYWORD_INDEX).getValue())
        || !isValidValueNode(node.getChildAt(FROM_VALUE_INDEX))
        || !(node.getChildAt(TO_KEYOWRD_INDEX) instanceof CssLiteralNode)
        || !TO_KEYWORD.equals(node.getChildAt(TO_KEYOWRD_INDEX).getValue())
        || !isValidValueNode(node.getChildAt(TO_VALUE_INDEX))) {
      reportError(SYNTAX_ERROR, node);
      return false;
    }
    String variableName = node.getChildAt(VARIABLE_INDEX).getValue();
    if (!VARIABLE_PATTERN.matcher(variableName).matches()) {
      reportError(ILLEGAL_VARIABLE_NAME, node.getChildAt(VARIABLE_INDEX));
      return false;
    }
    if (variables.contains(variableName)) {
      reportError(OVERRIDE_VARIABLE_NAME , node.getChildAt(VARIABLE_INDEX));
      return false;
    }

    CssValueNode from = node.getChildAt(FROM_VALUE_INDEX);
    CssValueNode to = node.getChildAt(TO_VALUE_INDEX);
    CssValueNode step = new CssNumericNode("1", CssNumericNode.NO_UNITS);
    if (node.getChildren().size() == ARGUMENT_COUNT_WITH_STEP) {
      if (!(node.getChildAt(STEP_KEYOWRD_INDEX) instanceof CssLiteralNode)
          || !STEP_KEYWORD.equals(node.getChildAt(STEP_KEYOWRD_INDEX).getValue())
          || !isValidValueNode(node.getChildAt(STEP_VALUE_INDEX))) {
        reportError(SYNTAX_ERROR, node);
        return false;
      }
      step = node.getChildAt(STEP_VALUE_INDEX);
    }

    CssForLoopRuleNode loopNode = new CssForLoopRuleNode(node.getName(),
        node.getBlock(),
        node.getComments(),
        from,
        to,
        step,
        variableName,
        nextLoopId());
    loopNode.setParameters(node.getChildren());

    visitController.replaceCurrentBlockChildWith(Lists.newArrayList(loopNode), true);
    variables.push(variableName);
    return true;
  }

  @Override
  public void leaveForLoop(CssForLoopRuleNode node) {
    variables.pop();
  }

  private boolean isValidValueNode(CssValueNode node) {
    return node instanceof CssNumericNode || node instanceof CssLoopVariableNode;
  }

  private void reportError(String message, CssNode node) {
    errorManager.report(new GssError(message, node.getSourceCodeLocation()));
    visitController.removeCurrentNode();
  }

  private int nextLoopId() {
    return uniqueLoopId++;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
