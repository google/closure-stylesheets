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

import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;
import javax.annotation.Nullable;

/** A compact-printer for {@link CssTree} instances. */
public class CompactPrinter extends CodePrinter {

  private String compactedPrintedString = null;

  public CompactPrinter(
      CssNode subtree, @Nullable CodeBuffer buffer, @Nullable GssSourceMapGenerator generator) {
    super(subtree.getVisitController(), buffer, generator);
  }

  public CompactPrinter(CssNode subtree, @Nullable CodeBuffer buffer) {
    this(subtree, buffer, null /* generator */);
  }

  public CompactPrinter(CssNode subtree) {
    this(subtree, null /* buffer */);
  }

  public CompactPrinter(
      CssTree tree, @Nullable CodeBuffer buffer, @Nullable GssSourceMapGenerator generator) {
    super(tree.getVisitController(), buffer, generator);
  }

  public CompactPrinter(CssTree tree, CodeBuffer buffer) {
    this(tree, buffer, null /* generator */);
  }

  public CompactPrinter(CssTree tree, GssSourceMapGenerator generator) {
    this(tree, null /* buffer */, generator);
  }

  public CompactPrinter(CssTree tree) {
    this(tree, null /* buffer */, null /* generator */);
  }

  @Override
  protected CssTreeVisitor createVisitor(VisitController visitController, CodeBuffer buffer) {
    return new CompactPrintingVisitor(visitController, buffer);
  }

  /** Returns the CSS compacted printed output. */
  public String getCompactPrintedString() {
    return compactedPrintedString;
  }

  @Override
  public void runPass() {
    resetBuffer();
    visit();
    compactedPrintedString = getOutputBuffer();
  }

  public static String printCompactly(CssNode n) {
    CompactPrinter p = new CompactPrinter(n);
    p.runPass();
    return p.getCompactPrintedString().trim();
  }

  public static String printCompactly(CssTree t) {
    return printCompactly(t.getRoot());
  }
}
