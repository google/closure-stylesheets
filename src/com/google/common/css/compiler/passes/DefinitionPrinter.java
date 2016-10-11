/*
 * Copyright 2010 Google Inc.
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
 * Printer for definition nodes, which outputs GSS definitions so that they can be re-parsed later.
 *
 * <p>This pass can only be used if {@link MapChunkAwareNodesToChunk} pass has been run before.
 * Otherwise this pass won't work.
 *
 * @param <T> type of chunk id objects
 * @author dgajda@google.com (Damian Gajda)
 */
public class DefinitionPrinter<T> extends CodePrinter {

  private final T chunk;

  /**
   * Create a printer for all the definitions in the given chunk.
   *
   * @param subtree the subtree to be printed
   * @param chunk the selected chunk
   */
  public DefinitionPrinter(CssNode subtree, T chunk) {
    super(subtree.getVisitController(), null /* buffer */, null /* generator */);
    this.chunk = chunk;
  }

  /**
   * Create a printer for all the definitions in the given chunk.
   *
   * @param tree the css tree to be printed
   * @param chunk the selected chunk
   */
  public DefinitionPrinter(CssTree tree, T chunk) {
    super(tree.getVisitController(), null /* buffer */, null /* generator */);
    this.chunk = chunk;
  }

  /**
   * Returns a GSS output with all the printed definitions.
   */
  public String getDefinitionGss() {
    return getOutputBuffer();
  }

  @Override
  public void runPass() {
    resetBuffer();
    visit();
  }

  @Override
  protected CssTreeVisitor createVisitor(VisitController visitController, CodeBuffer codeBuffer) {
    return new DefinitionPrintingVisitor<>(chunk, codeBuffer);
  }
}
