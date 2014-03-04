/*
 * Copyright 2007 Google Inc.
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

package com.google.common.css.compiler.gssfunctions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.awt.Color;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser that recognizes all color formats allowed by the CSS Level 2
 * and SVG 1.0 specifications.
 *
 * <p>See <a href="http://www.w3.org/TR/CSS21/syndata.html#color-units">the CSS
 * 2.1 colors</a>.
 *
 * <p>A CSS color can be one of 5 things:
 *
 * <ul>
 * <li>A 6-character hexadecimal color, such as "#f6e43a" (lower or upper
 *      case).</li>
 * <li>A 3-character hexadecimal color, such as "#c4d".</li>
 * <li>A 255-based RGB value, such as "rgb(255, 10, 0)" (spaces are allowed
 *     around values).</li>
 * <li>A percent-based RGB value, such as "rgb(100%, 0%, 10%)" (spaces are
 *     allowed around values).</li>
 * <li>A predefined color keyword, such as "red".</li>
 * </ul>
 *
 * <p>An example of a parser that will handle all valid CSS 2.1 colors:
 * <pre>
 * // Construct a ColorParser instance (thread-safe, and can be reused):
 * ColorParser parser = new ColorParser(
 *     ColorParser.Format.HEX3,
 *     ColorParser.Format.HEX6,
 *     ColorParser.Format.CSS_RGB,
 *     ColorParser.Format.CSS_KEYWORDS);
 * // Parse a color:
 * Color red = parser.parse("rgb(255, 0, 0)");
 * </pre>
 *
 * <p>Also see the {@link #parseAny} static convenience method.
 *
 * @author chrisn@google.com (Chris Nokleberg)
 */
final class ColorParser {

  /** Pattern for matching #RRGGBB */
  private static final Pattern HEX6_PATTERN = formatHexadecimalPattern(6);

  /** Pattern for matching #RRGGBBAA */
  private static final Pattern HEX8_PATTERN = formatHexadecimalPattern(8);

  /** Pattern for matching #RGBA */
  private static final Pattern HEX4_PATTERN = formatHexadecimalPattern(4);

  /** Pattern for matching rgb(r, g, b) [0-255] */
  private static final Pattern CSS_RGB_PATTERN;

  /** Pattern for matching rgb(r, g, b) [0-100%] */
  private static final Pattern CSS_PERCENT_PATTERN;

  /** Pattern for matching rgba(r, g, b, a) [0-255, a = 0-1] */
  private static final Pattern CSS_RGBA_PATTERN;

  /** Pattern for matching rgba(r, g, b, a) [0-100%, a = 0-1] */
  private static final Pattern CSS_RGBA_PERCENT_PATTERN;

  // Initialize the patterns to recognize "rgb(...)" colors.
  static {
    /*
      From CSS2 spec:

      An <integer> consists of one or more digits "0" to "9".
      A <number> can either be an <integer>, or it can be zero
      or more digits followed by a dot (.) followed by one or more
      digits. Both integers and real numbers may be preceded by a "-"
      or "+" to indicate the sign.

      The format of a percentage value is an optional sign character
      ('+' or '-', with '+' being the default) immediately followed by a
      <number> immediately followed by '%'.
    */
    String integerPattern = "([+-]?[0-9]+)";
    String numberPattern = "([+-]?[0-9]+|[+-]?[0-9]*\\.[0-9]+)";
    String rgbTemplate = "^rgb\\(\\s*%1$s\\s*,\\s*%1$s\\s*,\\s*%1$s\\s*\\)$";
    CSS_RGB_PATTERN = Pattern.compile(
        String.format(rgbTemplate, integerPattern));
    CSS_PERCENT_PATTERN = Pattern.compile(
        String.format(rgbTemplate, numberPattern + "%"));

    String rgbaTemplate =
        "^rgba\\(\\s*%1$s\\s*,\\s*%1$s\\s*,\\s*%1$s\\s*\\,\\s*%2$s\\s*\\)$";
    CSS_RGBA_PATTERN = Pattern.compile(
        String.format(rgbaTemplate, integerPattern, numberPattern));
    CSS_RGBA_PERCENT_PATTERN = Pattern.compile(
        String.format(rgbaTemplate, numberPattern + "%", numberPattern));
  }

