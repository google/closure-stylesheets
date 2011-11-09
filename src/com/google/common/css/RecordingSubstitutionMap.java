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

// TODO(bolinfest): Move this to com.google.common.css.compiler.passes.
package com.google.common.css;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.css.MultipleMappingSubstitutionMap.ValueWithMappings;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * A decorator for a {@link SubstitutionMap} that records which values it maps.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class RecordingSubstitutionMap implements SubstitutionMap {

  private final SubstitutionMap delegate;

  private final Predicate<? super String> shouldRecordMappingForCodeGeneration;

  private final Map<String,String> mappings;

  /**
   * If defined, this should be used rather than delegate to look up a mapping.
   */
  @Nullable
  private final MultipleMappingSubstitutionMap multipleMappingSubstitutionMap;

  /**
   * Creates a new instance.
   * @param map A SubstitutionMap decorated by this class
   * @param shouldRecordMappingForCodeGeneration A predicate that returns true
   *     if the mapping should be recorded for code-generation purposes
   */
  public RecordingSubstitutionMap(SubstitutionMap map,
      Predicate<? super String> shouldRecordMappingForCodeGeneration) {
    this.delegate = map;
    this.shouldRecordMappingForCodeGeneration =
        shouldRecordMappingForCodeGeneration;

    // Use a LinkedHashMap so getMappings() is deterministic.
    this.mappings = Maps.newLinkedHashMap();

    this.multipleMappingSubstitutionMap =
        (map instanceof MultipleMappingSubstitutionMap)
        ? (MultipleMappingSubstitutionMap)map
        : null;
  }

  /**
   * {@inheritDoc}
   * @throws NullPointerException if key is null.
   */
  @Override
  public String get(String key) {
    Preconditions.checkNotNull(key);
    if (!shouldRecordMappingForCodeGeneration.apply(key)) {
      return key;
    }

    if (mappings.containsKey(key)) {
      return mappings.get(key);
    } else if (multipleMappingSubstitutionMap != null) {
      ValueWithMappings valueWithMappings = multipleMappingSubstitutionMap
          .getValueWithMappings(key);
      mappings.putAll(valueWithMappings.mappings);
      return valueWithMappings.value;
    } else {
      String value = delegate.get(key);
      mappings.put(key, value);
      return value;
    }
  }

  /**
   * @return The recorded mappings in the order they were created.
   */
  public Map<String,String> getMappings() {
    return ImmutableMap.copyOf(mappings);
  }
}
