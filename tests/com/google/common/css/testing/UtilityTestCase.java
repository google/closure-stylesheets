/*
 * Copyright 2005 Google Inc.
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

package com.google.common.css.testing;

import com.google.common.base.Joiner;

import junit.framework.TestCase;

/**
 * A base class for test cases that provides utility methods commonly used in
 * {@code CssParser}'s tests.
 *
 */
public class UtilityTestCase extends TestCase {

  public UtilityTestCase() {
  }

  public UtilityTestCase(String name) {
    super(name);
  }

  private static String joinWithNewLines(String ... lines) {
    return Joiner.on("\n").join(lines);
  }

  private static String newLineAfterEachString(String ... lines) {
    return joinWithNewLines(lines) + "\n";
  }

  protected static String linesToString(String ... lines) {
    return joinWithNewLines(lines);
  }

  /**
   * The parser adds a newline at the end of the CSS file, so we append one here
   * to our expected output as well.
   *
   * @param lines array of Strings, each representing a line of output
   * @return expected output as a single string with newlines embedded
   */
  protected String linesToParsedString(String... lines) {
    return newLineAfterEachString(lines);
  }

  /**
   * When a multi-line message is output, a newline gets appended after each
   * line. Use this to convert an array to the corresponding output.
   *
   * @param lines array of Strings, each representing a line of output
   * @return expected output as a single string with newlines embedded
   */
  protected String linesToMessageOutput(String ... lines) {
    return newLineAfterEachString(lines);
  }
}
