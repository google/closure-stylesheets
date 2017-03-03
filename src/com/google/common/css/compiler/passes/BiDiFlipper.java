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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssHexColorNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPriorityNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Compiler pass that BiDi flips all the flippable nodes.
 * TODO(user): Need to add a function to return tree before flipping.
 *
 * @author roozbeh@google.com (Roozbeh Pournader)
 */
public class BiDiFlipper extends DefaultTreeVisitor implements CssCompilerPass {

  private final DecimalFormat percentFormatter =
      new DecimalFormat("#.########", DecimalFormatSymbols.getInstance(Locale.US));
  private final MutatingVisitController visitController;

  boolean shouldSwapLeftRightInUrl;
  boolean shouldSwapLtrRtlInUrl;
  boolean shouldFlipConstantReferences;

  private static final Logger logger = Logger.getLogger(
      BiDiFlipper.class.getName());

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
   * Map with exact strings to match and their corresponding flipped value. For example, in "float:
   * left" we need an exact match to flip "left" because we don't want to touch things like
   * "background: left.png".
   */
  private static final ImmutableMap<String, String> EXACT_MATCHING_FOR_FLIPPING =
      new ImmutableMap.Builder<String, String>()
          .put("ltr", "rtl")
          .put("rtl", "ltr")
          .put("left", "right")
          .put("right", "left")
          .put("e-resize", "w-resize")
          .put("w-resize", "e-resize")
          .put("ne-resize", "nw-resize")
          .put("nw-resize", "ne-resize")
          .put("nesw-resize", "nwse-resize")
          .put("nwse-resize", "nesw-resize")
          .put("se-resize", "sw-resize")
          .put("sw-resize", "se-resize")
          .build();

  /**
   * Map with the "ends-with" substrings that can be flipped and their corresponding flipped value.
   * For example, for
   *
   * <p>padding-right: 2px
   *
   * <p>we need to match that the property name ends with "-right".
   */
  private static final ImmutableMap<String, String> ENDS_WITH_MATCHING_FOR_FLIPPING =
      new ImmutableMap.Builder<String, String>()
          .put("-left", "-right")
          .put("-right", "-left")
          .put("-bottomleft", "-bottomright")
          .put("-topleft", "-topright")
          .put("-bottomright", "-bottomleft")
          .put("-topright", "-topleft")
          .build();

  /**
   * Map with the "contains" substrings that can be flipped and their corresponding flipped value.
   * For example, for
   *
   * <p>border-right-width: 2px
   *
   * <p>we need to match that the property name contains "-right-".
   */
  private static final ImmutableMap<String, String> CONTAINS_MATCHING_FOR_FLIPPING =
      new ImmutableMap.Builder<String, String>()
          .put("-left-", "-right-")
          .put("-right-", "-left-")
          .build();

  /** Set of properties that have flippable percentage values. */
  private static final ImmutableSet<String> PROPERTIES_WITH_FLIPPABLE_PERCENTAGE =
      ImmutableSet.of(
          "background",
          "background-position",
          "background-position-x",
          "-ms-background-position-x");

  /*
   * Set of properties that are equivalent to border-radius.
   * TODO(roozbeh): Replace the explicit listing of prefixes with a general
   * pattern of "-[a-z]+-" to avoid maintaining a prefix list.
   */
  private static final ImmutableSet<String> BORDER_RADIUS_PROPERTIES =
      ImmutableSet.of(
          "border-radius",
          "-webkit-border-radius",
          "-moz-border-radius");

  /** Set of properties whose property values may flip if they match the four-part pattern. */
  private static final ImmutableSet<String> FOUR_PART_PROPERTIES_THAT_SHOULD_FLIP =
      ImmutableSet.of("border-color", "border-style", "border-width", "margin", "padding");

  /**
   * Map with the patterns to match URLs against if swap_ltr_rtl_in_url flag is true, and their
   * replacement string. Only the first occurrence of the pattern is flipped. This would match "ltr"
   * and "rtl" if they occur as a word inside the path specified by the url. For example, for
   *
   * <p>background: url(/foo/rtl/bkg.gif)
   *
   * <p>the flipped value would be
   *
   * <p>background: url(/foo/ltr/bkg.gif)
   *
   * <p>whereas for
   *
   * <p>background: url(/foo/bkg-ltr.gif)
   *
   * <p>the flipped value would be
   *
   * <p>background: url(/foo/bkg-rtl.gif)
   *
   * <p>
   */
  private static final ImmutableMap<Pattern, String> URL_LTRTL_PATTERN_FOR_FLIPPING =
      new ImmutableMap.Builder<Pattern, String>()
          .put(Pattern.compile("(?<![a-zA-Z])([-_\\./]*)ltr([-_\\./]+)"), "$1rtl$2")
          .put(Pattern.compile("(?<![a-zA-Z])([-_\\./]*)rtl([-_\\./]+)"), "$1ltr$2")
          .build();

