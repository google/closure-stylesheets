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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.Set;

/**
 * Test for MinimalSubstitutionMap.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class MinimalSubstitutionMapTest extends TestCase {

  private static final char[] START_CHARS = new char[] { 'a' };

  private static final char[] CHARS = new char[] { '1', '2' };

  private MinimalSubstitutionMap map;

  /**
   * @return a MinimalSubstitutionMap that uses fewer characters for CSS class
   *     names
   */
  private static MinimalSubstitutionMap createTestMap() {
    return new MinimalSubstitutionMap(START_CHARS, CHARS);
  }

  /**
   * Tests basic get() functionality, and that get() returns the same value when
   * applied to the same key.
   */
  public void testGet() {
    map = createTestMap();
    assertEquals("a", map.get("foo"));

    // Note that the order the secondary characters appear in the generated CSS
    // class names does not match the order they appear in the CHARS array. This
    // is acceptable; the only important thing is that the names are unique,
    // which is confirmed by test_toShortString().
    assertEquals("a2", map.get("bar"));
    assertEquals("a1", map.get("baz"));

    assertEquals("a", map.get("foo"));
  }

  /**
   * Tests that the get() function correctly omits values from the blacklist.
   */
  public void testGetWithBlacklist() {
    map = new MinimalSubstitutionMap(START_CHARS, CHARS, ImmutableSet.of("a"));

    // We skipped over "a".  See testGet().
    assertEquals("a2", map.get("foo"));

    // Move on to a new value, and then go back to "foo" to prove repeatability
    assertEquals("a1", map.get("bar"));
    assertEquals("a2", map.get("foo"));
  }

  /**
   * Tests toShortString() by enumerating all values of toShortString(0) to
   * toShortString(2^8-1) and verifying that each value is unique. Also ensures
   * that there are only 2 strings with 2 chars, 4 strings with 3 chars,
   * 8 strings with 4 chars, etc.
   */
  public void testToShortString() {
    map = createTestMap();

    Set<String> classes = Sets.newHashSet();
    int n = 0;

    BigInteger NUM_CHARS = BigInteger.valueOf(CHARS.length);
    int MAX_POWER = 8;

    for (int power = 0; power <= MAX_POWER; ++power) {
      int stringLength = power + 1;
      int numberOfStringsWithThisLength = NUM_CHARS.pow(power).intValue();
      for (int i = 0; i < numberOfStringsWithThisLength; ++i) {
        String renamedClass = map.toShortString(n);

        System.out.println("RENAMED CLASS: " + renamedClass);

        assertFalse("Already contains a class named: " + renamedClass,
            classes.contains(renamedClass));
        assertEquals("Class name did not match expected length", stringLength,
            renamedClass.length());
        classes.add(renamedClass);
        ++n;
      }
      assertEquals("Does not contain all possible CSS class names of length "
          + stringLength, NUM_CHARS.pow(power + 1).intValue() - 1,
          classes.size());
    }

    assertEquals("Does not contain all possible CSS class names of length "
        + (MAX_POWER + 1), NUM_CHARS.pow(MAX_POWER + 1).intValue() - 1,
        classes.size());
  }
}
