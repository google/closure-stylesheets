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

import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 * Unit tests for the {@link ReplaceMixins} compiler pass.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class ReplaceMixinsTest extends PassesTestBase {

  public void testMixinReplacement1() {
    testTreeConstruction(linesToString(
        "@defmixin test(COLOR) {",
        "  color: COLOR;",
        "}",
        "p {",
        "  @mixin test(#FFF);",
        "}"),

        "[[p]{[color:[#fff];]}]");
  }

  public void testMixinReplacement2() {
    testTreeConstruction(linesToString(
        "@defmixin color(COLOR) {",
        "  color: COLOR;",
        "}",
        "@defmixin size(H, W) {",
        "  width: W;",
        "  height: H;",
        "}",
        "p {",
        "  @mixin size(50%, 60%);",
        "  position: absolute;",
        "  @mixin color(red);",
        "}"),

        "[[p]{[width:[60%];height:[50%];position:[absolute];color:[red];]}]");
  }

  public void testMixinReplacement3() {
    testTreeConstruction(linesToString(
        "@defmixin size(H, W) {",
        "  width: W;",
        "  height: H;",
        "}",
        "p {",
        "  @mixin size(50%, 60%);",
        "  position: absolute;",
        "}",
        "div {",
        "  @mixin size(100px, 200px);",
        "}"),

        "[[p]{[width:[60%];height:[50%];position:[absolute];]}"
        + "[div]{[width:[200px];height:[100px];]}]");
  }

  public void testMixinReplacement4() {
    testTreeConstruction(linesToString(
        "@defmixin size(W, H) {",
        "  width: W;",
        "  height: H;",
        "}",
        "@defmixin color(COLOR) {",
        "  color: COLOR;",
        "}",
        "@defmixin position(POS) {",
        "  position: POS;",
        "}",
        "p {",
        "  @mixin color(#fff);",
        "  @mixin size(50%, 60%);",
        "  @mixin position(absolute);",
        "}"),

        "[[p]{[color:[#fff];width:[50%];height:[60%];position:[absolute];]}]");
  }

  public void testMixinReplacement5() {
    testTreeConstruction(linesToString(
        "@defmixin empty(X, Y) {",
        "}",
        "p {",
        "  @mixin empty(10, 20);",
        "}"),

        "[[p]{[]}]");
  }

  public void testMixinReplacementTwice() {
    testTreeConstruction(linesToString(
        "@defmixin test(COLOR) {",
        "  color: COLOR;",
        "}",
        "p {",
        "  @mixin test(#FFF);",
        "  @mixin test(#FFF);",
        "}"),

        "[[p]{[color:[#fff];color:[#fff];]}]");
  }

  public void testMixinReplacementNested1() {
    testTreeConstruction(linesToString(
        "@defmixin width(W) {",
        "  width: W;",
        "}",
        "@defmixin size(W, H) {",
        "  @mixin width(W);",
        "  height: H;",
        "}",
        "p {",
        "  @mixin size(50%, 60%);",
        "  position: absolute;",
        "}"),

        "[[p]{[width:[50%];height:[60%];position:[absolute];]}]");
  }

  public void testMixinReplacementNested2() {
    testTreeConstruction(linesToString(
        "@defmixin width(W2) {",
        "  width: W2;",
        "}",
        "@defmixin size(W, H) {",
        "  @mixin width(W);",
        "  height: H;",
        "}",
        "p {",
        "  @mixin size(50%, 60%);",
        "  @mixin width(10px);",
        "  position: absolute;",
        "}"),

        "[[p]{[width:[50%];height:[60%];width:[10px];position:[absolute];]}]");
  }

  public void testMixinReplacementNested3() {
    testTreeConstruction(linesToString(
        "@defmixin width(W2) {",
        "  width: W2;",
        "}",
        "@defmixin size(W, H) {",
        "  @mixin width(W);",
        "  height: H;",
        "  @mixin width(H);",
        "}",
        "p {",
        "  @mixin size(50%, 60%);",
        "  @mixin width(10px);",
        "  position: absolute;",
        "}"),

        "[[p]{[width:[50%];height:[60%];width:[60%];width:[10px];"
        + "position:[absolute];]}]");
  }

  public void testMixinReplacementNested4() {
    testTreeConstruction(linesToString(
        "@defmixin empty() {",
        "}",
        "@defmixin callEmpty() {",
        "  @mixin empty();",
        "}",
        "p {",
        "  @mixin empty();",
        "  @mixin callEmpty();",
        "  @mixin callEmpty();",
        "  @mixin empty();",
        "}"),

        "[[p]{[]}]");
  }

  public void testMixinReplacementNested5() {
    testTreeConstruction(linesToString(
        "@defmixin empty() {",
        "}",
        "@defmixin callEmpty1() {",
        "  @mixin empty();",
        "}",
        "@defmixin callEmpty2() {",
        "  @mixin callEmpty1();",
        "}",
        "@defmixin callEmpty3() {",
        "  @mixin callEmpty2();",
        "}",
        "p {",
        "  @mixin empty();",
        "  @mixin callEmpty3();",
        "  @mixin empty();",
        "}"),

        "[[p]{[]}]");
  }

  public void testCompositeValues1() {
    testTreeConstruction(linesToString(
        "@defmixin font(RATIO) {",
        "  font: RATIO Arial;",
        "}",
        "p {",
        "  @mixin font(1em/1.3em);",
        "}"),

        "[[p]{[font:[1em/1.3em Arial];]}]");
  }

  public void testCompositeValues2() {
    testTreeConstruction(linesToString(
        "@defmixin margin(MARGIN) {",
        "  margin: MARGIN;",
        "}",
        "p {",
        "  @mixin margin(1px 2px 3px 4px);",
        "}"),

        "[[p]{[margin:[1px 2px 3px 4px];]}]");
  }

  public void testMixinReplacementComponents() {
    testTreeConstruction(linesToString(
        "@defmixin col(PAR) {",
        "  color: PAR;",
        "  background-color: CHANGES;",
        "}",
        "@abstract_component ABY {",
        "  @def STAYS red;",
        "  @def CHANGES none;",
        "  .ABSTRACT { background: CHANGES; font: STAYS; }",
        "}",
        "@component BLUE_ABY extends ABY {",
        "  @def CHANGES  blue;",
        "  .INCOMP { @mixin col(CHANGES); }",
        "}",
        "@component YELLOW_ABY extends ABY {",
        "  @def CHANGES yellow;",
        "  .INCOMP { @mixin col(green);}",
        "}"),

        "[[.BLUE_ABY-ABSTRACT]{[background:[blue];font:[red];]}"
        + "[.BLUE_ABY-INCOMP]{[color:[blue];background-color:[blue];]}"
        + "[.YELLOW_ABY-ABSTRACT]{[background:[yellow];font:[red];]}"
        + "[.YELLOW_ABY-INCOMP]{[color:[green];background-color:[yellow];]}]");
  }

  public void testMixinReplacementGradient1() {
    testTreeConstruction(linesToString(
        "@def GRADIENT_POS top;",
        "@def BASE_COLOR #cc0000;",
        "@def FONT_SIZE_SMALL 80%;",
        "@defmixin gradient(HSL1, HSL2, HSL3, COLOR) {",
        "  background-color: COLOR;",
        "  background-image:",
        "      -webkit-linear-gradient(GRADIENT_POS, hsl(HSL1, HSL2, HSL3),",
        "          COLOR);",
        "}",
        ".SOME_CLASS {",
        "  @mixin gradient(0%, 50%, 70%, BASE_COLOR);",
        "  font-size: FONT_SIZE_SMALL;",
        "}"),

        "[[.SOME_CLASS]{[background-color:[#cc0000];"
        + "background-image:"
        + "[-webkit-linear-gradient(top,hsl(0%,50%,70%),#cc0000)];"
        + "font-size:[80%];]}]");
  }

  public void testMixinReplacementGradient2() {
    testTreeConstruction(linesToString(
        "@defmixin grad(A, B, C, D, E) {",
        "  background-image:-webkit-linear-gradient(A, B, C, D, E);",
        "}",
        ".SOME_CLASS {",
        "  @mixin grad(bottom left, red 20px, yellow, green, blue 90%);",
        "}"),

        "[[.SOME_CLASS]{["
        + "background-image:[-webkit-linear-gradient("
        + "bottom left,red 20px,yellow,green,blue 90%)];]}]");
  }

  public void testMixinReplacementGradient3() {
    testTreeConstruction(linesToString(
        "@defmixin grad(A, B, C, D, E) {",
        "  background-image:-webkit-linear-gradient(A, B, C, D, E);",
        "}",
        ".SOME_CLASS {",
        "  @mixin grad( bottom  left,  red  20px, yellow,  green , blue  90%);",
        "}"),

        "[[.SOME_CLASS]{["
        + "background-image:[-webkit-linear-gradient("
        + "bottom left,red 20px,yellow,green,blue 90%)];]}]");
  }

  public void testNoMatchingMixinDefinition() throws GssParserException {
    parseAndRun("@defmixin test() {} h1 { @mixin unkown(); }",
        ReplaceMixins.NO_MATCHING_MIXIN_DEFINITION_ERROR_MESSAGE);
  }

  public void testDifferentArgumentCount1() throws GssParserException {
    parseAndRun("@defmixin test(PAR1) {} h1 { @mixin test(10px, 20px); }",
        ReplaceMixins.ARGUMENT_MISMATCH_ERROR_MESSAGE);
  }

  public void testDifferentArgumentCount2() throws GssParserException {
    parseAndRun("@defmixin test(PAR1, PAR2) {} h1 { @mixin test(10px); }",
        ReplaceMixins.ARGUMENT_MISMATCH_ERROR_MESSAGE);
  }

  public void testDifferentArgumentCount3() throws GssParserException {
    parseAndRun("@defmixin test() {} h1 { @mixin test(10px, 20px); }",
        ReplaceMixins.ARGUMENT_MISMATCH_ERROR_MESSAGE);
  }

  public void testDifferentArgumentCount4() throws GssParserException {
    parseAndRun("@defmixin test(PAR1) {} h1 { @mixin test(); }",
        ReplaceMixins.ARGUMENT_MISMATCH_ERROR_MESSAGE);
  }

  public void testDifferentArgumentCount5() throws GssParserException {
    parseAndRun(linesToString(
        "@defmixin someMixin(A, C, D, E) {",
        "  background-image:some-function(A, C, D, E);",
        "}",
        ".SOME_CLASS {",
        "  @mixin someMixin(bottom left, red 20px, yellow, green, blue 90%);",
        "}"),
        "GSS constant not defined: A",
        "GSS constant not defined: C",
        "GSS constant not defined: D",
        "GSS constant not defined: E",
        ReplaceMixins.ARGUMENT_MISMATCH_ERROR_MESSAGE);
  }

  public void testNestedBad() throws GssParserException {
    parseAndRun(linesToString(
        "@defmixin color(COL) {",
        "  width: COLOR;",
        "}",
        "@defmixin box(W, H, COLOR) {",
        "  width: W;",
        "  height: H;",
        "  @mixin color(COLOR);",
        "}",
        "p {",
        "  @mixin box(50%, 60%, red);",
        "}"),

        "GSS constant not defined: COLOR");
  }

  public void testCycle() throws GssParserException {
    parseAndRun(linesToString(
        "@defmixin a(A) {",
        "  @mixin c(A);",
        "  height: A;",
        "}",
        "@defmixin b(B) {",
        "  @mixin a(B);",
        "  width: B;",
        "}",
        "@defmixin c(C) {",
        "  @mixin b(C);",
        "}",
        "div {",
        "  @mixin a(10px);",
        "}"),

        ReplaceMixins.CYCLE_ERROR_MESSAGE);
  }

  @Override
  protected void runPass() {
    // This passes have to run before.
    new CreateMixins(tree.getMutatingVisitController(), errorManager).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();

    // This passes should run before to produce the expected behavior.
    // They are needed for testMixinReplacementComponents.
    new CreateDefinitionNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    new CreateComponentNodes(tree.getMutatingVisitController(),
        errorManager).runPass();

    // The passes tested here.
    CollectMixinDefinitions collectDefinitions = new CollectMixinDefinitions(
        tree.getMutatingVisitController(), errorManager);
    collectDefinitions.runPass();
    new ReplaceMixins(tree.getMutatingVisitController(), errorManager,
        collectDefinitions.getDefinitions()).runPass();

    // This passes should run afterwards to produce the expected behavior.
    // They are needed for testMixinReplacementComponents.
    new ProcessComponents<Object>(tree.getMutatingVisitController(),
        errorManager).runPass();
    CollectConstantDefinitions collectConstantDefinitionsPass =
        new CollectConstantDefinitions(tree);
    collectConstantDefinitionsPass.runPass();
    ReplaceConstantReferences replaceConstantReferences =
        new ReplaceConstantReferences(tree,
            collectConstantDefinitionsPass.getConstantDefinitions(),
            true /* removeDefs */, errorManager,
            false /* allowUndefinedConstants */);
    replaceConstantReferences.runPass();
  }
}
