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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.css.SourceCodeLocation;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * A node representing a function.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssFunctionNode extends CssValueNode implements ChunkAware {

  /**
   * Contains the list of recognized CSS functions.
   */
  public abstract static class Function {

    /** A map of function names to function objects. */
    private static final Map<String, Function> NAME_TO_FUNCTION_MAP;

    static {
      List<String> recognizedCssFunctions = ImmutableList.of(
          // CSS 2.1
          "attr",
          "counter",
          "rect",
          "rgb",
          "url",

          // Per-site user stylesheet rules
          // http://lists.w3.org/Archives/Public/www-style/2004Aug/0135
          "domain",
          "url-prefix",

          // IE8 and earlier:
          // .fiftyPercentOpacity { filter: alpha(opacity=50); }
          "alpha",

          // CSS 3
          "cubic-bezier",
          "format", // used with @font-face
          "from",
          "hsl",
          "hsla",
          "local", // used with @font-face
          "perspective",
          "rgba",
          "rotate",
          "rotateX",
          "rotateY",
          "rotateZ",
          "scale",
          "scaleX",
          "scaleY",
          "scaleZ",
          "to",
          "translate",
          "translateX",
          "translateY",
          "translateZ",
          "translate3d",

          // Image-Set
          "image-set",
          "-moz-image-set",
          "-ms-image-set",
          "-o-image-set",
          "-webkit-image-set",

          // These take the type of gradient (linear or radial) as a parameter.
          "-khtml-gradient",
          "-webkit-gradient", // Prefer -webkit-(linear|radial)-gradient

          // Linear gradients
          "linear-gradient",
          "-moz-linear-gradient",
          "-ms-linear-gradient",
          "-o-linear-gradient",
          "-webkit-linear-gradient",

          // Radial gradients
          "radial-gradient",
          "-moz-radial-gradient",
          "-ms-radial-gradient",
          "-o-radial-gradient",
          "-webkit-radial-gradient",

          // Calc
          "calc",
          "-webkit-calc",
          "-moz-calc"
      );
      ImmutableMap.Builder<String, Function> builder = ImmutableMap.builder();
      for (String functionName : recognizedCssFunctions) {
        builder.put(
            functionName,
            new Function(functionName) {
              @Override public boolean isRecognized() {
                return true;
              }
            });
      }
      NAME_TO_FUNCTION_MAP = builder.build();
    }

    /**
     * Serves as a placeholder for custom functions.
     */
    public static final Function CUSTOM =
        new Function(null /* functionName */) {
          @Override public boolean isRecognized() {
            return false;
          }
        };


    /** The name of the function, as it appears in a CSS stylesheet. */
    private final String functionName;

    private Function(String functionName) {
      this.functionName = functionName;
    }

    /**
     * Returns the CSS {@link Function} with the specified name, or {@code null}
     * if the name is not in the list of recognized names. Multiple invocations
     * of this method with the same parameter will return the same object. For a
     * function that is not in the list of recognized names but should be
     * considered valid, use {@link Function#CUSTOM}.
     */
    public static Function byName(String name) {
      return NAME_TO_FUNCTION_MAP.get(name);
    }

    /**
     * Returns {@code true} when this function is in the list of
     * recognized names.
     */
    public abstract boolean isRecognized();

    /**
     * @return the name of the CSS function, such as "rgb" or "url"
     */
    public String getFunctionName() {
      return functionName;
    }

    /**
     * For debugging only.
     */
    @Override
    public String toString() {
      return getFunctionName();
    }
  }

  private final Function function;
  private CssFunctionArgumentsNode arguments;
  private Object chunk;

  /**
   * Constructor of the class.
   *
   * TODO(oana): Deal with the situation that we have an unrecognized
   * function.
   *
   * @param function
   * @param sourceCodeLocation
   */
  public CssFunctionNode(@Nullable Function function,
                         @Nullable SourceCodeLocation sourceCodeLocation) {
    super(null, sourceCodeLocation);
    this.function = function;
    this.arguments = new CssFunctionArgumentsNode();
    becomeParentForNode(this.arguments);
  }

  /**
   * Copy constructor.
   *
   * @param function
   */
  public CssFunctionNode(CssFunctionNode function) {
    super(function);
    this.function = function.getFunction();
    this.arguments = new CssFunctionArgumentsNode(function.getArguments());
    becomeParentForNode(this.arguments);
    this.chunk = function.getChunk();
  }

  /**
   * Constructor used by the proxy mechanism, avoids unnecessary arguments node
   * initialization.
   *
   * <p>NOTE(dgajda): The signature of this constructor only differs in argument
   * order from the main constructor of this class.
   *
   * @param function implementation of the function which is "called" by this
   *     node
   * @param sourceCodeLocation location of this node
   */
  protected CssFunctionNode(@Nullable SourceCodeLocation sourceCodeLocation,
                         @Nullable Function function) {
    super(null, sourceCodeLocation);
    this.function = function;
  }

  @Override
  public CssFunctionNode deepCopy() {
    return new CssFunctionNode(this);
  }

  public Function getFunction() {
    return function;
  }

  public String getFunctionName() {
    return function.toString();
  }

  public CssFunctionArgumentsNode getArguments() {
    return arguments;
  }

  public void setArguments(CssFunctionArgumentsNode arguments) {
    removeAsParentOfNode(this.arguments);
    this.arguments = arguments;
    becomeParentForNode(this.arguments);
  }

  @Override
  public String toString() {
    StringBuffer output = new StringBuffer();
    if (function.getFunctionName() != null) {
      output.append(function.getFunctionName());
    }
    output.append("(");
    for (CssNode node : getArguments().childIterable()) {
      output.append(node.toString());
    }
    output.append(")");
    return output.toString();
  }

  @Override
  public Object getChunk() {
    return chunk;
  }

  @Override
  public void setChunk(Object chunk) {
    this.chunk = chunk;
  }

  /**
   * Print the node instead of null when this node is a parameter.
   */
  @Override
  public String getValue() {
    return toString();
  }
}
