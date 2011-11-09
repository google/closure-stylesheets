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

/**
 * Provides substitution maps for use with command-line compilers.
 *
 * Any implementation should provide a parameterless constructor, as the
 * provider is instantiated via {@link Class#newInstance()}.
 *
 */
public interface SubstitutionMapProvider {
  /**
   * Gets the substitution map.
   *
   * @return The substitution map provided by this class.
   */
  SubstitutionMap get();
}
