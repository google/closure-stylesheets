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

import javax.annotation.Nullable;

/**
 * Class to write/delete characters used by {@link CodePrinter} subclasses to print out code.
 * It has finalized core public APIs to endure correct update of character index and line index
 * while operating on the buffer.
 *
 * <p>This class can be extended and with helper methods if necessary.
 * @see TemplateCompactPrinter
 *
 * <p>{@code char} is used as operation unit for methods, there is no support for surrogates.
 *
 * @author steveyang@google.com (Chenyun Yang)
 */
public class CodeBuffer {

  private final StringBuilder sb;

  /**
   * The index within the line of the next character to be written to the buffer.
   * Indices start at 0, following source map v3.
   */
  private int nextCharIndex = 0;

  /**
   * The number of the line that will contain the character at characterIndex.
   * Numbers start at 0, following source map v3.
   */
  private int nextLineIndex = 0;

  CodeBuffer () {
    this.sb = new StringBuilder();
  };

  /** Returns buffer as String. */
  public final String getOutput() {
    return sb.toString();
  }

  /** Returns the current length of the buffer. */
  public final int getCurrentLength() {
    return sb.length();
  }

  /** Returns the last character in the buffer. */
  public final char getLastChar() {
    return sb.charAt(sb.length() - 1);
  }

  /**
   * Returns the index within the line of the next character to be written to the buffer.
   * Indices start at 0, following source map v3.
   */
  public final int getNextCharIndex() {
    return nextCharIndex;
  }

  /**
   * Returns the number of the line that will contain the next character at characterIndex. Numbers
   * start at 0, following source map v3.
   */
  public final int getNextLineIndex() {
    return nextLineIndex;
  }

  /**
   * Appends {@code str} to the buffer. The string is safe to contain newlines.
   * {@code nextCharIndex} and {@code nextLineIndex} will be updated accordingly.
   *
   * <p>Prefer to use append(char c) API to append characters for efficiency.
   */
  public final CodeBuffer append(@Nullable String str) {
    if (str == null) {
      return this;
    }

    if (str.length() == 1) {
      append(str.charAt(0));
    } else {
      String[] parts = str.split("\n", -1);
      for (int i = 0; i < parts.length; i++) {
        sb.append(parts[i]);
        nextCharIndex += parts[i].length();
        if (i != (parts.length - 1)) {
          startNewLine();
        }
      }
    }

    return this;
  }

  /**
   * Append {@code c} to the buffer and update {@code nextCharIndex}.
   */
  public final CodeBuffer append(char c) {
    if (c == '\n') {
      startNewLine();
    } else {
      sb.append(c);
      nextCharIndex += 1;
    }
    return this;
  }

  /** Appends the {@code toString} representation of {@code o} to the buffer. */
  public final CodeBuffer append(Object o) {
    append(o.toString());
    return this;
  }

  /**
   * Appends a newline to buffer, resetting {@code nextCharIndex} and incrementing
   * {@code nextLineIndex}.
   */
  public final CodeBuffer startNewLine() {
    if (nextCharIndex > 0) {
      sb.append('\n');
      nextLineIndex++;
      nextCharIndex = 0;
    }
    return this;
  }

  /**
   * Deletes the last character in the buffer and updates the {@code nextCharIndex} and
   * {@code nextLineIndex}.
   */
  public final CodeBuffer deleteLastChar() {
    int index = getCurrentLength() - 1;
    char c = sb.charAt(index);
    sb.deleteCharAt(index);

    if (c == '\n') {
      nextLineIndex--;
      int lastNewline = sb.lastIndexOf("\n");
      if (lastNewline != -1) {
        // Transform length() from 1-based index to 0-based.
        nextCharIndex = (sb.length() - 1) - lastNewline;
      } else {
        // Situation when delete back to the first line.
        nextCharIndex = sb.length();
      }
    } else {
      nextCharIndex--;
    }
    return this;
  }

  /** Deletes last {@code n} characters in the buffer. */
  public final CodeBuffer deleteLastChars(int n) {
    for (int i = 0; i < n; i++) {
      deleteLastChar();
    }
    return this;
  }

  /**
   * Clears the contents of the buffer and resets {@code nextCharIndex}
   * and {@code nextLineIndex}.
   */
  public final CodeBuffer reset() {
    nextCharIndex = 0;
    nextLineIndex = 0;
    sb.setLength(0);
    sb.trimToSize();
    return this;
  }

  /**
   * Deletes the last character from the string builder if the character is as given.
   *
   * <p>Subclasses can modify this method in order to delete more in cases where they've added extra
   * delimiters.
   *
   * @param ch the character to delete
   */
  public void deleteLastCharIfCharIs(char ch) {
    if (getCurrentLength() > 0 && getLastChar() == ch) {
      deleteLastChar();
    }
  }

  /** Deletes the end of the buffer if it exactly equals {@code s}. */
  public void deleteEndingIfEndingIs(String s) {
    if (sb.subSequence(sb.length() - s.length(), sb.length()).equals(s)) {
      deleteLastChars(s.length());
    }
  }
}
