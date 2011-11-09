/*
 * Copyright 2010 Google Inc.
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

import java.util.Map;

/**
 * Provides maps of custom GSS functions for use with command-line compilers.
 *
 * <p>Any implementation should provide a parameterless constructor, as the
 * provider is instantiated via {@link Class#newInstance()}.
 *
 */
public interface GssFunctionMapProvider {
  /**
   * Gets the map of custom GSS functions for the given class.
   *
   * @param <F> the interface implemented by the GSS functions
   * @param gssFunctionClass the class of {@code F}
   * @return a map from each custom function name to its implementation
   */
  <F> Map<String, F> get(Class<F> gssFunctionClass);
}
