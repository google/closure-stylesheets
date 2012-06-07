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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssHexColorNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Compiler pass that BiDi flips all the flippable nodes.
 * TODO(user): Need to add a function to return tree before flipping.
 *
 */
public class BiDiFlipper extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;

  boolean shouldSwapLeftRightInUrl;
  boolean shouldSwapLtrRtlInUrl;
  boolean shouldFlipConstantReferences;

  public BiDiFlipper(MutatingVisitController visitController,
      boolean swapLtrRtlInUrl,
      boolean swapLeftRightInUrl,
      boolean shouldFlipConstantReferences) {
    this.visitController = visitController;
    this.shouldSwapLtrRtlInUrl = swapLtrRtlInUrl;
    this.shouldSwapLeftRightInUrl = swapLeftRightInUrl;
    this.shouldFlipConstantReferences = shouldFlipConstantReferences;
  }

  public BiDiFlipper(MutatingVisitController visitController,
      boolean swapLtrRtlInUrl,
      boolean swapLeftRightInUrl) {
    this(visitController,
        swapLtrRtlInUrl,
        swapLeftRightInUrl,
        false /* Don't flip constant reference by default. */);
  }

  /**
   * Map with exact strings to match and their corresponding flipped value.
   * For example, in "float: left" we need an exact match to flip "left" because
   * we don't want to touch things like "background: left.png".
   */
  private static final Map<String, String> EXACT_MATCHING_FOR_FLIPPING =
    new ImmutableMap.Builder<String, String>()
    .put("ltr", "rtl")
    .put("rtl", "ltr")
    .put("left", "right")
    .put("right", "left")
    .build();

  /**
   * Map with the "ends-with" substrings that can be flipped and their
   * corresponding flipped value.
   * For example, for
   * <p>
   *   padding-right: 2px
   * <p>
   * we need to match that the property name ends with "-right".
   */
  private static final Map<String, String> ENDS_WITH_MATCHING_FOR_FLIPPING =
    new ImmutableMap.Builder<String, String>()
    .put("-left", "-right")
    .put("-right", "-left")
    .put("-bottomleft", "-bottomright")
    .put("-topleft", "-topright")
    .put("-bottomright", "-bottomleft")
    .put("-topright", "-topleft")
    .build();

  /**
   * Map with the "contains" substrings that can be flipped and their
   * corresponding flipped value.
   * For example, for
   * <p>
   *   border-right-width: 2px
   * <p>
   * we need to match that the property name contains "-right-".
   */
  private static final Map<String, String> CONTAINS_MATCHING_FOR_FLIPPING =
    new ImmutableMap.Builder<String, String>()
    .put("-left-", "-right-")
    .put("-right-", "-left-")
    .build();

  /**
   * Map with the "starts-with" substrings that can be flipped and their
   * corresponding flipped value.
   * For example, for
   * <p>
   *   cursor: e-resize
   * <p>
   * we need to match that the value "e-resize" starts with "e-" as there are
   * other values that may contain it and they should not be flipped such as
   * "ie-something: boo".
   */
  private static final Map<String, String> STARTS_WITH_MATCHING_FOR_FLIPPING =
    new ImmutableMap.Builder<String, String>()
    .put("e-", "w-")
    .put("se-", "sw-")
    .put("ne-", "nw-")
    .put("w-", "e-")
    .put("sw-", "se-")
    .put("nw-", "ne-")
    .build();

  /**
   * Set of properties that have flippable percentage values.
   */
  private static final Set<String> PROPERTIES_WITH_FLIPPABLE_PERCENTAGE =
    ImmutableSet.of("background");

  /**
   * Map with the patterns to match URLs against if swap_ltr_rtl_in_url flag is
   * true, and their replacement string. Only the first occurrence of the
   * pattern is flipped. This would match "ltr" and "rtl" if they occur as a
   * word inside the path specified by the url.
   * For example, for
   * <p>
   *   background: url(/foo/rtl/bkg.gif)
   * <p>
   * the flipped value would be
   * <p>
   *   background: url(/foo/ltr/bkg.gif)
   * <p>
   * whereas for
   * <p>
   *   background: url(/foo/bkg-ltr.gif)
   * <p>
   * the flipped value would be
   * <p>
   *   background: url(/foo/bkg-rtl.gif)
   * <p>
   */
  private static final Map<Pattern, String> URL_LTRTL_PATTERN_FOR_FLIPPING =
    new ImmutableMap.Builder<Pattern, String>()
    .put(Pattern.compile("(?<![a-zA-Z])([-_\\./]*)ltr([-_\\./]+)"),
        "$1rtl$2")
    .put(Pattern.compile("(?<![a-zA-Z])([-_\\./]*)rtl([-_\\./]+)"),
        "$1ltr$2")
    .build();

  /**
   * Map with the patterns to match URLs against if swap_left_right_in_url flag
   * is true, and their replacement string. Only the first occurrence of the
   * pattern is flipped. This would match "left" and "right" if they occur as a
   * word inside the path specified by the url.
   * For example, for
   * <p>
   *   background: url(/foo/right/bkg.gif)
   * <p>
   * the flipped value would be
   * <p>
   *   background: url(/foo/left/bkg.gif)
   * <p>
   * whereas for
   * <p>
   *   background: url(/foo/bkg-left.gif)
   * <p>
   * the flipped value would be
   * <p>
   *   background: url(/foo/bkg-right.gif)
   * <p>
   */
  private static final Map<Pattern, String> URL_LEFTRIGHT_PATTERN_FOR_FLIPPING =
    new ImmutableMap.Builder<Pattern, String>()
    .put(Pattern.compile("(?<![a-zA-Z])([-_\\./]*)left([-_\\./]+)"),
        "$1right$2")
    .put(Pattern.compile("(?<![a-zA-Z])([-_\\./]*)right([-_\\./]+)"),
        "$1left$2")
    .build();


  /**
   * Return if the string is "auto" or "inherit" or "transparent".
   */
  private boolean isAutoOrInheritOrTransparent(String value) {
    return "auto".equals(value)
        || "inherit".equals(value)
        || "transparent".equals(value);
  }

  /**
   * Return if the node is CssHexColorNode.
   */
  private boolean isCssHexColorNode(CssValueNode valueNode) {
    return (valueNode instanceof CssHexColorNode);
  }

  /**
   * Return if the node is CssNumericNode.
   */
  private boolean isNumericNode(CssValueNode valueNode) {
    return (valueNode instanceof CssNumericNode);
  }

  /**
   * Return if the node is ConstantReference and also flippable.
   */
  private boolean shouldFlipConstantReference(CssValueNode valueNode) {
    if (!shouldFlipConstantReferences) {
      return false;
    }
    if (!(valueNode instanceof CssConstantReferenceNode)) {
      return false;
    }
    String ref = valueNode.getValue();
    if (ref.startsWith(ResolveCustomFunctionNodesForChunks.DEF_PREFIX)) {
      // Since gss function could generate multiple values, we can't do flip if
      // there's gss function call in place, simply skip this case.
      return false;
    }
    return true;
  }

  /**
   * Return if the node is numeric and also has '%'.
   */
  private boolean isNumericAndHasPercentage(CssValueNode value) {
    if (!isNumericNode(value)) {
      return false;
    }
    CssNumericNode numericNode = (CssNumericNode) value;
    return "%".equals(numericNode.getUnit());
  }

  /**
   * Returns if the percentage value of this node is flippable.
   */
  private boolean isNodeValidForPercentageFlipping(
      CssPropertyNode propertyNode) {
    boolean flippable = false;
    for (String flippableProperty : PROPERTIES_WITH_FLIPPABLE_PERCENTAGE) {
      if (propertyNode.getPropertyName().startsWith(flippableProperty)) {
        flippable = true;
        break;
      }
    }
    return flippable;
  }

  /**
   * Sets the percentage to flipped value(100 - 'old value'), if the node is
   * valid numeric node with percentage.
   */
  private CssValueNode flipPercentageValueNode(CssValueNode valueNode) {
    if (!isNumericAndHasPercentage(valueNode)) {
      return valueNode;
    }

    CssNumericNode numericNode = (CssNumericNode) valueNode;
    String oldPercentageValue = numericNode.getNumericPart();
    CssValueNode newNumericNode = new CssNumericNode(
        String.valueOf(100 - Integer.parseInt(oldPercentageValue)), "%");

    return newNumericNode;
  }

  /**
   * Takes the list of property values, validate them, then swap the second
   * and last values.
   */
  private List<CssValueNode> flipFourNumericValues(
      List<CssValueNode> valueNodes) {
    if (valueNodes.size() != 4) {
      return valueNodes;
    }

    int count = 0;
    CssValueNode secondValueNode = null;
    CssValueNode lastValueNode = null;
    for (CssValueNode valueNode : valueNodes) {
      if (isNumericNode(valueNode)
          || isAutoOrInheritOrTransparent(valueNode.toString())
          || isCssHexColorNode(valueNode)
          || shouldFlipConstantReference(valueNode)) {
        switch (count) {
          case 3:
            lastValueNode = valueNode.deepCopy();
            break;
          case 1:
            secondValueNode = valueNode.deepCopy();
        }
      } else {
        return valueNodes;
      }
      count++;
    }

    // Swap second and last in the new list.
    count = 0;
    List<CssValueNode> newValueList = Lists.newArrayList();
    for (CssValueNode valueNode : valueNodes) {
      if (1 == count) {
        newValueList.add(lastValueNode);
      } else if (3 == count) {
        newValueList.add(secondValueNode);
      } else {
        newValueList.add(valueNode);
      }
      count++;
    }
    return newValueList;
  }

  /**
   * Performs appropriate replacements needed for BiDi flipping.
   */
  private String flipValue(String value) {
    for (String s : EXACT_MATCHING_FOR_FLIPPING.keySet()) {
      if (value.equals(s)) {
        value = EXACT_MATCHING_FOR_FLIPPING.get(s);
        break;
      }
    }
    for (String s : ENDS_WITH_MATCHING_FOR_FLIPPING.keySet()) {
      if (value.endsWith(s)) {
        value = value.replace(s, ENDS_WITH_MATCHING_FOR_FLIPPING.get(s));
        break;
      }
    }
    for (String s : CONTAINS_MATCHING_FOR_FLIPPING.keySet()) {
      if (value.indexOf(s) > 0) {
        value = value.replace(s, CONTAINS_MATCHING_FOR_FLIPPING.get(s));
        break;
      }
    }
    for (String s : STARTS_WITH_MATCHING_FOR_FLIPPING.keySet()) {
      if (value.startsWith(s)) {
        value = value.replace(s, STARTS_WITH_MATCHING_FOR_FLIPPING.get(s));
        break;
      }
    }
    return value;
  }

  /**
   * Returns flipped node after making appropriate replacements needed for
   * BiDi flipping, if the node is either a LiteralNode or PropertyNode.
   * Eg: PropertyNode 'padding-right' would become 'padding-left'.
   */
  private <T extends CssValueNode> T flipNode(T tNode) {
    if (tNode instanceof CssLiteralNode) {
      CssLiteralNode literalNode = (CssLiteralNode) tNode;
      String oldValue = literalNode.getValue();
      if (null == oldValue) {
        return tNode;
      }
      String flippedValue = flipValue(oldValue);
      if (flippedValue.equals(oldValue)) {
        return tNode;
      }

      // This is safe because of the instanceof check above.
      @SuppressWarnings("unchecked")
      T flippedLiteralNode = (T) new CssLiteralNode(flippedValue);

      return flippedLiteralNode;
    } else if (tNode instanceof CssPropertyNode) {
      CssPropertyNode propertyNode = (CssPropertyNode) tNode;
      String oldValue = propertyNode.getPropertyName();
      if (null == oldValue) {
        return tNode;
      }
      String flippedValue = flipValue(oldValue);
      if (flippedValue.equals(oldValue)) {
        return tNode;
      }

      // This is safe because of the instanceof check above.
      @SuppressWarnings("unchecked")
      T flippedPropertyNode = (T) new CssPropertyNode(flippedValue);

      return flippedPropertyNode;
    } else {
      return tNode;
    }
   }

  /**
   * Performs appropriate replacements required for flipping url.
   */
  private String flipUrlValue(String value) {
    if (null == value) {
      return null;
    }
    if (shouldSwapLtrRtlInUrl) {
      for (Pattern p : URL_LTRTL_PATTERN_FOR_FLIPPING.keySet()) {
        if (p.matcher(value).find()) {
          String s = URL_LTRTL_PATTERN_FOR_FLIPPING.get(p);
          value =  p.matcher(value).replaceFirst(s);
          break;
        }
      }
    }
    if (shouldSwapLeftRightInUrl) {
      for (Pattern p : URL_LEFTRIGHT_PATTERN_FOR_FLIPPING.keySet()) {
        if (p.matcher(value).find()) {
          String s = URL_LEFTRIGHT_PATTERN_FOR_FLIPPING.get(p);
          value = p.matcher(value).replaceFirst(s);
          break;
        }
      }
    }
    return value;
  }

  /**
   * Return node with flipped url, if it is a 'CssFunctionNode' with
   * function 'URL'.
   */
  private CssValueNode flipUrlNode(CssValueNode valueNode) {
    if (!((valueNode instanceof CssFunctionNode)
          && ("url".equals(((CssFunctionNode) valueNode).getFunctionName())))) {
      return valueNode;
    }

    // Get the url to be flipped.
    CssFunctionNode oldFunctionNode = (CssFunctionNode) valueNode;
    CssFunctionArgumentsNode functionArguments = oldFunctionNode.getArguments();

    // Asserting if url function has more than one argument, which
    // is unusual.
    Preconditions.checkArgument((1 == functionArguments.numChildren()),
                                "url function taking more than one argument");

    CssValueNode oldArgument = functionArguments.getChildAt(0);
    String oldUrlValue = oldArgument.getValue();
    // Get the flipped url.
    String newUrlValue = flipUrlValue(oldUrlValue);

    // Make a new FunctionNode out of flipped url argument.
    CssValueNode newArgument = oldArgument.deepCopy();
    newArgument.setValue(newUrlValue);
    List<CssValueNode> newArgumentsList = new ArrayList<CssValueNode>();
    newArgumentsList.add(newArgument);

    CssFunctionNode newFunctionNode = oldFunctionNode.deepCopy();
    newFunctionNode.setArguments(new CssFunctionArgumentsNode(newArgumentsList));
    return newFunctionNode;
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declarationNode) {
    // Return if node is set to non-flippable.
    if (!declarationNode.getShouldBeFlipped()) {
      return true;
    }

    // Update the property name in the declaration.
    CssDeclarationNode newDeclarationNode = declarationNode.deepCopy();
    CssPropertyNode propertyNode = declarationNode.getPropertyName();
    newDeclarationNode.setPropertyName(flipNode(propertyNode));

    // Update the property value.
    CssPropertyValueNode propertyValueNode = declarationNode.getPropertyValue();
    List<CssValueNode> valueNodes = Lists.newArrayList();
    for (CssValueNode valueNode : propertyValueNode.childIterable()) {
      // Flip URL argument, if it is a valid url function.
      CssValueNode temp = flipUrlNode(valueNode);
      // Flip node value, if it is a property node or literal node with value
      // that required flipping.
      temp = flipNode(temp);
      // Flip node value, if it is numeric and has percentage that
      // needs flipping.
      if (isNodeValidForPercentageFlipping(propertyNode)) {
        temp = flipPercentageValueNode(temp);
      }
      valueNodes.add(temp.deepCopy());
    }
    if (valueNodes.size() != 0) {
      newDeclarationNode.setPropertyValue(new CssPropertyValueNode(
          flipFourNumericValues(valueNodes)));
    } else {
      newDeclarationNode.setPropertyValue(propertyValueNode.deepCopy());
    }

    List<CssNode> replacementList = Lists.newArrayList();
    replacementList.add(newDeclarationNode);
    visitController.replaceCurrentBlockChildWith(replacementList, false);
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
