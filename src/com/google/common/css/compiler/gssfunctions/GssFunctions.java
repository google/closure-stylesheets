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

import static com.google.common.css.compiler.gssfunctions.ColorUtil.formatColor;
import static com.google.common.css.compiler.gssfunctions.ColorUtil.hsbToColor;
import static com.google.common.css.compiler.gssfunctions.ColorUtil.testContrast;
import static com.google.common.css.compiler.gssfunctions.ColorUtil.toHsb;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssHexColorNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Container for common GSS functions.
 *
 */
public class GssFunctions {

  /**
   * @return a map from each GSS function name to the function
   */
  public static Map<String, GssFunction> getFunctionMap() {
    // TODO(dgajda): Add getName() to the function interface.
    return ImmutableMap.<String, GssFunction>builder()

        // Arithmetic functions.
        .put("add", new GssFunctions.AddToNumericValue())
        .put("sub", new GssFunctions.SubtractFromNumericValue())
        .put("mult", new GssFunctions.Mult())
        // Not named "div" so it will not be confused with the HTML element.
        .put("divide", new GssFunctions.Div())
        .put("min", new GssFunctions.MinValue())
        .put("max", new GssFunctions.MaxValue())

        // Color functions.
        .put("blendColorsHsb", new BlendColorsHsb())
        .put("blendColorsRgb", new BlendColorsRgb())
        .put("makeMutedColor", new MakeMutedColor())
        .put("addHsbToCssColor", new AddHsbToCssColor())
        .put("makeContrastingColor", new MakeContrastingColor())
        .put("adjustBrightness", new AdjustBrightness())
        .put("makeTranslucent", new MakeTranslucent())

        // Logic functions.
        .put("selectFrom", new SelectFrom())

        .build();
  }

  /**
   * Round decimals to the eight places, which appears to be the smallest
   * precision that works well across all browsers. (Yes, this is crazy.)
   */
  private static final String DECIMAL_FORMAT = "#.########";

  /**
   * This class encapsulates results of background definition calculation and
   * is used to build either a list of {@link CssValueNode} instances or a string that
   * represents the background CSS property.
   */
  static class ImageBackground {

    private static final String NO_REPEAT = "no-repeat";

    private final String url;
    private final String positionH;
    private final String positionHUnit;
    private final String positionV;
    private final String positionVUnit;

    /**
     * @param url The URL to be used as the background image URL
     * @param cornerId The corner id which tells how the image is positioned
     * @param imgSize The size of the image
     * @param units The units of the image size
     */
    public ImageBackground(
        String url, String cornerId, String imgSize, String units) {
      this.url = url;

      boolean isZero = Float.parseFloat(imgSize) == 0;

      boolean isLeft = isZero || cornerId.endsWith("l");
      positionH = isLeft ? "0" : "-" + imgSize;
      positionHUnit = isLeft ? CssNumericNode.NO_UNITS : units;

      boolean isTop = isZero || cornerId.startsWith("t");
      positionV = isTop ? "0" : "-" + imgSize;
      positionVUnit = isTop ? CssNumericNode.NO_UNITS : units;
    }

    @Override
    public String toString() {
      return createUrl(url) + " " + NO_REPEAT + " " +
          positionH + positionHUnit + " " + positionV + positionVUnit;
    }

    public List<CssValueNode> toNodes(SourceCodeLocation location) {
      return ImmutableList.of(
          createUrlNode(url, location),
          new CssLiteralNode(NO_REPEAT, location),
          new CssNumericNode(positionH, positionHUnit, location),
          new CssNumericNode(positionV, positionVUnit, location));
    }
  }

  /**
   * Base implementation of the color blending GSS function. Returns a color
   * half way between the two colors supplied as arguments.
   */
  public abstract static class BaseBlendColors implements GssFunction {

    /**
     * Returns the number of expected arguments of this GSS function.
     *
     * @return Number of expected arguments
     */
    @Override
    public Integer getNumExpectedArguments() {
      return 2;
    }

    /**
     * Returns the string representation in hex format for a color half way in
     * between the two supplied colors.
     *
     * @param args The list of arguments
     * @return The computed color
     */
    @Override
    public List<CssValueNode> getCallResultNodes(List<CssValueNode> args,
        ErrorManager errorManager) {

      CssValueNode arg1 = args.get(0);
      CssValueNode arg2 = args.get(1);

      String startColorStr = arg1.getValue();
      String endColorStr = arg2.getValue();

      String resultString = blend(startColorStr, endColorStr);

      CssHexColorNode result = new CssHexColorNode(resultString,
          arg1.getSourceCodeLocation());
      return ImmutableList.of((CssValueNode) result);
    }

    @Override
    public String getCallResultString(List<String> args)
        throws GssFunctionException {
      try {
        return blend(args.get(0), args.get(1));
      } catch (IllegalArgumentException e) {
        throw new GssFunctionException("Colors could not be parsed", e);
      }
    }

    // TODO(dgajda): Hide it, this function is only visible because
    public abstract String blend(String startColor, String endColor);
  }

  /**
   * Implementation of the blendColorsHsb GSS function. Returns a color half way
   * between the two colors supplied as arguments.
   */
  public static class BlendColorsHsb extends BaseBlendColors {

