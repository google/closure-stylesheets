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
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;
import javax.annotation.Nullable;

/**
 * Compact-printer for {@link CssTree} instances that only outputs rulesets which
 * include a selector belonging to one chunk. This printer does not support
 * code moving between chunks and ouputs the same ruleset for as many chunks
 * as this ruleset belongs to.
 *
 * <p>This pass can only be used if {@link MapChunkAwareNodesToChunk} pass has been
 * run before. Otherwise this pass won't work.
 *
 * @param <T> type of chunk id objects set on {@link CssSelectorNode} instances
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class ChunkCompactPrinter<T> extends CompactPrinter {

  /** Chunk to be printed by this printer. */
  protected final T chunk;

  public ChunkCompactPrinter(
      CssNode subtree,
      T chunk,
      @Nullable CodeBuffer buffer,
      @Nullable GssSourceMapGenerator generator) {
    super(subtree, buffer, generator);
    this.chunk = chunk;
  }

  public ChunkCompactPrinter(CssNode subtree, T chunk, @Nullable CodeBuffer buffer) {
    this(subtree, chunk, buffer, null /* generator */);
  }

  public ChunkCompactPrinter(CssNode subtree, T chunk) {
    this(subtree, chunk, null /* buffer */);
  }

  public ChunkCompactPrinter(
      CssTree tree,
      T chunk,
      @Nullable CodeBuffer buffer,
      @Nullable GssSourceMapGenerator generator) {
    super(tree, buffer, generator);
    this.chunk = chunk;
  }

  public ChunkCompactPrinter(CssTree tree, T chunk, @Nullable CodeBuffer buffer) {
    this(tree, chunk, buffer, null /* generator */);
  }

  public ChunkCompactPrinter(CssTree tree, T chunk, @Nullable GssSourceMapGenerator generator) {
    this(tree, chunk, null /* buffer */, generator);
  }

  public ChunkCompactPrinter(CssTree tree, T chunk) {
    this(tree, chunk, null /* buffer */, null /* generator */);
  }

  @Override
  protected CssTreeVisitor createVisitor(VisitController visitController, CodeBuffer buffer) {
    return new ChunkCompactPrintingVisitor<T>(visitController, chunk, buffer);
  }
}
