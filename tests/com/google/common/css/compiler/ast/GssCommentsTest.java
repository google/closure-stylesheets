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

package com.google.common.css.compiler.ast;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import com.google.common.css.compiler.passes.CreateConditionalNodes;
import com.google.common.css.compiler.passes.CreateDefinitionNodes;
import com.google.common.css.compiler.passes.MarkDefaultDefinitions;
import com.google.common.css.compiler.passes.MarkNonFlippableNodes;

/**
 * Tests the handling of GSS comments.
 *
 */
public class GssCommentsTest extends NewFunctionalTestBase {

  @Override
  protected void runPass() {
  }

  protected void createDefinintions() {
    CreateDefinitionNodes pass = new CreateDefinitionNodes(
        tree.getMutatingVisitController(), errorManager);
    pass.runPass();
  }

  protected void createConditionalNodes() {
    CreateConditionalNodes pass = new CreateConditionalNodes(
        tree.getMutatingVisitController(), errorManager);
    pass.runPass();
  }

  protected void relocateDefinintionsComments() {
    RelocateDefaultComments pass = new RelocateDefaultComments(
        tree.getMutatingVisitController());
    pass.runPass();
  }

  protected void markDefaultDefinitions() {
   MarkDefaultDefinitions pass = new MarkDefaultDefinitions(
        tree.getMutatingVisitController());
    pass.runPass();
  }

  protected void markNonFlippableNodes() {
   MarkNonFlippableNodes pass = new MarkNonFlippableNodes(
        tree.getVisitController(), errorManager);
    pass.runPass();
  }

  public void testCreateDefinitionComments() throws Exception {
    parseAndRun("/* comment0 */ @def /* comment-x */ X /* comment-y */Y " +
        "/* comment1 */  /* comment2 */   ;");
    assertThat(getFirstActualNode()).isInstanceOf(CssUnknownAtRuleNode.class);
    CssUnknownAtRuleNode def = (CssUnknownAtRuleNode) getFirstActualNode();
    assertThat(def.getName().getValue()).isEqualTo("def");
    CssCommentNode comment0 = def.getComments().get(0);
    assertThat(comment0.getValue()).isEqualTo("/* comment0 */");
    CssCommentNode comment1 = def.getComments().get(1);
    assertThat(comment1.getValue()).isEqualTo("/* comment1 */");
    CssCommentNode comment2 = def.getComments().get(2);
    assertThat(comment2.getValue()).isEqualTo("/* comment2 */");
    assertThat(def.getParameters().get(0).getComments().get(0).getValue())
        .isEqualTo("/* comment-x */");
    assertThat(def.getParameters().get(1).getComments().get(0).getValue())
        .isEqualTo("/* comment-y */");
  }

  public void testCreateFunctionComments() throws Exception {
    parseAndRun("@def /*comment0*/func(x, y/*comment1*/);");
    assertThat(getFirstActualNode()).isInstanceOf(CssUnknownAtRuleNode.class);
    CssUnknownAtRuleNode def = (CssUnknownAtRuleNode) getFirstActualNode();
    int defComments = def.getComments().size();
    assertThat(defComments).isEqualTo(0);
    CssValueNode valueNode = def.getParameters().get(0);
    assertThat(valueNode).isInstanceOf(CssFunctionNode.class);
    CssFunctionNode functionNode = (CssFunctionNode) valueNode;
    CssCommentNode comment0 = functionNode.getComments().get(0);
    assertThat(comment0.getValue()).isEqualTo("/*comment0*/");
    CssCommentNode comment1 = functionNode.getComments().get(1);
    assertThat(comment1.getValue()).isEqualTo("/*comment1*/");
  }

  public void testCreateDefinitionCommentsAfterRelocation() throws Exception {
    parseAndRun("@def A /* @default */#fff;");
    createDefinintions();
    relocateDefinintionsComments();
    assertThat(getFirstActualNode()).isInstanceOf(CssDefinitionNode.class);
    CssDefinitionNode def = (CssDefinitionNode) getFirstActualNode();
    CssCommentNode comment0 = def.getComments().get(0);
    assertThat(comment0.getValue()).isEqualTo("/* @default */");
  }

