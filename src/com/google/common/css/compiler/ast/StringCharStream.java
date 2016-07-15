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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.util.List;

/**
 * Efficient {@code String} based {@link CharStream} implementation.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class StringCharStream implements CharStream {

  private static final IOException END_OF_STREAM = new IOException();

  /** The input string. */
  private final String input;
  private final int length;
  private int charPos;
  private int line;
  private int column;
  private char lastChar;

  private int tokenStart;
  private int beginLine;
  private int beginColumn;

  private int tabSize = 1;
  private boolean trackLineColumn;

  /**
   * This array (working as a map: lineNumber -> characterIndex) helps to
   * compute token locations efficiently. First element is not used as line
   * numbers are 1 based.
   */
  private int[] lineToCharIndex;

  /**
   * Creates a character stream for a given string.
   *
   * @param inputString input string for this stream
   */
  public StringCharStream(String inputString) {
    input = inputString;
    length = input.length();

    lastChar = '\u0000';
    charPos = -1;
    column = 0;
    line = 1;

    tokenStart = charPos;
    beginLine = line;
    beginColumn = column;

    initCharIndex(input);
  }

  private void initCharIndex(String source) {
    List<Integer> lineToCharIndexList = Lists.newArrayList();
    int charIndex = -1;
    lineToCharIndexList.add(charIndex);
    do {
      charIndex++;
      lineToCharIndexList.add(charIndex);
      charIndex = source.indexOf('\n', charIndex);
    } while (charIndex >= 0);
    lineToCharIndex = Ints.toArray(lineToCharIndexList);
  }

  /**
   * Returns an absolute character location for given line and column location.
   *
   * @param lineNumber line number (1 based)
   * @param indexInLine column number (1 based)
   * @return 0 based absolute character index in the input string
   */
  public int convertToCharacterIndex(int lineNumber, int indexInLine) {
    return lineToCharIndex[lineNumber] + indexInLine - 1;
  }

  /**
   * @return index of last read character
   */
  public int getCharIndex() {
    return charPos;
  }

  /**
   * @return index of the first character of a token
   */
  @VisibleForTesting
  int getTokenStart() {
    return tokenStart;
  }

  /** {@inheritDoc} */
  @Override
  public char readChar() throws IOException {
    if (charPos + 1 == length) {
      throw END_OF_STREAM;
    }

    if (lastChar == '\n') {
      line++;
      column = 0;
    }
    if (lastChar == '\t') {
      column += (tabSize - (column % tabSize));
    } else {
      column++;
    }
    lastChar = input.charAt(++charPos);
    return lastChar;
  }

  /** {@inheritDoc} */
  @Deprecated
  @Override
  public int getColumn() {
    return getEndColumn();
  }

  /** {@inheritDoc} */
  @Deprecated
  @Override
  public int getLine() {
    return getEndLine();
  }

  /** {@inheritDoc} */
  @Override
  public int getEndColumn() {
    return column;
  }

  /** {@inheritDoc} */
  @Override
  public int getEndLine() {
    return line;
  }

  /** {@inheritDoc} */
  @Override
  public int getBeginColumn() {
    return beginColumn;
  }

  /** {@inheritDoc} */
  @Override
  public int getBeginLine() {
    return beginLine;
  }

  /** {@inheritDoc} */
  @Override
  public void backup(int amount) {
    charPos -= amount;
    while (line > 1 && lineToCharIndex[line] > charPos) {
      line--;
    }
    column = charPos - lineToCharIndex[line] + 1;
    lastChar = charPos < 0 ? '\u0000' : input.charAt(charPos);
  }

  /** {@inheritDoc} */
  @Override
  public char BeginToken() throws IOException {
    readChar();
    tokenStart = charPos;
    beginLine = line;
    beginColumn = column;
    return lastChar;
  }

  /** {@inheritDoc} */
  @Override
  public String GetImage() {
    return input.substring(tokenStart, charPos + 1);
  }

  /** {@inheritDoc} */
  @Override
  public char[] GetSuffix(int len) {
    int end = charPos + 1;
    int start = end - len;
    char[] chars = new char[end - start];
    input.getChars(start, end, chars, 0);
    return chars;
  }

  /** {@inheritDoc} */
  @Override
  public void Done() {
    // Does nothing since no resources need to be freed.
  }

  @Override
  public void setTabSize(int tabSize) {
    throw new UnsupportedOperationException("setTabSize() is not supported.");
  }

  @Override
  public int getTabSize() {
    return tabSize;
  }

  @Override
  public boolean getTrackLineColumn() {
    return trackLineColumn;
  }

  @Override
  public void setTrackLineColumn(boolean trackLineColumn) {
    this.trackLineColumn = trackLineColumn;
  }
}
