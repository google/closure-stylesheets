/*
 * Copyright 2011 Google Inc.
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
import com.google.common.base.CharMatcher;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssPseudoClassNode;
import com.google.common.css.compiler.ast.CssPseudoClassNode.FunctionType;
import com.google.common.css.compiler.ast.CssPseudoElementNode;
import com.google.common.css.compiler.ast.CssRefinerListNode;
import com.google.common.css.compiler.ast.CssRefinerNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

/**
 * Compiler pass which ensures that refiners are correctly formated because
 * not every invalid format is rejected by the parser. This pass checks for
 * a correct nth-format and can make it compact. In addition, the pass checks
 * the constraints for the :not pseudo-class.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class ProcessRefiners extends DefaultTreeVisitor
    implements CssCompilerPass {
  @VisibleForTesting
  static final String INVALID_NTH_ERROR_MESSAGE =
    "the format for NTH is not in the form an+b, 'odd', or 'even' where a " +
    "and b are (signed) integers that can be omitted";
  @VisibleForTesting
  static final String INVALID_NOT_SELECTOR_ERROR_MESSAGE =
    "a :not selector and pseudo-elements ('::') are not allowed inside of" +
    " a :not";
  @VisibleForTesting
  static final String NOT_LANG_ERROR_MESSAGE =
    "a pseudo-class which takes arguments has to be ':lang()' or has to " +
    "start with 'nth-'";
  private static final CharMatcher CSS_WHITESPACE =
      CharMatcher.anyOf(" \t\r\n\f");

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;
  private final boolean simplifyCss;

  public ProcessRefiners(MutatingVisitController visitController,
      ErrorManager errorManager, boolean simplifyCss) {
    this.visitController = visitController;
    this.errorManager = errorManager;
    this.simplifyCss = simplifyCss;
  }

  @Override
  public boolean enterPseudoClass(CssPseudoClassNode refiner) {
    FunctionType functionType = refiner.getFunctionType();
    switch (functionType) {
      case NONE:
        return true;
      case LANG:
        return handleLang(refiner);
      case NTH:
        return handleNth(refiner);
      case NOT:
        return handleNot(refiner);
    }
    return true;
  }

  private static String trim(String input) {
    return CSS_WHITESPACE.trimFrom(input);
  }

  private boolean handleLang(CssPseudoClassNode refiner) {
    if (!refiner.getRefinerName().equals("lang")) {
      errorManager.report(new GssError(NOT_LANG_ERROR_MESSAGE,
          refiner.getSourceCodeLocation()));
      return false;
    }
    return true;
  }

  private boolean handleNot(CssPseudoClassNode refiner) {
    if (refiner.getNotSelector() == null) {
      errorManager.report(new GssError(INVALID_NOT_SELECTOR_ERROR_MESSAGE,
          refiner.getSourceCodeLocation()));
      return false;
    }
    CssRefinerListNode refinerList = refiner.getNotSelector().getRefiners();
    if (refinerList.numChildren() == 0) {
      return true;
    } else if (refinerList.numChildren() > 1) {
      // should not be possible due to the grammar
      errorManager.report(new GssError(INVALID_NOT_SELECTOR_ERROR_MESSAGE,
          refiner.getSourceCodeLocation()));
      return false;
    }
    CssRefinerNode nestedRefiner = refinerList.getChildAt(0);
    // a pseudo-element is not allowed inside a :not
    if (nestedRefiner instanceof CssPseudoElementNode) {
      errorManager.report(new GssError(INVALID_NOT_SELECTOR_ERROR_MESSAGE,
          refiner.getSourceCodeLocation()));
      return false;
    }
    // the negation pseudo-class is not allowed inside a :not
    if (nestedRefiner instanceof CssPseudoClassNode) {
      CssPseudoClassNode pseudoClass = (CssPseudoClassNode) nestedRefiner;
      if (pseudoClass.getFunctionType() == FunctionType.NOT) {
        errorManager.report(new GssError(INVALID_NOT_SELECTOR_ERROR_MESSAGE,
            refiner.getSourceCodeLocation()));
        return false;
      }
    }
    return true;
  }

  private boolean handleNth(CssPseudoClassNode refiner) {
    String argument = trim(refiner.getArgument());
    if (argument.contains(".")) {
      errorManager.report(new GssError(INVALID_NTH_ERROR_MESSAGE,
          refiner.getSourceCodeLocation()));
      return false;
    }
    // general pattern: an+b
    int a, b;
    if (argument.equals("even")) {
      // 2n
      a = 2;
      b = 0;
    } else if (argument.equals("odd")) {
      // 2n+1
      a = 2;
      b = 1;
    } else {
      try {
        int indexOfN = argument.indexOf('n');
        a = parseA(argument, indexOfN);
        b = parseB(argument, indexOfN);
      } catch (NumberFormatException e) {
        errorManager.report(new GssError(INVALID_NTH_ERROR_MESSAGE,
            refiner.getSourceCodeLocation()));
        return false;
      }
    }
    if (simplifyCss) {
      refiner.setArgument(compactRepresentation(a, b));
    }
    return true;
  }

  private String compactRepresentation(int a, int b) {
    if (a == 2 && b == 1) {
      // 2n+1 -> odd
      return "odd";
    }
    if (a == 0 && b == 0) {
      return "0";
    }
    StringBuilder compact = new StringBuilder();
    if (a != 0) {
      if (a != 1 || b == 0 /* for WebKit */) {
        compact.append(Integer.toString(a));
      }
      compact.append("n");
    }
    if (b > 0 && a != 0) {
      compact.append("+");
    }
    if (b != 0) {
      compact.append(Integer.toString(b));
    }
    return compact.toString();
  }

  @VisibleForTesting
  int parseA(String argument, int indexOfN) {
    if (indexOfN == -1) {
      // b
      return 0;
    } else {
      if (indexOfN > 0) {
        String aStr = trim(argument.substring(0, indexOfN));
        if (aStr.equals("+")) {
          // +n+b
          return 1;
        } else if (aStr.equals("-")) {
          // -n+b
          return -1;
        } else {
          // an+b
          aStr = aStr.replace("+", "");
          return Integer.parseInt(aStr);
        }
      } else {
        // n+b
        return 1;
      }
    }
  }

  @VisibleForTesting
  int parseB(String argument, int indexOfN) {
    if (indexOfN == -1) {
      // b
      argument = trim(argument.replace("+", ""));
      return Integer.parseInt(argument);
    } else {
      if (indexOfN + 1 < argument.length()) {
        // an+b
        String bStr = argument.substring(indexOfN + 1, argument.length());
        bStr = trim(bStr);
        bStr = bStr.replace("+", "");
        return Integer.parseInt(bStr);
      } else {
        // an
        return 0;
      }
    }
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