  // We don't test for comments between '!' and 'important'. See the comment on
  // the IMPORTANT_SYM in the grammar for the reason.
  public void testCreateRulesetComments() throws Exception {
    parseAndRun("A {/* comment-d */b:c /*comment0*/!important }");
    assertThat(getFirstActualNode()).isInstanceOf(CssRulesetNode.class);
    CssRulesetNode ruleset = (CssRulesetNode)getFirstActualNode();
    CssDeclarationNode declNode =
        (CssDeclarationNode) ruleset.getDeclarations().getChildren().get(0);
    CssCommentNode commentDecl = declNode.getComments().get(0);
    assertThat(commentDecl.getValue()).isEqualTo("/* comment-d */");
    CssPropertyValueNode propValue = declNode.getPropertyValue();
    CssValueNode prioNode = propValue.getChildren().get(1);
    CssCommentNode comment0 = prioNode.getComments().get(0);
    assertThat(comment0.getValue()).isEqualTo("/*comment0*/");
  }

  public void testNoflip() throws Exception {
    parseAndRun(".a .b { \n /* @noflip */\n float: left;\n}");
    assertThat(getFirstActualNode()).isInstanceOf(CssRulesetNode.class);
    CssRulesetNode ruleset = (CssRulesetNode)getFirstActualNode();
    CssDeclarationNode declNode =
        (CssDeclarationNode) ruleset.getDeclarations().getChildren().get(0);
    CssCommentNode commentDecl = declNode.getComments().get(0);
    assertThat(commentDecl.getValue()).isEqualTo("/* @noflip */");
  }

  public void testMarkNonFlippable() throws Exception {
    parseAndRun(linesToString("/* @noflip */ @if COND {",
        "  foo { top : expression('cond') }",
        "  bar { bottom : expression('cond') }",
        "} @else {",
        "  bar { top : expression('cond') }",
        "}"));
    createConditionalNodes();
    markNonFlippableNodes();

    CssConditionalBlockNode block =
        (CssConditionalBlockNode) getFirstActualNode();
    assertThat(block.getShouldBeFlipped()).isFalse();
    CssConditionalRuleNode condRule1 = block.getChildren().get(0);
    assertThat(condRule1.getShouldBeFlipped()).isFalse();
  }

  public void testMarkDefaultDefinitions1() throws Exception {
    parseAndRun("/* @default */ @def PADDING 2px 3px 5px 1px;");
    createDefinintions();
    relocateDefinintionsComments();
    markDefaultDefinitions();
    CssDefinitionNode definition = (CssDefinitionNode) getFirstActualNode();
    for (CssValueNode node : definition.getParameters()) {
      assertThat(node.getIsDefault()).isTrue();
    }
  }

  public void testMarkDefaultDefinitions2() throws Exception {
    parseAndRun("@def PADDING /* @default */ 2px 3px 5px 1px;");
    createDefinintions();
    relocateDefinintionsComments();
    markDefaultDefinitions();
    CssDefinitionNode definition = (CssDefinitionNode) getFirstActualNode();
    for (CssValueNode node : definition.getParameters()) {
      assertThat(node.getIsDefault()).isTrue();
    }
  }

  public void testSelectorList() throws Exception {
    parseAndRun("foo/*foo*/, /*bar1*/ /*bar2*/ bar /*bar3*/ , zoo /*zoo*/ { a:b }");
    CssRulesetNode ruleset = (CssRulesetNode) getFirstActualNode();
    CssSelectorListNode selectors = ruleset.getSelectors();
    CssSelectorNode fooSelector = selectors.getChildren().get(0);
    CssCommentNode fooComment = fooSelector.getComments().get(0);
    assertThat(fooComment.getValue()).isEqualTo("/*foo*/");
    CssSelectorNode barSelector = selectors.getChildren().get(1);
    CssCommentNode barComment1 = barSelector.getComments().get(0);
    assertThat(barComment1.getValue()).isEqualTo("/*bar1*/");
    CssCommentNode barComment2 = barSelector.getComments().get(1);
    assertThat(barComment2.getValue()).isEqualTo("/*bar2*/");
    CssCommentNode barComment3 = barSelector.getComments().get(2);
    assertThat(barComment3.getValue()).isEqualTo("/*bar3*/");
    CssSelectorNode zooSelector = selectors.getChildren().get(2);
    // The comment after the last selector goes to the rulesetNode.
    CssCommentNode zooComment = ruleset.getComments().get(0);
    assertThat(zooComment.getValue()).isEqualTo("/*zoo*/");
  }
}
