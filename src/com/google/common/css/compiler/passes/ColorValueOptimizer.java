/*
 * Copyright 2009 Google Inc.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssFunctionNode.Function;
import com.google.common.css.compiler.ast.CssHexColorNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;
import java.util.logging.Logger;

/**
 * Compiler pass that optimizes color values. It shrinks 6-digit hex values to
 * 3-digit where possible, and converts rgb(r, g, b) to hex.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class ColorValueOptimizer extends DefaultTreeVisitor
    implements CssCompilerPass {

  private static final Logger logger = Logger.getLogger(
      ColorValueOptimizer.class.getName());

  private static final Function RGB = Function.byName("rgb");
  
  private MutatingVisitController visitController;

  public ColorValueOptimizer(MutatingVisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public boolean enterFunctionNode(CssFunctionNode function) {
    if (function.getFunction() == RGB) {
      try {
        String hexValue = parseRgbArguments(function);
        if (canShortenHexString(hexValue)) {
          hexValue = shortenHexString(hexValue);
        }
        CssValueNode optimizedColor = new CssHexColorNode(
            hexValue,
            function.getSourceCodeLocation());
        List<CssNode> temp = Lists.newArrayList();
        temp.add(optimizedColor);
        visitController.replaceCurrentBlockChildWith(temp, true);
      } catch (NumberFormatException nfe) {
        logger.info("Error parsing rgb() function: " + nfe.toString());
      }
    }
    return true;
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    if (node instanceof CssHexColorNode) {
      CssHexColorNode color = (CssHexColorNode) node;

      if (canShortenHexString(color.getValue())) {
        String hexValue = shortenHexString(color.getValue());

        CssValueNode optimizedColor = new CssHexColorNode(
            hexValue,
            node.getSourceCodeLocation());
        List<CssNode> temp = Lists.newArrayList();
        temp.add(optimizedColor);
        visitController.replaceCurrentBlockChildWith(temp, true);
      }
    }
    return true;
  }

  /**
   * Extract the rgb function arguments and convert them to a standard RGB
   * hex value.
   * @param function A function node.
   * @return The 6-digit hex value, including leading # sign.
   * @throws NumberFormatException when input is invalid.
   */
  @VisibleForTesting
  static String parseRgbArguments(CssFunctionNode function)
      throws NumberFormatException {
    CssFunctionArgumentsNode args = function.getArguments();

    int numArgs = 0;
    StringBuilder hexValue = new StringBuilder("#");
    for (CssValueNode rgbValue : args.getChildren()) {
      if (rgbValue instanceof CssNumericNode) {
        numArgs++;
        CssNumericNode numericValue = (CssNumericNode) rgbValue;
        int scalarValue = Integer.parseInt(numericValue.getNumericPart());
        if ("%".equals(numericValue.getUnit())) {
          scalarValue = (int) (255.0 * scalarValue / 100 + 0.5);
        } else if (!CssNumericNode.NO_UNITS.equals(numericValue.getUnit())) {
          throw new NumberFormatException("rgb arguments must be scalar or " +
              "%. Bad value:" + numericValue.toString());
        }
        // According to W3C specs, out-of-range values are OK, but there's a
        // good chance it's unintentional, so emit a warning.
        if (scalarValue < 0) {
          logger.info("Out of range argument to rgb(): " + numericValue);
          scalarValue = 0;
        }
        if (scalarValue > 255) {
          logger.info("Out of range argument to rgb(): " + numericValue);
          scalarValue = 255;
        }
        if (scalarValue < 16) {
          hexValue.append('0');
        }
        hexValue.append(Integer.toHexString(scalarValue));
      } else if (rgbValue instanceof CssLiteralNode &&
          ",".equals(rgbValue.getValue())) {
        // Sadly, the comma separators parse as function arguments, just
        // ignore and skip over them.
      } else {
        throw new NumberFormatException("Expected numeric value:" +
            rgbValue.getValue());
      }
    }

    if (numArgs != 3) {
      throw new NumberFormatException("Invalid number of arguments to rgb().");
    }

    return hexValue.toString();
  }

  /**
   * Determine whether an RGB hex value can be abbreviated.
   * @param hex An RGB hex value, such as "#00ffcc".
   * @return Whether the value can be abbreviated.
   */
  @VisibleForTesting
  static boolean canShortenHexString(String hex) {
    Preconditions.checkArgument(hex.startsWith("#"));
    return hex.length() == 7 &&
        hex.charAt(1) == hex.charAt(2) &&
        hex.charAt(3) == hex.charAt(4) &&
        hex.charAt(5) == hex.charAt(6);
  }

  /**
   * Converts a 6-digit RGB hex value to its 3-digit equivalent.
   * This method assumes that {@link #canShortenHexString} has returned true.
   * @param hex Hex value, including leading "#".
   * @return 3-digit hex value, including leading "#".
   */
  @VisibleForTesting
  static String shortenHexString(String hex) {
    StringBuilder optimizedHexValue = new StringBuilder("#");
    optimizedHexValue.append(hex.charAt(1));
    optimizedHexValue.append(hex.charAt(3));
    optimizedHexValue.append(hex.charAt(5));

    return optimizedHexValue.toString();
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
