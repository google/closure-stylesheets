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

import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssForLoopRuleNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Evaluates integer constants provided typically through command line. These constants were
 * introduced to specify for loop range, but they can also be used as value nodes.
 */
public class EvaluateCompileConstants extends DefaultTreeVisitor implements CssCompilerPass {

  private final MutatingVisitController visitController;
  private final Map<String, Integer> compileConstants;

  public EvaluateCompileConstants(
      MutatingVisitController visitController, Map<String, Integer> compileConstants) {
    this.visitController = visitController;
    this.compileConstants = compileConstants;
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    CssNumericNode newNode = updateValueNode(node);
    if (newNode != null) {
      visitController.replaceCurrentBlockChildWith(ImmutableList.of(newNode), false);
    }
    return true;
  }

  @Override
  public boolean enterArgumentNode(CssValueNode node) {
    return enterValueNode(node);
  }

  @Override
  public boolean enterForLoop(CssForLoopRuleNode node) {
    CssNumericNode from = updateValueNode(node.getFrom());
    if (from != null) {
      node.setFrom(from);
    }
    CssNumericNode to = updateValueNode(node.getTo());
    if (to != null) {
      node.setTo(to);
    }
    CssNumericNode step = updateValueNode(node.getStep());
    if (step != null) {
      node.setStep(step);
    }
    return true;
  }

  /**
   * Check if the value node refers to a compile constant and if so returns a numeric node with the
   * replaced value.
   */
  @Nullable
  private CssNumericNode updateValueNode(CssValueNode node) {
    Integer value = compileConstants.get(node.getValue());
    if (value != null) {
      return new CssNumericNode(
          String.valueOf(value), CssNumericNode.NO_UNITS, node.getSourceCodeLocation());
    }
    return null;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
