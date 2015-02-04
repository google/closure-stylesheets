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

package com.google.common.css.compiler.ast;

import com.google.common.css.SourceCodeLocation;

import java.util.List;

import javax.annotation.Nullable;


/**
 * A node representing a {@code @for} loop rule.
 */
public class CssForLoopRuleNode extends CssAtRuleNode {

  public static final String VARIABLE_PREFIX = "$";

  private CssValueNode from;
  private CssValueNode to;
  private CssValueNode step;
  private final String variableName;
  private final int loopId;

  public CssForLoopRuleNode(CssLiteralNode name,
      CssAbstractBlockNode block,
      @Nullable List<CssCommentNode> comments,
      CssValueNode from,
      CssValueNode to,
      CssValueNode step,
      String variableName,
      int loopId,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(CssAtRuleNode.Type.FOR, name, block, comments);
    this.from = from;
    this.to = to;
    this.step = step;
    this.variableName = variableName;
    this.loopId = loopId;
    setSourceCodeLocation(sourceCodeLocation);
  }

  public CssForLoopRuleNode(CssForLoopRuleNode node) {
    super(node);
    this.from = node.from.deepCopy();
    this.to = node.to.deepCopy();
    this.step = node.step.deepCopy();
    this.variableName = node.variableName;
    this.loopId = node.loopId;
  }

  public CssValueNode getFrom() {
    return from;
  }

  public CssValueNode getTo() {
    return to;
  }

  public CssValueNode getStep() {
    return step;
  }

  public void setFrom(CssValueNode value) {
    from = value;
  }

  public void setTo(CssValueNode value) {
    to = value;
  }

  public void setStep(CssValueNode value) {
    step = value;
  }

  public int getFromValue() {
    return getNumberValue(from);
  }

  public int getToValue() {
    return getNumberValue(to);
  }

  public int getStepValue() {
    return getNumberValue(step);
  }

  private int getNumberValue(CssValueNode node) {
    if (node instanceof CssNumericNode) {
      return Integer.parseInt(((CssNumericNode) node).getNumericPart());
    } else if (node instanceof CssLoopVariableNode) {
      throw new RuntimeException("For loop variable is used before it was evaluated");
    } else {
      throw new RuntimeException("Unsupported value type for loop variable: " + node.getClass());
    }
  }

  public String getVariableName() {
    return variableName;
  }

  public int getLoopId() {
    return loopId;
  }

  @Override
  public CssBlockNode getBlock() {
    return (CssBlockNode) super.getBlock();
  }

  @Override
  public CssNode deepCopy() {
    return new CssForLoopRuleNode(this);
  }
}
