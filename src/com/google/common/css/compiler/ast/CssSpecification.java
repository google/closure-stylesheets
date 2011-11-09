/*
 * Copyright 2008 Google Inc.
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

package com.google.common.css.compiler.ast;

/**
 */
public enum CssSpecification {
  // TODO(user): This is not very useful. A more useful class would allow
  //     us to specify individual CSS 3 modules and figure out the relationship
  //     between different CSS standards.
  CSS_1,
  CSS_2,
  CSS_2_1,
  CSS_3_DRAFT, // This refers to the set of all CSS 3 modules
  GSS,
  NONSTANDARD
}
