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
   * The string builder used to keep the printout in progress while the tree is visited. Subclasses
   * may only access this buffer through the defined API.
   */
  private final StringBuilder sb;

  /**
   * Initializes this instance from the given {@code visitController}.
   */
  protected CodePrinter(VisitController visitController) {
    this.visitController = visitController;
    this.sb = new StringBuilder();
  }

  /**
   * Initializes this instance from the given subtree. This allows printing just a subtree instead
   * of an entire tree.
   */
  protected CodePrinter(CssNode subtree) {
    this(subtree.getVisitController());
  }

  /**
   * Initializes this instance from the given tree.
   */
  protected CodePrinter(CssTree tree) {
    this(tree.getRoot());
  }

  /** Appends {@code str} to the buffer. */
  protected final CodePrinter append(CharSequence str) {
    sb.append(str);
    return this;
  }

  /** Append {@code c} to the buffer. */
  protected final CodePrinter append(char c) {
    sb.append(c);
    return this;
  }

  /** Appends the {@code toString} representation of {@code o} to the buffer. */
  protected final CodePrinter append(Object o) {
    sb.append(o);
    return this;
  }

  /** Clears the contents of the buffer. */
  protected final CodePrinter resetBuffer() {
    sb.setLength(0);
    sb.trimToSize();
    return this;
  }

  /** Returns a new {@code String} representation of the buffer contents. */
  protected final String getOutputBuffer() {
    return sb.toString();
  }

  /** Deletes the character at a particular index in the buffer. */
  protected final CodePrinter deleteBufferCharAt(int index) {
    sb.deleteCharAt(index);
    return this;
  }

  /** Returns the current length of the buffer. */
  protected final int getCurrentBufferLength() {
    return sb.length();
  }

  /** Returns the last character in the buffer. */
  protected final char getLastCharInBuffer() {
    return sb.charAt(sb.length() - 1);
  }

  /**
   * Deletes the last character from the string builder if the character is as given.
   *
   * <p>Subclasses can modify this method in order to delete more in cases where they've added extra
   * delimiters.
   *
   * @param ch the character to delete
   */
  protected void deleteLastCharIfCharIs(char ch) {
    if (getCurrentBufferLength() == 0) {
      return;
    }
    if (getLastCharInBuffer() == ch) {
      deleteBufferCharAt(getCurrentBufferLength() - 1);
    }
  }

  /** Deletes the end of the buffer if it exactly equals {@code s}. */
  protected void deleteEndingIfEndingIs(String s) {
    if (sb.subSequence(sb.length() - s.length(), sb.length()).equals(s)) {
      sb.delete(sb.length() - s.length(), sb.length());
    }
  }
}
