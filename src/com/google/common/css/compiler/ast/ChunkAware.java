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

package com.google.common.css.compiler.ast;

/**
 * Interface to annotate CSS nodes with target CSS chunk id.
 *
 * <p>Compiled CSS can be split into multiple parts to serve them separately,
 * improving page load and browser's CSS parsing time. These parts are called
 * chunks. Usually source code of a CSS chunk is a small group of CSS/GSS
 * files.
 *
 * <p>Because of inter-chunk dependencies (constant definitions and their
 * references, and rules order) chunks are parsed and processed as a single
 * file. On top of that, CSS size optimizations mix CSS rules coming from many
 * chunks. The {@link ChunkAware} interface is used to preserve the chunk
 * information during all those operations. Chunk information attached to a CSS
 * node is used to print the optimized CSS AST into separate chunks. It can
 * also be used to preserve CSS correctness while performing optimizations.
 *
 * <p>The chunk id is represented as a general {@link Object} so that
 * the user can choose a representation that it convenient for their
 * task. In particular, tests simply use {@link String} as chunk
 * ids. The type of the chunk ids cannot be parameterized because of
 * the strange inheritance hierarchy of the CSS AST. Chunks are identified
 * using {@link Object#equals}.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public interface ChunkAware {

  /**
   * Sets the chunk id on the node.
   *
   * @param chunk an object identifying a chunk
   */
  public void setChunk(Object chunk);

  /**
   * Gets the chunk id of the node.
   *
   * @return the chunk id or {@code null} if no chunk id was previously set
   */
  public Object getChunk();
}
