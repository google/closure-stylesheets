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

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * The CSS class substitution map which splits CSS class names on the "-" (dash)
 * character and processes them separately using a delegate substitution map.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class SplittingSubstitutionMap implements
    MultipleMappingSubstitutionMap {
  private static final Splitter DASH = Splitter.on('-');
  private final SubstitutionMap delegate;

  public SplittingSubstitutionMap(SubstitutionMap substitutionMap) {
    this.delegate = substitutionMap;
  }

  @Override
  public String get(String key) {
    return getValueWithMappings(key).value;
  }

  @Override
  public ValueWithMappings getValueWithMappings(String key) {
    Preconditions.checkNotNull(key, "CSS key cannot be null");
    Preconditions.checkArgument(!key.isEmpty(), "CSS key cannot be empty");

    // Efficiently handle the common case with no dashes.
    if (key.indexOf('-') == -1) {
      String value = delegate.get(key);
      return ValueWithMappings.createForSingleMapping(key, value);
    }

    StringBuilder buffer = new StringBuilder();
    // Cannot use an ImmutableMap.Builder because the same key/value pair may be
    // inserted more than once in this loop.
    Map<String, String> mappings = Maps.newHashMap();
    for (String part : DASH.split(key)) {
      if (buffer.length() != 0) {
        buffer.append('-');
      }

      String value = delegate.get(part);
      mappings.put(part, value);
      buffer.append(value);
    }

    String renamedClassComposedFromParts = buffer.toString();

    return ValueWithMappings.createWithValueAndMappings(
        renamedClassComposedFromParts,
        ImmutableMap.copyOf(mappings));
  }
}
