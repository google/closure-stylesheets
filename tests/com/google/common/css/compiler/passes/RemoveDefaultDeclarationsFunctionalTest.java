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
 * Functional tests for {@link RemoveDefaultDeclarations}.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class RemoveDefaultDeclarationsFunctionalTest extends PassesTestBase {

  public void testRemoveDeclarations1() {
    testTreeConstruction(linesToString(
        "@def COLOR /* @default */ #fff;",
        "@def PADDING 1px;",
        ".CSS_RULE {",
        "  color: COLOR;",
        "  padding: PADDING;",
        "}"),
        "[[.CSS_RULE]{[padding:[1px];]}]");
  }

  public void testRemoveDeclarations2() {
    testTreeConstruction(linesToString(
        "@def COLOR /* @default */ #fff;",
        "@def PADDING 1px;",
        ".CSS_RULE_1 {",
        "  color: COLOR;",
        "  padding: PADDING;",
        "}",
        "@def COLOR red;",
        ".CSS_RULE_2 {",
        "  color: COLOR;",
        "  border: 2px;",
        "}"),
        "[[.CSS_RULE_1]{[color:[red];padding:[1px];]}" +
        "[.CSS_RULE_2]{[color:[red];border:[2px];]}]");
  }

  public void testRemoveDeclarations3() {
    testTreeConstruction(linesToString(
        "@def COLOR /* @default */ #fff;",
        "@def OTHER_COLOR COLOR;",
        ".CSS_RULE_1 {",
        "  color: COLOR;",
        "  padding: 1px;",
        "}",
        ".CSS_RULE_2 {",
        "  color: OTHER_COLOR;",
        "  border: 2px;",
        "}"),
        "[[.CSS_RULE_1]{[padding:[1px];]}" +
        "[.CSS_RULE_2]{[border:[2px];]}]");
  }

public void testRemoveDeclarations4() {
    testTreeConstruction(linesToString(
        "@def COLOR /* @default */ #fff;",
        ".CSS_RULE_1 {",
        "  border-color: COLOR;",
        "  padding: 1px;",
        "}",
        ".CSS_RULE_2 {",
        "  border-color: COLOR COLOR COLOR COLOR;",
        "  border: 2px;",
        "}",
        ".CSS_RULE_3 {",
        "  border-color: #000 COLOR #000 COLOR;",
        "}"),
        "[[.CSS_RULE_1]{[padding:[1px];]}" +
        "[.CSS_RULE_2]{[border:[2px];]}" +
        "[.CSS_RULE_3]{[border-color:[#000 #fff #000 #fff];]}]");
  }

public void testRemoveDeclarations5() {
    testTreeConstruction(linesToString(
        "@def WIDTH /* @default */ thin;",
        ".CSS_RULE_1 {",
        "  border-width: WIDTH;",
        "  padding: 1px;",
        "}",
        ".CSS_RULE_2 {",
        "  border-width: WIDTH WIDTH;",
        "  margin: 2px;",
        "}",
        ".CSS_RULE_3 {",
        "  border-width: normal WIDTH thick WIDTH;",
        "}"),
        "[[.CSS_RULE_1]{[padding:[1px];]}" +
        "[.CSS_RULE_2]{[margin:[2px];]}" +
        "[.CSS_RULE_3]{[border-width:[normal thin thick thin];]}]");
  }

public void testRemoveDeclarations6() {
    testTreeConstruction(linesToString(
        "@def STYLE /* @default */ solid;",
        ".CSS_RULE_1 {",
        "  border-style: STYLE;",
        "  padding: 1px;",
        "}",
        ".CSS_RULE_2 {",
        "  border-style: STYLE STYLE;",
        "  margin: 2px;",
        "}",
        ".CSS_RULE_3 {",
        "  border-style: STYLE dotted;",
        "}"),
        "[[.CSS_RULE_1]{[padding:[1px];]}" +
        "[.CSS_RULE_2]{[margin:[2px];]}" +
        "[.CSS_RULE_3]{[border-style:[solid dotted];]}]");
  }

public void testRemoveDeclarations7() {
    testTreeConstruction(linesToString(
        "@def MARGIN /* @default */ 4px;",
        ".CSS_RULE_1 {",
        "  margin: MARGIN;",
        "  padding: 1px;",
        "}",
        ".CSS_RULE_2 {",
        "  margin: MARGIN MARGIN;",
        "  color: #fff",
        "}",
        ".CSS_RULE_3 {",
        "  margin: MARGIN 0  MARGIN 1px;",
        "}"),
        "[[.CSS_RULE_1]{[padding:[1px];]}" +
        "[.CSS_RULE_2]{[color:[#fff];]}" +
        "[.CSS_RULE_3]{[margin:[4px 0 4px 1px];]}]");
  }

public void testRemoveDeclarations8() {
    testTreeConstruction(linesToString(
        "@def PADDING /* @default */ 4px;",
        ".CSS_RULE_1 {",
        "  padding: PADDING;",
        "  margin: 1px;",
        "}",
        ".CSS_RULE_2 {",
        "  padding: PADDING PADDING PADDING;",
        "  color: #fff",
        "}",
        ".CSS_RULE_3 {",
        "  padding: PADDING PADDING PADDING 0;",
        "}"),
        "[[.CSS_RULE_1]{[margin:[1px];]}" +
        "[.CSS_RULE_2]{[color:[#fff];]}" +
        "[.CSS_RULE_3]{[padding:[4px 4px 4px 0];]}]");
  }

public void testRemoveDeclarations9() {
    testTreeConstruction(linesToString(
        "@def WIDTH /* @default */ thin;",
        "@def STYLE /* @default */ solid;",
        "@def COLOR /* @default */ #fff;",
        ".CSS_RULE_1 {",
        "  border: WIDTH STYLE COLOR;",
        "  margin: 1px;",
        "}",
        ".CSS_RULE_2 {",
        "  border: WIDTH STYLE #000;",
        "}",
        ".CSS_RULE_3 {",
        "  border: normal STYLE COLOR;",
        "}"),
        "[[.CSS_RULE_1]{[margin:[1px];]}" +
        "[.CSS_RULE_2]{[border:[#000];]}" +
        "[.CSS_RULE_3]{[border:[normal];]}]");
  }

public void testRemoveDeclarations10() {
  testTreeConstruction(linesToString(
      "@def WIDTH /* @default */ thin;",
      "@def STYLE /* @default */ solid;",
      "@def COLOR /* @default */ #fff;",
      ".CSS_RULE_1 {",
      "  border: WIDTH STYLE COLOR !important;",
      "  margin: 1px;",
      "}",
      ".CSS_RULE_3 {",
      "  border: normal STYLE !important;",
      "}"),

      "[[.CSS_RULE_1]{[border:[thin solid #fff!important];margin:[1px];]}" +
      "[.CSS_RULE_3]{[border:[normal solid!important];]}]");
}

public void testRemoveDeclarations11() {
  testTreeConstruction(linesToString(
      "@def WIDTH /* @default */ thin;",
      "@def COLOR /* @default */ #fff;",
      "@def HEIGHT WIDTH;",
      ".CSS_RULE_1 {",
      "  border: WIDTH COLOR !important;",
      "  margin: 1px;",
      "}",
      ".CSS_RULE_2 {",
      "  border: WIDTH COLOR;",
      "}"
      ),
      "[[.CSS_RULE_1]{[border:[thin #fff!important];margin:[1px];]}" +
      "[.CSS_RULE_2]{[]}]");
}

public void testRemoveDeclarations12() {
  testTreeConstruction(linesToString(
      "@def COLOR /* @default */ #fff;",
      "@def BGCOLOR COLOR;",
      "@def BORDERCOLOR BGCOLOR;",
      ".CSS_RULE_1 {",
      "  border: BORDERCOLOR !important;",
      "}",
      ".CSS_RULE_2 {",
      "  border: BORDERCOLOR;",
      "}"
      ),
      "[[.CSS_RULE_1]{[border:[#fff!important];]}" +
      "[.CSS_RULE_2]{[]}]");
}

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    new MarkDefaultDefinitions(tree.getVisitController()).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController())
        .runPass();
    new CreateConditionalNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    new CreateComponentNodes(tree.getMutatingVisitController(),
        errorManager).runPass();

    CollectConstantDefinitions defPass = new CollectConstantDefinitions(tree);
    defPass.runPass();

    new ReplaceConstantReferences(tree, defPass.getConstantDefinitions(),
        true /* removeDefs */, errorManager,
        true /* allowUndefinedConstants */).runPass();

    new RemoveDefaultDeclarations(tree.getMutatingVisitController()).runPass();
  }
}