    @Override
    // TODO(dgajda): Hide it, this function is only visible because
    public String blend(String startColorStr, String endColorStr) {
      Color midColor = blendHsb(
          ColorParser.parseAny(startColorStr),
          ColorParser.parseAny(endColorStr));
      return formatColor(midColor);
    }
  }

  private static Color blendHsb(Color startColor, Color endColor) {

    float[] startColorHsb = toHsb(startColor);
    float[] endColorHsb = toHsb(endColor);

    float diffHue = Math.abs(startColorHsb[0] - endColorHsb[0]);
    float sumHue = startColorHsb[0] + endColorHsb[0];
    float midHue = (diffHue <= 0.5)
        ? sumHue / 2
        : (sumHue + 1) / 2;  // Hue values range 0 to 1 and wrap (i.e. 0 == 1)
    if (midHue > 1) {
      midHue -= 1;
    }

    return Color.getHSBColor(
        midHue,
        (startColorHsb[1] + endColorHsb[1]) / 2,
        (startColorHsb[2] + endColorHsb[2]) / 2);
  }

  /**
   * Implementation of the blendColorsRgb GSS function. Returns a color half way
   * between the two colors by averaging each of red, green & blue.
   */
  public static class BlendColorsRgb extends BaseBlendColors {

    /**
     * Returns the string representation in hex format for a color half way in
     * between the two supplied colors by averaging each of red, green & blue.
     *
     * @param startColorStr The start color in string form
     * @param endColorStr The endcolor in string form
     * @return The computed color
     */
    @Override
    // TODO(dgajda): Hide it, this function is only visible because
    public String blend(String startColorStr, String endColorStr) {
      Color startColor = ColorParser.parseAny(startColorStr);
      Color endColor = ColorParser.parseAny(endColorStr);

      Color midColor = new Color(
          (startColor.getRed() + endColor.getRed()) / 2,
          (startColor.getGreen() + endColor.getGreen()) / 2,
          (startColor.getBlue() + endColor.getBlue()) / 2);

      return formatColor(midColor);
    }
  }

  /**
   * Helper method to convert a numeric value of "0" or "1" into a boolean.
   *
   * @param numericPart The string containing the value
   * @return The corresponding boolean value
   */
  public static boolean parseBoolean(String numericPart) {
    return Integer.parseInt(numericPart) == 1;
  }

  /**
   * Helper method for implementors of GssFunction to allow the creation of
   * a url entry node in a GSS file.
   *
   * @param imageUrl The url of the image to add.
   * @param location The location in the GSS file to place the node.
   * @return The node containing the url entry.
   */
  public static CssFunctionNode createUrlNode(
      String imageUrl, SourceCodeLocation location) {
    CssFunctionNode url =
        new CssFunctionNode(CssFunctionNode.Function.byName("url"), location);

    if (!imageUrl.equals("")) {
      CssLiteralNode argument = new CssLiteralNode(imageUrl, location);
      List<CssValueNode> argList = ImmutableList.of((CssValueNode) argument);
      CssFunctionArgumentsNode arguments =
          new CssFunctionArgumentsNode(argList);
      url.setArguments(arguments);
    }
    return url;
  }

  /**
   * Implementation of the addHsbToCssColor GSS function.
   */
  public static class AddHsbToCssColor implements GssFunction {

    @Override
    public Integer getNumExpectedArguments() {
      return 4;
    }

    @Override
    public List<CssValueNode> getCallResultNodes(List<CssValueNode> args,
        ErrorManager errorManager) throws GssFunctionException {
      CssValueNode arg1 = args.get(0);
      CssValueNode arg2 = args.get(1);
      CssValueNode arg3 = args.get(2);
      CssValueNode arg4 = args.get(3);

      if (!(arg1 instanceof CssHexColorNode
          || arg1 instanceof CssLiteralNode)) {
        String message =
            "The first argument must be a CssHexColorNode or a CssLiteralNode.";
        errorManager.report(
            new GssError(message, arg1.getSourceCodeLocation()));
        throw new GssFunctionException(message);
      }
      CssNumericNode numeric2, numeric3, numeric4;
      if (arg2 instanceof CssNumericNode && arg3 instanceof CssNumericNode
          && arg4 instanceof CssNumericNode) {
         numeric2 = (CssNumericNode)arg2;
         numeric3 = (CssNumericNode)arg3;
         numeric4 = (CssNumericNode)arg4;
      } else {
        String message = "Arguments number 2, 3 and 4 must be CssNumericNodes";
        errorManager.report(
            new GssError(message, arg2.getSourceCodeLocation()));
        throw new GssFunctionException(message);
      }

      try {
        String resultString =
            addHsbToCssColor(args.get(0).getValue(),
                             numeric2.getNumericPart(),
                             numeric3.getNumericPart(),
                             numeric4.getNumericPart());

        CssHexColorNode result = new CssHexColorNode(resultString,
            arg1.getSourceCodeLocation());
        return ImmutableList.of((CssValueNode)result);
      } catch (GssFunctionException e) {
        errorManager.report(
            new GssError(e.getMessage(), arg2.getSourceCodeLocation()));
        throw e;
      }
    }

    @Override
    public String getCallResultString(List<String> args)
        throws GssFunctionException {
      String baseColorString = args.get(0);
      return addHsbToCssColor(
          baseColorString, args.get(1), args.get(2), args.get(3));
    }

