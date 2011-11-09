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

package com.google.common.css.compiler.gssfunctions;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;

import java.util.List;

/**
 * Custom {@link GssFunction}s performing arithmetic operations.
 *
 */
public final class ArithmeticGssFunctions {

  /**
   * Wrapper for Float.valueOf() because Java does not have first-order
   * functions.
   */
  private static final Function<String, Float> stringToFloat =
      new Function<String, Float>() {
        @Override
        public Float apply(String input) {
          return Float.valueOf(input);
        }
  };

  private ArithmeticGssFunctions() {}

  /**
   * Base class for arithmetic computations.
   */
  public static abstract class BaseArithmeticFunction implements GssFunction {

    @Override
    public Integer getNumExpectedArguments() {
      // At least two numbers and a unit.
      return null;
    }

    @Override
    public List<CssValueNode> getCallResultNodes(List<CssValueNode> args,
        ErrorManager errorManager) throws GssFunctionException {
      // Validate the number of arguments.
      if (args.size() < 3) {
        String message =
            "Incorrect number of arguments: must have at least three";
        // Note that there is no way to report this error if there are no
        // arguments because then it is not possible to determine the
        // SourceCodeLocation.
        if (errorManager != null && args.size() > 0) {
          errorManager.report(new GssError(message,
              args.get(0).getSourceCodeLocation()));
        }
        throw new GssFunctionException(message);
      }

      // Validate arguments 1...N-1 are numeric.
      List<String> values = Lists.newArrayListWithCapacity(args.size() - 1);
      for (int i = 0, maxIndex = args.size() - 1; i < maxIndex; i++) {
        CssValueNode argNode = args.get(i);
        if (argNode instanceof CssNumericNode) {
          values.add(((CssNumericNode)argNode).getNumericPart());
        } else {
          String message = "Incorrect argument #" + (i + 1) + " in function";
          if (errorManager != null) {
            errorManager.report(new GssError(message,
                argNode.getSourceCodeLocation()));
          }
          throw new GssFunctionException(message + ": \"" + argNode +
              "\". Expected: CssNumericNode");
        }
      }

      // Validate the last argument is a literal (for the unit, e.g., 'px').
      CssValueNode lastArg = args.get(values.size());
      if (!(lastArg instanceof CssLiteralNode)) {
        String message =
            "Incorrect last argument in function -- should be a unit.";
        if (errorManager != null) {
          errorManager.report(new GssError(message,
              lastArg.getSourceCodeLocation()));
        }
        throw new GssFunctionException(message + ": \"" + lastArg +
            "\". Expected: CssLiteralNode");
      }

      String unit = lastArg.getValue();
      return ImmutableList.<CssValueNode>of(compute(values, unit));
    }

    @Override
    public String getCallResultString(List<String> args)
        throws GssFunctionException {
      try {
        List<String> values = args.subList(0, args.size() - 1);
        String unit = args.get(args.size() - 1);
        CssNumericNode result = compute(values, unit);
        return result.getNumericPart() + result.getUnit();
      } catch (NumberFormatException e) {
        throw new GssFunctionException(
            "First and second argument should be numeric: " + args);
      }
    }

    private CssNumericNode compute(List<String> values, String unit) {
      if (unit.startsWith("\"") && unit.endsWith("\"")) {
        unit = unit.substring(1, unit.length() - 1);
      }
      Float result = compute(Lists.transform(values, stringToFloat));
      String resultString = result.toString();
      if (result == Math.ceil(result)) {
        resultString = resultString.substring(0, resultString.indexOf('.'));
      }
      return new CssNumericNode(resultString, unit, null);
    }

    public Float compute(List<Float> values) {
      Preconditions.checkNotNull(values);
      Preconditions.checkArgument(values.size() >= 2,
          "Must have at least two numeric arguments.");
      return computeInternal(values.get(0), values.subList(1, values.size()));
    }

    /**
     * When this method is invoked, it is guaranteed to have at least one value
     * in the floats array.
     */
    protected abstract Float computeInternal(Float first, List<Float> rest);
  }

  public static class Plus extends BaseArithmeticFunction {
    @Override
    protected Float computeInternal(Float first, List<Float> rest) {
      float sum = first;
      for (Float f : rest) {
        sum += f;
      }
      return sum;
    }
  }

  public static class Minus extends BaseArithmeticFunction {
    @Override
    protected Float computeInternal(Float first, List<Float> rest) {
      float difference = first;
      for (Float f : rest) {
        difference -= f;
      }
      return difference;
    }
  }

  public static class Mult extends BaseArithmeticFunction {
    @Override
    protected Float computeInternal(Float first, List<Float> rest) {
      float product = first;
      for (Float f : rest) {
        product *= f;
      }
      return product;
    }
  }

  public static class Div extends BaseArithmeticFunction {
    @Override
    protected Float computeInternal(Float first, List<Float> rest) {
      float quotient = first;
      for (Float f : rest) {
        quotient /= f;
      }
      return quotient;
    }
  }

  public static class Max extends BaseArithmeticFunction {
    @Override
    protected Float computeInternal(Float first, List<Float> rest) {
      float max = first;
      for (Float f : rest) {
        max = Math.max(f, max);
      }
      return max;
    }
  }

  public static class Min extends BaseArithmeticFunction {
    @Override
    protected Float computeInternal(Float first, List<Float> rest) {
      float min = first;
      for (Float f : rest) {
        min = Math.min(f, min);
      }
      return min;
    }
  }
}
