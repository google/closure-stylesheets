/*
 * Copyright 2015 Google Inc.
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

package com.google.common.css.compiler.passes;

import com.google.common.collect.ImmutableList;

/**
 * A utility for the AutoExpandBrowserPrefix pass, which provides a list of rules
 * that govern automatic addition of browser specific property declarations.
 *
 * <p>Currently three most common cases are handled:
 * #1 Matching and replacing only the property name. Eg. flex-grow: VALUE;
 * #2 Matching property name and value, replacing the value. Eg. display: flex;
 * #3 Matching property name and value where value is a function, replacing the function name.
 *    Eg. background-image: linear-gradient(ARGS);
 *
 */
final class BrowserPrefixGenerator {

  private static ImmutableList<BrowserPrefixRule> expansionRules = null;

  /**
   * Builds and returns the rules for automatic expansion of mixins.
   */
  static ImmutableList<BrowserPrefixRule> getExpansionRules() {
    if (expansionRules == null) {
      expansionRules = buildExpansionRules();
    }
    return expansionRules;
  }

  private static ImmutableList<BrowserPrefixRule> buildExpansionRules() {
    ImmutableList.Builder<BrowserPrefixRule> builder = ImmutableList.builder();
    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("background-image")
        .matchPropertyValue("linear-gradient")
        .isFunction(true)
        .addExpandPropertyValue("-webkit-linear-gradient")
        .addExpandPropertyValue("-moz-linear-gradient")
        .addExpandPropertyValue("-ms-linear-gradient")
        .addExpandPropertyValue("-o-linear-gradient")
        .addExpandPropertyValue("linear-gradient")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("display")
        .matchPropertyValue("flex")
        .isFunction(false)
        .addExpandPropertyValue("-webkit-box")
        .addExpandPropertyValue("-moz-box")
        .addExpandPropertyValue("-webkit-flex")
        .addExpandPropertyValue("-ms-flexbox")
        .addExpandPropertyValue("flex")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("flex-grow")
        .isFunction(false)
        .addExpandPropertyName("-webkit-box-flex")
        .addExpandPropertyName("box-flex")
        .addExpandPropertyName("-ms-flex-positive")
        .addExpandPropertyName("-webkit-flex-grow")
        .addExpandPropertyName("flex-grow")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("animation")
        .isFunction(false)
        .addExpandPropertyName("-webkit-animation")
        .addExpandPropertyName("-moz-animation")
        .addExpandPropertyName("-o-animation")
        .addExpandPropertyName("animation")
        .build());

    return builder.build();
  }
}
