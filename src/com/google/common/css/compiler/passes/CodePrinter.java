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

import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.VisitController;

import javax.annotation.Nullable;

/**
 * An abstract code-printer for {@link CssTree} instances that provides read/write access
 * to the output buffer and performs common tasks during code generation, like creating
 * sourcemaps.
 *
 * @author steveyang@google.com (Chenyun Yang)
 */
public abstract class CodePrinter extends UniformVisitor {

  protected final VisitController visitController;

  /**
   * CodeBuffer used in this CodePrinter.
   */
  protected final CodeBuffer buffer;

  /**
   * The source map generator used by CodePrinter and subclasses.
   */
  private final GssSourceMapGenerator generator;

  /**
   * Initializes this instance from the given {@link VisitController}, could optionally
   * accept {@link CodeBuffer} and {@link GssSourceMapGenerator} to use.
   */
  protected CodePrinter(VisitController visitController,
      @Nullable CodeBuffer buffer,
      @Nullable GssSourceMapGenerator generator) {
    this.visitController = visitController;
    this.buffer = buffer != null ? buffer : new CodeBuffer();
    this.generator = generator != null ? generator : new NullGssSourceMapGenerator();
  }

  protected CodePrinter(VisitController visitController, CodeBuffer buffer) {
    this(visitController, buffer, null /* generator */);
  }

  protected CodePrinter(VisitController visitController) {
    this(visitController, null /* buffer */);
  }

  /**
   * Initializes this instance from the given {@link CssNode}, could optionally
   * accept {@link CodeBuffer} and {@link GssSourceMapGenerator} to use.
   */
  protected CodePrinter(CssNode subtree, CodeBuffer buffer, GssSourceMapGenerator generator) {
    this(subtree.getVisitController(), buffer, generator);
  }

  protected CodePrinter(CssNode subtree, CodeBuffer buffer) {
    this(subtree.getVisitController(), buffer, null /* generator */);
  }
  protected CodePrinter(CssNode subtree) {
    this(subtree.getVisitController(), null /* buffer */);
  }

  /**
   * Initializes this instance from the given {@link CssTree}, could optionally
   * accept {@link CodeBuffer} and {@link GssSourceMapGenerator} to use.
   */
  protected CodePrinter(CssTree tree, CodeBuffer buffer, GssSourceMapGenerator generator) {
    this(tree.getVisitController(), buffer, generator);
  }

  protected CodePrinter(CssTree tree, CodeBuffer buffer) {
    this(tree.getVisitController(), buffer, null /* generator */);
  }
  protected CodePrinter(CssTree tree) {
    this(tree.getVisitController(), null /* buffer */);
  }

  @Override
  public void enter(CssNode node) {
    generator.startSourceMapping(node, buffer.getNextLineIndex(), buffer.getNextCharIndex());
  }

  @Override
  public void leave(CssNode node) {
    generator.endSourceMapping(node, buffer.getLastLineIndex(), buffer.getLastCharIndex());
  }

  // Proxy method for external usage.
  protected void resetBuffer() {
    buffer.reset();
  }

  protected String getOutputBuffer() {
    return buffer.getOutput();
  }
}
