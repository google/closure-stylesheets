/*
 * Copyright 2012 Google Inc.
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

import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNodesListNode;
import com.google.common.css.compiler.ast.CssValueNode;

/**
 * Build up an s-expression corresponding to the AST for
 * debugging purposes.
 */
public class SExprPrinter extends UniformVisitor {

  public final StringBuilder sb;
  public final boolean includeHashCodes;

  public SExprPrinter(StringBuilder sb, boolean includeHashCodes) {
    this.sb = sb;
    this.includeHashCodes = includeHashCodes;
  }

  public SExprPrinter(StringBuilder sb) {
    this(sb, true);
  }

  public SExprPrinter() {
    this(new StringBuilder());
  }

  @Override
  public void enter(CssNode node) {
    if (includeHashCodes) {
      sb.append(
          String.format("(%s@%d ", node.getClass().getName(), node.hashCode()));
    } else {
      sb.append(
          String.format("(%s ", node.getClass().getName()));
    }
  }

  private void trim(StringBuilder sb, String waste) {
    if (sb.length() == 0) {
      return;
    }
    int expectedPosition = sb.length() - waste.length();
    if (sb.indexOf(waste, expectedPosition) != -1) {
      sb.delete(expectedPosition, sb.length());
    }
  }

  @Override
  public void leave(CssNode node) {
    trim(sb, " ");
    sb.append(")");
  }

  /** Called between adjacent nodes in a media type list */
  @Override
  public boolean enterMediaTypeListDelimiter(
      CssNodesListNode<? extends CssNode> node) {
    super.enterMediaTypeListDelimiter(node);
    // this very special state does not represent a node
    sb.append("(MediaTypeListDelimiter");
    return true;
  }

  /** Called between adjacent nodes in a media type list */
  @Override
  public void leaveMediaTypeListDelimiter(
      CssNodesListNode<? extends CssNode> node) {
    // this very special state does not represent a node
    sb.append(")");
    super.leaveMediaTypeListDelimiter(node);
  }

  /** Called between values in a {@code CssCompositeValueNode} */
  @Override
  public boolean enterCompositeValueNodeOperator(
      CssCompositeValueNode parent) {
    super.enterCompositeValueNodeOperator(parent);
    // this very special state does not represent a node
    sb.append("(CompositeValueNodeOperator ");
    sb.append(parent.getOperator().name());
    return true;
  }

  /** Called between values in a {@code CssCompositeValueNode} */
  @Override
  public void leaveCompositeValueNodeOperator(CssCompositeValueNode parent) {
    sb.append(")");
    super.leaveCompositeValueNodeOperator(parent);
  }

  @Override
  public boolean enterValueNode(CssValueNode n) {
    super.enterValueNode(n);
    sb.append(n.toString() + " ");
    return true;
  }

  @Override
  public void leaveValueNode(CssValueNode n) {
    super.leaveValueNode(n);
  }

  public static String print(CssTree t) {
    StringBuilder sb = new StringBuilder();
    t.getVisitController().startVisit(new SExprPrinter(sb));
    return sb.toString();
  }

  public static String print(boolean includeHashCodes, CssTree t) {
    StringBuilder sb = new StringBuilder();
    t.getVisitController().startVisit(new SExprPrinter(sb, includeHashCodes));
    return sb.toString();
  }

  public static String print(CssNode n) {
    StringBuilder sb = new StringBuilder();
    n.getVisitController().startVisit(new SExprPrinter(sb));
    return sb.toString();
  }

  public static String print(boolean includeHashCodes, CssNode n) {
    StringBuilder sb = new StringBuilder();
    n.getVisitController().startVisit(new SExprPrinter(sb, includeHashCodes));
    return sb.toString();
  }
}
