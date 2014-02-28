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

/**
 * A SubstitutionMap that trivially renames its CSS classes by adding an
 * underscore. This may be helpful in debugging because it makes it easy to
 * match the renamed value with the original while helping catch bugs where the
 * CSS class name is hardcoded in the code. Example in Soy:
 * <pre>
 * // Case I: Hardcoded name (incorrect)
 * &lt;div class="CSS_MENU_BAR">&lt;/div>
 *
 * // Case II: Name used as variable (correct)
 * &lt;div class="{css CSS_MENU_BAR}">&lt;/div>
 * </pre>
 * <p>In Case I, the div would not get the effects of the CSS class when it is
 * renamed, so hopefully the missing styles would help discover the source of
 * the error.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class SimpleSubstitutionMap implements SubstitutionMap {

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException if key is null
   */
  @Override
  public String get(String key) {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }
    return key + "_";
  }
}
