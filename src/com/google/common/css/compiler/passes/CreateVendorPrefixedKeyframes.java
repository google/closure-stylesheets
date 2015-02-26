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
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.MutatingVisitController;

/**
 * Compiler pass which duplicates @keyframes rules creating browser specific
 * prefixed form. A {@code @keyframes} rule preceded by these annotation
 * comment will be duplicated:
 * <ul>
 * <li>@gen-webkit-keyframes to generate a {@code @-webkit-keyframes} rule.
 * </li>
 * </ul>
 *
 */
public class CreateVendorPrefixedKeyframes extends DefaultTreeVisitor
    implements CssCompilerPass {
  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;

  @VisibleForTesting
  static final String GEN_WEBKIT_KEYFRAMES_COMMENT =
      "/* @gen-webkit-keyframes */";

  public CreateVendorPrefixedKeyframes(
      MutatingVisitController visitController,
      ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public void leaveKeyframesRule(CssKeyframesNode node) {
    if (node.hasComment(GEN_WEBKIT_KEYFRAMES_COMMENT)
        && node.getName().toString().equals("keyframes")) {
      CssKeyframesNode copy = new CssKeyframesNode(
          new CssLiteralNode(
              "-webkit-" + node.getName().toString(),
              node.getName().getSourceCodeLocation()),
          node);
      visitController.replaceCurrentBlockChildWith(
          Lists.newArrayList(node, copy),
          false /* visitTheReplacementNodes */);
    }
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
