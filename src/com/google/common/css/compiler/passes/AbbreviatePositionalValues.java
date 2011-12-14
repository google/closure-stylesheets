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

package com.google.common.css.compiler.passes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssHexColorNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.ast.Property;

import java.util.List;

/**
 * Check shorthand rule declarations that define positional values,
 * such as padding and margin, and eliminate duplicate values if possible.
 * For example, "margin: 1px 2px 3px 2px" will be shortened to just
 * "margin: 1px 2px 3px", since the final 2px is redundant.
 *
 * Note: At present, this pass applies to border-width, but not border.
 * @see Property#hasPositionalParameters()
 */
public class AbbreviatePositionalValues extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;

  public AbbreviatePositionalValues(
      MutatingVisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    CssPropertyNode property = declaration.getPropertyName();
    if (property.hasPositionDependentValues()) {
      CssPropertyValueNode valueNode = declaration.getPropertyValue();
      List<CssValueNode> newValues = abbreviateValues(valueNode.getChildren());
      if (newValues != null) {
        CssDeclarationNode newDeclaration = new CssDeclarationNode(declaration);
        CssPropertyValueNode newValuesNode = new CssPropertyValueNode(newValues);
        newDeclaration.setPropertyValue(newValuesNode);
        List<CssNode> replacementList = Lists.newArrayList();
        replacementList.add(newDeclaration);
        visitController.replaceCurrentBlockChildWith(replacementList, false);
      }
    }
    return true;
  }

  /**
   * Attempt to shorten a CSS positional list containing values for
   * -top, -right, -bottom, and -left properties of a shorthand rule.
   * Returns null if the list is not shortenable.
   * @param values List of values.
   * @return A shortened version of the input list, or null if not possible
   * to abbreviate.  The returned list is a "shallow" copy.
   */
  private List<CssValueNode> abbreviateValues(List<CssValueNode> values) {
    int numValues = values.size();
    if (numValues <= 1 || numValues > 4) {
      // Already abbreviated, or unexpected input.
      return null;
    }

    List<CssValueNode> mutableList = Lists.newArrayList(values);

    if (numValues == 4) {
      // Compare foo-left to foo-right.
      if (equalValues(values.get(3), values.get(1))) {
        numValues--;
        mutableList.remove(3);
      }
    }
    if (numValues == 3) {
      // Compare foo-bottom to foo-top.
      if (equalValues(values.get(2), values.get(0))) {
        numValues--;
        mutableList.remove(2);
      }
    }
    if (numValues == 2) {
      // Compare foo-{right,left} to foo-{top,bottom}.
      if (equalValues(values.get(1), values.get(0))) {
        numValues--;
        mutableList.remove(1);
      }
    }

    return numValues != values.size() ? mutableList : null;
  }

  /**
   * Compare 2 value nodes to see if they represent the same value for the
   * purposes of this compiler pass.  See {@link CssNode#equals} for an
   * explanation of why this is not defined elsewhere.
   * @param v1 First value node.
   * @param v2 Second value node.
   * @return Whether the values are equivalent.
   */
  @VisibleForTesting
  static boolean equalValues(CssValueNode v1, CssValueNode v2) {
    if (v1.equals(v2)) {
      return true;
    }
    if (v1 instanceof CssNumericNode && v2 instanceof CssNumericNode) {
      CssNumericNode numeric1 = (CssNumericNode) v1;
      CssNumericNode numeric2 = (CssNumericNode) v2;
      return numeric1.getNumericPart().equals(numeric2.getNumericPart()) &&
          numeric1.getUnit().equals(numeric2.getUnit());
    }
    if (v1 instanceof CssLiteralNode && v2 instanceof CssLiteralNode) {
      return v1.getValue().equals(v2.getValue());
    }
    if (v1 instanceof CssHexColorNode && v2 instanceof CssHexColorNode) {
      CssHexColorNode hex1 = (CssHexColorNode) v1;
      CssHexColorNode hex2 = (CssHexColorNode) v2;
      return hex1.toString().equals(hex2.toString());
    }
    return false;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
