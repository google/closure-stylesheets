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

import com.google.common.base.Preconditions;
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
  private int nextCharIndex;

  /**
   * The index of the line that will contain the character at nextCharIndex.
   * Indices start at 0, following source map v3.
   */
  private int nextLineIndex;

  /**
   * The index of the last character written to the buffer.
   * Indices start at 0, following source map v3.
   */
  private int lastCharIndex;

  public CodeBuffer() {
    this.sb = new StringBuilder();
    resetIndex();
  }

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
   * Returns {@link #nextCharIndex}.
   */
  public final int getNextCharIndex() {
    return nextCharIndex;
  }

  /**
   * Returns {@link #nextLineIndex}.
   */
  public final int getNextLineIndex() {
    return nextLineIndex;
  }

  /**
   * Returns {@link #lastCharIndex}.
   */
  public final int getLastCharIndex() {
    return lastCharIndex;
  }

  /**
   * Returns the index of the line which contains the last character at lastCharIndex.
   * It is always the same or 1 index behind {@link #nextLineIndex}.
   */
  public final int getLastLineIndex() {
    return nextLineIndex - (
        (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') ? 1 : 0);
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
        incrementIndexBy(parts[i].length());
        if (i != (parts.length - 1)) {
          startNewLine();
        }
      }
    }

    return this;
  }

  /**
   * Appends {@code c} to the buffer and update {@code nextCharIndex}.
   */
  public final CodeBuffer append(char c) {
    if (c == '\n') {
      startNewLine();
    } else {
      sb.append(c);
      incrementIndexBy(1);
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
    sb.append('\n');
    incrementIndexForNewline();
    return this;
  }

  /**
   * Deletes the last character in the buffer and updates the {@code nextCharIndex} and
   * {@code nextLineIndex}.
   */
  public final CodeBuffer deleteLastChar() {
    if (getCurrentLength() > 0) {
      decrementIndex();
      sb.deleteCharAt(getCurrentLength() - 1);
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
    resetIndex();
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

  /**
   * Updates character-related indexes before or after writing some non-newline characters
   * to the buffer. Use {@link #incrementIndexForNewline} when writing '\n'.
   *
   * @param step numbers of characters writes to buffer
   */
  private void incrementIndexBy(int step) {
    if (step > 0) {
      lastCharIndex = nextCharIndex + step - 1;
      nextCharIndex = nextCharIndex + step;
    }
  }

  /**
   * Updates character-related indexes before/after writing a newline character to the buffer.
   */
  private void incrementIndexForNewline() {
    // '\n' is written at {@code nextCharIndex}
    lastCharIndex = nextCharIndex;
    nextCharIndex = 0;

    // future writing starts at a new line
    nextLineIndex++;
  }

  /**
   * Updates character-related indexes <b>before</b> deleting a character in buffer.
   *
   * <p>Note: Indexes updates must take place before deletion as the last two chars determine
   * the behavior.
   */
  private void decrementIndex() {
    Preconditions.checkState(getCurrentLength() > 0);

    nextCharIndex = lastCharIndex;

    // Need to look at the last character to determine how to update indexes
    int lastIndex = getCurrentLength() - 1;

    // As a '\n' will be removed, {@code nextLineIndex} should be moved to previous line
    if (sb.charAt(lastIndex) == '\n') {
      nextLineIndex--;
    }

    // When the second to last char is a newline, needs to recalculate the {@code lastCharIndex}
    if (lastIndex - 1 > 0 && sb.charAt(lastIndex - 1) == '\n') {
      int lastNewline = sb.lastIndexOf("\n");
      int secondToLastNewLine = sb.substring(0, lastNewline - 1).lastIndexOf('\n');
      if (secondToLastNewLine == -1) {
        // when only one line left after deletion
        lastCharIndex = lastNewline - 1;
      } else {
        // Adjust to 0-based index for {@code lastCharIndex}.
        lastCharIndex = lastNewline - secondToLastNewLine - 1;
      }
    } else {
      // Otherwise, when deletion happens on same line, move the index one character to the left
      lastCharIndex = lastCharIndex - 1;
    }
  }

  private void resetIndex() {
    nextCharIndex = 0;
    nextLineIndex = 0;
    lastCharIndex = -1;
  }
}
