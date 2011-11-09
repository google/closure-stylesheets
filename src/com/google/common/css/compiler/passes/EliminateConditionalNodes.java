/*
 * Copyright 2008 Google Inc.
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

import com.google.common.css.compiler.ast.CssAtRuleNode.Type;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.Set;

/**
 * A compiler pass that eliminates the conditional blocks for which the boolean
 * expression does not evaluate to true.
 *
 */
public class EliminateConditionalNodes extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;

  private Set<String> trueConditions;

  public EliminateConditionalNodes(MutatingVisitController visitController,
      Set<String> trueConditions) {
    this.visitController = visitController;
    this.trueConditions = trueConditions;
  }

  @Override
  public boolean enterConditionalBlock(CssConditionalBlockNode block) {

    int i = 0;
    for (CssConditionalRuleNode currentCondtional : block.childIterable()) {

      if (currentCondtional.getType() == Type.ELSE) {
        // assert i == block.numChildren() - 1;
        // TODO(dgajda): Optimize by using immutable children list or an
        //     iterable.
        visitController.replaceCurrentBlockChildWith(
            currentCondtional.getBlock().getChildren(), true);
        return true;
      }

      BooleanExpressionEvaluator evaluator = new BooleanExpressionEvaluator(
          currentCondtional.getCondition(), trueConditions);
      CssBooleanExpressionNode result = evaluator.evaluate();
      if (CssBooleanExpressionNode.Type.TRUE_CONSTANT.equals(
          result.getValue())) {
        visitController.replaceCurrentBlockChildWith(
            currentCondtional.getBlock().getChildren(), true);
        return true;
      }

      i++;
    }

    // No ELSE was encountered and nothing was true.
    visitController.removeCurrentNode();
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
