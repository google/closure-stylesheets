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
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.css.MultipleMappingSubstitutionMap.ValueWithMappings;
import java.util.Map;

/**
 * A decorator for a {@link SubstitutionMap} that records which values it maps.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class RecordingSubstitutionMap implements SubstitutionMap.Initializable {

  private final SubstitutionMap delegate;

  private final Predicate<? super String> shouldRecordMappingForCodeGeneration;

  // Use a LinkedHashMap so getMappings() is deterministic.
  private final Map<String, String> mappings = Maps.newLinkedHashMap();

  /**
   * Creates a new instance.
   *
   * @param map A SubstitutionMap decorated by this class
   * @param shouldRecordMappingForCodeGeneration A predicate that returns true if the mapping should
   *     be recorded for code-generation purposes
   * @deprecated Use {@link Builder} instead.
   */
  @Deprecated
  public RecordingSubstitutionMap(
      SubstitutionMap map, Predicate<? super String> shouldRecordMappingForCodeGeneration) {
    this.delegate = map;
    this.shouldRecordMappingForCodeGeneration =
        shouldRecordMappingForCodeGeneration;
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

    if (delegate instanceof MultipleMappingSubstitutionMap) {
      // The final value only bears a loose relationship to the mappings.
      // For example, PrefixingSubstitutionMap applied to a MinimalSubstitutionMap
      // minimizes all components but only prefixes the first.
      // We can't memoize the value here, so don't look up in mappings first.
      ValueWithMappings valueWithMappings =
          ((MultipleMappingSubstitutionMap) delegate).getValueWithMappings(key);
      mappings.putAll(valueWithMappings.mappings);
      return valueWithMappings.value;
    } else {
      String value = mappings.get(key);
      if (value == null) {
        value = delegate.get(key);
        mappings.put(key, value);
      }
      return value;
    }
  }

  /**
   * @return The recorded mappings in the order they were created. This output may be used with
   *     {@link RenamingMapFormat#writeRenamingMap}
   */
  public Map<String, String> getMappings() {
    return ImmutableMap.copyOf(mappings);
  }

  @Override
  public void initializeWithMappings(Map<? extends String, ? extends String> newMappings) {
    Preconditions.checkState(mappings.isEmpty());
    if (!newMappings.isEmpty()) {
      mappings.putAll(newMappings);
      ((SubstitutionMap.Initializable) delegate).initializeWithMappings(newMappings);
    }
  }

  /** A-la-carte builder. */
  public static final class Builder {
    private SubstitutionMap delegate = new IdentitySubstitutionMap();
    private Predicate<? super String> shouldRecordMappingForCodeGeneration =
        Predicates.alwaysTrue();
    private Map<String, String> mappings = Maps.newLinkedHashMap();

    /** Specifies the underlying map. Multiple calls clobber. */
    public Builder withSubstitutionMap(SubstitutionMap d) {
      this.delegate = Preconditions.checkNotNull(d);
      return this;
    }

    /**
     * True keys that should be treated mapped to themselves instead of passing through Multiple
     * calls AND.
     */
    public Builder shouldRecordMappingForCodeGeneration(Predicate<? super String> p) {
      shouldRecordMappingForCodeGeneration =
          Predicates.and(shouldRecordMappingForCodeGeneration, p);
      return this;
    }

    /**
     * Specifies mappings to {@linkplain Initializable initialize} the delegate with. Multiple calls
     * putAll. This can be used to reconstitute a map that was written out by {@link
     * RenamingMapFormat#writeRenamingMap} from the output of {@link
     * RenamingMapFormat#readRenamingMap}.
     */
    public Builder withMappings(Map<? extends String, ? extends String> m) {
      this.mappings.putAll(m);
      return this;
    }

    /** Builds the substitution map based on previous operations on this builder. */
    public RecordingSubstitutionMap build() {
      // TODO(msamuel): if delegate instanceof MultipleMappingSubstitutionMap
      // should this return a RecordingSubstitutionMap that is itself
      // a MultipleMappingSubstitutionMap.
      RecordingSubstitutionMap built =
          new RecordingSubstitutionMap(delegate, shouldRecordMappingForCodeGeneration);
      built.initializeWithMappings(mappings);
      return built;
    }
  }
}
