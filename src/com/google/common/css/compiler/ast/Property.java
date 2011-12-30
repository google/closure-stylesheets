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
        builder("azimuth"),
        builder("background-attachment"),
        builder("background-clip"),
        builder("background-color"),
        builder("background-image"),
        builder("background-position"),
        builder("background-position-x"),
        builder("background-position-y"),
        builder("background-repeat"),
        builder("background-size"),
        builder("background"),
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
        builder("box-shadow"),
        builder("box-sizing"),
        builder("caption-side"),
        builder("clear"),
        builder("clip"),
        builder("color"),
        builder("content"),
        builder("counter-increment"),
        builder("counter-reset"),
        builder("cue-after"),
        builder("cue-before"),
        builder("cue"),
        builder("cursor"),
        builder("direction"),
        builder("display"),
        builder("elevation"),
        builder("empty-cells"),
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
        builder("filter").setVendor(Vendor.MICROSOFT),
        builder("float"),
        builder("font-family"),
        builder("font-size"),
        builder("font-style"),
        builder("font-variant"),
        builder("font-weight"),
        builder("font"),
        builder("height"),
        builder("left"),
        builder("letter-spacing"),
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
        builder("max-height"),
        builder("max-width"),
        builder("min-height"),
        builder("min-width"),
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
        builder("-moz-background-inline-policy"),
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
        builder("-moz-box-align"),
        builder("-moz-box-direction"),
        builder("-moz-box-flex"),
        builder("-moz-box-ordinal-group"),
        builder("-moz-box-orient"),
        builder("-moz-box-pack"),
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
        builder("-moz-float-edge"),
        builder("-moz-font-feature-settings"),
        builder("-moz-font-language-override"),
        builder("-moz-force-broken-image-icon"),
        builder("-moz-hyphens"),
        builder("-moz-image-region"),
        builder("-moz-margin-end"),
        builder("-moz-margin-start"),
        builder("-moz-opacity"),
        builder("-moz-orient"),
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
        builder("-ms-background-position-x"),
        builder("-ms-background-position-y"),
        builder("-ms-behavior"),
        builder("-ms-block-progression"),
        builder("-ms-box-align"),
        builder("-ms-box-direction"),
        builder("-ms-box-flex"),
        builder("-ms-box-line-progression"),
        builder("-ms-box-lines"),
        builder("-ms-box-ordinal-group"),
        builder("-ms-box-orient"),
        builder("-ms-box-pack"),
        builder("-ms-filter"),
        builder("-ms-grid-column"),
        builder("-ms-grid-column-align"),
        builder("-ms-grid-column-span"),
        builder("-ms-grid-columns"),
        builder("-ms-grid-layer"),
        builder("-ms-grid-row"),
        builder("-ms-grid-row-align"),
        builder("-ms-grid-row-span"),
        builder("-ms-grid-rows"),
        builder("-ms-ime-mode"),
        builder("-ms-interpolation-mode"),
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
        builder("-ms-user-select"),
        builder("-ms-word-break"),
        builder("-ms-word-wrap"),
        builder("-ms-writing-mode"),
        builder("-ms-zoom"),
        builder("-o-background-size"),
        builder("-o-transform"),
        builder("-o-transform-origin"),
        builder("-o-transition"),
        builder("-o-transition-delay"),
        builder("-o-transition-duration"),
        builder("-o-transition-property"),
        builder("opacity"),
        builder("orphans"),
        builder("outline-color"),
        builder("outline-style"),
        builder("outline-width"),
        builder("outline"),
        builder("overflow"),
        builder("overflow-x"),
        builder("overflow-y"),
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
        builder("pitch-range"),
        builder("pitch"),
        builder("play-during"),
        builder("pointer-events"),
        builder("position"),
        builder("quotes"),
        builder("resize"),
        builder("richness"),
        builder("right"),
        builder("speak-header"),
        builder("speak-numeral"),
        builder("speak-punctuation"),
        builder("speak"),
        builder("speech-rate"),
        builder("stress"),
        builder("table-layout"),
        builder("text-align"),
        builder("text-decoration"),
        builder("text-indent"),
        builder("text-overflow"),
        builder("text-shadow"),
        builder("text-transform"),
        builder("transform"),
        builder("transform-origin"),
        builder("top"),
        builder("transition"),
        builder("unicode-bidi"),
        builder("vertical-align"),
        builder("visibility"),
        builder("voice-family"),
        builder("volume"),
        builder("-webkit-animation"),
        builder("-webkit-animation-delay"),
        builder("-webkit-animation-direction"),
        builder("-webkit-animation-duration"),
        builder("-webkit-animation-fill-mode"),
        builder("-webkit-animation-iteration-count"),
        builder("-webkit-animation-name"),
        builder("-webkit-animation-play-state"),
        builder("-webkit-animation-timing-function"),
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
        builder("-webkit-box-align"),
        builder("-webkit-box-direction"),
        builder("-webkit-box-flex"),
        builder("-webkit-box-flex-group"),
        builder("-webkit-box-lines"),
        builder("-webkit-box-ordinal-group"),
        builder("-webkit-box-orient"),
        builder("-webkit-box-pack"),
        builder("-webkit-box-reflect"),
        builder("-webkit-box-shadow"),
        builder("-webkit-box-sizing"),
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
        builder("-webkit-dashboard-region"),
        builder("-webkit-filter"),
        builder("-webkit-flex-align"),
        builder("-webkit-flex-direction"),
        builder("-webkit-flex-flow"),
        builder("-webkit-flex-order"),
        builder("-webkit-flex-pack"),
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
        builder("width"),
        builder("word-spacing"),
        builder("word-wrap"),
        builder("z-index"),
        builder("zoom")
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

  private Property(String name,
      Set<String> shorthands,
      String partition,
      @Nullable Vendor vendor,
      boolean hasPositionDependentValues) {
    this.name = name;
    this.shorthands = shorthands;
    this.partition = partition;
    this.vendor = vendor;
    this.hasPositionalParameters = hasPositionDependentValues;
  }

  private static Property createUserDefinedProperty(String name) {
    Preconditions.checkArgument(!NAME_TO_PROPERTY_MAP.containsKey(name));
    Builder builder = builder(name);
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

  /**
   * @return an immutable set of CSS properties recognized by default by the CSS
   *     Compiler
   */
  public static Set<String> allRecognizedPropertyNames() {
    return NAME_TO_PROPERTY_MAP.keySet();
  }

  /**
   * @return an immutable set of {@link Property}s recognized by default by the
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
    private final Set<String> shorthands;
    private final String partition;
    private Vendor vendor;
    private boolean hasPositionalParameters;

    private Builder(String name) {
      Preconditions.checkNotNull(name);
      this.name = name;
      this.shorthands = computeShorthandPropertiesFor(name);
      this.partition = Iterables.getFirst(this.shorthands, name);
      this.vendor = Vendor.parseProperty(name);
      this.hasPositionalParameters = false;
    }

    public Property build() {
      return new Property(
          this.name,
          this.shorthands,
          this.partition,
          this.vendor,
          this.hasPositionalParameters);
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

  /**
   * A browser vendor that provides non-standard CSS properties that can be
   * identified by a special prefix.
   */
  public static enum Vendor {
    WEBKIT("-webkit-"),
    MOZILLA("-moz-"),
    MICROSOFT("-ms-"),
    OPERA("-o-"),
    ;

    private final String prefix;

    private Vendor(String prefix) {
      this.prefix = prefix;
    }

    public static @Nullable Vendor parseProperty(String propertyName) {
      for (Vendor vendor : values()) {
        if (propertyName.startsWith(vendor.prefix)) {
          return vendor;
        }
      }
      return null;
    }
  }
}
