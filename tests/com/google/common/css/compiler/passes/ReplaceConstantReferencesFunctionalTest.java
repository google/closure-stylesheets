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

import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 * Functional tests for {@link ReplaceConstantReferences}.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class ReplaceConstantReferencesFunctionalTest extends PassesTestBase {

  public void testReplaceConstants1() {
    testTreeConstruction(linesToString(
        "@def A red;",
        "@def B 3px;",
        "@def C 1;",
        "@def CORNER_BG gssFunction(A, B, C);",
        ".CSS_RULE {",
        "  background: CORNER_BG;",
        "}"),
        "[[.CSS_RULE]{[background:[gssFunction(red,3px,1)];]}]");
  }

  public void testReplaceConstants2() {
    testTreeConstruction(linesToString(
        "@def A red;",
        "@def B A;",
        "@def C green;",
        "@def D blue;",
        "@def COLOR B C D;",
        ".CSS_RULE {",
        "  border-color: COLOR;",
        "}"),
        "[[.CSS_RULE]{[border-color:[[red][green][blue]];]}]");
  }

  public void testReplaceConstants3() {
    testTreeConstruction(linesToString(
        "@def COLOR #ccc;",
        "@def BG_COLOR #fff;",
        "@def CONTAINER_COLOR BG_COLOR;",
        "@def BORDER_TOP_COLOR blendColors(COLOR, #000);",
        "@def INPUT_BG_COLOR  CONTAINER_COLOR;",
        "@def BORDER_COLOR BORDER_TOP_COLOR COLOR COLOR COLOR;",
        ".CSS_RULE {",
        "  border-color: BORDER_COLOR;",
        "}"),
        "[[.CSS_RULE]{[border-color:"
        + "[blendColors(#ccc,#000) [#ccc][#ccc][#ccc]];]}]");
  }

  public void testReplaceConstants4() {
    testTreeConstruction(linesToString(
        "@def IE6 0;",
        "@def HEIGHT 1px;",
        "@def TOP_RIGHT tr;",
        "@def EXT_COLOR red;",
        "@def COLOR blendColors(EXT_COLOR, 0, -8, -8);",
        "@def BORDER_TOP_COLOR function(COLOR, HEIGHT, TOP_RIGHT, IE6);",
        "@def BORDER_COLOR BORDER_TOP_COLOR;",
        ".CSS_RULE {",
        "  border-color: BORDER_COLOR;",
        "}"),
        "[[.CSS_RULE]{[border-color:"
        + "[function(blendColors(red,0,-8,-8),1px,tr,0)];]}]");
  }

  public void testReplaceConstants5() {
    testTreeConstruction(linesToString(
        "@def EXT_COLOR red;",
        "@def COLOR blendColors(EXT_COLOR);",
        "@def FUN_COLOR function(COLOR);",
        "@def BORDER_COLOR FUN_COLOR;",
        ".CSS_RULE {",
        "  border-color: BORDER_COLOR;",
        "}"),
        "[[.CSS_RULE]{[border-color:"
        + "[function(blendColors(red))];]}]");
  }

  public void testWebkitGradient() {
    testTreeConstruction(linesToString(
        "@def A #fff;",
        "@def B #ddd;",
        "@def C 100%;",
        "@def D -webkit-gradient(linear, 0 0, 0 C, from(A), to(B));",
        ".CSS_RULE {",
        "  background: D;",
        "}"),
        "[[.CSS_RULE]{[background:"
        + "[-webkit-gradient(linear,0 0,0 100%,from(#fff),to(#ddd))];]}]");
  }

  public void testCompositeValueNodeReplacement() {
    testTreeConstruction(linesToString(
      "@def DARK_DIVIDER_LEFT -1px 0 1px rgba(5,4,4,.3);",
      "@def DARK_DIVIDER_RIGHT 1px 0 1px rgba(73,71,71,.3);",
      ".A {",
      "  box-shadow: inset DARK_DIVIDER_RIGHT, inset DARK_DIVIDER_LEFT;",
      "}"),
      "[[.A]{[box-shadow:"
      + "[[inset][[[1px] [0] [1px] rgba(73,71,71,.3)],"
      + "[inset]][-1px][0][1px]rgba(5,4,4,.3)"
      + "];]}]");
  }

  public void testVariableInFunctionInComposite() throws Exception {
    testTreeConstruction(linesToString(
        "@def BG_COLOR beige;",
        "",
        "a {",
        "  background:-webkit-linear-gradient(top, BG_COLOR 30%,"
        + " rgba(255,255,255,0)),",
        "    -webkit-linear-gradient(top, rgba(255,255,255,0), BG_COLOR 70%);",
        "}"),

        "[[a]{[background:[[-webkit-linear-gradient(top,beige 30%,"
        + "rgba(255,255,255,0)),-webkit-linear-gradient(top,"
        + "rgba(255,255,255,0),beige 70%)]];]}]");
  }

  public void testCompositeValueNodeWithFunctions() {
    testTreeConstruction(linesToString(
        "@def DEF_A top, red 30%, rgba(0, 0, 0, 0);",
        "@def DEF_B top, rgba(0, 0, 0, 0), red 30%;",
        ".A {",
        "  background: linear-gradient(DEF_A), linear-gradient(DEF_B);",
        "}"),
        "[[.A]{[background:[["
        + "linear-gradient([[top],[red]] [[30%],rgba(0,0,0,0)]),"
        + "linear-gradient([[top],rgba(0,0,0,0),[red]] 30%)]];]}]");
  }

  public void testFontReplacement() throws Exception {
    testTreeConstruction(linesToString(
      "@def BASE_TINY_FONT_FACE verdana, arial, \"Courrier New\", sans-serif;",
      "@def BASE_TINY_FONT_SIZE 19px;",
      "@def BASE_TINY_FONT      BASE_TINY_FONT_SIZE BASE_TINY_FONT_FACE;",
      ".A {",
      "  font: BASE_TINY_FONT;",
      "}"),
      "[[.A]{[font:[[19px][[verdana],[arial],[\"Courrier New\"],[sans-serif]]];]}]");
  }

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
    new CreateConditionalNodes(tree.getMutatingVisitController(), errorManager).runPass();
    CollectConstantDefinitions defPass = new CollectConstantDefinitions(tree);
    defPass.runPass();
    new ReplaceConstantReferences(tree, defPass.getConstantDefinitions(),
        true /* removeDefs */, errorManager,
        true /* allowUndefinedConstants */).runPass();
  }
}