    protected String addHsbToCssColor(
        String baseColorString, String hueToAdd, String saturationToAdd,
        String brightnessToAdd) throws GssFunctionException {
      try {
        return addHsbToCssColor(
            baseColorString,
            Integer.parseInt(hueToAdd),
            Integer.parseInt(saturationToAdd),
            Integer.parseInt(brightnessToAdd));
      } catch (NumberFormatException e) {
        String message = String.format("Could not parse the integer arguments"
            + " for the function 'addHsbToCssColor'. The list of arguments was:"
            + " %s, %s, %s, %s. ",
            baseColorString, hueToAdd, saturationToAdd, brightnessToAdd);
        throw new GssFunctionException(message);
      } catch (IllegalArgumentException e) {
        String message = String.format("Could not parse the color argument"
            + " for the function 'addHsbToCssColor'. The list of arguments was:"
            + " %s, %s, %s, %s. ",
            baseColorString, hueToAdd, saturationToAdd, brightnessToAdd);
        throw new GssFunctionException(message);
      }
    }

    /**
     * Takes a CSS color string, and adds the specified amount of hue,
     * saturation and brightness to it.
     *
     * @param baseColorString The string representing the color to change
     * @param hueToAdd The amount of hue to add (can be negative)
     * @param saturationToAdd The amount of saturation to add (can be negative)
     * @param brightnessToAdd The amount of brightness to add (can be negative)
     * @return A CSS String representing the new color
     */
    public String addHsbToCssColor(String baseColorString,
                                   int hueToAdd,
                                   int saturationToAdd,
                                   int brightnessToAdd) {

      // Skip transformation for the transparent color.
      if ("transparent".equals(baseColorString)) {
        return baseColorString;
      }

      Color baseColor = ColorParser.parseAny(baseColorString);
      Color newColor = addValuesToHsbComponents(baseColor,
                                                hueToAdd,
                                                saturationToAdd,
                                                brightnessToAdd);

      return formatColor(newColor);
    }

    /**
     * Adds the specified amount to the specified HSB (Hue, Saturation,
     * Brightness) parameter of the given color. The amount can be negative.
     *
     * @param baseColor The color to modify
     * @param hueToAdd The amount of hue to add
     * @param saturationToAdd The amount of saturation to add
     * @param brightnessToAdd The amount of brightness to add
     * @return The modified color
     */
    public Color addValuesToHsbComponents(Color baseColor,
                                          int hueToAdd,
                                          int saturationToAdd,
                                          int brightnessToAdd) {

      float[] hsbValues = toHsb(baseColor);

      // In HSB color space, Hue goes from 0 to 360, Saturation and Brightness
      // from 0 to 100. However, in Java all three parameters vary from 0.0 to
      // 1.0, so we need some basic conversion.
      hsbValues[0] = (float) (hsbValues[0] + hueToAdd / 360.0);
      // The hue needs to wrap around, so just keep hue - floor(hue).
      hsbValues[0] -= (float) Math.floor(hsbValues[0]);

      // For saturation and brightness, no wrapping around, we just make sure
      // we don't go over 1.0 or under 0.0
      hsbValues[1] = (float) Math.min(1.0, Math.max(0,
          hsbValues[1] + saturationToAdd / 100.0));
      hsbValues[2] = (float) Math.min(1.0, Math.max(0,
          hsbValues[2] + brightnessToAdd / 100.0));

      return Color.getHSBColor(hsbValues[0], hsbValues[1], hsbValues[2]);
    }
  }

  /**
   * Implementation of the semiTransparentBackgroundColor function. Takes
   * two arguments: the color and the alpha value, both in hexadecimal format.
   * This is implemented with two arguments so that the alpha value can be
   * reused for rounded corners.
   */
  public abstract static class SemiTransparentBackgroundColor
      implements GssFunction {

    @Override
    public Integer getNumExpectedArguments() {
      return 2;
    }

    /**
     * Returns the semi-transparent image corresponding to the arguments
     * documented in {@link SemiTransparentBackgroundColor}.
     *
     * @param args The list of arguments
     * @return The image of the rounded corner
     */
    @Override
    public List<CssValueNode> getCallResultNodes(List<CssValueNode> args,
        ErrorManager errorManager) throws GssFunctionException {
      CssValueNode arg1 = args.get(0);
      CssValueNode arg2 = args.get(1);

      String color = getColorFromNode(arg1, errorManager);
      String alphaChannel = arg2.toString();

      String urlArg = semiTransparentBackgroundUrl(color, alphaChannel);
      SourceCodeLocation location = arg1.getSourceCodeLocation();

      return ImmutableList.of((CssValueNode)createUrlNode(urlArg, location));
    }

    @Override
    public String getCallResultString(List<String> args) {
      return createUrl(semiTransparentBackgroundUrl(
          getColor(args.get(0)), args.get(1)));
    }

    protected abstract String semiTransparentBackgroundUrl(
        String color, String alphaChannel);
  }


  /**
   * Implementation of the makeMutedColor GSS function. This is intended to
   * generate a muted flavor of a text or link color. Takes three arguments: the
   * background color over which this text or link will appear, and the text or
   * link color this should be a muted version of and optionally the loss of
   * saturation for muted tone (0 <= loss <= 1).
   */
  public static class MakeMutedColor implements GssFunction {

