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

package com.google.common.css.compiler.gssfunctions;

import java.awt.Color;
import java.lang.Math;

/**
 * Utility functions to deal with colors.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
class ColorUtil {

  /**
   * Index of Hue in HSB and HSL array.
   */
  static final int H = 0;
  /**
   * Index of Saturation in HSB and HSL array.
   */
  static final int S = 1;
  /**
   * Index of Brightness in HSB array.
   */
  static final int B = 2;
  /**
   * Index of Lightness in HSL array.
   */
  static final int L = 2;

  static float[] toHsb(Color color) {
    return Color.RGBtoHSB(
        color.getRed(), color.getGreen(), color.getBlue(), null);
  }

  static String formatColor(Color color) {
    return String.format("#%02X%02X%02X",
        color.getRed(), color.getGreen(), color.getBlue());
  }

  static Color hsbToColor(float[] inputHsb) {
    return Color.getHSBColor(inputHsb[H], inputHsb[S], inputHsb[B]);
  }

  static float[] hsbToHsl(float[] inputHsb) {
    float hHsb = inputHsb[H];
    float sHsb = inputHsb[S];
    float bHsb = inputHsb[B];

    float hHsl = hHsb;
    float lHsl = bHsb * (2 - sHsb) / 2;
    float sHsl = bHsb * sHsb / (1 - Math.abs(2 * lHsl - 1));

    float[] hsl = new float[3];
    hsl[H] = hHsl;
    hsl[S] = sHsl;
    hsl[L] = lHsl;

    return hsl;
  }

  static float[] toHsl(Color color) {
    return hsbToHsl(toHsb(color));
  }

  static float[] hslToHsb(float[] inputHsl) {
    float hHsl = inputHsl[H];
    float sHsl = inputHsl[S];
    float lHsl = inputHsl[L];

    float hHsb = hHsl;
    float bHsb = (2 * lHsl + sHsl * (1 - Math.abs(2 * lHsl - 1))) / 2;
    float sHsb = 2 * (bHsb - lHsl) / bHsb;

    float[] hsb = new float[3];
    hsb[H] = hHsb;
    hsb[S] = sHsb;
    hsb[B] = bHsb;

    return hsb;
  }

  static Color hslToColor(float[] inputHsl) {
    float[] hsb = hslToHsb(inputHsl);

    return Color.getHSBColor(hsb[H], hsb[S], hsb[B]);
  }

  /**
   * Tests whether the given colors are contrasting colors, according to the
   * test described in the W3C accessibility evaluation working draft
   * {@link "http://www.w3.org/TR/AERT#color-contrast"}.  This is a lenient
   * version of the test which allows the user to pass in the accepted leniency
   * margin.
   *
   * <p>The value of the leniency margin is in the range 0 to 1.0.  0 means
   * that the test is not lenient at all, 1.0 means that the test will pass for
   * all colors that are different.  It is recommended not to use values of more
   * than 0.01.
   *
   * @param color1 the first of the two checked colors
   * @param color2 the second of the two checked colors
   * @param margin the test leniency margin
   * @return whether the given colors are considered contrasting, taking the
   *     leniency margin into account
   */
  static boolean testContrast(Color color1, Color color2, float margin) {
    float differenceFraction = 1f - margin;
    return luminanceDiff(color1, color2) > 125 * differenceFraction
        && colorDiff(color1, color2) > 500 * differenceFraction;
  }

  /**
   * Tests whether the given colors are contrasting colors, according to the
   * test described in the W3C accessibility evaluation working draft
   * {@link "http://www.w3.org/TR/AERT#color-contrast"}.
   *
   * @param color1 the first of the two checked colors
   * @param color2 the second of the two checked colors
   * @return whether the given colors are considered contrasting
   */
  static boolean testContrast(Color color1, Color color2) {
    return luminanceDiff(color1, color2) > 125
        && colorDiff(color1, color2) > 500;
  }

  /**
   * Computes the luminance difference of two colors (a value in range 0-255).
   * It is the luminance value equal to the Y component of the YIQ or the YUV
   * color space models.
   */
  static int luminanceDiff(Color c1, Color c2) {
    return Math.abs(luminance(c1) - luminance(c2));
  }

  /**
   * Computes the luminance value of a color (a value in range 0-255).
   * It is the luminance value equal to the Y component of the YIQ or the YUV
   * color space models.
   */
  static int luminance(Color color) {
    return luminance(color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * Computes the luminance value of a color (a value in range 0-255).
   * It is the luminance value equal to the Y component of the YIQ or the YUV
   * color space models.
   */
  static int luminance(int red, int green, int blue) {
    return (red * 299 + green * 587 + blue * 114) / 1000;
  }

  /**
   * Calculates the Manhattan distance of two colors in the RGB color space
   * (a value in range 0-(255*3)).
   */
  static int colorDiff(Color color1, Color color2) {
    return colorDiff(
        color1.getRed(), color1.getGreen(), color1.getBlue(),
        color2.getRed(), color2.getGreen(), color2.getBlue());
  }

  /**
   * Calculates the Manhattan distance of two colors in the RGB color space
   * (a value in range 0-(255*3)).
   */
  static int colorDiff(int r1, int g1, int b1, int r2, int g2, int b2) {
    return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
  }

  // Utility class, static methods only.
  private ColorUtil() {
  }
}
