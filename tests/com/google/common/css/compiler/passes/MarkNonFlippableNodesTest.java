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

import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 * Unit tests for {@link MarkNonFlippableNodes}.
 *
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
public class MarkNonFlippableNodesTest extends PassesTestBase {

  public void testMisplacedAnnotation() throws GssParserException {
    parseAndRun(
        linesToString(".CLASSX,", "/* @noflip */ .CLASSY { left:10px; }"),
        MarkNonFlippableNodes.INVALID_NOFLIP_ERROR_MESSAGE);
  }

  public void testMarkNonFlippableSingelDeclaration() {
    parseAndBuildTree(linesToString(
        ".CLASSX {",
        "  /* @noflip */ left: 10px;",
        "  margin: 10px 9px 8px 7px;",
        "}"));
    runPass();

    CssRulesetNode ruleset =
        (CssRulesetNode) tree.getRoot().getBody().getChildren().get(0);
    // The ruleset itself has to be marked as flippable.
    assertTrue(ruleset.getShouldBeFlipped());

    CssDeclarationBlockNode block = ruleset.getDeclarations();
    assertEquals(2, block.getChildren().size());
    // The first declaration has to be marked as not flippable.
    assertFalse(block.getChildren().get(0).getShouldBeFlipped());
    // The second declaration has to be marked as flippable.
    assertTrue(block.getChildren().get(1).getShouldBeFlipped());
  }

  public void testMarkNonFlippableRuleset() {
    parseAndBuildTree(linesToString(
        "/* @noflip */ .CLASSX {",
        "  left: 10px;",
        "  margin: 10px 9px 8px 7px;",
        "}"));
    runPass();

    CssRulesetNode ruleset =
        (CssRulesetNode) tree.getRoot().getBody().getChildren().get(0);
    // The ruleset itself has to be marked as not flippable.
    assertFalse(ruleset.getShouldBeFlipped());

    // All declaration in the ruleset have to be marked as flippable.
    CssDeclarationBlockNode block = ruleset.getDeclarations();
    assertEquals(2, block.getChildren().size());
    assertFalse(block.getChildren().get(0).getShouldBeFlipped());
    assertFalse(block.getChildren().get(1).getShouldBeFlipped());
  }

  public void testMarkNonFlippableConditional() {
    parseAndBuildTree(linesToString(
        "/* @noflip */ @if COND {",
        "  foo { top : expression('cond') }",
        "  bar { bottom : expression('cond') }",
        "} @else {",
        "  bar { top : expression('cond') }",
        "}"));
    new CreateConditionalNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    runPass();

    CssConditionalBlockNode block =
        (CssConditionalBlockNode) tree.getRoot().getBody().getChildren().get(0);
    assertFalse(block.getShouldBeFlipped());

    // Check each of the conditional rules.
    for (CssConditionalRuleNode rule : block.getChildren()) {
      assertFalse(rule.getShouldBeFlipped());
      // Check each of the rules inside the conditional rule.
      for (CssNode node : rule.getBlock().getChildren()) {
        assertTrue(node instanceof CssRulesetNode);
        assertFalse(node.getShouldBeFlipped());
        // Check each of the declarations inside the style rule.
        for (CssNode decl
            : ((CssRulesetNode) node).getDeclarations().getChildren()) {
          assertFalse(decl.getShouldBeFlipped());
        }
      }
    }
  }