    private float LOSS_OF_SATURATION_FOR_MUTED_TONE = 0.2f;
    private String ARGUMENT_COUNT_ERROR_MESSAGE = "makeMutedColor " +
        "expected arguments: backgroundColorStr, foregroundColorStr and an " +
        "optional loss of saturation value (0 <= loss <= 1).";

    /**
     * Returns the number of expected arguments of this GSS function.
     *
     * @return Number of expected arguments
     */
    @Override
    public Integer getNumExpectedArguments() {
      return null;
    }

    /**
     * Returns the muted color corresponding to the arguments
     * documented in {@link MakeMutedColor}.
     *
     * @param args The list of arguments.
     * @return The generated muted color.
     */
    @Override
    public List<CssValueNode> getCallResultNodes(List<CssValueNode> args,
        ErrorManager errorManager) throws GssFunctionException {

      if (args.size() != 2 && args.size() != 3) {
        throw new GssFunctionException(ARGUMENT_COUNT_ERROR_MESSAGE);
      }

      CssValueNode backgroundColorNode = args.get(0);
      CssValueNode foregroundColorNode = args.get(1);
      String lossOfSaturationForMutedTone =
          String.valueOf(LOSS_OF_SATURATION_FOR_MUTED_TONE);

      if (args.size() == 3) {
        lossOfSaturationForMutedTone = args.get(2).getValue();
      }

      String backgroundColorStr = backgroundColorNode.getValue();
      String foregroundColorStr = foregroundColorNode.getValue();

      String resultStr = makeMutedColor(backgroundColorStr, foregroundColorStr,
          lossOfSaturationForMutedTone);

      CssHexColorNode result = new CssHexColorNode(resultStr,
          backgroundColorNode.getSourceCodeLocation());
      return ImmutableList.of((CssValueNode) result);
    }

    protected String makeMutedColor(
        String backgroundColorStr, String foregroundColorStr, String lossStr) {

      // If the background is transparent, or if the foreground is transparent,
      // there's really no way we can know how to pick a muted color. We thus
      // return the foreground color unchanged.
      if ("transparent".equalsIgnoreCase(backgroundColorStr)
          || "transparent".equalsIgnoreCase(foregroundColorStr)) {
        return foregroundColorStr;
      }
      Color backgroundColor = ColorParser.parseAny(backgroundColorStr);
      Color foregroundColor = ColorParser.parseAny(foregroundColorStr);

      float[] backgroundColorHsb = toHsb(backgroundColor);
      float[] foregroundColorHsb = toHsb(foregroundColor);
      float lossOfSaturationForMutedTone = Float.valueOf(lossStr);

      // Make sure that 0 <= lossOfSaturationForMutedTone <= 1
      if (lossOfSaturationForMutedTone < 0) {
        lossOfSaturationForMutedTone = 0;
      } else if (lossOfSaturationForMutedTone > 1) {
        lossOfSaturationForMutedTone = 1;
      }

      // We take the hue from the foreground color, we desaturate it a little
      // bit, and choose a brightness halfway between foreground and background.
      // For example, if the background has a brightness of 50, and 100 for the
      // foreground, the muted color will have 75. If we have a dark background,
      // it should be the reverse.
      float mutedHue = foregroundColorHsb[0];
      float mutedSaturation = Math.max(
          foregroundColorHsb[1] - lossOfSaturationForMutedTone, 0);
      float mutedBrightness = (foregroundColorHsb[2] + backgroundColorHsb[2]) /
          2;

      Color mutedColor
          = Color.getHSBColor(mutedHue, mutedSaturation, mutedBrightness);

      return formatColor(mutedColor);
    }

    protected String makeMutedColor(
        String backgroundColorStr, String foregroundColorStr) {
      return  makeMutedColor(backgroundColorStr, foregroundColorStr,
          String.valueOf(LOSS_OF_SATURATION_FOR_MUTED_TONE));
    }

    @Override
    public String getCallResultString(List<String> args) throws
        GssFunctionException {
      if (args.size() == 2) {
        return makeMutedColor(args.get(0), args.get(1));
      } else if (args.size() == 3) {
        return makeMutedColor(args.get(0), args.get(1), args.get(2));
      } else {
        throw new GssFunctionException(ARGUMENT_COUNT_ERROR_MESSAGE);
      }
    }
  }


  /**
   * Abstract class implementing the shared logic for the arithmetic functions.
   */
  private static abstract class LeftAssociativeOperator implements GssFunction {

    /**
     * Returns the number of expected arguments of this GSS function.
     *
     * @return Number of expected arguments
     */
    @Override
    public Integer getNumExpectedArguments() {
      // Takes a variable number of arguments.
      return null;
    }

    /**
     * Returns a new value of the same unit as the original.
     *
     * @param args The list of arguments
     * @return The resulting fingerprint as a numeric node in a list
     */
    @Override
    public List<CssValueNode> getCallResultNodes(List<CssValueNode> args,
        ErrorManager errorManager) throws GssFunctionException {
      List<CssNumericNode> numericList = Lists.newArrayList();
      for (CssValueNode arg : args) {
        numericList.add(getSizeNode(arg, errorManager,
            true /* isUnitOptional */));
      }
      return ImmutableList.<CssValueNode>of(
          calculate(numericList, errorManager));
    }

