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
import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssCommentNode;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorListNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;

import java.util.List;

/**
 * Utility methods that can be useful for compiler passes.
 *
 */
public class PassUtil {

  /**
   * Annotation to explicitly state that several consecutive declarations are
   * alternatives and it should be left up to the user agent to interpret
   * whichever is appropriate. The annotation excludes the declarations from
   * being treated as redundant.
   */
  @VisibleForTesting
  public static final String ALTERNATE = "/* @alternate */";

  /**
   * Prints a selector including the combinators and refiners but without
   * a block behind with the compact printer.
   */
  public static String printSelector(CssSelectorNode selector) {
    CssTree t = createTreeWithSelector(selector);
    // Print the whole tree.
    String selectorString = compactPrintTree(t);
    // As the whole tree is printed, the output is the selector with an empty
    // declaration block (e.g. 'foo{}'). The two curly brackets are removed so
    // that only the selector remains.
    return selectorString.substring(0, selectorString.length() - 2);
  }

  /**
   * Prints a list of selector including the combinators and refiners but
   * without a block behind with the compact printer.
   */
  public static String printSelectorList(CssSelectorListNode selectorList) {
    CssTree t = createTreeWithSelectorList(selectorList);
    // Print the whole tree.
    String selectorListString = compactPrintTree(t);
    // As the whole tree is printed, the output is the selector with an empty
    // declaration block (e.g. 'foo{}'). The two curly brackets are removed so
    // that only the selector remains.
    return selectorListString.substring(0, selectorListString.length() - 2);
  }

  /**
   * Prints a selector including the combinators and refiners but without
   * a block behind with the pretty printer.
   */
  public static String prettyPrintSelector(CssSelectorNode selector) {
    CssTree t = createTreeWithSelector(selector);
    // Print the whole tree.
    PrettyPrinter prettyPrinter = new PrettyPrinter(t.getVisitController());
    prettyPrinter.runPass();
    String selectorString = prettyPrinter.getPrettyPrintedString();
    // As the whole tree is printed, the output is the selector with an empty
    // declaration block (e.g. 'foo {\n}\n'). The two curly brackets are removed so
    // that only the selector remains.
    int index = selectorString.indexOf('{');
    return selectorString.substring(0, index - 1);
  }

  private static CssTree createTreeWithSelector(CssSelectorNode selector) {
    // Create tree with only this selector.
    CssDeclarationBlockNode declarations = new CssDeclarationBlockNode();
    CssRulesetNode rulesetNode = new CssRulesetNode(declarations);
    rulesetNode.addSelector(selector);
    return createTreeWithRuleset(rulesetNode);
  }

  private static CssTree createTreeWithSelectorList(
      CssSelectorListNode selectorList) {
    CssDeclarationBlockNode declarations = new CssDeclarationBlockNode();
    CssRulesetNode rulesetNode = new CssRulesetNode(declarations);
    rulesetNode.setSelectors(selectorList);
    return createTreeWithRuleset(rulesetNode);
  }

  private static CssTree createTreeWithRuleset(CssRulesetNode rulesetNode) {
    List<CssNode> blockChildren = ImmutableList.of((CssNode) rulesetNode);
    CssBlockNode block = new CssBlockNode(false, blockChildren);
    CssRootNode root = new CssRootNode(block);
    return new CssTree(null, root);
  }

  private static String compactPrintTree(CssTree tree) {
    CompactPrinter compactPrinter = new CompactPrinter(tree);
    compactPrinter.runPass();
    return compactPrinter.getCompactPrintedString();
  }

  /**
   * Returns whether the ALTERNATE comment has been found among the comments of
   * the node.
   */
  public static boolean hasAlternateAnnotation(CssNode node) {
    for (CssCommentNode comment : node.getComments()) {
      if (comment.getValue().equals(ALTERNATE)) {
        return true;
      }
    }
    return false;
  }
}
