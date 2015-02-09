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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssForLoopRuleNode;
import com.google.common.css.compiler.ast.CssLoopVariableNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * A pass that updates for loop variables with a given value.
 *
 * <p>The pass is expected to run multiple times on the same loop, where each time the loop variable
 * is replaced with a different value, according to the loop iteration.
 */
class LoopVariableReplacementPass extends DefaultTreeVisitor implements CssCompilerPass {

  private final String variable;
  private final int value;
  private final Set<String> loopDefinitions;
  private final MutatingVisitController visitController;
  private final int loopId;

  public LoopVariableReplacementPass(
      String variable,
      int value,
      Set<String> loopDefinitions,
      MutatingVisitController visitController,
      int loopId) {
    this.variable = variable;
    this.value = value;
    this.loopDefinitions = loopDefinitions;
    this.visitController = visitController;
    this.loopId = loopId;
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    CssNumericNode newNode = updateValueNode(node);
    if (newNode != null) {
      visitController.replaceCurrentBlockChildWith(ImmutableList.of(newNode), true);
    }
    return true;
  }

  @Override
  public boolean enterClassSelector(CssClassSelectorNode classSelector) {
    String[] parts = classSelector.getRefinerName().split("-");
    for (int i = 0; i < parts.length; ++i) {
      parts[i] = replaceVariable(parts[i]);
    }
    String refinerName = Joiner.on("-").join(parts);
    if (!refinerName.equals(classSelector.getRefinerName())) {
      visitController.replaceCurrentBlockChildWith(
          ImmutableList.of(new CssClassSelectorNode(
              refinerName,
              classSelector.isComponentScoped(),
              classSelector.getSourceCodeLocation())),
          true);
    }
    return true;
  }

  @Override
  public boolean enterArgumentNode(CssValueNode node) {
    CssNumericNode newNode = updateValueNode(node);
    if (newNode != null) {
      visitController.replaceCurrentBlockChildWith(ImmutableList.of(newNode), true);
    }
    return true;
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    CssValueNode definitionName = node.getName();
    definitionName.setValue(replaceDefinition(definitionName.getValue()));
    return true;
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
   * If possible updates the given node with the right value. In the case the node type has to be
   * replaced, it returns the new node.
   */
  @Nullable
  private CssNumericNode updateValueNode(CssValueNode node) {
    if (node instanceof CssConstantReferenceNode) {
      if (loopDefinitions.contains(node.getValue())) {
        node.setValue(replaceDefinition(node.getValue()));
      }
    } else if (node instanceof CssLoopVariableNode) {
      CssLoopVariableNode variableNode = (CssLoopVariableNode) node;
      if (needsReplacement(variableNode.getValue())) {
        return new CssNumericNode(
            String.valueOf(value), CssNumericNode.NO_UNITS, node.getSourceCodeLocation());
      }
    }
    return null;
  }

  private boolean needsReplacement(String identifier) {
    return identifier.equals(variable);
  }

  private String replaceVariable(String identifier) {
    return needsReplacement(identifier) ? String.valueOf(value) : identifier;
  }

  private String replaceDefinition(String definiton) {
    return definiton + "__" + "LOOP" + loopId + "__" + value;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
