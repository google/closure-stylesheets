/*
 * Copyright 2008 Google Inc.
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

package com.google.common.css;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

/**
 * MinimalSubstitutionMap is a SubstitutionMap that renames CSS classes to the
 * shortest string possible.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class MinimalSubstitutionMap implements SubstitutionMap {

  /** Possible first chars in a CSS class name */
  private static final char[] START_CHARS = {
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
      'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
      'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
  };

  /** Possible non-first chars in a CSS class name */
  private static final char[] CHARS = {
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
      'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
      'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  };

  /**
   * Last value used with toShortString().
   */
  private int lastIndex;

  /**
   * Characters that can be used at the start of a CSS class name.
   */
  private final char[] startChars;

  /**
   * Characters that can be used in a CSS class name (though not necessarily as
   * the first character).
   */
  private final char[] chars;

  /**
   * Number of startChars.
   */
  private final int startCharsRadix;

  /**
   * Number of chars.
   */
  private final int charsRadix;

  /**
   * Value equal to Math.log(charsRadix). Stored as a field so it does not need
   * to be recomputed each time toShortString() is invoked.
   */
  private final double logCharsRadix;

  /**
   * Map of CSS classes that were renamed. Keys are original class names and
   * values are their renamed equivalents.
   */
  private final Map<String, String> renamedCssClasses;

  public MinimalSubstitutionMap() {
    this(START_CHARS, CHARS);
  }

  /**
   * Creates a new MinimalSubstitutionMap that generates CSS class names from
   * the specified set of characters.
   * @param startChars Possible values for the first character of a CSS class
   *     name.
   * @param chars Possible values for the characters other than the first
   *     character in a CSS class name.
   */
  @VisibleForTesting
  MinimalSubstitutionMap(char[] startChars, char[] chars) {
    this.lastIndex = 0;
    this.startChars = Arrays.copyOf(startChars, startChars.length);
    this.startCharsRadix = this.startChars.length;
    this.chars = Arrays.copyOf(chars, chars.length);
    this.charsRadix = this.chars.length;
    this.logCharsRadix = Math.log(charsRadix);
    this.renamedCssClasses = Maps.newHashMap();
  }

  /** {@inheritDoc} */
  @Override
  public String get(String key) {
    String value = renamedCssClasses.get(key);
    if (value == null) {
      value = toShortString(lastIndex++);
      renamedCssClasses.put(key, value);
    }
    return value;
  }

  /**
   * Converts a 32-bit integer to a unique short string whose first character
   * is in {@link #START_CHARS} and whose subsequent characters, if any, are
   * in {@link #CHARS}. The result is 1-6 characters in length.
   * @param index The index into the enumeration of possible CSS class names
   *     given the set of valid CSS characters in this class.
   * @return The CSS class name that corresponds to the index of the
   *     enumeration.
   */
  @VisibleForTesting
  String toShortString(int index) {
    // Given the number of non-start characters, C, then for each start
    // character, S, there will be:
    //   1 one-letter CSS class name that starts with S
    //   C two-letter CSS class names that start with S
    //   C^2 three-letter CSS class names that start with S
    //   C^3 four-letter CSS class names that start with S
    //   and so on...
    //
    // That means that the number of non-start characters, n, in terms of i is
    // defined as the greatest value of n that satisfies the following:
    //
    // 1 + C + C^2 + ... + C^(n - 1) <= i
    //
    // Substituting (C^n - 1) / (C - 1) for the geometric series, we get:
    //
    // (C^n - 1) / (C - 1) <= i
    // (C^n - 1) <= i * (C - 1)
    // C^n <= i * (C - 1) + 1
    // log C^n <= log (i * (C - 1) + 1)
    // n log C <= log (i * (C - 1) + 1)
    // n <= log (i * (C - 1) + 1) / log C
    //
    // Because we are looking for the largest value of n that satisfies the
    // inequality and we require n to be an integer, n can be expressed as:
    //
    // n = [[ log (i * (C - 1) + 1) / log C ]]
    //
    // where [[ x ]] is the greatest integer not exceeding x.
    //
    // Once n is known, the standard modulo-then-divide approach can be used to
    // determine each character that should be appended to s.
    int i = index / startCharsRadix;
    final int n = (int) (Math.log(i * (charsRadix - 1) + 1) / logCharsRadix); 

    // The array is 1 more than the number of secondary chars to account for the
    // first char.
    char[] cssNameChars = new char[n + 1];    
    cssNameChars[0] = startChars[index % startCharsRadix];

    for (int k = 1; k <= n; ++k) {
      cssNameChars[k] = chars[i % charsRadix];
      i /= charsRadix;
    }

    return new String(cssNameChars);
  }
}