  /** Map of the 16 allowed colors defined in HTML 4.01. */
  private static final Map<String, Color> HTML_COLOR_MAP
      = new ImmutableMap.Builder<String, Color>()
      .put("aqua", new Color(0x00FFFF))
      .put("black", new Color(0x000000))
      .put("blue", new Color(0x0000FF))
      .put("fuchsia", new Color(0xFF00FF))
      .put("gray", new Color(0x808080))
      .put("green", new Color(0x008000))
      .put("lime", new Color(0x00FF00))
      .put("maroon", new Color(0x800000))
      .put("navy", new Color(0x000080))
      .put("olive", new Color(0x808000))
      .put("purple", new Color(0x800080))
      .put("red", new Color(0xFF0000))
      .put("silver", new Color(0xC0C0C0))
      .put("teal", new Color(0x008080))
      .put("white", new Color(0xFFFFFF))
      .put("yellow", new Color(0xFFFF00))
      .build();

  /** All the named colors defined in CSS 2.1 */
  private static final Map<String, Color> CSS_COLOR_MAP
      = new ImmutableMap.Builder<String, Color>()
      .putAll(HTML_COLOR_MAP)
      .put("orange", new Color(0xFFA500))
      .build();

  /** All the named colors defined in SVG 1.0 */
  private static final Map<String, Color> SVG_COLOR_MAP
      = new ImmutableMap.Builder<String, Color>()
      .putAll(CSS_COLOR_MAP)
      .put("aliceblue", new Color(0xF0F8FF))
      .put("antiquewhite", new Color(0xFAEBD7))
      .put("aquamarine", new Color(0x7FFFD4))
      .put("azure", new Color(0xF0FFFF))
      .put("beige", new Color(0xF5F5DC))
      .put("bisque", new Color(0xFFE4C4))
      .put("blanchedalmond", new Color(0xFFEBCD))
      .put("blueviolet", new Color(0x8A2BE2))
      .put("brown", new Color(0xA52A2A))
      .put("burlywood", new Color(0xDEB887))
      .put("cadetblue", new Color(0x5F9EA0))
      .put("chartreuse", new Color(0x7FFF00))
      .put("chocolate", new Color(0xD2691E))
      .put("coral", new Color(0xFF7F50))
      .put("cornflowerblue", new Color(0x6495ED))
      .put("cornsilk", new Color(0xFFF8DC))
      .put("crimson", new Color(0xDC143C))
      .put("cyan", new Color(0x00FFFF))
      .put("darkblue", new Color(0x00008B))
      .put("darkcyan", new Color(0x008B8B))
      .put("darkgoldenrod", new Color(0xB8860B))
      .put("darkgray", new Color(0xA9A9A9))
      .put("darkgreen", new Color(0x006400))
      .put("darkgrey", new Color(0xA9A9A9))
      .put("darkkhaki", new Color(0xBDB76B))
      .put("darkmagenta", new Color(0x8B008B))
      .put("darkolivegreen", new Color(0x556B2F))
      .put("darkorange", new Color(0xFF8C00))
      .put("darkorchid", new Color(0x9932CC))
      .put("darkred", new Color(0x8B0000))
      .put("darksalmon", new Color(0xE9967A))
      .put("darkseagreen", new Color(0x8FBC8F))
      .put("darkslateblue", new Color(0x483D8B))
      .put("darkslategray", new Color(0x2F4F4F))
      .put("darkslategrey", new Color(0x2F4F4F))
      .put("darkturquoise", new Color(0x00CED1))
      .put("darkviolet", new Color(0x9400D3))
      .put("deeppink", new Color(0xFF1493))
      .put("deepskyblue", new Color(0x00BFFF))
      .put("dimgray", new Color(0x696969))
      .put("dimgrey", new Color(0x696969))
      .put("dodgerblue", new Color(0x1E90FF))
      .put("firebrick", new Color(0xB22222))
      .put("floralwhite", new Color(0xFFFAF0))
      .put("forestgreen", new Color(0x228B22))
      .put("gainsboro", new Color(0xDCDCDC))
      .put("ghostwhite", new Color(0xF8F8FF))
      .put("gold", new Color(0xFFD700))
      .put("goldenrod", new Color(0xDAA520))
      .put("greenyellow", new Color(0xADFF2F))
      .put("grey", new Color(0x808080))
      .put("honeydew", new Color(0xF0FFF0))
      .put("hotpink", new Color(0xFF69B4))
      .put("indianred", new Color(0xCD5C5C))
      .put("indigo", new Color(0x4B0082))
      .put("ivory", new Color(0xFFFFF0))
      .put("khaki", new Color(0xF0E68C))
      .put("lavender", new Color(0xE6E6FA))
      .put("lavenderblush", new Color(0xFFF0F5))
      .put("lawngreen", new Color(0x7CFC00))
      .put("lemonchiffon", new Color(0xFFFACD))
      .put("lightblue", new Color(0xADD8E6))
      .put("lightcoral", new Color(0xF08080))
      .put("lightcyan", new Color(0xE0FFFF))
      .put("lightgoldenrodyellow", new Color(0xFAFAD2))
      .put("lightgray", new Color(0xD3D3D3))
      .put("lightgreen", new Color(0x90EE90))
      .put("lightgrey", new Color(0xD3D3D3))
      .put("lightpink", new Color(0xFFB6C1))
      .put("lightsalmon", new Color(0xFFA07A))
      .put("lightseagreen", new Color(0x20B2AA))
      .put("lightskyblue", new Color(0x87CEFA))
      .put("lightslategray", new Color(0x778899))
      .put("lightslategrey", new Color(0x778899))
      .put("lightsteelblue", new Color(0xB0C4DE))
      .put("lightyellow", new Color(0xFFFFE0))
      .put("limegreen", new Color(0x32CD32))
      .put("linen", new Color(0xFAF0E6))
      .put("magenta", new Color(0xFF00FF))
      .put("mediumaquamarine", new Color(0x66CDAA))
      .put("mediumblue", new Color(0x0000CD))
      .put("mediumorchid", new Color(0xBA55D3))
      .put("mediumpurple", new Color(0x9370DB))
      .put("mediumseagreen", new Color(0x3CB371))
      .put("mediumslateblue", new Color(0x7B68EE))
      .put("mediumspringgreen", new Color(0x00FA9A))
      .put("mediumturquoise", new Color(0x48D1CC))
      .put("mediumvioletred", new Color(0xC71585))
      .put("midnightblue", new Color(0x191970))
      .put("mintcream", new Color(0xF5FFFA))
      .put("mistyrose", new Color(0xFFE4E1))
      .put("moccasin", new Color(0xFFE4B5))
      .put("navajowhite", new Color(0xFFDEAD))
      .put("oldlace", new Color(0xFDF5E6))
      .put("olivedrab", new Color(0x6B8E23))
      .put("orangered", new Color(0xFF4500))
      .put("orchid", new Color(0xDA70D6))
      .put("palegoldenrod", new Color(0xEEE8AA))
      .put("palegreen", new Color(0x98FB98))
      .put("paleturquoise", new Color(0xAFEEEE))
      .put("palevioletred", new Color(0xDB7093))
      .put("papayawhip", new Color(0xFFEFD5))
      .put("peachpuff", new Color(0xFFDAB9))
      .put("peru", new Color(0xCD853F))
      .put("pink", new Color(0xFFC0CB))
      .put("plum", new Color(0xDDA0DD))
      .put("powderblue", new Color(0xB0E0E6))
      .put("rosybrown", new Color(0xBC8F8F))
      .put("royalblue", new Color(0x4169E1))
      .put("saddlebrown", new Color(0x8B4513))
      .put("salmon", new Color(0xFA8072))
      .put("sandybrown", new Color(0xF4A460))
      .put("seagreen", new Color(0x2E8B57))
      .put("seashell", new Color(0xFFF5EE))
      .put("sienna", new Color(0xA0522D))
      .put("skyblue", new Color(0x87CEEB))
      .put("slateblue", new Color(0x6A5ACD))
      .put("slategray", new Color(0x708090))
      .put("slategrey", new Color(0x708090))
      .put("snow", new Color(0xFFFAFA))
      .put("springgreen", new Color(0x00FF7F))
      .put("steelblue", new Color(0x4682B4))
      .put("tan", new Color(0xD2B48C))
      .put("thistle", new Color(0xD8BFD8))
      .put("tomato", new Color(0xFF6347))
      .put("turquoise", new Color(0x40E0D0))
      .put("violet", new Color(0xEE82EE))
      .put("wheat", new Color(0xF5DEB3))
      .put("whitesmoke", new Color(0xF5F5F5))
      .put("yellowgreen", new Color(0x9ACD32))
      .build();

