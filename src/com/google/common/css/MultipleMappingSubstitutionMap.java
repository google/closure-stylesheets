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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * {@link MultipleMappingSubstitutionMap} is a special type of
 * {@link SubstitutionMap} that can create multiple mappings for a single
 * lookup. This is particularly important with respect to CSS renaming.
 * <p>
 * For example, a {@link SplittingSubstitutionMap} may rename "goog-component"
 * as "a-b", in which case it has created two renaming mappings: "a" -> "goog"
 * and "b" -> "component", both of which are necessary to produce the full
 * renaming map.
 * <p>
 * Note that in the case of {@link SplittingSubstitutionMap}, the map that is
 * returned by {@link #getValueWithMappings(String)} does not contain
 * "goog-component" as a key because the entries "goog" and "component" are
 * sufficient to construct the renamed version of "goog-component". Therefore,
 * the key passed into {@link #getValueWithMappings(String)} is not guaranteed
 * to appear in the output.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public interface MultipleMappingSubstitutionMap extends SubstitutionMap {

  /**
   * Contains both the value and mappings returned by
   * {@link MultipleMappingSubstitutionMap#getValueWithMappings(String)}.
   */
  public static class ValueWithMappings {
    public final String value;
    public final Map<String, String> mappings;

    private ValueWithMappings(String value, Map<String, String> mappings) {
      Preconditions.checkNotNull(value);
      Preconditions.checkNotNull(mappings);
      this.value = value;
      this.mappings = ImmutableMap.copyOf(mappings);
    }

    public static ValueWithMappings createWithValueAndMappings(String value,
        Map<String, String> mappings) {
      return new ValueWithMappings(value, mappings);
    }

    public static ValueWithMappings createForSingleMapping(String key, String value) {
      return new ValueWithMappings(value, ImmutableMap.of(key, value));
    }
  }

  /**
   * Like an ordinary {@link SubstitutionMap}, this returns a value to
   * substitute for the specified {@code key}. This value is available as
   * {@link ValueWithMappings#value}.
   * <p>
   * Additionally, it also returns any renaming mappings that should be
   * associated with this substitution. These mappings are available as
   * {@link ValueWithMappings#mappings}.
   */
  public ValueWithMappings getValueWithMappings(String key);
}
