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

import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;

/**
 * Build up an s-expression corresponding to the AST for debugging purposes.
 */
public class SExprPrinter extends CodePrinter {
  private final boolean includeHashCodes;
  private final boolean withLocationAnnotation;

  /**
   * A S-Expr printer for {@link CssNode} instances.
   */
  public SExprPrinter(CssNode subtree) {
    this(subtree, false /* includeHashCodes */, false /* withLocationAnnotation */);
  }

  /**
   * A S-Expr printer for {@link CssTree} instances.
   */
  public SExprPrinter(CssTree tree) {
    this(tree, false /* includeHashCodes */, false /* withLocationAnnotation */);
  }

  /**
   * A S-Expr printer for {@link CssNode} instances.
   * @param includeHashCodes boolean switch to include hash code for node or not.
   * @param withLocationAnnotation boolean switch to include source code location or not.
   */
  public SExprPrinter(CssNode subtree, boolean includeHashCodes, boolean withLocationAnnotation) {
    super(subtree.getVisitController(), null /* buffer */, null /* generator */);
    this.includeHashCodes = includeHashCodes;
    this.withLocationAnnotation = withLocationAnnotation;
  }

  /**
   * A S-Expr printer for {@link CssTree} instances.
   * @param includeHashCodes boolean switch to include hash code for node or not.
   * @param withLocationAnnotation boolean switch to include source code location or not.
   */
  public SExprPrinter(CssTree tree, boolean includeHashCodes, boolean withLocationAnnotation) {
    super(tree.getVisitController(), null /* buffer */, null /* generator */);
    this.includeHashCodes = includeHashCodes;
    this.withLocationAnnotation = withLocationAnnotation;
  }

  @Override
  protected CssTreeVisitor createVisitor(VisitController visitController, CodeBuffer codeBuffer) {
    SExprPrintingVisitor sExprPrintingVisitor =
        new SExprPrintingVisitor(codeBuffer, includeHashCodes, withLocationAnnotation);
    return UniformVisitor.Adapters.asCombinedVisitor(sExprPrintingVisitor);
  }

  @Override
  public void runPass() {
    resetBuffer();
    visit();
  }

  public static String print(CssTree t) {
    SExprPrinter printer = new SExprPrinter(t);
    printer.visit();
    return printer.getOutputBuffer();
  }

  public static String print(boolean includeHashCodes, boolean withLocationAnnotation, CssTree t) {
    SExprPrinter printer = new SExprPrinter(t, includeHashCodes, withLocationAnnotation);
    printer.visit();
    return printer.getOutputBuffer();
  }

  public static String print(CssNode n) {
    SExprPrinter printer = new SExprPrinter(n);
    printer.visit();
    return printer.getOutputBuffer();
  }

  public static String print(boolean includeHashCodes, boolean withLocationAnnotation, CssNode n) {
    SExprPrinter printer = new SExprPrinter(n, includeHashCodes, withLocationAnnotation);
    printer.visit();
    return printer.getOutputBuffer();
  }
}
