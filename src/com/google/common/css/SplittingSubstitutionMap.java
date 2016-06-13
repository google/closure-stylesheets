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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * <p>The CSS class substitution map which splits CSS class names on the "-" (dash)
 * character and processes them separately using a delegate substitution map.</p>
 *
 * <p>At the same time, if customized splitter character is assigned, the CSS class 
 * substitution map will splits CSS class names on the specific character;</p>
 * 
 * @author dgajda@google.com (Damian Gajda)
 */
public class SplittingSubstitutionMap implements
    MultipleMappingSubstitutionMap {
  public static final char DEFAULT_CSS_CLASS_SPLITTER_CHAR = '-';
  private static final Splitter DEFAULT_CSS_CLASS_SPLITTER = Splitter.
      on(DEFAULT_CSS_CLASS_SPLITTER_CHAR);
  private final SubstitutionMap delegate;
  
  /**
   * Make splitter become configurable. The value can be decided by the option 
   * user input. Default value is dash character '-'.
  */
  private char splitterChar = DEFAULT_CSS_CLASS_SPLITTER_CHAR;
  private Splitter splitter = DEFAULT_CSS_CLASS_SPLITTER;
  
  public SplittingSubstitutionMap(SubstitutionMap substitutionMap) {
    this.delegate = substitutionMap;
  }
  
  /**
   * Please NOTE: this method is used as internal method when developers want to 
   * introduce customized css class splitter. So do not call it manually.
   * 
   * @param splitterChar The customized splitter you need.
   */
  public void registerSplitter(char splitterChar) {
    this.splitterChar = splitterChar;
    this.splitter = Splitter.on(splitterChar);
  }
  
  /**
   * Please NOTE: this method is used as testing method. So do not call it manually.
   * 
   * @return splitterChar
   */
  @VisibleForTesting
  public char getSplitterChar() {
    return splitterChar;
  }

  @Override
  public String get(String key) {
    return getValueWithMappings(key).value;
  }

  @Override
  public ValueWithMappings getValueWithMappings(String key) {
    Preconditions.checkNotNull(key, "CSS key cannot be null");
    Preconditions.checkArgument(!key.isEmpty(), "CSS key cannot be empty");

    // Efficiently handle the common case with no specific splitter character.
    if (key.indexOf(this.splitterChar) == -1) {
      String value = delegate.get(key);
      return ValueWithMappings.createForSingleMapping(key, value);
    }

    StringBuilder buffer = new StringBuilder();
    // Cannot use an ImmutableMap.Builder because the same key/value pair may be
    // inserted more than once in this loop.
    Map<String, String> mappings = Maps.newHashMap();
    for (String part : splitter.split(key)) {
      if (buffer.length() != 0) {
        buffer.append(this.splitterChar);
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
