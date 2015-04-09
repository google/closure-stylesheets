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
import java.io.IOException;

/**
 * Interface to collect and generate source map for {@link CssNode} in Gss compiler. 
 *
 * @author steveyang@google.com (Chenyun Yang)
 */
public interface GssSourceMapGenerator {
  /** 
   * Appends the generated source map to {@code out}. 
   * 
   * @param out an {@link Appendable} object to append the output on
   * @param name filename to be written inside the source map (not the filename where writes at)
   */
  public void appendOutputTo(Appendable out, String name) throws IOException;

  /**
   * Starts the source mapping for the given node at the current position.
   * This is intended to be called before the node is written to the buffer.
   *
   * @param node the {@link CssNode} to be processed
   * @param startLine the first character's line number once it starts writing output
   * @param startCharIndex the first character's character index once it starts writing output
   */
  public void startSourceMapping(CssNode node, int startLine, int startCharIndex);

  /**
   * Finishes the source mapping for the given node at the current position.
   * This is intended to be called immediately after the whole node is written to the buffer.
   *
   * @param node the {@link CssNode} to be processed
   * @param endLine the last character's line number when it ends writing output
   * @param endCharIndex the last character's character index when it ends writing output
   */
  public void endSourceMapping(CssNode node, int endLine, int endCharIndex);

  /**
   * A prefix to be added to the beginning of each source file name.
   * Debuggers expect (prefix + sourceName) to be a URL for loading the source code.
   *
   * @param path The URL prefix to save in the sourcemap file.
   */
  public void setSourceRoot(String path);
}
