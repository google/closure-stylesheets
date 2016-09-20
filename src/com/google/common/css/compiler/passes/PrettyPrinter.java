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

import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;
import javax.annotation.Nullable;

/**
 * A pretty-printer for {@link CssTree} instances. This is work in progress.
 * Look at PrettyPrinterTest to see what's supported.
 *
 * @author mkretzschmar@google.com (Martin Kretzschmar)
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
public class PrettyPrinter extends CodePrinter implements CssCompilerPass {
  private String prettyPrintedString = null;
  private boolean stripQuotes = false;
  private boolean preserveComments = false;

  public PrettyPrinter(VisitController visitController, 
      @Nullable CodeBuffer buffer,
      @Nullable GssSourceMapGenerator generator) {
    super(visitController, buffer, generator);
  }

  public PrettyPrinter(VisitController visitController, GssSourceMapGenerator generator) {
    this(visitController, null /* buffer */, generator);
  }

  public PrettyPrinter(VisitController visitController) {
    this(visitController, null /* buffer */, null /* generator */);
  }

  /**
   * Whether to strip quotes from certain values. This facilitates
   * tests that want to compare trees.
   */
  public void setStripQuotes(boolean stripQuotes) {
    this.stripQuotes = stripQuotes;
  }

  /**
   * Whether comments in the CSS nodes are preserved in the pretty printed
   * output.
   * <p>Note: Comments layout is not guaranteed, since detailed position
   * information in the input files is not preserved by the parser. Line breaks
   * are added after every comment with current identation as best effort.</p>
   */
  public PrettyPrinter setPreserveComments(boolean preserve) {
    this.preserveComments = preserve;
    return this;
  }

  public String getPrettyPrintedString() {
    return prettyPrintedString;
  }

  @Override
  protected CssTreeVisitor createVisitor(VisitController visitController, CodeBuffer codeBuffer) {
    return new PrettyPrintingVisitor(codeBuffer, stripQuotes, preserveComments);
  }

  @Override
  public void runPass() {
    resetBuffer();
    visit();
    prettyPrintedString = getOutputBuffer();
  }
}
