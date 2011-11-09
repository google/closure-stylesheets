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

package com.google.common.css;

import junit.framework.TestCase;

/**
 * Test for the delegating CSS class substitution map which handles compound
 * class names.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class SplittingSubstitutionMapTest extends TestCase {

  public void testGet() throws Exception {
    SubstitutionMap map = new SplittingSubstitutionMap(
        new SimpleSubstitutionMap());
    assertEquals("a_", map.get("a"));
    assertEquals("a_-b_", map.get("a-b"));
    assertEquals("a_-b_-c_", map.get("a-b-c"));
    assertEquals("a_-b_-c_-d_", map.get("a-b-c-d"));
  }

  public void testSameObjectReturnedIfNoDash() {
    // Manually force a non-interned string so that we can prove we got back
    // the same one we meant. If we just used a String literal, it would be
    // less convincing.
    String input = new String("abc");
    SubstitutionMap map = new SplittingSubstitutionMap(
        new PassThroughSubstitutionMap());
    assertSame(input, map.get(input));
  }

  private static class PassThroughSubstitutionMap implements SubstitutionMap {
    @Override
    public String get(String key) {
      return key;
    }
  }
}