  /** Optional formats that each parser instance can accept. */
  public static enum Format {
    /** #RRGGBB format */
    HEX6 {
      @Override Color parse(String value) {
        return (HEX6_PATTERN.matcher(value).matches())
            ? new Color(Integer.parseInt(value.substring(1), 16)) : null;
      }
    },

    /** #RGB format */
    HEX3 {
      @Override Color parse(String value) {
        return HEX4.parse(value + "F");
      }
    },

    /** #RGBA format */
    HEX4 {
      @Override Color parse(String value) {
        if (HEX4_PATTERN.matcher(value).matches()) {
          int r = Integer.parseInt(value.substring(1, 2), 16);
          int g = Integer.parseInt(value.substring(2, 3), 16);
          int b = Integer.parseInt(value.substring(3, 4), 16);
          int a = Integer.parseInt(value.substring(4, 5), 16);
          return
              new Color((r << 4) | r, (g << 4) | g, (b << 4) | b, (a << 4) | a);
        }
        return null;
      }
    },

    /** #RRGGBBAA format */
    HEX8 {
      @Override Color parse(String value) {
        if (HEX8_PATTERN.matcher(value).matches()) {
          int rgb = Integer.parseInt(value.substring(1, 7), 16);
          int a = Integer.parseInt(value.substring(7, 9), 16);
          return new Color(a << 24 | rgb, true);
        }
        return null;
      }
    },