    @Override
    public String getCallResultString(List<String> args)
        throws GssFunctionException {
      List<CssNumericNode> numericList = Lists.newArrayList();
      for (String arg : args) {
        // Note, the unit may be 'NO_UNITS'
        Size sizeWithUnits = parseSize(arg, true /* isUnitOptional */);
        numericList.add(
            new CssNumericNode(sizeWithUnits.size, sizeWithUnits.units));
      }
      CssNumericNode result = calculate(numericList, null);
      return result.getNumericPart() + result.getUnit();
    }

    // Note: Keep an eye on the performance of these functions, as creating
    // intermediate CssNumericNodes may be wasteful. Instead, the values in the
    // nodes could be used directly for computation, though that may make
    // accurate error reporting more difficult.

    protected CssNumericNode calculate(List<CssNumericNode> args,
        ErrorManager errorManager) throws GssFunctionException {
      if (args.size() < 2) {
        throw error("Not enough arguments",
                    errorManager, args.get(0).getSourceCodeLocation());
      }

      double total = Double.valueOf(args.get(0).getNumericPart());
      String overallUnit =
          isIdentityValue(total) ? null : args.get(0).getUnit();

      for (CssNumericNode node : args.subList(1, args.size())) {
        double value = Double.valueOf(node.getNumericPart());
        if (isIdentityValue(value)) {
          continue;
        }
        if (overallUnit == null) {
          overallUnit = node.getUnit();
        } else if (!overallUnit.equals(node.getUnit())) {
          throw error(
              "Parameters' units don't match (\""
              + overallUnit + "\" vs \"" + node.getUnit() + "\")",
              errorManager, node.getSourceCodeLocation());
        }
        total = performOperation(total, value);
      }
      String resultString = new DecimalFormat(DECIMAL_FORMAT).format(total);

      return new CssNumericNode(resultString,
          overallUnit != null ? overallUnit : CssNumericNode.NO_UNITS,
          args.get(0).getSourceCodeLocation());
    }

    // Perform the mathematical operation.
    protected abstract double performOperation(double left, double right);

    /**
     * By default, this method returns {@code false}.
     * @return whether the identity value has no effect on the output. In this
     *     case, any units (px, pt, etc.) will be ignored.
     */
    protected boolean isIdentityValue(double value) {
      return false;
    }
  }


  /**
   * The "add()" function adds a list of numeric values.
   */
  public static class AddToNumericValue extends LeftAssociativeOperator {
    @Override
    protected double performOperation(double left, double right) {
      return left + right;
    }

    @Override
    protected boolean isIdentityValue(double value) {
      return value == 0.0;
    }
  }

  /**
   * The "sub()" function subtracts a list of numeric values.
   * SubtractFromNumericValue(a, b, c) evaluates to ((a - b) - c).
   */
  public static class SubtractFromNumericValue extends LeftAssociativeOperator {
    @Override
    protected double performOperation(double left, double right) {
      return left - right;
    }

    @Override
    protected boolean isIdentityValue(double value) {
      return value == 0.0;
    }
  }

  /**
   * A {@link GssFunction} that returns the max value from its list of
   * arguments.
   */
  public static class MaxValue extends LeftAssociativeOperator {
    @Override
    protected double performOperation(double left, double right) {
      return Math.max(left, right);
    }
  }

  /**
   * A {@link GssFunction} that returns the min value from its list of
   * arguments.
   */
  public static class MinValue extends LeftAssociativeOperator {
    @Override
    protected double performOperation(double left, double right) {
      return Math.min(left, right);
    }
  }

  /**
   * A {@link ScalarLeftAssociativeOperator} is a left associative operator
   * whose arguments are all scalars, with the possible exception of the first
   * argument, which may be a {@link Size} rather than a scalar.
   */
  private static abstract class ScalarLeftAssociativeOperator extends
      LeftAssociativeOperator {

    @Override
    protected CssNumericNode calculate(List<CssNumericNode> args,
        ErrorManager errorManager) throws GssFunctionException {
      if (args.size() == 0) {
        throw error("Not enough arguments",
                    errorManager, args.get(0).getSourceCodeLocation());
      }

      double total = Double.valueOf(args.get(0).getNumericPart());
      String overallUnit = args.get(0).getUnit();

      for (CssNumericNode node : args.subList(1, args.size())) {
        if (node.getUnit() != null
            && !node.getUnit().equals(CssNumericNode.NO_UNITS)) {
          throw error(
              "Only the first argument may have a unit associated with it, "
              + " but has unit: " + node.getUnit(),
              errorManager, node.getSourceCodeLocation());
        }

        double value = Double.valueOf(node.getNumericPart());
        total = performOperation(total, value);
      }
      String resultString = new DecimalFormat(DECIMAL_FORMAT).format(total);

      return new CssNumericNode(resultString,
          overallUnit != null ? overallUnit : CssNumericNode.NO_UNITS,
          args.get(0).getSourceCodeLocation());
    }
  }

  /**
   * A {@link GssFunction} that returns the product of its arguments. Only the
   * first argument may have a unit.
   */
  public static class Mult extends ScalarLeftAssociativeOperator {
    @Override
    protected double performOperation(double left, double right) {
      return left * right;
    }