  /**
   * Map with the patterns to match URLs against if swap_left_right_in_url flag is true, and their
   * replacement string. Only the first occurrence of the pattern is flipped. This would match
   * "left" and "right" if they occur as a word inside the path specified by the url. For example,
   * for
   *
   * <p>background: url(/foo/right/bkg.gif)
   *
   * <p>the flipped value would be
   *
   * <p>background: url(/foo/left/bkg.gif)
   *
   * <p>whereas for
   *
   * <p>background: url(/foo/bkg-left.gif)
   *
   * <p>the flipped value would be
   *
   * <p>background: url(/foo/bkg-right.gif)
   *
   * <p>
   */
  private static final ImmutableMap<Pattern, String> URL_LEFTRIGHT_PATTERN_FOR_FLIPPING =
      new ImmutableMap.Builder<Pattern, String>()
          .put(Pattern.compile("(?<![a-zA-Z])([-_\\./]*)left([-_\\./]+)"), "$1right$2")
          .put(Pattern.compile("(?<![a-zA-Z])([-_\\./]*)right([-_\\./]+)"), "$1left$2")
          .build();

  /**
   * Return if the string is "left" or "center" or "right".
   */
  private boolean isLeftOrCenterOrRight(String value) {
    return "left".equals(value)
        || "center".equals(value)
        || "right".equals(value);
  }

  /**
   * Return if the node is CssHexColorNode.
   */
  private boolean isCssHexColorNode(CssValueNode valueNode) {
    return (valueNode instanceof CssHexColorNode);
  }

  /**
   * Return if the node is CssLiteralNode.
   */
  private boolean isCssLiteralNode(CssValueNode valueNode) {
    return (valueNode instanceof CssLiteralNode);
  }

  /**
   * Return if the node is CssNumericNode.
   */
  private boolean isNumericNode(CssValueNode valueNode) {
    return (valueNode instanceof CssNumericNode);
  }