  public void testMarkNonFlippableInConditional() {
    parseAndBuildTree(linesToString(
        "@if COND {",
        "  /* @noflip */foo { top : expression('cond') }",
        "  bar { bottom : expression('cond') }",
        "} @else {",
        "  bar { top : expression('cond') }",
        "}"));
    new CreateConditionalNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    runPass();

    CssConditionalBlockNode block =
        (CssConditionalBlockNode) tree.getRoot().getBody().getChildren().get(0);
    assertTrue(block.getShouldBeFlipped());

    // Check each of the conditional rules.

    // First conditional rule.
    CssConditionalRuleNode rule = block.getChildren().get(0);
    assertTrue(rule.getShouldBeFlipped());
    // Check each of the rules inside the conditional rule.
    int i = 0;
    for (CssNode node : rule.getBlock().getChildren()) {
      assertTrue(node instanceof CssRulesetNode);
      if (i == 0) {
        // Only the first rule is non flippable.
        assertFalse(node.getShouldBeFlipped());
        i++;
        // Check each of the declarations inside the style rule.
        for (CssNode decl
            : ((CssRulesetNode) node).getDeclarations().getChildren()) {
          assertFalse(decl.getShouldBeFlipped());
        }
      } else {
        assertTrue(node.getShouldBeFlipped());
        // Check each of the declarations inside the style rule.
        for (CssNode decl
            : ((CssRulesetNode) node).getDeclarations().getChildren()) {
          assertTrue(decl.getShouldBeFlipped());
        }
      }
    }

    // Second conditional rule.
    rule = block.getChildren().get(1);
    assertTrue(rule.getShouldBeFlipped());
    // Check each of the rules inside the conditional rule.
    for (CssNode node : rule.getBlock().getChildren()) {
      assertTrue(node instanceof CssRulesetNode);
      assertTrue(node.getShouldBeFlipped());
      // Check each of the declarations inside the style rule.
      for (CssNode decl
          : ((CssRulesetNode) node).getDeclarations().getChildren()) {
        assertTrue(decl.getShouldBeFlipped());
      }
    }
  }

  public void testMarkNonFlippableMedia() {
    parseAndBuildTree(linesToString(
        "@media print /* @noflip */{",
        "  .CSS_RULE_1, .CSS_RULE_2:hover a {",
        "     border: thickBorder(red, 2px);",
        "  }",
        "}",
        "@media tv {",
        "  .CSS_RULE_1, .CSS_RULE_2:hover a /* @noflip */{",
        "     border: thickBorder(green, 2px);",
        "  }",
        "}",
        ".CSS_RULE_3 { /* @noflip */top : expression('cond') }"
        ));
    new CreateStandardAtRuleNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    runPass();

    CssNode rule = tree.getRoot().getBody().getChildren().get(0);
    assertTrue(rule instanceof CssMediaRuleNode);
    assertFalse(rule.getShouldBeFlipped());

    // Check the rules inside the media rule.
    for (CssNode ruleset : ((CssMediaRuleNode) rule).getBlock().getChildren()) {
      assertTrue(ruleset instanceof CssRulesetNode);
      assertFalse(ruleset.getShouldBeFlipped());
      // Check the declarations inside the ruleset.
      for (CssNode decl
          : ((CssRulesetNode) ruleset).getDeclarations().getChildren()) {
        assertFalse(decl.getShouldBeFlipped());
      }
    }

    // Check the second media rule.
    rule = tree.getRoot().getBody().getChildren().get(1);
    assertTrue(rule instanceof CssMediaRuleNode);
    assertTrue(rule.getShouldBeFlipped());

    for (CssNode ruleset : ((CssMediaRuleNode) rule).getBlock().getChildren()) {
      assertTrue(ruleset instanceof CssRulesetNode);
      assertFalse(ruleset.getShouldBeFlipped());
      // Check the declarations inside the ruleset.
      for (CssNode decl
          : ((CssRulesetNode) ruleset).getDeclarations().getChildren()) {
        assertFalse(decl.getShouldBeFlipped());
      }
    }

    // Check the ruleset following the media rule.
    rule = tree.getRoot().getBody().getChildren().get(2);
    assertTrue(rule instanceof CssRulesetNode);
    assertTrue(rule.getShouldBeFlipped());
    // Check the declarations inside the ruleset.
    for (CssNode decl
        : ((CssRulesetNode) rule).getDeclarations().getChildren()) {
      assertFalse(decl.getShouldBeFlipped());
    }
  }

  @Override
  protected void runPass() {
    MarkNonFlippableNodes pass = new MarkNonFlippableNodes(
        tree.getMutatingVisitController(), errorManager);
    pass.runPass();
  }
}
