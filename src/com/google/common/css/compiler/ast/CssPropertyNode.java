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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.css.SourceCodeLocation;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * A node representing a CSS property, such as background or padding.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssPropertyNode extends CssValueNode {
  private final String property;

  private final boolean isStandardProperty;

  private final boolean hasPositionDependentValues;

  /**
   * Creates a property node with the specified value.
   */
  public CssPropertyNode(String value) {
    this(value, null);
  }

  /**
   * Creates a property node with the specified value and source code location.
   */
  public CssPropertyNode(
      String value, @Nullable SourceCodeLocation sourceCodeLocation) {
    super(value, sourceCodeLocation);
    this.property = value.toLowerCase();
    this.isStandardProperty = Standard.PROPERTIES.contains(this.property);
    this.hasPositionDependentValues =
        Standard.HAS_POSITION_DEPENDENT_VALUES.contains(this.property);
  }

  /**
   * Creates a property node by deep-copying the specified property node.
   */
  public CssPropertyNode(CssPropertyNode node) {
    super(node);
    this.isStandardProperty = node.isStandardProperty();
    this.hasPositionDependentValues = node.hasPositionDependentValues();
    this.property = node.getProperty();
  }

  @Override
  public CssPropertyNode deepCopy() {
    return new CssPropertyNode(this);
  }

  public String getProperty() {
    return property;
  }

  public boolean isStandardProperty() {
    return isStandardProperty;
  }

  public boolean hasPositionDependentValues() {
    return hasPositionDependentValues;
  }

  /**
   * Returns the set of shorthand properties related to this property, or the
   * empty set if this property is not standard.
   *
   * <p>For example, {@code border-left-style} has {@code border},
   * {@code border-left} and {@code border-style} as shorthands.
   */
  public Set<String> getShorthands() {
    Set<String> shorthands = Standard.TO_SHORTHANDS.get(property);
    if (shorthands == null) {
      Preconditions.checkArgument(!isStandardProperty);
      return ImmutableSet.of();
    } else {
      return shorthands;
    }
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
    String partition = Standard.TO_PARTITIONS.get(property);
    if (partition == null) {
      Preconditions.checkArgument(!isStandardProperty);
      return property;
    }
    return partition;
  }

  // TODO(user): Add support for breaking a shorthand into a list of
  //    declarations and for doing the inverse operation. These operations might
  //    actually belong to another class.
  @Override
  public String toString() {
    return property;
  }

  private static class Standard {
    /**
     * Standard CSS properties.
     */
    // TODO(oana): Make this into an enum or some other data structure that
    // would make it easy to retrieve special elements such as composite etc.
    private static final ImmutableSet<String> PROPERTIES = ImmutableSet.of(
        "azimuth",
        "background-attachment",
        "background-color",
        "background-image",
        "background-position",
        "background-repeat",
        "background",
        "border-collapse",
        "border-color",
        "border-spacing",
        "border-style",
        "border-top",
        "border-right",
        "border-bottom",
        "border-left",
        "border-top-color",
        "border-right-color",
        "border-bottom-color",
        "border-left-color",
        "border-top-style",
        "border-right-style",
        "border-bottom-style",
        "border-left-style",
        "border-top-width",
        "border-right-width",
        "border-bottom-width",
        "border-left-width",
        "border-width",
        "border",
        "bottom",
        "caption-side",
        "clear",
        "clip",
        "color",
        "content",
        "counter-increment",
        "counter-reset",
        "cue-after",
        "cue-before",
        "cue",
        "cursor",
        "direction",
        "display",
        "elevation",
        "empty-cells",
        "float",
        "font-family",
        "font-size",
        "font-style",
        "font-variant",
        "font-weight",
        "font",
        "height",
        "left",
        "letter-spacing",
        "line-height",
        "list-style-image",
        "list-style-position",
        "list-style-type",
        "list-style",
        "margin-right",
        "margin-left",
        "margin-top",
        "margin-bottom",
        "margin",
        "max-height",
        "max-width",
        "min-height",
        "min-width",
        "orphans",
        "outline-color",
        "outline-style",
        "outline-width",
        "outline",
        "overflow",
        "padding-top",
        "padding-right",
        "padding-bottom",
        "padding-left",
        "padding",
        "page-break-after",
        "page-break-before",
        "page-break-inside",
        "pause-after",
        "pause-before",
        "pause",
        "pitch-range",
        "pitch",
        "play-during",
        "position",
        "quotes",
        "richness",
        "right",
        "speak-header",
        "speak-numeral",
        "speak-punctuation",
        "speak",
        "speech-rate",
        "stress",
        "table-layout",
        "text-align",
        "text-decoration",
        "text-indent",
        "text-transform",
        "top",
        "unicode-bidi",
        "vertical-align",
        "visibility",
        "voice-family",
        "volume",
        "white-space",
        "windows",
        "width",
        "word-spacing",
        "z-index");

    /**
     * The set of properties which take multiple values and for which it is not
     * safe to remove some but not all of the values. The interpretation of the
     * values depends upon their position in the sequence, e.g. multiple values
     * are interpreted as applying to top, right, bottom, left.
     */
    private static final ImmutableSet<String> HAS_POSITION_DEPENDENT_VALUES = ImmutableSet.of(
        "border-color",
        "border-style",
        "border-width",
        "margin",
        "padding");

    /**
     * The set of all standard shorthand properties.
     */
    private static final ImmutableSet<String> SHORTHAND_PROPERTIES = ImmutableSet.of(
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
    private static final ImmutableSet<String> NO_SHORTHAND = ImmutableSet.of(
        "border-collapse",
        "border-spacing");

    /**
     * A map from each standard property to a set of its shorthands.
     *
     * <p>For example, {@code border-left-style} has {@code border},
     * {@code border-left} and {@code border-style} as shorthands. Note that
     * standard properties all have exactly 0, 1 or 3 shorthands.
     */
    private static final ImmutableSetMultimap<String, String> TO_SHORTHANDS =
        computeAllShorthands(PROPERTIES, SHORTHAND_PROPERTIES, NO_SHORTHAND);

    /**
     * A map from each standard property to its 'partition'. All properties in
     * a partition share a common shorthand.
     *
     * <p>For example, {@code padding}, {@code padding-bottom},
     * {@code padding-left}, {@code padding-right}, {@code padding-top} are all
     * in the {@code padding} partition. As another example, {@code z-index} is
     * its own single partition.
     */
    private static final ImmutableMap<String, String> TO_PARTITIONS =
        computeAllPartitions(PROPERTIES, TO_SHORTHANDS);

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
     * @param shorthandProperties the set of standard shorthand properties
     * @param noShorthand set of properties for which not to attempt
     *     computation, just returning no shorthands
     * @return the set of shorthands of the given property (not including the
     *     property itself if it is a shorthand), a subset of the given set of
     *     standard shorthand properties
     */
    private static ImmutableSet<String> computeShorthandPropertiesFor(
        String property,
        Set<String> shorthandProperties,
        Set<String> noShorthand) {

      if (noShorthand.contains(property)) {
        return ImmutableSet.of();
      }

      int lastHyphenIndex = property.lastIndexOf('-');
      if (lastHyphenIndex == -1) {
        return ImmutableSet.of();
      }

      String possibleShorthand = property.substring(0, lastHyphenIndex);
      if (!shorthandProperties.contains(possibleShorthand)) {
        return ImmutableSet.of();
      }

      int butlastHyphenIndex = possibleShorthand.lastIndexOf('-');
      if (butlastHyphenIndex == -1) {
        return ImmutableSet.of(possibleShorthand);
      }

      String smallestShorthand = property.substring(0, butlastHyphenIndex);
      if (!shorthandProperties.contains(smallestShorthand)) {
        return ImmutableSet.of(possibleShorthand);
      }

      // Only border-related properties may have more than one shorthand,
      // in which case they will have three shorthands.
      Preconditions.checkArgument(smallestShorthand.equals("border"));

      String otherShorthand =
          smallestShorthand + property.substring(lastHyphenIndex);

      Preconditions.checkArgument(shorthandProperties.contains(otherShorthand));

      return ImmutableSet.of(
          smallestShorthand, possibleShorthand, otherShorthand);
    }

    private static ImmutableSetMultimap<String,String> computeAllShorthands(
        Set<String> properties,
        Set<String> shorthandProperties,
        Set<String> noShorthand) {

      ImmutableSetMultimap.Builder<String, String> builder =
          new ImmutableSetMultimap.Builder<String, String>();
      for (String property : properties) {
        builder.putAll(property,
            computeShorthandPropertiesFor(property, shorthandProperties, noShorthand));
      }
      return builder.build();
    }

    private static String computePartitionFor(
        String property,
        SetMultimap<String, String> toShorthands) {

      Set<String> shorthands = toShorthands.get(property);
      return shorthands.isEmpty()
          ? property
          : computePartitionFor(shorthands.iterator().next(), toShorthands);
    }

    private static ImmutableMap<String, String> computeAllPartitions(
        Set<String> properties,
        SetMultimap<String, String> toShorthands) {
      ImmutableMap.Builder<String, String> builder =
          new ImmutableMap.Builder<String, String>();
      for (String property : properties) {
        builder.put(property, computePartitionFor(property, toShorthands));
      }
      return builder.build();
    }
  }
}