    @Override
    protected boolean isIdentityValue(double value) {
      return value == 1.0;
    }
  }

  /**
   * A {@link GssFunction} that returns the quotient of its arguments. Only the
   * first argument may have a unit.
   */
  public static class Div extends ScalarLeftAssociativeOperator {
    @Override
    protected double performOperation(double left, double right) {
      return left / right;
    }

    @Override
    protected boolean isIdentityValue(double value) {
      return value == 1.0;
    }
  }

  /**
   * Implementation of the adjustBrightness GSS function. This generates
   * a slightly differentiated color suitable for hover styling. Takes the
   * color to modify as the first argument, and the requested brightness
   * difference as a second argument (between 0 and 100). This function will
   * always ensure that the returned color is different from the input, e.g.
   * attempting to "brighten" white will return a light grey instead of white,
   * but if it isn't possible to find a color that matches the request
   * difference (e.g. asking to brighten by 100 from a medium bright color),
   * the returned value will be a color with a difference from the input color
   * as close as possible to what is being requested. See the unit test for
   * some examples.
   */
  public static class AdjustBrightness implements GssFunction {

    /**
     * Returns the number of expected arguments of this GSS function.
     *
     * @return Number of expected arguments
     */
    @Override
    public Integer getNumExpectedArguments() {
      return 2;
    }

    /**
     * Returns the hover color corresponding to the argument
     * documented in {@link AdjustBrightness}.
     *
     * @param args The list of arguments.
     * @return The generated hover color.
     */
    @Override
    public List<CssValueNode> getCallResultNodes(List<CssValueNode> args,
        ErrorManager errorManager) {

      CssValueNode originalColorNode = args.get(0);
      CssValueNode brightnessAmount = args.get(1);

      String originalColorStr = originalColorNode.getValue();
      String brightnessAmountStr =
          ((CssNumericNode) brightnessAmount).getNumericPart();

      String resultStr = adjustBrightness(originalColorStr,
          brightnessAmountStr);

      CssHexColorNode result = new CssHexColorNode(resultStr,
          originalColorNode.getSourceCodeLocation());
      return ImmutableList.of((CssValueNode) result);
    }

    private float normalize(float value) {
      if (value > 1.0) {
        return 1;
      }
      if (value < 0.0) {
        return 0;
      }
      return value;
    }

    private String formatColorWithAdjustedBrightness (float[] originalHsb,
        float adjustedBrightness) {
      return formatColor(Color.getHSBColor(originalHsb[0],
          originalHsb[1], adjustedBrightness));
    }

    protected String adjustBrightness(String originalColorStr,
        String brightnessStr) {

      // If the input color is transparent, there's really no way we can know
      // how to pick a good output color. We thus return the color unchanged.
      if ("transparent".equalsIgnoreCase(originalColorStr)) {
        return originalColorStr;
      }
      Color originalColor = ColorParser.parseAny(originalColorStr);
      float brightnessFloat = Float.parseFloat(brightnessStr) / (float) 100.0;

      float[] originalColorHsb = toHsb(originalColor);
      float requestedBrightness = originalColorHsb[2] + brightnessFloat;

      // If we're not "saturating" to white or black, then we can meet
      // exactly what the caller requests.
      if (requestedBrightness >= 0.0 && requestedBrightness <= 1.0) {
        return formatColorWithAdjustedBrightness(originalColorHsb,
            requestedBrightness);
      }

      // If we can't get exactly what's requested, we try both directions to
      // be as close as possible to the requested brightness difference.
      requestedBrightness = normalize(requestedBrightness);
      float oppositeDirectionBrightness =
          normalize(originalColorHsb[2] - brightnessFloat);

      // Calculate the distance between what the caller requests and the two
      // possibilites we have, then return the closest.
      float chosenBrightness = Math.abs(brightnessFloat -
          (originalColorHsb[2] - requestedBrightness)) >
              Math.abs(brightnessFloat -
                  (originalColorHsb[2] - oppositeDirectionBrightness)) ?
                      oppositeDirectionBrightness : requestedBrightness;
      return formatColorWithAdjustedBrightness(originalColorHsb,
          chosenBrightness);
    }

    @Override
    public String getCallResultString(List<String> args) {
      return adjustBrightness(args.get(0), args.get(1));
    }
  }


  /**
   * The "makeContrastingColor" function generates a contrasting color with the
   * same hue as the input color.  The generated color passes (or almost
   * passes) the contrast test described in the W3C accessibility evaluation
   * working draft {@link "http://www.w3.org/TR/AERT#color-contrast"}.
   *
   * <p>The function takes two parameters:
   * <ul>
   * <li>The input color to find the contrasting color for,
   * <li>the similarity value (a float between 0.0 - 1.0) which tells how
   *   similar the computed color should be to the input color.
   * </ul>
   *
   * <p>The algorithm for the contrasting color generation is as follows:
   * <ol>
   * <li>First a base contrasting color is chosen.  It is either black or
   *   white, as one of these colors is guaranteed to be in contrast with any
   *   given color.  Additionally both of these colors are "hue-neutral", so
   *   choosing the contrasting color in between the input color and black or
   *   white should give pleasant results.
   *
   * <li>A closest required contrasting color is found.  This is a color in
   *   between the input color and the base contrasting color.  It has the same
   *   hue as the input color.  This color is found in HSB color space, by a
   *   bisection of a line between the input color and the base contrasting
   *   color.  To improve function performance, a limited number of the
   *   bisection steps ({@link MakeContrastingColor#NUM_ITERATIONS}) is
   *   performed.  This gives a constant expected time of function execution.
   *   At the same time the color that is found may not pass the contrast test,
   *   but is guaranteed to be close to the "contrast boundary".
   *
   * <li>The output color of the function is an interpolation of the closest
   *   contrasting color and the base contrasting color.  The interpolation is
   *   controlled using the "similarity" parameter.  If similarity is set to 1,
   *   it means that the result is the closest contrasting color.  If
   *   similarity is set to 0, it means that the result is the base contrasting
   *   color.
   * </ol>
   */
  public static class MakeContrastingColor implements GssFunction {

