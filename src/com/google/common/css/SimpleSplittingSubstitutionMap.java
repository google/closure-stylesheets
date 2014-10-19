/*
 * Copyright 2014 Google Inc.
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

/**
 * This class uses {@link SplittingSubstitutionMap} to split css class names
 * to their parts and then {@link SimpleSubstitutionMap} to append an
 * underscore to each part. For example, {@code kd-button} would become
 * {@code kd_-button_}.
 *
 * <p>This class is intended for use when debugging your application. This is
 * because the class names will still be readable, but the minimal renaming
 * ensures you'll also spot bugs where you've forgotten to use
 * {@code goog.getCssName('foo')} or {@code {css foo}}.
 *
 * <p>To use this, pass the following flags to the CSS compiler:<pre>   {@code
 *
 *   --css_substitution_map_provider=com.google.common.css.SimpleSplittingSubstitutionMapProvider
 *   --output_renaming_map_format=CLOSURE_COMPILED_SPLIT_HYPHENS}</pre>
 *
 * @see SimpleSplittingSubstitutionMapProvider
 * @author jart@google.com (Justine Tunney)
 */
public class SimpleSplittingSubstitutionMap implements SubstitutionMap {

  private final SplittingSubstitutionMap splittingSubstitutionMap;

  public SimpleSplittingSubstitutionMap() {
    splittingSubstitutionMap = new SplittingSubstitutionMap(
        new SimpleSubstitutionMap());
  }

  @Override
  public String get(String value) {
    return splittingSubstitutionMap.get(value);
  }
}