    /** rgb(R, G, B) format (R/G/B = 0-255 or 0-100%) */
    CSS_RGB {
      @Override Color parse(String value) {
        // Try to parse with 255-based values...
        Matcher matcher = CSS_RGB_PATTERN.matcher(value);
        if (matcher.matches()) {
          return newColor(matcher, 255);
        } else {
          // ... or with percent-based values.
          matcher = CSS_PERCENT_PATTERN.matcher(value);
          return matcher.matches() ? newColor(matcher, 100) : null;
        }
      }

      /** Creates a new color from a regexp match. */
      private Color newColor(Matcher matcher, float max) {
        return new Color(
            clipRangeAndNormalize(matcher.group(1), max),
            clipRangeAndNormalize(matcher.group(2), max),
            clipRangeAndNormalize(matcher.group(3), max));
      }
    },

    /** rgba(R, G, B, A) format (R/G/B = 0-255 or 0-100%, A = 0.0-1.0) */
    CSS_RGBA {
      @Override Color parse(String value) {
        // Try to parse with 255-based values...
        Matcher matcher = CSS_RGBA_PATTERN.matcher(value);
        if (matcher.matches()) {
          return newColor(matcher, 255);
        } else {
          // ... or with percent-based values.
          matcher = CSS_RGBA_PERCENT_PATTERN.matcher(value);
          return matcher.matches() ? newColor(matcher, 100) : null;
        }
      }

      /** Creates a new color from a regexp match. */
      private Color newColor(Matcher matcher, float max) {
        return new Color(
            clipRangeAndNormalize(matcher.group(1), max),
            clipRangeAndNormalize(matcher.group(2), max),
            clipRangeAndNormalize(matcher.group(3), max),
            clipRangeAndNormalize(matcher.group(4), 1));
      }
    },

