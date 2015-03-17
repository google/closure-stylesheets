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

import junit.framework.TestCase;

/**
 * Test {@link CodeBuffer} for correct behaviors when writing buffer and update
 * lineIndex and charIndex.
 *
 * @author steveyang@google.com (Chenyun Yang)
 */
public class CodeBufferTest extends TestCase {
  public void testInitialSetup() {
    CodeBuffer buffer = new CodeBuffer();
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(-1, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
  }

  public void testReset() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo");
    buffer.reset();
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(-1, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
  }

  public void testAppendNull() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append(null);
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(-1, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
  }

  public void testAppendChar() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append('c');
    assertEquals(1, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(0, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
  }

  public void testAppendStr() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo");
    assertEquals(3, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(2, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
  }

  public void testAppendStrIncludeNewLine() {
    CodeBuffer buffer;

    buffer = new CodeBuffer();
    buffer.append("foo\nbarrr\n");
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(2, buffer.getNextLineIndex());
    assertEquals(5, buffer.getLastCharIndex());
    assertEquals(1, buffer.getLastLineIndex());

    buffer = new CodeBuffer();
    buffer.append("foo\nbarrr");
    assertEquals(5, buffer.getNextCharIndex());
    assertEquals(1, buffer.getNextLineIndex());
    assertEquals(4, buffer.getLastCharIndex());
    assertEquals(1, buffer.getLastLineIndex());
  }

  public void testAppendObject() {
    CodeBuffer buffer = new CodeBuffer();
    class TestObject {
      @Override
      public String toString() {
        return "foobar";
      }
    }
    buffer.append(new TestObject());
    assertEquals(6, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(5, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
  }

  public void testAppendNewLineChar() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo");
    buffer.append('\n');
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(1, buffer.getNextLineIndex());
    assertEquals(3, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
    buffer.append("bar");
    assertEquals(3, buffer.getNextCharIndex());
    assertEquals(1, buffer.getNextLineIndex());
    assertEquals(2, buffer.getLastCharIndex());
    assertEquals(1, buffer.getLastLineIndex());
  }
  public void testAppendSequenceOfNewLineChar() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo\n\nbar");
    assertEquals(3, buffer.getNextCharIndex());
    assertEquals(2, buffer.getNextLineIndex());
    assertEquals(2, buffer.getLastCharIndex());
    assertEquals(2, buffer.getLastLineIndex());
  }

  public void testStartNewLine() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo");
    buffer.startNewLine();
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(1, buffer.getNextLineIndex());
    assertEquals(3, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
    buffer.append("bar");
    assertEquals(3, buffer.getNextCharIndex());
    assertEquals(1, buffer.getNextLineIndex());
    assertEquals(2, buffer.getLastCharIndex());
    assertEquals(1, buffer.getLastLineIndex());
  }

  public void testDeleteLastChar() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo");
    buffer.deleteLastChar();
    assertEquals(2, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(1, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
    buffer.deleteLastChar();
    assertEquals(1, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(0, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
    buffer.deleteLastChar();
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(-1, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
  }

  public void testDeleteLastChars() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo");
    buffer.deleteLastChars(2);
    assertEquals(1, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(0, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
  }

  public void testDeleteLastCharsWhenExceedBufferLength() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo");
    buffer.deleteLastChars(10);
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(0, buffer.getNextLineIndex());
    assertEquals(-1, buffer.getLastCharIndex());
    assertEquals(0, buffer.getLastLineIndex());
  }

  public void testDeleteLastCharForNewLine() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo");
    buffer.startNewLine();
    buffer.append("barrr");
    buffer.startNewLine();
    buffer.append("c");
    buffer.deleteLastChar();
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(2, buffer.getNextLineIndex());
    assertEquals(5, buffer.getLastCharIndex());
    assertEquals(1, buffer.getLastLineIndex());
    buffer.deleteLastChar();
    assertEquals(5, buffer.getNextCharIndex());
    assertEquals(1, buffer.getNextLineIndex());
    assertEquals(4, buffer.getLastCharIndex());
    assertEquals(1, buffer.getLastLineIndex());
  }

  public void testDeleteEndingIfEndingIs() {
    CodeBuffer buffer = new CodeBuffer();
    buffer.append("foo");
    buffer.startNewLine();
    buffer.append("barrr");
    buffer.startNewLine();
    buffer.append("c");
    buffer.deleteEndingIfEndingIs("c");
    assertEquals(0, buffer.getNextCharIndex());
    assertEquals(2, buffer.getNextLineIndex());
    assertEquals(5, buffer.getLastCharIndex());
    assertEquals(1, buffer.getLastLineIndex());
    buffer.deleteEndingIfEndingIs("arrr\n");
    assertEquals(1, buffer.getNextCharIndex());
    assertEquals(1, buffer.getNextLineIndex());
    assertEquals(0, buffer.getLastCharIndex());
    assertEquals(1, buffer.getLastLineIndex());
  }
}
