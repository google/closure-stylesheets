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
        .matchPropertyName("display")
        .matchPropertyValue("inline-flex")
        .isFunction(false)
        .addExpandPropertyValue("-webkit-inline-box")
        .addExpandPropertyValue("-webkit-inline-flex")
        .addExpandPropertyValue("-ms-inline-flexbox")
        .addExpandPropertyValue("inline-flex")
        .build());

     builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("flex-flow")
        .isFunction(false)
        .addExpandPropertyName("-ms-flex-flow")
        .addExpandPropertyName("-webkit-flex-flow")
        .addExpandPropertyName("flex-flow")
        .build());

     builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("flex-direction")
        .isFunction(false)
        .addExpandPropertyName("-ms-flex-direction")
        .addExpandPropertyName("-webkit-flex-direction")
        .addExpandPropertyName("flex-direction")
        .build());

     builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("flex-wrap")
        .isFunction(false)
        .addExpandPropertyName("-moz-flex-wrap")
        .addExpandPropertyName("-ms-flex-wrap")
        .addExpandPropertyName("-webkit-flex-wrap")
        .addExpandPropertyName("flex-wrap")
        .build());

     builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("flex")
        .isFunction(false)
        .addExpandPropertyName("-ms-flex")
        .addExpandPropertyName("-webkit-flex")
        .addExpandPropertyName("flex")
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
        .matchPropertyName("flex-shrink")
        .isFunction(false)
        .addExpandPropertyName("-ms-flex-shrink")
        .addExpandPropertyName("-webkit-flex-shrink")
        .addExpandPropertyName("flex-shrink")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("align-items")
        .isFunction(false)
        .addExpandPropertyName("-webkit-align-items")
        .addExpandPropertyName("align-items")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("align-self")
        .isFunction(false)
        .addExpandPropertyName("-webkit-align-self")
        .addExpandPropertyName("align-self")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("justify-content")
        .isFunction(false)
        .addExpandPropertyName("-webkit-justify-content")
        .addExpandPropertyName("justify-content")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("animation")
        .isFunction(false)
        .addExpandPropertyName("-webkit-animation")
        .addExpandPropertyName("-moz-animation")
        .addExpandPropertyName("-o-animation")
        .addExpandPropertyName("animation")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("animation-delay")
        .isFunction(false)
        .addExpandPropertyName("-webkit-animation-delay")
        .addExpandPropertyName("-moz-animation-delay")
        .addExpandPropertyName("-o-animation-delay")
        .addExpandPropertyName("animation-delay")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("animation-direction")
        .isFunction(false)
        .addExpandPropertyName("-webkit-animation-direction")
        .addExpandPropertyName("-moz-animation-direction")
        .addExpandPropertyName("-o-animation-direction")
        .addExpandPropertyName("animation-direction")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("animation-duration")
        .isFunction(false)
        .addExpandPropertyName("-webkit-animation-duration")
        .addExpandPropertyName("-moz-animation-duration")
        .addExpandPropertyName("-o-animation-duration")
        .addExpandPropertyName("animation-duration")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("animation-fill-mode")
        .isFunction(false)
        .addExpandPropertyName("-webkit-animation-fill-mode")
        .addExpandPropertyName("animation-fill-mode")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("animation-iteration-count")
        .isFunction(false)
        .addExpandPropertyName("-webkit-animation-iteration-count")
        .addExpandPropertyName("-moz-animation-iteration-count")
        .addExpandPropertyName("-o-animation-iteration-count")
        .addExpandPropertyName("animation-iteration-count")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("animation-name")
        .isFunction(false)
        .addExpandPropertyName("-webkit-animation-name")
        .addExpandPropertyName("-moz-animation-name")
        .addExpandPropertyName("-o-animation-name")
        .addExpandPropertyName("animation-name")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("animation-timing-function")
        .isFunction(false)
        .addExpandPropertyName("-webkit-animation-timing-function")
        .addExpandPropertyName("-moz-animation-timing-function")
        .addExpandPropertyName("-o-animation-timing-function")
        .addExpandPropertyName("animation-timing-function")
        .build());

    // Useful for high resolution (retina) displays.
    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("background-size")
        .isFunction(false)
        .addExpandPropertyName("-webkit-background-size")
        .addExpandPropertyName("-moz-background-size")
        .addExpandPropertyName("-o-background-size")
        .addExpandPropertyName("background-size")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("backface-visibility")
        .isFunction(false)
        .addExpandPropertyName("-webkit-backface-visibility")
        .addExpandPropertyName("-moz-backface-visibility")
        .addExpandPropertyName("-o-backface-visibility")
        .addExpandPropertyName("backface-visibility")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("border-radius")
        .isFunction(false)
        .addExpandPropertyName("-webkit-border-radius")
        .addExpandPropertyName("-moz-border-radius")
        .addExpandPropertyName("border-radius")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("box-shadow")
        .isFunction(false)
        .addExpandPropertyName("-webkit-box-shadow")
        .addExpandPropertyName("-moz-box-shadow")
        .addExpandPropertyName("box-shadow")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("box-sizing")
        .isFunction(false)
        .addExpandPropertyName("-webkit-box-sizing")
        .addExpandPropertyName("-moz-box-sizing")
        .addExpandPropertyName("box-sizing")
        .build());

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
        .matchPropertyName("background-image")
        .matchPropertyValue("repeating-linear-gradient")
        .isFunction(true)
        .addExpandPropertyValue("-webkit-repeating-linear-gradient")
        .addExpandPropertyValue("repeating-linear-gradient")
        .build());

    // Needed for Firefox 15, Chrome 25, Safari 6, iOS Safari 6.1 or less
    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyValue("calc")
        .isFunction(true)
        .addExpandPropertyValue("-webkit-calc")
        .addExpandPropertyValue("-moz-calc")
        .addExpandPropertyValue("calc")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("perspective")
        .isFunction(false)
        .addExpandPropertyName("-webkit-perspective")
        .addExpandPropertyName("-moz-perspective")
        .addExpandPropertyName("perspective")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("perspective-origin")
        .isFunction(false)
        .addExpandPropertyName("-webkit-perspective-origin")
        .addExpandPropertyName("-moz-perspective-origin")
        .addExpandPropertyName("perspective-origin")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("background-image")
        .matchPropertyValue("radial-gradient")
        .isFunction(true)
        .addExpandPropertyValue("-webkit-radial-gradient")
        .addExpandPropertyValue("-moz-radial-gradient")
        .addExpandPropertyValue("-o-radial-gradient")
        .addExpandPropertyValue("radial-gradient")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("perspective-origin")
        .isFunction(false)
        .addExpandPropertyName("-webkit-perspective-origin")
        .addExpandPropertyName("-moz-perspective-origin")
        .addExpandPropertyName("perspective-origin")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("transform")
        .isFunction(false)
        .addExpandPropertyName("-webkit-transform")
        .addExpandPropertyName("-moz-transform")
        .addExpandPropertyName("-ms-transform")
        .addExpandPropertyName("-o-transform")
        .addExpandPropertyName("transform")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("transform-origin")
        .isFunction(false)
        .addExpandPropertyName("-webkit-transform-origin")
        .addExpandPropertyName("-moz-transform-origin")
        .addExpandPropertyName("-ms-transform-origin")
        .addExpandPropertyName("-o-transform-origin")
        .addExpandPropertyName("transform-origin")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("transform-style")
        .isFunction(false)
        .addExpandPropertyName("-webkit-transform-style")
        .addExpandPropertyName("-moz-transform-style")
        .addExpandPropertyName("transform-style")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("transition")
        .isFunction(false)
        .addExpandPropertyName("-webkit-transition")
        .addExpandPropertyName("-moz-transition")
        .addExpandPropertyName("-o-transition")
        .addExpandPropertyName("transition")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("transition-delay")
        .isFunction(false)
        .addExpandPropertyName("-webkit-transition-delay")
        .addExpandPropertyName("-moz-transition-delay")
        .addExpandPropertyName("-o-transition-delay")
        .addExpandPropertyName("transition-delay")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("transition-duration")
        .isFunction(false)
        .addExpandPropertyName("-webkit-transition-duration")
        .addExpandPropertyName("-moz-transition-duration")
        .addExpandPropertyName("-o-transition-duration")
        .addExpandPropertyName("transition-duration")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("transition-property")
        .isFunction(false)
        .addExpandPropertyName("-webkit-transition-property")
        .addExpandPropertyName("-moz-transition-property")
        .addExpandPropertyName("-o-transition-property")
        .addExpandPropertyName("transition-property")
        .build());

    builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("transition-timing-function")
        .isFunction(false)
        .addExpandPropertyName("-webkit-transition-timing-function")
        .addExpandPropertyName("-moz-transition-timing-function")
        .addExpandPropertyName("-o-transition-timing-function")
        .addExpandPropertyName("transition-timing-function")
        .build());

        builder.add(new BrowserPrefixRule.Builder()
        .matchPropertyName("user-select")
        .isFunction(false)
         // Don't add user-select to expansion list as it is non-standard.
        .addExpandPropertyName("-webkit-user-select")
        .addExpandPropertyName("-moz-user-select")
        .addExpandPropertyName("-ms-user-select")
        .build());

    return builder.build();
  }
}
