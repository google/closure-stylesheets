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

import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;

/**
 * Printer for templates, which outputs GSS with holes to be filled
 * for references. In addition, the declaration boundaries are
 * explicitly noted, so that a declaration can be removed if it ends
 * up empty.
 *
 * @param <T> type of chunk id objects
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class TemplateCompactPrinter<T> extends CompactPrinter {

  public static final char REFERENCE_START = '\u0123';
  public static final char REFERENCE_END = '\u0122';
  public static final char REFERENCE_START_OLD = '$';
  public static final char REFERENCE_END_OLD = '^';
  public static final char DECLARATION_START = '\u0105';
  public static final char DECLARATION_END = '\u0104';
  public static final char RULE_START = '\u0118';
  public static final char RULE_END = '\u0119';

  /** Chunk to be printed by this printer. */
  private final T chunk;

  // CodeBuffer with specific behavior for the printer
  private static final class CodeBufferForTemplate extends CodeBuffer {
    @Override
    public void deleteLastCharIfCharIs(char ch) {
      if (ch == ';' && getLastChar() == DECLARATION_END) {
        deleteLastChars(2);
        append(DECLARATION_END);
      } else {
        super.deleteLastCharIfCharIs(ch);
      }
    }
  }

  /**
   * Create a template printer for a given chunk.
   *
   * @param tree CSS AST to be printed (with regard to a selected chunk)
   * @param chunk the chunk selected for printing
   */
  public TemplateCompactPrinter(CssTree tree, T chunk) {
    super(tree, new CodeBufferForTemplate(), null /* generator */);
    this.chunk = chunk;
  }

  @Override
  protected CssTreeVisitor createVisitor(VisitController visitController, CodeBuffer buffer) {
    return new TemplateCompactPrintingVisitor<>(visitController, chunk, buffer);
  }
}
