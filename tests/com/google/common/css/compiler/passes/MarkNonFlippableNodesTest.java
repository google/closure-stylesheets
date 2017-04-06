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

import static com.google.common.truth.Truth.assertThat;

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
    parseAndRun(linesToString(
        ".CLASSX,",
        "/* @noflip */ .CLASSY {",
        "  left:10px;",
        "}"),
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
    assertThat(ruleset.getShouldBeFlipped()).isTrue();

    CssDeclarationBlockNode block = ruleset.getDeclarations();
    assertThat(block.getChildren()).hasSize(2);
    // The first declaration has to be marked as not flippable.
    assertThat(block.getChildren().get(0).getShouldBeFlipped()).isFalse();
    // The second declaration has to be marked as flippable.
    assertThat(block.getChildren().get(1).getShouldBeFlipped()).isTrue();
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
    assertThat(ruleset.getShouldBeFlipped()).isFalse();

    // All declaration in the ruleset have to be marked as flippable.
    CssDeclarationBlockNode block = ruleset.getDeclarations();
    assertThat(block.getChildren()).hasSize(2);
    assertThat(block.getChildren().get(0).getShouldBeFlipped()).isFalse();
    assertThat(block.getChildren().get(1).getShouldBeFlipped()).isFalse();
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
    assertThat(block.getShouldBeFlipped()).isFalse();

    // Check each of the conditional rules.
    for (CssConditionalRuleNode rule : block.getChildren()) {
      assertThat(rule.getShouldBeFlipped()).isFalse();
      // Check each of the rules inside the conditional rule.
      for (CssNode node : rule.getBlock().getChildren()) {
        assertThat(node).isInstanceOf(CssRulesetNode.class);
        assertThat(node.getShouldBeFlipped()).isFalse();
        // Check each of the declarations inside the style rule.
        for (CssNode decl
            : ((CssRulesetNode) node).getDeclarations().getChildren()) {
          assertThat(decl.getShouldBeFlipped()).isFalse();
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
    assertThat(block.getShouldBeFlipped()).isTrue();

    // Check each of the conditional rules.

    // First conditional rule.
    CssConditionalRuleNode rule = block.getChildren().get(0);
    assertThat(rule.getShouldBeFlipped()).isTrue();
    // Check each of the rules inside the conditional rule.
    int i = 0;
    for (CssNode node : rule.getBlock().getChildren()) {
      assertThat(node).isInstanceOf(CssRulesetNode.class);
      if (i == 0) {
        // Only the first rule is non flippable.
        assertThat(node.getShouldBeFlipped()).isFalse();
        i++;
        // Check each of the declarations inside the style rule.
        for (CssNode decl
            : ((CssRulesetNode) node).getDeclarations().getChildren()) {
          assertThat(decl.getShouldBeFlipped()).isFalse();
        }
      } else {
        assertThat(node.getShouldBeFlipped()).isTrue();
        // Check each of the declarations inside the style rule.
        for (CssNode decl
            : ((CssRulesetNode) node).getDeclarations().getChildren()) {
          assertThat(decl.getShouldBeFlipped()).isTrue();
        }
      }
    }

    // Second conditional rule.
    rule = block.getChildren().get(1);
    assertThat(rule.getShouldBeFlipped()).isTrue();
    // Check each of the rules inside the conditional rule.
    for (CssNode node : rule.getBlock().getChildren()) {
      assertThat(node).isInstanceOf(CssRulesetNode.class);
      assertThat(node.getShouldBeFlipped()).isTrue();
      // Check each of the declarations inside the style rule.
      for (CssNode decl
          : ((CssRulesetNode) node).getDeclarations().getChildren()) {
        assertThat(decl.getShouldBeFlipped()).isTrue();
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
    assertThat(rule).isInstanceOf(CssMediaRuleNode.class);
    assertThat(rule.getShouldBeFlipped()).isFalse();

    // Check the rules inside the media rule.
    for (CssNode ruleset : ((CssMediaRuleNode) rule).getBlock().getChildren()) {
      assertThat(ruleset).isInstanceOf(CssRulesetNode.class);
      assertThat(ruleset.getShouldBeFlipped()).isFalse();
      // Check the declarations inside the ruleset.
      for (CssNode decl
          : ((CssRulesetNode) ruleset).getDeclarations().getChildren()) {
        assertThat(decl.getShouldBeFlipped()).isFalse();
      }
    }

    // Check the second media rule.
    rule = tree.getRoot().getBody().getChildren().get(1);
    assertThat(rule).isInstanceOf(CssMediaRuleNode.class);
    assertThat(rule.getShouldBeFlipped()).isTrue();

    for (CssNode ruleset : ((CssMediaRuleNode) rule).getBlock().getChildren()) {
      assertThat(ruleset).isInstanceOf(CssRulesetNode.class);
      assertThat(ruleset.getShouldBeFlipped()).isFalse();
      // Check the declarations inside the ruleset.
      for (CssNode decl
          : ((CssRulesetNode) ruleset).getDeclarations().getChildren()) {
        assertThat(decl.getShouldBeFlipped()).isFalse();
      }
    }

    // Check the ruleset following the media rule.
    rule = tree.getRoot().getBody().getChildren().get(2);
    assertThat(rule).isInstanceOf(CssRulesetNode.class);
    assertThat(rule.getShouldBeFlipped()).isTrue();
    // Check the declarations inside the ruleset.
    for (CssNode decl
        : ((CssRulesetNode) rule).getDeclarations().getChildren()) {
      assertThat(decl.getShouldBeFlipped()).isFalse();
    }
  }

  @Override
  protected void runPass() {
    MarkNonFlippableNodes pass = new MarkNonFlippableNodes(
        tree.getVisitController(), errorManager);
    pass.runPass();
  }
}
