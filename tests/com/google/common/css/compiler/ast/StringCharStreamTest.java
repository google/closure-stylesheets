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

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests for a more efficient implementation of the CharStream which only
 * expects to operate on {@code String} object.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class StringCharStreamTest extends TestCase {

  public void testLocation() throws Exception {
    StringCharStream s = new StringCharStream(
        "01234\n" +
        "6789\n" +
        "bcd");

    assertEquals("column", 0, s.getEndColumn());
    assertEquals("line", 1, s.getEndLine());
    assertEquals("char index", -1, s.getCharIndex());

    readCharCheckLocation(s, '0',  1, 1, 0);
    readCharCheckLocation(s, '1',  1, 2, 1);
    readCharCheckLocation(s, '2',  1, 3, 2);
    readCharCheckLocation(s, '3',  1, 4, 3);
    readCharCheckLocation(s, '4',  1, 5, 4);
    readCharCheckLocation(s, '\n', 1, 6, 5);
    readCharCheckLocation(s, '6',  2, 1, 6);
    readCharCheckLocation(s, '7',  2, 2, 7);
    readCharCheckLocation(s, '8',  2, 3, 8);
    readCharCheckLocation(s, '9',  2, 4, 9);
    readCharCheckLocation(s, '\n', 2, 5, 10);
    readCharCheckLocation(s, 'b',  3, 1, 11);
    readCharCheckLocation(s, 'c',  3, 2, 12);
    readCharCheckLocation(s, 'd',  3, 3, 13);
  }

  public void testBackup() throws Exception {
    StringCharStream s = new StringCharStream(
        "01234\n" +
        "6789\n" +
        "bcd");
    readCharCheckLocation(s, '0',  1, 1, 0);
    readCharCheckLocation(s, '1',  1, 2, 1);
    readCharCheckLocation(s, '2',  1, 3, 2);
    
    s.backup(3);
    readCharCheckLocation(s, '0',  1, 1, 0);
    readCharCheckLocation(s, '1',  1, 2, 1);
    readCharCheckLocation(s, '2',  1, 3, 2);
    
    s.backup(2);
    readCharCheckLocation(s, '1',  1, 2, 1);
    readCharCheckLocation(s, '2',  1, 3, 2);

    readCharCheckLocation(s, '3',  1, 4, 3);
    readCharCheckLocation(s, '4',  1, 5, 4);
    readCharCheckLocation(s, '\n', 1, 6, 5);
    readCharCheckLocation(s, '6',  2, 1, 6);
    readCharCheckLocation(s, '7',  2, 2, 7);

    s.backup(4);
    readCharCheckLocation(s, '4',  1, 5, 4);
    readCharCheckLocation(s, '\n', 1, 6, 5);
    readCharCheckLocation(s, '6',  2, 1, 6);
    readCharCheckLocation(s, '7',  2, 2, 7);

    s.backup(3);
    readCharCheckLocation(s, '\n', 1, 6, 5);
    readCharCheckLocation(s, '6',  2, 1, 6);
    readCharCheckLocation(s, '7',  2, 2, 7);

    readCharCheckLocation(s, '8',  2, 3, 8);
    readCharCheckLocation(s, '9',  2, 4, 9);
    readCharCheckLocation(s, '\n', 2, 5, 10);
    readCharCheckLocation(s, 'b',  3, 1, 11);
    readCharCheckLocation(s, 'c',  3, 2, 12);
    readCharCheckLocation(s, 'd',  3, 3, 13);
    
    s.backup(2);
    readCharCheckLocation(s, 'c',  3, 2, 12);
    readCharCheckLocation(s, 'd',  3, 3, 13);
  }

  public void testConvertCharacterIndex() throws Exception {
    StringCharStream s = new StringCharStream(
        "01234\n" +
        "6789\n" +
        "bcd");
    checkCharacterIndex(s, 1, 1, 0);
    checkCharacterIndex(s, 1, 2, 1);
    checkCharacterIndex(s, 1, 3, 2);
    checkCharacterIndex(s, 1, 4, 3);
    checkCharacterIndex(s, 1, 5, 4);
    checkCharacterIndex(s, 1, 6, 5);
    checkCharacterIndex(s, 2, 1, 6);
    checkCharacterIndex(s, 2, 2, 7);
    checkCharacterIndex(s, 2, 3, 8);
    checkCharacterIndex(s, 2, 4, 9);
    checkCharacterIndex(s, 2, 5, 10);
    checkCharacterIndex(s, 3, 1, 11);
    checkCharacterIndex(s, 3, 2, 12);
    checkCharacterIndex(s, 3, 3, 13);
  }

  public void testGetImageAndGetSuffix() throws Exception {
    StringCharStream s = new StringCharStream(
        "01234\n" +
        "6789\n" +
        "bcd");
    readCharCheckLocation(s, '0',  1, 1, 0);
    readCharCheckLocation(s, '1',  1, 2, 1);
    
    beginTokenCheckLocation(s, '2',  1, 3, 2);
    readCharCheckLocation(s, '3',  1, 4, 3);
    readCharCheckLocation(s, '4',  1, 5, 4);
    readCharCheckLocation(s, '\n', 1, 6, 5);
    readCharCheckLocation(s, '6',  2, 1, 6);

    assertEquals("234\n6", s.GetImage());
    assertEquals("4\n6", new String(s.GetSuffix(3)));
    assertEquals("34\n6", new String(s.GetSuffix(4)));

    s.backup(2);
    readCharCheckLocation(s, '\n', 1, 6, 5);
    readCharCheckLocation(s, '6',  2, 1, 6);
    readCharCheckLocation(s, '7',  2, 2, 7);

    assertEquals("234\n67", s.GetImage());

    beginTokenCheckLocation(s, '8',  2, 3, 8);
    readCharCheckLocation(s, '9',  2, 4, 9);
    readCharCheckLocation(s, '\n', 2, 5, 10);

    assertEquals("89\n", s.GetImage());
    assertEquals("89\n", new String(s.GetSuffix(3)));
    // Try to reach behind the token start!
    assertEquals("789\n", new String(s.GetSuffix(4)));

    beginTokenCheckLocation(s, 'b',  3, 1, 11);
    readCharCheckLocation(s, 'c',  3, 2, 12);
    readCharCheckLocation(s, 'd',  3, 3, 13);
    
    assertEquals("bcd", s.GetImage());
    assertEquals("cd", new String(s.GetSuffix(2)));
    assertEquals("\nbcd", new String(s.GetSuffix(4)));

    try {
      s.readChar();
      fail();
    } catch (IOException e) {
      // Should thrown an exception, when reaching behind the end of the string.
    }

    // Token creation and suffix creation, should be unaffected by trying to
    // read EOF.
    assertEquals("bcd", s.GetImage());
    assertEquals("cd", new String(s.GetSuffix(2)));
    assertEquals("\nbcd", new String(s.GetSuffix(4)));
  }

  private void checkCharacterIndex(StringCharStream s,
      int line, int column, int charIndex) {
    assertEquals("char index",
        charIndex, s.convertToCharacterIndex(line, column));
  }

  private void readCharCheckLocation(
      StringCharStream s, char c, int line, int column, int charIndex)
      throws IOException {
    assertEquals("char", c, s.readChar());
    checkLocation(s, line, column, charIndex);
  }

  private void beginTokenCheckLocation(
      StringCharStream s, char c, int line, int column, int charIndex)
      throws IOException {
    assertEquals("char", c, s.BeginToken());
    checkLocation(s, line, column, charIndex);
    assertEquals("begin column", column, s.getBeginColumn());
    assertEquals("begin line", line, s.getBeginLine());
    assertEquals("char index", charIndex, s.getCharIndex());
    assertEquals("token start", charIndex, s.getTokenStart());
  }

  private void checkLocation(
      StringCharStream s, int line, int column, int charIndex) {
    assertEquals("column", column, s.getEndColumn());
    assertEquals("line", line, s.getEndLine());
    assertEquals("char index", charIndex, s.getCharIndex());
  }
}