  /**
   * Return if the node is a slash operator node.
   */
  private boolean isSlashNode(CssValueNode valueNode) {
    if (valueNode instanceof CssCompositeValueNode) {
      CssCompositeValueNode compositeNode = (CssCompositeValueNode) valueNode;
      return
          compositeNode.getOperator() == CssCompositeValueNode.Operator.SLASH;
    }
    return false;
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
   *
   * Assumes simpler CSS 2.1 use of background and background-position
   * (multi-layer is not supported yet, neither is the extended CSS 3 syntax
   * for positioning, like "right 10% top 20%").
   * TODO(roozbeh): add support CSS 3 multi-layer backgrounds.
   * TODO(roozbeh): add support for extended CSS 3 syntax for positioning.
   */
  private boolean isValidForPercentageFlipping(
      CssPropertyNode propertyNode, CssPropertyValueNode propertyValueNode,
      int valueIndex) {

    String propertyName = propertyNode.getPropertyName();
    if (PROPERTIES_WITH_FLIPPABLE_PERCENTAGE.contains(propertyName)) {
      if (valueIndex == 0) {
        return true; // If this is the first value, it's always flippable
      }
      if ("background".equals(propertyName)) {
        // Make sure this is not the vertical position: Only flip if the
        // previous value is not numeric or "left", "center", or "right".
        CssValueNode previousValueNode =
            propertyValueNode.getChildAt(valueIndex - 1);
        if (!isNumericNode(previousValueNode)
            && !isLeftOrCenterOrRight(previousValueNode.getValue())) {
          return true;
        }
      }
    }

    return false;
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
    double newPercentValue = 100 - Double.parseDouble(oldPercentageValue);
    CssValueNode newNumericNode = new CssNumericNode(
        percentFormatter.format(newPercentValue), "%");

    return newNumericNode;
  }

  /**
   * Flips corners of a border-radius property. Corners are reordered in the
   * following way:
   * <ul>
   *   <li>0 1 is replaced with 1 0,
   *   <li> 0 1 2 is replaced with 1 0 1 2, and
   *   <li> 0 1 2 3 is replaced with 1 0 3 2.
   * </ul>
   *
   * <p>Lists of other lengths are returned unchanged.
   *
   * @param valueNodes the list of values representing the corners of a
   * border-radius property.
   * @return a list of values with the corners flipped.
   */
  private List<CssValueNode> flipCorners(List<CssValueNode> valueNodes) {
    switch (valueNodes.size()) {
      case 2:
        return Lists.newArrayList(
            valueNodes.get(1),
            valueNodes.get(0));
      case 3:
        return Lists.newArrayList(
            valueNodes.get(1),
            valueNodes.get(0),
            valueNodes.get(1).deepCopy(),
            valueNodes.get(2));
      case 4:
        return Lists.newArrayList(
            valueNodes.get(1),
            valueNodes.get(0),
            valueNodes.get(3),
            valueNodes.get(2));
      default:
        return valueNodes;
    }
  }

  /**
   * Takes a list of property values that belong to a border-radius property
   * and flips them. If there is a slash in the values, the data is divided
   * around the slash. Then for each section, flipCorners is called.
   */
  private List<CssValueNode> flipBorderRadius(List<CssValueNode> valueNodes) {

    int count = 0, slashLocation = -1;
    CssCompositeValueNode slashNode = null;
    for (CssValueNode valueNode : valueNodes) {
      if (isSlashNode(valueNode)) {
        slashLocation = count;
        slashNode = (CssCompositeValueNode) valueNode;
        break;
      }
      ++count;
    }

    if (slashLocation == -1) { // No slash found, just one set of values
      return flipCorners(valueNodes);
    }

    // The parser treats slashes as combinging the two values around the slash
    // into one composite value node. This is not really the correct semantics
    // for the border-radius properties, as the parser will treat
    // "border-radius: 1px 2px / 5px 6px" as having three value nodes: the first
    // one will be "1px", the second one the composite value "2px / 5px",
    // and the third one "6px". We work in this unfortunate parser model here,
    // first deconstructing and later reconstructing that tree.

    List<CssValueNode> slashNodeValues = slashNode.getValues();

    // Create a list of horizontal values and flip them
    List<CssValueNode> horizontalValues = Lists.newArrayList();
    horizontalValues.addAll(valueNodes.subList(0, slashLocation));
    horizontalValues.add(slashNodeValues.get(0));
    List<CssValueNode> newHorizontalValues = flipCorners(horizontalValues);

    // Do the same for vertical values
    List<CssValueNode> verticalValues = Lists.newArrayList();
    verticalValues.add(slashNodeValues.get(1));
    verticalValues.addAll(valueNodes.subList(slashLocation + 1,
        valueNodes.size()));
    List<CssValueNode> newVerticalValues = flipCorners(verticalValues);

    // Create a new slash node
    List<CssValueNode> newSlashNodeValues = Lists.newArrayList();
    newSlashNodeValues.add(newHorizontalValues.get(
        newHorizontalValues.size() - 1));
    newSlashNodeValues.add(newVerticalValues.get(0));
    CssCompositeValueNode newSlashNode = new CssCompositeValueNode(
      newSlashNodeValues,
      CssCompositeValueNode.Operator.SLASH,
      null
    );

    List<CssValueNode> newValueList = Lists.newArrayList();
    newValueList.addAll(newHorizontalValues.subList(0,
        newHorizontalValues.size() - 1));
    newValueList.add(newSlashNode);
    newValueList.addAll(newVerticalValues.subList(1, newVerticalValues.size()));

    return newValueList;
  }

  /**
   * Takes the list of property values, validate them, then swap the second
   * and last values. So that 0 1 2 3 becomes 0 3 2 1.
   *
   * That is unless the length of the list is not four, it belongs to a property
   * that shouldn't be flipped, or it's border-radius, where it will be
   * specially handled.
   *
   * TODO(roozbeh): Add explicit flipping for 'border-image*' and '*-shadow'
   * properties.
   */
  private List<CssValueNode> flipNumericValues(
      List<CssValueNode> valueNodes,
      String propertyName) {

    if (BORDER_RADIUS_PROPERTIES.contains(propertyName)) {
      return flipBorderRadius(valueNodes);
    } else if (valueNodes.size() != 4
        || !FOUR_PART_PROPERTIES_THAT_SHOULD_FLIP.contains(propertyName)) {
      return valueNodes;
    }

    int count = 0;
    CssValueNode secondValueNode = null;
    CssValueNode fourthValueNode = null;
    for (CssValueNode valueNode : valueNodes) {
      if (isNumericNode(valueNode)
          || isCssLiteralNode(valueNode)
          || isCssHexColorNode(valueNode)
          || shouldFlipConstantReference(valueNode)) {
        switch (count) {
          case 3:
            fourthValueNode = valueNode.deepCopy();
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
        newValueList.add(fourthValueNode);
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
    int valueIndex = 0;
    for (CssValueNode valueNode : propertyValueNode.childIterable()) {
      // Flip URL argument, if it is a valid url function.
      CssValueNode temp = flipUrlNode(valueNode);
      // Flip node value, if it is a property node or literal node with value
      // that required flipping.
      temp = flipNode(temp);
      // Flip node value, if it is numeric and has percentage that
      // needs flipping.
      if (isValidForPercentageFlipping(propertyNode, propertyValueNode,
                                       valueIndex)) {
        temp = flipPercentageValueNode(temp);
      }
      valueNodes.add(temp.deepCopy());
      valueIndex++;
    }
    if (valueNodes.size() != 0) {
      CssValueNode priority = null;
      // Remove possible !important priority node.
      if (!valueNodes.isEmpty() && Iterables.getLast(valueNodes) instanceof CssPriorityNode) {
        priority = Iterables.getLast(valueNodes);
        valueNodes = valueNodes.subList(0, valueNodes.size() - 1);
      }
      List<CssValueNode> newValueList =
          flipNumericValues(valueNodes, propertyNode.getPropertyName());
      // Re-add priority node if we removed it earlier.
      if (priority != null) {
        newValueList.add(priority);
      }
      newDeclarationNode.setPropertyValue(new CssPropertyValueNode(newValueList));
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