    /**
     * Number of iterations to approximate the closest contrasting color.
     * It is set to a number which should be enough to converge in the 8-bit
     * color component space we deal with. Since number of iterations is fixed
     * function computation time is also fixed.
     */
    private static final int NUM_ITERATIONS = 8;

    @Override
    public Integer getNumExpectedArguments() {
      return 2;
    }

    @Override
    public List<CssValueNode> getCallResultNodes(
        List<CssValueNode> args, ErrorManager errorManager) {
      CssValueNode arg1 = args.get(0);
      CssValueNode arg2 = args.get(1);

      // TODO(dgajda): We should check the type of the color node.
      String color = arg1.getValue();
      String similarity = arg2.toString();

      String resultStr = makeContrastingColor(color, similarity);

      CssHexColorNode result = new CssHexColorNode(resultStr,
          arg1.getSourceCodeLocation());
      return ImmutableList.of((CssValueNode)result);
    }

    @Override
    public String getCallResultString(List<String> args) {
      return makeContrastingColor(args.get(0), args.get(1));
    }

    protected String makeContrastingColor(
        String inputColorStr, String similarityStr) {
      if ("transparent".equalsIgnoreCase(inputColorStr)) {
        return inputColorStr;
      }
      Color inputColor = ColorParser.parseAny(inputColorStr);
      float similarity = Float.parseFloat(similarityStr);

      float[] distantColor = toHsb(
          getDistantColor(inputColor, Color.BLACK, Color.WHITE));

      float[] startColor = toHsb(inputColor);
      float[] endColor = distantColor;
      float[] closestContrastColor = null;

      for (int i = 0; i < NUM_ITERATIONS; i++) {
        closestContrastColor = blendSb(startColor, endColor, 0.5f);
        if (testContrast(inputColor, hsbToColor(closestContrastColor))) {
          endColor = closestContrastColor;
        } else {
          startColor = closestContrastColor;
        }
      }

      float[] resultColor = blendSb(
          closestContrastColor, distantColor, similarity);
      return formatColor(hsbToColor(resultColor));
    }

    private Color getDistantColor(
        Color color, Color first, Color second) {
      int firstLuminanceDiff = ColorUtil.luminanceDiff(color, first);
      int secondLuminanceDiff = ColorUtil.luminanceDiff(color, second);
      return firstLuminanceDiff >= secondLuminanceDiff ? first : second;
    }

    private float[] blendSb(float[] keepHue, float[] other, float similarity) {
      float[] result = Arrays.copyOf(keepHue, keepHue.length);
      mix(ColorUtil.S, keepHue, other, similarity, result);
      mix(ColorUtil.B, keepHue, other, similarity, result);
      return result;
    }

    private void mix(
        int componentIdx, float[] sourceHsb, float[] otherColorHsb,
        float sourceSimilarity, float[] resultHsb) {
      resultHsb[componentIdx] =
          sourceHsb[componentIdx] * sourceSimilarity
          + otherColorHsb[componentIdx] * (1f - sourceSimilarity);
    }
  }

  /**
   * Takes an input color and sets its alpha component without affecting
   * the RGB components.
   * Usage: makeTranslucent(existingColor, alphaValue);
   */
  public static class MakeTranslucent implements GssFunction {
    @Override
    public Integer getNumExpectedArguments() {
      return 2;
    }

    @Override
    public List<CssValueNode> getCallResultNodes(
        List<CssValueNode> args, ErrorManager errorManager) {
      CssValueNode arg1 = args.get(0);
      CssValueNode arg2 = args.get(1);

      String color = arg1.getValue();
      String alpha = arg2.toString();

      return ImmutableList.of(makeTranslucent(
          color, alpha, arg1.getSourceCodeLocation()));
    }

    @Override
    public String getCallResultString(List<String> args) {
      return makeTranslucent(args.get(0), args.get(1), null).getValue();
    }

