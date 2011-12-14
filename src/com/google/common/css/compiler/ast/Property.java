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

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.List;
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
        // TODO(bolinfest): Add more properties, including non-standard ones,
        // such as "-webkit-border-radius".
        builder("azimuth"),
        builder("background-attachment"),
        builder("background-color"),
        builder("background-image"),
        builder("background-position"),
        builder("background-repeat"),
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
        builder("border"),
        builder("bottom"),
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
        builder("orphans"),
        builder("outline-color"),
        builder("outline-style"),
        builder("outline-width"),
        builder("outline"),
        builder("overflow"),
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
        builder("position"),
        builder("quotes"),
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
        builder("text-transform"),
        builder("top"),
        builder("unicode-bidi"),
        builder("vertical-align"),
        builder("visibility"),
        builder("voice-family"),
        builder("volume"),
        builder("white-space"),
        builder("windows"),
        builder("width"),
        builder("word-spacing"),
        builder("z-index")
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
  private static final class Builder {
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