    /** HTML 4.0 color keywords (16 colors) */
    HTML_KEYWORDS {
      @Override Color parse(String value) {
        return HTML_COLOR_MAP.get(value.toLowerCase());
      }
    },

    /** CSS 2.1 color keywords (HTML + "orange") */
    CSS_KEYWORDS {
      @Override Color parse(String value) {
        return CSS_COLOR_MAP.get(value.toLowerCase());
      }
    },

    /** SVG 1.0 color keywords */
    SVG_KEYWORDS {
      @Override Color parse(String value) {
        return SVG_COLOR_MAP.get(value.toLowerCase());
      }
    };

    /**
     * Parses the given color description.
     *
     * @param value the value to parse
     * @return the parsed color, or null if this format cannot parse the value
     */
    abstract Color parse(String value);

    /**
     * Parses value as float, clips to [0, max], and returns the
     * clipped value divided by the maximum value.
     */
    private static float clipRangeAndNormalize(String value, float max) {
      return Math.max(0, Math.min(max, Float.parseFloat(value))) / max;
    }
  }

  private final Set<Format> formats;

  private static final ColorParser ANY_COLOR_PARSER = new ColorParser(
      Format.HEX3, Format.HEX6, Format.CSS_RGB, Format.CSS_RGBA,
      Format.SVG_KEYWORDS);

  /**
   * Parses a color description using all supported CSS2/CSS3
   * {@linkplain Format formats} (meaning all formats except
   * {@linkplain Format#HEX4 hex-4} and {@linkplain Format#HEX8 hex-8}).
   *
   * @param value the value to parse
   * @return the parsed color
   * @throws IllegalArgumentException if the value cannot be parsed
   */
  public static Color parseAny(String value) {
    return ANY_COLOR_PARSER.parse(value);
  }

  /**
   * Constructs a new instance using the given formats.
   *
   * @param formats the formats to accept
   * @throws IllegalArgumentException if no formats are specified
   */
  public ColorParser(Format... formats) {
    Preconditions.checkArgument(formats.length > 0,
        "At least one format is required");
    this.formats = EnumSet.copyOf(Arrays.asList(formats));
  }

  /**
   * Parses the given color description.
   *
   * @param value the value to parse
   * @return the parsed color
   * @throws IllegalArgumentException if the value cannot be parsed
   */
  public Color parse(String value) {
    value = value.trim();
    for (Format format : formats) {
      Color result = format.parse(value);
      if (result != null) {
        return result;
      }
    }
    // If we get to this point, we're unable to parse the color.
    throw new IllegalArgumentException("Illegal color value, does not match "
        + "any of " + formats + ": " + value);
  }

  /**
   * Formats a hexadecimal color pattern with the given number of digits. For
   * example, a pattern made from 6 digits matches strings such as "#AB4D9F".
   *
   * @param numberOfDigits The number of digits that the pattern should match
   * @return The requested pattern.
   */
  @VisibleForTesting
  static Pattern formatHexadecimalPattern(int numberOfDigits) {
    return Pattern.compile(String.format("^#[0-9a-fA-F]{%d}$", numberOfDigits));
  }
}
