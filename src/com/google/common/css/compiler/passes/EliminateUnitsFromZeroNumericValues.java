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

import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssCompositeValueNode.Operator;
import com.google.common.css.compiler.ast.CssMathNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.logging.Logger;

/**
 * A compiler pass that eliminates useless units from 0.
 *
 * <p>It is only allowed to eliminate units from relative or absolute length
 * units. See:
 * <a href="http://www.w3.org/TR/1998/WD-css2-19980128/syndata.html#length-units">
 * CSS 2 length units</a> and
 * <a href="http://www.w3.org/TR/css3-values/#lengths">CSS 3 length units</a>
 *
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
public class EliminateUnitsFromZeroNumericValues extends DefaultTreeVisitor
    implements CssCompilerPass {
  private static final ImmutableSet<String> REMOVABLE_LENGTH_UNITS =
      ImmutableSet.of("em", "ex", "px", "gd", "rem", "vw", "vh", "vm", "ch",
          "in", "cm", "mm", "pt", "pc");

  private static final Logger logger = Logger.getLogger(
      EliminateUnitsFromZeroNumericValues.class.getName());

  private final MutatingVisitController visitController;

  public EliminateUnitsFromZeroNumericValues(
      MutatingVisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    if (!(node instanceof CssNumericNode) // Don't process non-numeric nodes
        // Don't strip units from operands of + and - inside calc() expressions; they are invalid.
        // See https://www.w3.org/TR/css3-values/#calc-type-checking, and clarification by
        // spec author: https://bugs.chromium.org/p/chromium/issues/detail?id=641556#c5.
        || isPlusOrMinusOperand(node)) {
      return true;
    }
    CssNumericNode numericNode = (CssNumericNode) node;
    String numericValue = numericNode.getNumericPart();
    try {
      float value = Float.parseFloat(numericValue);
      if (value == 0.0) {
        if (REMOVABLE_LENGTH_UNITS.contains(numericNode.getUnit())) {
          numericNode.setUnit("");
        }
        numericNode.setNumericPart("0");
      } else {
        // Removes the 0s at the left of the dot.
        int stripFront = 0;
        while (numericValue.charAt(stripFront) == '0') {
          stripFront++;
        }

        int stripBack = numericValue.length() - 1;
        if (numericValue.contains(".")) {
          // Remove the 0s at the right of the dot.
          while (numericValue.charAt(stripBack) == '0') {
            stripBack--;
          }
          // Maybe remove the dot.
          if (numericValue.charAt(stripBack) == '.') {
            stripBack--;
          }
          numericValue = numericValue.substring(stripFront, stripBack + 1);
        }
        numericNode.setNumericPart(numericValue);
      }
    } catch (NumberFormatException e) {
      logger.warning(
          "Numeric part of the numeric value node could not be "
              + "parsed: "
              + node.toString()
              + ((node.getSourceCodeLocation() != null)
                  ? "@" + node.getSourceCodeLocation().getLineNumber()
                  : ""));
    }
    return true;
  }

  private static boolean isPlusOrMinusOperand(CssValueNode node) {
    CssNode parent = node.getParent();
    return parent instanceof CssMathNode
        && (((CssMathNode) parent).getOperator() == Operator.ADD
            || ((CssMathNode) parent).getOperator() == Operator.SUB);
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
