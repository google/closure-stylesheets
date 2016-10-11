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

import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;
import javax.annotation.Nullable;

/**
 * An abstract code-printer for {@link CssTree} instances that provides read/write access to the
 * output buffer and performs common tasks during code generation, like creating sourcemaps.
 *
 * @author steveyang@google.com (Chenyun Yang)
 */
public abstract class CodePrinter implements CssCompilerPass {

  /** The visit controller for the (sub)tree being printed. */
  private final VisitController visitController;

  /** Holds the output of the printing visitor. */
  private final CodeBuffer buffer;

  /** The source map generator used by CodePrinter and subclasses. */
  private final GssSourceMapGenerator generator;

  /**
   * Initializes this instance from the given {@link VisitController}, could optionally accept
   * {@link CodeBuffer} and {@link GssSourceMapGenerator} to use.
   */
  protected CodePrinter(
      VisitController visitController,
      @Nullable CodeBuffer buffer,
      @Nullable GssSourceMapGenerator generator) {
    this.visitController = visitController;
    this.buffer = buffer != null ? buffer : new CodeBuffer();
    this.generator = generator != null ? generator : new NullGssSourceMapGenerator();
  }

  /**
   * Constructs the visitor required by the subclass. This visitor's {@code enter*} methods will be
   * called after the source map generator's {@code startSourceMapping} method and before its {@code
   * endSourceMapping} method.
   */
  protected abstract CssTreeVisitor createVisitor(
      VisitController visitController, CodeBuffer codeBuffer);

  protected final void visit() {
    CssTreeVisitor visitor =
        DelegatingVisitor.from(
            UniformVisitor.Adapters.asVisitor(new SourceMapVisitor()),
            createVisitor(visitController, buffer));
    visitController.startVisit(visitor);
  }

  // Proxy method for external usage.
  protected final void resetBuffer() {
    buffer.reset();
  }

  protected final String getOutputBuffer() {
    return buffer.getOutput();
  }

  private class SourceMapVisitor implements UniformVisitor {

    @Override
    public void enter(CssNode node) {
      generator.startSourceMapping(node, buffer.getNextLineIndex(), buffer.getNextCharIndex());
    }

    @Override
    public void leave(CssNode node) {
      generator.endSourceMapping(node, buffer.getLastLineIndex(), buffer.getLastCharIndex());
    }
  }
}
