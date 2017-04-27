/*
 * Copyright 2011 Google Inc.
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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit test for {@link PrefixingSubstitutionMap}.
 *
 */
@RunWith(JUnit4.class)
public class PrefixingSubstitutionMapTest {
  @Test
  public void testNoPrefix() throws Exception {
    SubstitutionMap map = new PrefixingSubstitutionMap(
        new SimpleSubstitutionMap(), "");
    assertThat(map.get("foo")).isEqualTo("foo_");
    assertThat(map.get("foo-bar")).isEqualTo("foo-bar_");
  }

  @Test
  public void testPrefix() throws Exception {
    SubstitutionMap map = new PrefixingSubstitutionMap(
        new SimpleSubstitutionMap(), "PREFIX_");
    assertThat(map.get("foo")).isEqualTo("PREFIX_foo_");
    assertThat(map.get("foo-bar")).isEqualTo("PREFIX_foo-bar_");
  }
}