    protected CssValueNode makeTranslucent(
        String inputColorStr, String alphaStr,
        @Nullable SourceCodeLocation sourceCodeLocation) {
      Color inputColor = ColorParser.parseAny(inputColorStr);
      double alpha = Math.min(1.0, Math.max(0, Float.parseFloat(alphaStr)));

      float[] rgb = inputColor.getRGBColorComponents(null);
      Color outputColor = new Color(rgb[0], rgb[1], rgb[2], (float) alpha);

      List<CssValueNode> argList = ImmutableList.<CssValueNode>of(
          new CssLiteralNode(
              Integer.toString(outputColor.getRed()), sourceCodeLocation),
          new CssLiteralNode(
              Integer.toString(outputColor.getGreen()), sourceCodeLocation),
          new CssLiteralNode(
              Integer.toString(outputColor.getBlue()), sourceCodeLocation),
          new CssLiteralNode(
              new DecimalFormat("#.###").format(outputColor.getAlpha() / 255f),
              sourceCodeLocation));
      CssValueNode argsValue = new CssCompositeValueNode(
          argList, CssCompositeValueNode.Operator.COMMA,
          sourceCodeLocation);
      CssFunctionNode result = new CssFunctionNode(
          CssFunctionNode.Function.byName("rgba"),
          sourceCodeLocation);
      result.setArguments(new CssFunctionArgumentsNode(
          ImmutableList.of(argsValue)));
      return result;
    }
  }


  /**
   * Allows the equivalent of the ternary operator in GSS, using three
   * {@code @def} statements as inputs. This GSS:
   * <p>
   * {@code @def MYDEF selectFrom(FOO, BAR, BAZ);}
   * <p>
   * implies:
   * <p>
   * {@code MYDEF = FOO ? BAR : BAZ;}
   * <p>
   * So this gss:
   * <p>
   * {@code @def FOO true;}
   * <p>
   * then implies:
   * <p>
   * {@code MYDEF = BAR;}
   */
  public static class SelectFrom implements GssFunction {
    @Override
    public Integer getNumExpectedArguments() {
      return 3;
    }

    @Override
    public List<CssValueNode> getCallResultNodes(
        List<CssValueNode> args, ErrorManager errorManager) {
      return ImmutableList.of("true".equals(args.get(0).getValue()) ?
          args.get(1) : args.get(2));
    }

    @Override
    public String getCallResultString(List<String> args) {
      return "true".equals(args.get(0)) ? args.get(1) : args.get(2);
    }
  }

  public static GssFunctionException error(
      CssValueNode node, String errorMessage, ErrorManager errorManager) {
    return error(errorMessage, errorManager, node.getSourceCodeLocation());
  }

  private static GssFunctionException error(
      String errorMessage, ErrorManager errorManager,
      SourceCodeLocation location) {
    if (errorManager != null) {
      errorManager.report(new GssError(errorMessage, location));
    }
    return new GssFunctionException(errorMessage);
  }

  private static CssNumericNode getSizeNode(CssValueNode valueNode,
      ErrorManager errorManager, boolean isUnitOptional)
      throws GssFunctionException {
    SourceCodeLocation location = valueNode.getSourceCodeLocation();
    if (valueNode instanceof CssNumericNode) {
      CssNumericNode node = (CssNumericNode)valueNode;
      checkSize(node.getNumericPart(), node.getUnit(), errorManager, location,
          isUnitOptional);
      return node;
    }
    String message = "Size must be a CssNumericNode with a unit or 0; "
        + "was: " + valueNode.toString();
    throw error(message, errorManager, location);
  }

  private static void checkSize(String valueString, String unit,
      ErrorManager errorManager, SourceCodeLocation location,
      boolean isUnitOptional) throws GssFunctionException {
    if (unit.equals(CssNumericNode.NO_UNITS)) {
      Double value = Double.parseDouble(valueString);
      if (value != 0.0 && !isUnitOptional) {
        String message = "Size must be 0 or have a unit; was: "
            + valueString + unit;
        throw error(message, errorManager, location);
      }
    }
  }

  private static String getColorFromNode(CssValueNode arg,
      ErrorManager errorManager) throws GssFunctionException {
    // Note: we can expect CssHexColorNode or a CssLiteralNode (for
    // example "white"), but not functions (like "rgb(x,y,z)").
    if (arg instanceof CssHexColorNode) {
      return arg.getValue().substring(1); // cut the "#"
    } else if (arg instanceof CssLiteralNode) {
      return arg.getValue();
    }
    String message = "First argument must be a CssHexColorNode or "
        + "LiteralNode containing a color name";
    throw error(arg, message, errorManager);
  }

  private static String getColor(String color) {
    if (color.charAt(0) == '#') {
      return color.substring(1);
    }
    return color;
  }

  /**
   * Helper method for implementors of GssFunction to allow the creation of
   * a url string in the GSS.
   *
   * @param url The url of the image to add.
   * @return The proper GSS url string.
   */
  public static String createUrl(String url) {
    return CssFunctionNode.Function.byName("url") + "(" + url  + ")";
  }

  private static final class Size {
    final String size;
    final String units;

    public Size(String size, String units) {
      this.size = size;
      this.units = units;
    }
  }

  private static Size parseSize(String sizeWithUnits, boolean isUnitOptional)
      throws GssFunctionException {
    int unitIndex = CharMatcher.JAVA_LETTER.indexIn(sizeWithUnits);
    String size = unitIndex > 0 ?
        sizeWithUnits.substring(0, unitIndex) : sizeWithUnits;
    String units = unitIndex > 0 ?
        sizeWithUnits.substring(unitIndex) : CssNumericNode.NO_UNITS;
    checkSize(size, units, null /* errorManager */, null /* location */,
        isUnitOptional);
    return new Size(size, units);
  }
}
