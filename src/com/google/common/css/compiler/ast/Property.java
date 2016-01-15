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

package com.google.common.css.compiler.ast;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.css.Vendor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * An object that represents a CSS property. Although this class contains a
 * large set of built-in properties (see {@link #allRecognizedProperties()}), it
 * is a class rather than an enum so that it is possible to create properties
 * that are not built-in. User agents will likely add support for new properties
 * faster than we can add them to this list.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public final class Property {

  /**
   * The CSS properties recognized by default by the CSS Compiler, indexed by
   * name. Note that this includes non-standard properties, such as
   * "-webkit-border-radius".
   */
  private static final BiMap<String, Property> NAME_TO_PROPERTY_MAP;


  static {
    List<Builder> recognizedProperties = ImmutableList.of(
        builder("align-content"),
        builder("align-items"),
        builder("align-self"),
        builder("alignment-baseline").isSvgOnly(),
        builder("animation"),
        builder("animation-delay"),
        builder("animation-direction"),
        builder("animation-duration"),
        builder("animation-fill-mode"),
        builder("animation-iteration-count"),
        builder("animation-name"),
        builder("animation-play-state"),
        builder("animation-timing-function"),
        builder("azimuth"),
        builder("backface-visibility"),
        builder("background-attachment"),
        builder("background-clip"),
        builder("background-color"),
        builder("background-image"),
        builder("background-origin"),
        builder("background-position"),
        builder("background-position-x"),
        builder("background-position-y"),
        builder("background-repeat"),
        builder("background-size"),
        builder("background"),
        builder("baseline-shift").isSvgOnly(),
        builder("border-collapse"),
        builder("border-color").setHasPositionalParameters(true),
        builder("border-spacing"),
        builder("border-style").setHasPositionalParameters(true),
        builder("border-top"),
        builder("border-right"),
        builder("border-bottom"),
        builder("border-left"),
        builder("border-top-color"),
        builder("border-right-color"),
        builder("border-bottom-color"),
        builder("border-left-color"),
        builder("border-top-style"),
        builder("border-right-style"),
        builder("border-bottom-style"),
        builder("border-left-style"),
        builder("border-top-width"),
        builder("border-right-width"),
        builder("border-bottom-width"),
        builder("border-left-width"),
        builder("border-width").setHasPositionalParameters(true),
        builder("border-image"),
        // None of border-radius, -webkit-border-radius, and -moz-border-radius
        // have setHasPositionalParameters(true) because each can take up to 8
        // values according to the spec, as it is possible to specify the length
        // of the horizontal and vertical radii independently for each corner:
        //
        // http://www.w3.org/TR/css3-background/#the-border-radius
        //
        // TODO(bolinfest): This is going to require special handling when it
        // comes to RTL flipping, which the current
        // "setHasPositionalParameters()" does not take into account.
        builder("border-radius"),
        builder("border-top-left-radius"),
        builder("border-top-right-radius"),
        builder("border-bottom-right-radius"),
        builder("border-bottom-left-radius"),
        builder("border"),
        builder("bottom"),
        builder("box-align").warn(
            "The flexbox spec has changed and this property is no longer supported."),
        builder("box-flex").warn(
            "The flexbox spec has changed and this property is no longer supported."),
        builder("box-orient").warn(
            "The flexbox spec has changed and this property is no longer supported."),
        builder("box-pack").warn(
            "The flexbox spec has changed and this property is no longer supported."),
        builder("box-shadow"),
        builder("box-sizing"),
        builder("break-after"),
        builder("break-before"),
        builder("break-inside"),
        builder("caption-side"),
        builder("clear"),
        builder("clip"),
        builder("clip-path").isSvgOnly(),
        builder("clip-rule").isSvgOnly(),
        builder("color"),
        builder("color-interpolation").isSvgOnly(),
        builder("color-interpolation-filters").isSvgOnly(),
        builder("color-profile").isSvgOnly(),
        builder("color-rendering").isSvgOnly(),
        builder("column-count"),
        builder("column-fill"),
        builder("column-gap"),
        builder("column-rule"),
        builder("column-rule-color"),
        builder("column-rule-style"),
        builder("column-rule-width"),
        builder("column-span"),
        builder("column-width"),
        builder("columns"),
        builder("content"),
        builder("counter-increment"),
        builder("counter-reset"),
        builder("cue-after"),
        builder("cue-before"),
        builder("cue"),
        builder("cursor"),
        builder("direction"),
        builder("display"),
        builder("dominant-baseline").isSvgOnly(),
        builder("elevation"),
        builder("empty-cells"),
        builder("enable-background").isSvgOnly(),
        builder("-epub-caption-side"),
        builder("-epub-hyphens"),
        builder("-epub-text-combine"),
        builder("-epub-text-emphasis"),
        builder("-epub-text-emphasis-color"),
        builder("-epub-text-emphasis-style"),
        builder("-epub-text-orientation"),
        builder("-epub-text-transform"),
        builder("-epub-word-break"),
        builder("-epub-writing-mode"),
        builder("fill").isSvgOnly(),
        builder("fill-opacity").isSvgOnly(),
        builder("fill-rule").isSvgOnly(),
        // This is not a MICROSOFT-specific property because it is also an SVG
        // property: http://www.w3.org/TR/SVG/styling.html
        builder("filter"),
        builder("flex"),
        builder("flex-basis"),
        builder("flex-direction"),
        builder("flex-flow"),
        builder("flex-grow"),
        builder("flex-shrink"),
        builder("flex-wrap"),
        builder("float"),
        builder("flood-color").isSvgOnly(),
        builder("flood-opacity").isSvgOnly(),
        builder("font"),
        builder("font-family"),
        builder("font-feature-settings"),
        builder("font-kerning"),
        builder("font-language-override"),
        builder("font-size"),
        builder("font-size-adjust"),
        builder("font-stretch"),
        builder("font-style"),
        builder("font-synthesis"),
        builder("font-variant"),
        builder("font-variant-alternates"),
        builder("font-variant-caps"),
        builder("font-variant-east-asian"),
        builder("font-variant-ligatures"),
        builder("font-variant-numeric"),
        builder("font-variant-position"),
        builder("font-weight"),
        builder("glyph-orientation-horizontal").isSvgOnly(),
        builder("glyph-orientation-vertical").isSvgOnly(),
        builder("height"),
        builder("image-rendering").isSvgOnly(),
        builder("justify-content"),
        builder("kerning").isSvgOnly(),
        builder("-khtml-user-select"),
        builder("left"),
        builder("letter-spacing"),
        builder("lighting-color").isSvgOnly(),
        builder("line-height"),
        builder("list-style-image"),
        builder("list-style-position"),
        builder("list-style-type"),
        builder("list-style"),
        builder("margin-right"),
        builder("margin-left"),
        builder("margin-top"),
        builder("margin-bottom"),
        builder("margin").setHasPositionalParameters(true),
        builder("marker").isSvgOnly(),
        builder("marker-end").isSvgOnly(),
        builder("marker-mid").isSvgOnly(),
        builder("marker-start").isSvgOnly(),
        builder("mask").isSvgOnly(),
        builder("max-height"),
        builder("max-width"),
        builder("min-height"),
        builder("min-width"),
        builder("mix-blend-mode"),
        builder("-moz-animation"),
        builder("-moz-animation-delay"),
        builder("-moz-animation-direction"),
        builder("-moz-animation-duration"),
        builder("-moz-animation-fill-mode"),
        builder("-moz-animation-iteration-count"),
        builder("-moz-animation-name"),
        builder("-moz-animation-play-state"),
        builder("-moz-animation-timing-function"),
        builder("-moz-appearance"),
        builder("-moz-backface-visibility"),
        builder("-moz-background-clip"),
        builder("-moz-background-inline-policy"),
        builder("-moz-background-origin"),
        builder("-moz-background-size"),
        builder("-moz-binding"),
        builder("-moz-border-bottom-colors"),
        builder("-moz-border-end"),
        builder("-moz-border-end-color"),
        builder("-moz-border-end-style"),
        builder("-moz-border-end-width"),
        builder("-moz-border-image"),
        builder("-moz-border-left-colors"),
        builder("-moz-border-radius"),
        builder("-moz-border-radius-topleft"),
        builder("-moz-border-radius-topright"),
        builder("-moz-border-radius-bottomright"),
        builder("-moz-border-radius-bottomleft"),
        builder("-moz-border-right-colors"),
        builder("-moz-border-start"),
        builder("-moz-border-start-color"),
        builder("-moz-border-start-style"),
        builder("-moz-border-start-width"),
        builder("-moz-border-top-colors"),
        // These deprecated flexbox properties are only supported for backwards compatibility.
        builder("-moz-box-align"),
        builder("-moz-box-direction"),
        builder("-moz-box-flex"),
        builder("-moz-box-ordinal-group"),
        builder("-moz-box-orient"),
        builder("-moz-box-pack"),
        // End of deprecated flexbox properties.
        builder("-moz-box-shadow"),
        builder("-moz-box-sizing"),
        builder("-moz-column-count"),
        builder("-moz-column-gap"),
        builder("-moz-column-rule"),
        builder("-moz-column-rule-color"),
        builder("-moz-column-rule-style"),
        builder("-moz-column-rule-width"),
        builder("-moz-column-width"),
        builder("-moz-columns"),
        builder("-moz-flex"),
        builder("-moz-flex-direction"),
        builder("-moz-float-edge"),
        builder("-moz-font-feature-settings"),
        builder("-moz-font-language-override"),
        builder("-moz-force-broken-image-icon"),
        builder("-moz-hyphens"),
        builder("-moz-image-region"),
        builder("-moz-justify-content"),
        builder("-moz-margin-end"),
        builder("-moz-margin-start"),
        builder("-moz-opacity"),
        builder("-moz-orient"),
        builder("-moz-osx-font-smoothing"),
        builder("-moz-outline-radius"),
        builder("-moz-outline-radius-bottomleft"),
        builder("-moz-outline-radius-bottomright"),
        builder("-moz-outline-radius-topleft"),
        builder("-moz-outline-radius-topright"),
        builder("-moz-padding-end"),
        builder("-moz-padding-start"),
        builder("-moz-perspective"),
        builder("-moz-perspective-origin"),
        builder("-moz-script-level"),
        builder("-moz-script-min-size"),
        builder("-moz-script-size-multiplier"),
        builder("-moz-stack-sizing"),
        builder("-moz-tab-size"),
        builder("-moz-text-blink"),
        builder("-moz-text-decoration-color"),
        builder("-moz-text-decoration-line"),
        builder("-moz-text-decoration-style"),
        builder("-moz-text-size-adjust"),
        builder("-moz-transform"),
        builder("-moz-transform-origin"),
        builder("-moz-transform-style"),
        builder("-moz-transition"),
        builder("-moz-transition-delay"),
        builder("-moz-transition-duration"),
        builder("-moz-transition-property"),
        builder("-moz-transition-timing-function"),
        builder("-moz-user-focus"),
        builder("-moz-user-input"),
        builder("-moz-user-modify"),
        builder("-moz-user-select"),
        builder("-moz-window-shadow"),
        builder("-ms-accelerator"),
        builder("-ms-align-items"),
        builder("-ms-animation"),
        builder("-ms-animation-delay"),
        builder("-ms-animation-direction"),
        builder("-ms-animation-duration"),
        builder("-ms-animation-fill-mode"),
        builder("-ms-animation-iteration-count"),
        builder("-ms-animation-name"),
        builder("-ms-animation-play-state"),
        builder("-ms-animation-timing-function"),
        builder("-ms-background-position-x"),
        builder("-ms-background-position-y"),
        builder("-ms-behavior"),
        builder("-ms-block-progression"),
        // These deprecated flexbox properties are only supported for backwards compatibility.
        builder("-ms-box-align"),
        builder("-ms-box-direction"),
        builder("-ms-box-flex"),
        builder("-ms-box-line-progression"),
        builder("-ms-box-lines"),
        builder("-ms-box-ordinal-group"),
        builder("-ms-box-orient"),
        builder("-ms-box-pack"),
        // End of deprecated flexbox properties.
        builder("-ms-box-shadow"),
        builder("-ms-box-sizing"),
        builder("-ms-filter"),
        builder("-ms-flex"),
        builder("-ms-flex-align"),
        builder("-ms-flex-direction"),
        builder("-ms-flex-flow"),
        builder("-ms-flex-negative"),
        builder("-ms-flex-order"),
        builder("-ms-flex-pack"),
        builder("-ms-flex-positive"),
        builder("-ms-flex-preferred-size"),
        builder("-ms-flex-wrap"),
        builder("-ms-grid-column"),
        builder("-ms-grid-column-align"),
        builder("-ms-grid-column-span"),
        builder("-ms-grid-columns"),
        builder("-ms-grid-layer"),
        builder("-ms-grid-row"),
        builder("-ms-grid-row-align"),
        builder("-ms-grid-row-span"),
        builder("-ms-grid-rows"),
        builder("-ms-high-contrast-adjust"),
        builder("-ms-ime-mode"),
        builder("-ms-interpolation-mode"),
        builder("-ms-justify-content"),
        builder("-ms-layout-flow"),
        builder("-ms-layout-grid"),
        builder("-ms-layout-grid-char"),
        builder("-ms-layout-grid-line"),
        builder("-ms-layout-grid-mode"),
        builder("-ms-layout-grid-type"),
        builder("-ms-line-break"),
        builder("-ms-line-grid-mode"),
        builder("-ms-overflow-x"),
        builder("-ms-overflow-y"),
        builder("-ms-scrollbar-3dlight-color"),
        builder("-ms-scrollbar-arrow-color"),
        builder("-ms-scrollbar-base-color"),
        builder("-ms-scrollbar-darkshadow-color"),
        builder("-ms-scrollbar-face-color"),
        builder("-ms-scrollbar-highlight-color"),
        builder("-ms-scrollbar-shadow-color"),
        builder("-ms-scrollbar-track-color"),
        builder("-ms-text-align-last"),
        builder("-ms-text-autospace"),
        builder("-ms-text-justify"),
        builder("-ms-text-kashida-space"),
        builder("-ms-text-overflow"),
        builder("-ms-text-size-adjust"),
        builder("-ms-text-underline-position"),
        builder("-ms-transform"),
        builder("-ms-transform-origin"),
        builder("-ms-transition"),
        builder("-ms-transition-delay"),
        builder("-ms-transition-duration"),
        builder("-ms-transition-property"),
        builder("-ms-transition-timing-function"),
        builder("-ms-user-select"),
        builder("-ms-word-break"),
        builder("-ms-word-wrap"),
        builder("-ms-writing-mode"),
        builder("-ms-zoom"),
        builder("-o-animation"),
        builder("-o-animation-delay"),
        builder("-o-animation-direction"),
        builder("-o-animation-duration"),
        builder("-o-animation-fill-mode"),
        builder("-o-animation-iteration-count"),
        builder("-o-animation-name"),
        builder("-o-animation-play-state"),
        builder("-o-animation-timing-function"),
        builder("-o-background-size"),
        builder("-o-object-fit"),
        builder("-o-object-position"),
        builder("-o-text-overflow"),
        builder("-o-transform"),
        builder("-o-transform-origin"),
        builder("-o-transition"),
        builder("-o-transition-delay"),
        builder("-o-transition-duration"),
        builder("-o-transition-property"),
        builder("-o-transition-timing-function"),
        builder("object-fit"),
        builder("object-position"),
        builder("opacity"),
        builder("order"),
        builder("orphans"),
        builder("outline-color"),
        builder("outline-offset"),
        builder("outline-style"),
        builder("outline-width"),
        builder("outline"),
        builder("overflow"),
        builder("overflow-x"),
        builder("overflow-y"),
        builder("overflow-wrap"),
        builder("padding-top"),
        builder("padding-right"),
        builder("padding-bottom"),
        builder("padding-left"),
        builder("padding").setHasPositionalParameters(true),
        builder("page-break-after"),
        builder("page-break-before"),
        builder("page-break-inside"),
        builder("pause-after"),
        builder("pause-before"),
        builder("pause"),
        builder("perspective"),
        builder("perspective-origin"),
        builder("pitch-range"),
        builder("pitch"),
        builder("play-during"),
        builder("pointer-events"),
        builder("position"),
        builder("quotes"),
        builder("resize"),
        builder("richness"),
        builder("right"),
        builder("scrollbar-3dlight-color").setVendor(Vendor.MICROSOFT),
        builder("scrollbar-arrow-color").setVendor(Vendor.MICROSOFT),
        builder("scrollbar-base-color").setVendor(Vendor.MICROSOFT),
        builder("scrollbar-darkshadow-color").setVendor(Vendor.MICROSOFT),
        builder("scrollbar-face-color").setVendor(Vendor.MICROSOFT),
        builder("scrollbar-highlight-color").setVendor(Vendor.MICROSOFT),
        builder("scrollbar-shadow-color").setVendor(Vendor.MICROSOFT),
        builder("scrollbar-track-color").setVendor(Vendor.MICROSOFT),
        builder("shape-rendering").isSvgOnly(),
        builder("shape-image-threshold"),
        builder("shape-margin"),
        builder("shape-outside"),
        builder("size"),
        builder("speak-header"),
        builder("speak-numeral"),
        builder("speak-punctuation"),
        builder("speak"),
        builder("speech-rate"),
        builder("src"), // Only for use within @font-face
        builder("stop-color").isSvgOnly(),
        builder("stop-opacity").isSvgOnly(),
        builder("stress"),
        builder("stroke").isSvgOnly(),
        builder("stroke-dasharray").isSvgOnly(),
        builder("stroke-dashoffset").isSvgOnly(),
        builder("stroke-linecap").isSvgOnly(),
        builder("stroke-linejoin").isSvgOnly(),
        builder("stroke-miterlimit").isSvgOnly(),
        builder("stroke-opacity").isSvgOnly(),
        builder("stroke-width").isSvgOnly(),
        builder("table-layout"),
        builder("text-align"),
        builder("text-anchor").isSvgOnly(),
        builder("text-decoration"),
        builder("text-decoration-color"),
        builder("text-decoration-line"),
        builder("text-decoration-skip"),
        builder("text-decoration-style"),
        builder("text-emphasis"),
        builder("text-emphasis-color"),
        builder("text-emphasis-position"),
        builder("text-emphasis-style"),
        builder("text-indent"),
        builder("text-overflow"),
        builder("text-rendering").isSvgOnly(),
        builder("text-shadow"),
        builder("text-transform"),
        builder("top"),
        builder("transform"),
        builder("transform-origin"),
        builder("transform-style"),
        builder("transition-delay"),
        builder("transition-duration"),
        builder("transition-property"),
        builder("transition-timing-function"),
        builder("transition"),
        builder("unicode-bidi"),
        builder("unicode-range"),
        builder("vertical-align"),
        builder("visibility"),
        builder("voice-balance"),
        builder("voice-duration"),
        builder("voice-family"),
        builder("voice-pitch"),
        builder("voice-range"),
        builder("voice-rate"),
        builder("voice-stress"),
        builder("voice-volume"),
        builder("volume"),
        builder("-webkit-align-content"),
        builder("-webkit-align-items"),
        builder("-webkit-align-self"),
        builder("-webkit-animation"),
        builder("-webkit-animation-delay"),
        builder("-webkit-animation-direction"),
        builder("-webkit-animation-duration"),
        builder("-webkit-animation-fill-mode"),
        builder("-webkit-animation-iteration-count"),
        builder("-webkit-animation-name"),
        builder("-webkit-animation-play-state"),
        builder("-webkit-animation-timing-function"),
        builder("-webkit-app-region"),
        builder("-webkit-appearance"),
        builder("-webkit-aspect-ratio"),
        builder("-webkit-backface-visibility"),
        builder("-webkit-background-clip"),
        builder("-webkit-background-composite"),
        builder("-webkit-background-origin"),
        builder("-webkit-background-size"),
        builder("-webkit-border-after"),
        builder("-webkit-border-after-color"),
        builder("-webkit-border-after-style"),
        builder("-webkit-border-after-width"),
        builder("-webkit-border-before"),
        builder("-webkit-border-before-color"),
        builder("-webkit-border-before-style"),
        builder("-webkit-border-before-width"),
        builder("-webkit-border-bottom-left-radius"),
        builder("-webkit-border-bottom-right-radius"),
        builder("-webkit-border-end"),
        builder("-webkit-border-end-color"),
        builder("-webkit-border-end-style"),
        builder("-webkit-border-end-width"),
        builder("-webkit-border-fit"),
        builder("-webkit-border-horizontal-spacing"),
        builder("-webkit-border-image"),
        builder("-webkit-border-radius"),
        builder("-webkit-border-start"),
        builder("-webkit-border-start-color"),
        builder("-webkit-border-start-style"),
        builder("-webkit-border-start-width"),
        builder("-webkit-border-top-left-radius"),
        builder("-webkit-border-top-right-radius"),
        builder("-webkit-border-vertical-spacing"),
        // These deprecated flexbox properties are only supported for backwards compatibility.
        builder("-webkit-box-align"),
        builder("-webkit-box-direction"),
        builder("-webkit-box-flex"),
        builder("-webkit-box-flex-group"),
        builder("-webkit-box-lines"),
        builder("-webkit-box-ordinal-group"),
        builder("-webkit-box-orient"),
        builder("-webkit-box-pack"),
        // End of deprecated flexbox properties.
        builder("-webkit-box-reflect"),
        builder("-webkit-box-shadow"),
        builder("-webkit-box-sizing"),
        builder("-webkit-clip-path"),
        builder("-webkit-color-correction"),
        builder("-webkit-column-axis"),
        builder("-webkit-column-break-after"),
        builder("-webkit-column-break-before"),
        builder("-webkit-column-break-inside"),
        builder("-webkit-column-count"),
        builder("-webkit-column-gap"),
        builder("-webkit-column-rule"),
        builder("-webkit-column-rule-color"),
        builder("-webkit-column-rule-style"),
        builder("-webkit-column-rule-width"),
        builder("-webkit-column-span"),
        builder("-webkit-column-width"),
        builder("-webkit-columns"),
        builder("-webkit-cursor-visibility"),
        builder("-webkit-dashboard-region"),
        builder("-webkit-filter"),
        builder("-webkit-flex"),
        builder("-webkit-flex-align"),
        builder("-webkit-flex-basis"),
        builder("-webkit-flex-direction"),
        builder("-webkit-flex-flow"),
        builder("-webkit-flex-grow"),
        builder("-webkit-flex-order"),
        builder("-webkit-flex-pack"),
        builder("-webkit-flex-shrink"),
        builder("-webkit-flex-wrap"),
        builder("-webkit-flow-from"),
        builder("-webkit-flow-into"),
        builder("-webkit-font-feature-settings"),
        builder("-webkit-font-size-delta"),
        builder("-webkit-font-smoothing"),
        builder("-webkit-grid-columns"),
        builder("-webkit-grid-rows"),
        builder("-webkit-highlight"),
        builder("-webkit-hyphenate-character"),
        builder("-webkit-hyphenate-limit-after"),
        builder("-webkit-hyphenate-limit-before"),
        builder("-webkit-hyphenate-limit-lines"),
        builder("-webkit-justify-content"),
        builder("-webkit-line-box-contain"),
        builder("-webkit-line-break"),
        builder("-webkit-line-clamp"),
        builder("-webkit-line-grid"),
        builder("-webkit-line-grid-snap"),
        builder("-webkit-locale"),
        builder("-webkit-logical-height"),
        builder("-webkit-logical-width"),
        builder("-webkit-margin-after"),
        builder("-webkit-margin-after-collapse"),
        builder("-webkit-margin-before"),
        builder("-webkit-margin-before-collapse"),
        builder("-webkit-margin-bottom-collapse"),
        builder("-webkit-margin-collapse"),
        builder("-webkit-margin-end"),
        builder("-webkit-margin-start"),
        builder("-webkit-margin-top-collapse"),
        builder("-webkit-marquee"),
        builder("-webkit-marquee-direction"),
        builder("-webkit-marquee-increment"),
        builder("-webkit-marquee-repetition"),
        builder("-webkit-marquee-speed"),
        builder("-webkit-marquee-style"),
        builder("-webkit-mask"),
        builder("-webkit-mask-attachment"),
        builder("-webkit-mask-box-image"),
        builder("-webkit-mask-box-image-outset"),
        builder("-webkit-mask-box-image-repeat"),
        builder("-webkit-mask-box-image-slice"),
        builder("-webkit-mask-box-image-source"),
        builder("-webkit-mask-box-image-width"),
        builder("-webkit-mask-clip"),
        builder("-webkit-mask-composite"),
        builder("-webkit-mask-image"),
        builder("-webkit-mask-origin"),
        builder("-webkit-mask-position"),
        builder("-webkit-mask-position-x"),
        builder("-webkit-mask-position-y"),
        builder("-webkit-mask-repeat"),
        builder("-webkit-mask-repeat-x"),
        builder("-webkit-mask-repeat-y"),
        builder("-webkit-mask-size"),
        builder("-webkit-match-nearest-mail-blockquote-color"),
        builder("-webkit-max-logical-height"),
        builder("-webkit-max-logical-width"),
        builder("-webkit-min-logical-height"),
        builder("-webkit-min-logical-width"),
        builder("-webkit-nbsp-mode"),
        builder("-webkit-opacity"),
        builder("-webkit-order"),
        builder("-webkit-overflow-scrolling"),
        builder("-webkit-padding-after"),
        builder("-webkit-padding-before"),
        builder("-webkit-padding-end"),
        builder("-webkit-padding-start"),
        builder("-webkit-perspective"),
        builder("-webkit-perspective-origin"),
        builder("-webkit-perspective-origin-x"),
        builder("-webkit-perspective-origin-y"),
        builder("-webkit-print-color-adjust"),
        builder("-webkit-region-break-after"),
        builder("-webkit-region-break-before"),
        builder("-webkit-region-break-inside"),
        builder("-webkit-region-overflow"),
        builder("-webkit-rtl-ordering"),
        builder("-webkit-svg-shadow"),
        builder("-webkit-tap-highlight-color"),
        builder("-webkit-text-decorations-in-effect"),
        builder("-webkit-text-emphasis-position"),
        builder("-webkit-text-fill-color"),
        builder("-webkit-text-security"),
        builder("-webkit-text-size-adjust"),
        builder("-webkit-text-stroke"),
        builder("-webkit-text-stroke-color"),
        builder("-webkit-text-stroke-width"),
        builder("-webkit-touch-callout"),
        builder("-webkit-transform"),
        builder("-webkit-transform-origin"),
        builder("-webkit-transform-origin-x"),
        builder("-webkit-transform-origin-y"),
        builder("-webkit-transform-origin-z"),
        builder("-webkit-transform-style"),
        builder("-webkit-transition"),
        builder("-webkit-transition-delay"),
        builder("-webkit-transition-duration"),
        builder("-webkit-transition-function"),
        builder("-webkit-transition-property"),
        builder("-webkit-transition-timing-function"),
        builder("-webkit-user-drag"),
        builder("-webkit-user-modify"),
        builder("-webkit-user-select"),
        builder("-webkit-wrap"),
        builder("-webkit-wrap-flow"),
        builder("-webkit-wrap-margin"),
        builder("-webkit-wrap-padding"),
        builder("-webkit-wrap-shape-inside"),
        builder("-webkit-wrap-shape-outside"),
        builder("-webkit-wrap-through"),
        builder("white-space"),
        builder("windows"),
        builder("will-change"),
        builder("width"),
        builder("word-break"),
        builder("word-spacing"),
        builder("word-wrap"),
        builder("writing-mode").isSvgOnly(),
        builder("z-index"),
        builder("zoom").setVendor(Vendor.MICROSOFT)
    );
    ImmutableBiMap.Builder<String, Property> allProperies =
        ImmutableBiMap.builder();
    for (Builder builder : recognizedProperties) {
      Property property = builder.build();
      allProperies.put(property.getName(), property);
    }
    NAME_TO_PROPERTY_MAP = allProperies.build();
  }

  private final String name;

  private final Set<String> shorthands;

  private final String partition;

  @Nullable
  private final Vendor vendor;

  private final boolean hasPositionalParameters;

  private final boolean isSvgOnly;

  private final String warning;

  private Property(String name,
      Set<String> shorthands,
      String partition,
      @Nullable Vendor vendor,
      boolean hasPositionDependentValues,
      boolean isSvgOnly,
      @Nullable String warning) {
    Preconditions.checkArgument(name.equals(name.toLowerCase()),
        "property name should be all lowercase: %s", name);
    this.name = name;
    this.shorthands = shorthands;
    this.partition = partition;
    this.vendor = vendor;
    this.hasPositionalParameters = hasPositionDependentValues;
    this.isSvgOnly = isSvgOnly;
    this.warning = warning;
  }

  private static Property createUserDefinedProperty(String name) {
    Preconditions.checkArgument(!NAME_TO_PROPERTY_MAP.containsKey(name));
    Builder builder = builder(name)
        .setShorthands(ImmutableSet.<String>of());
    return builder.build();
  }

  /**
   * @return a {@code Property} with the specified {@code name}. If {@code name}
   *     corresponds to a recognized property, then the corresponding
   *     {@code Property} will be returned; otherwise, a new {@code Property}
   *     with the specified {@code name} will be created.
   */
  public static Property byName(String name) {
    Property property = NAME_TO_PROPERTY_MAP.get(name);
    if (property != null) {
      return property;
    } else {
      return Property.createUserDefinedProperty(name);
    }
  }

  /**
   * @return the name of this CSS property as it appears in a stylesheet, such
   *     as "z-index"
   */
  public String getName() {
    return name;
  }

  /**
   * @return whether this Property is recognized by default by the CSS Compiler.
   *     Note that this is not the same as a being a "standard" CSS property
   *     because the CSS Compiler recognizes non-standard CSS properties such as
   *     "-webkit-border-radius", among others.
   */
  public boolean isRecognizedProperty() {
    return NAME_TO_PROPERTY_MAP.containsKey(name);
  }

  /**
   * Returns the set of shorthand properties related to this property, or the
   * empty set if this property is not standard.
   *
   * <p>For example, {@code border-left-style} has {@code border},
   * {@code border-left} and {@code border-style} as shorthands.
   */
  public Set<String> getShorthands() {
    return shorthands;
  }

  /**
   * Gets the partition of this property. All properties with the same partition
   * share a common shorthand. A non-standard property is its own single
   * partition.
   * <p>
   * For example, {@code padding}, {@code padding-bottom}, {@code padding-left},
   * {@code padding-right}, {@code padding-top} are all in the {@code padding}
   * partition. As another example, {@code z-index} is its own single partition.
   *
   * @return a string representing the partition
   */
  public String getPartition() {
    return partition;
  }

  /**
   * @return whether this Property is a vendor-specific CSS property, such as
   *     "-webkit-border-radius"
   */
  public boolean isVendorSpecific() {
    return vendor != null;
  }

  /**
   * @return the corresponding {@link Vendor} if {@link #isVendorSpecific()}
   *     returns {@code true}; otherwise, returns {@code null}
   */
  public @Nullable Vendor getVendor() {
    return vendor;
  }

  /**
   * @return whether this property can take positional parameters, such as
   *     "margin", where the parameters "1px 2px 3px" imply "1px 2px 3px 2px"
   */
  public boolean hasPositionalParameters() {
    return hasPositionalParameters;
  }

  public boolean isSvgOnly() {
    return isSvgOnly;
  }

  public boolean hasWarning() {
    return warning != null;
  }

  public String getWarning() {
    return warning;
  }

  /**
   * @return an immutable set of CSS properties recognized by default by the CSS
   *     Compiler
   */
  public static Set<String> allRecognizedPropertyNames() {
    return NAME_TO_PROPERTY_MAP.keySet();
  }

  /**
   * @return an immutable set of {@link Property} instances recognized by default by the
   *     CSS Compiler
   */
  public static Set<Property> allRecognizedProperties() {
    return NAME_TO_PROPERTY_MAP.values();
  }

  private static Builder builder(String name) {
    return new Builder(name);
  }

  /**
   * For now, this class is private. If it turns out there is a legitimate need
   * to create properties through a public builder (a need that cannot be met
   * by {@link Property#byName(String)}), then this class should be changed so
   * that it is public.
   */
  @VisibleForTesting
  static final class Builder {
    private final String name;
    private Set<String> shorthands;
    private Vendor vendor;
    private boolean hasPositionalParameters;
    private boolean isSvgOnly;
    private String warning;

    private Builder(String name) {
      Preconditions.checkNotNull(name);
      this.name = name;
      this.shorthands = null;
      this.vendor = Vendor.parseProperty(name);
      this.hasPositionalParameters = false;
      this.isSvgOnly = false;
      this.warning = null;
    }

    public Property build() {
      if (this.shorthands == null) {
        this.shorthands = computeShorthandPropertiesFor(this.name);
      }
      String partition = Iterables.getFirst(this.shorthands, name);
      return new Property(
          this.name,
          this.shorthands,
          partition,
          this.vendor,
          this.hasPositionalParameters,
          this.isSvgOnly,
          this.warning);
    }

    public Builder setVendor(Vendor vendor) {
      this.vendor = vendor;
      return this;
    }

    public Builder setHasPositionalParameters(boolean hasPositionalParameters) {
      this.hasPositionalParameters = hasPositionalParameters;
      return this;
    }

    /**
     * Indicates that the property is relevant only when styling SVG, but not
     * HTML.
     */
    public Builder isSvgOnly() {
      this.isSvgOnly = true;
      return this;
    }

    public Builder setShorthands(Set<String> shorthands) {
      this.shorthands = shorthands;
      return this;
    }

    /**
     * @param warning to display when this property is referenced
     */
    public Builder warn(String warning) {
      this.warning = warning;
      return this;
    }

    /**
     * The set of all standard shorthand properties.
     */
    private static final Set<String> SHORTHAND_PROPERTIES = ImmutableSet.of(
        "background",
        "border",
        "border-bottom",
        "border-color",
        "border-left",
        "border-right",
        "border-style",
        "border-top",
        "border-width",
        "font",
        "list-style",
        "margin",
        "outline",
        "padding",
        "pause");

    /**
     * The set of standard properties that seem to have shorthands as defined by
     * {@link #computeShorthandPropertiesFor}, but don't.
     */
    private static final Set<String> NO_SHORTHAND = ImmutableSet.of(
        "border-collapse",
        "border-spacing");

    private static final Map<String, String> BORDER_RADIUS_PROPERTIES =
        ImmutableMap.<String, String>builder()
        .put("border-radius", "border-radius")
        .put("border-top-left-radius", "border-radius")
        .put("border-top-right-radius", "border-radius")
        .put("border-bottom-right-radius", "border-radius")
        .put("border-bottom-left-radius", "border-radius")
        .put("-webkit-border-radius", "-webkit-border-radius")
        .put("-webkit-border-top-left-radius", "-webkit-border-radius")
        .put("-webkit-border-top-right-radius", "-webkit-border-radius")
        .put("-webkit-border-bottom-right-radius", "-webkit-border-radius")
        .put("-webkit-border-bottom-left-radius", "-webkit-border-radius")
        .put("-moz-border-radius", "-moz-border-radius")
        .put("-moz-border-radius-topleft", "-moz-border-radius")
        .put("-moz-border-radius-topright", "-moz-border-radius")
        .put("-moz-border-radius-bottomright", "-moz-border-radius")
        .put("-moz-border-radius-bottomleft", "-moz-border-radius")
        .build();

    /**
     * Computes the set of shorthand properties for a given standard property.
     *
     * <p>As a first approximation, property 'x-y' would have 'x' as a
     * shorthand, and property 'x-y-z' would have 'x', 'x-y' and 'x-z' as
     * shorthands. However, at each stage , we also check that the shorthands
     * computed as above are actually part of the given standard shorthand
     * properties. If not, then we return without including them. Note that
     * since we assume that the given shorthand properties are standard, only a
     * border-related shorthand such as 'border-left-color' can have three
     * shorthands. All other properties have either zero or one shorthand.
     *
     * @param property the given standard property
     * @return the set of shorthands of the given property (not including the
     *     property itself if it is a shorthand), a subset of the given set of
     *     standard shorthand properties
     */
    private static ImmutableSet<String> computeShorthandPropertiesFor(
        String property) {
      if (NO_SHORTHAND.contains(property)) {
        return ImmutableSet.of();
      }

      int lastHyphenIndex = property.lastIndexOf('-');
      if (lastHyphenIndex == -1) {
        return ImmutableSet.of();
      }

      // Special-case border-radius properties because they are particularly
      // odd.
      if (BORDER_RADIUS_PROPERTIES.keySet().contains(property)) {
        return ImmutableSet.of(BORDER_RADIUS_PROPERTIES.get(property));
      }

      String possibleShorthand = property.substring(0, lastHyphenIndex);
      if (!SHORTHAND_PROPERTIES.contains(possibleShorthand)) {
        return ImmutableSet.of();
      }

      int butlastHyphenIndex = possibleShorthand.lastIndexOf('-');
      if (butlastHyphenIndex == -1) {
        return ImmutableSet.of(possibleShorthand);
      }

      String smallestShorthand = property.substring(0, butlastHyphenIndex);
      if (!SHORTHAND_PROPERTIES.contains(smallestShorthand)) {
        return ImmutableSet.of(possibleShorthand);
      }

      // Only border-related properties may have more than one shorthand,
      // in which case they will have three shorthands.
      Preconditions.checkArgument(smallestShorthand.equals("border"));

      String otherShorthand =
          smallestShorthand + property.substring(lastHyphenIndex);

      Preconditions.checkArgument(SHORTHAND_PROPERTIES.contains(
          otherShorthand), "%s is not a shorthand property for %s",
          otherShorthand, property);

      return ImmutableSet.of(
          smallestShorthand, possibleShorthand, otherShorthand);
    }
  }
}
