/*
 * Copyright 2015 Google Inc.
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
 * Unit tests for the {@link AutoExpandBrowserPrefix} compiler pass.
 */
public class AutoExpandBrowserPrefixTest extends PassesTestBase {

  public void testMatchOnPropertyNameAndValue() {
    testTreeConstruction(linesToString(
        "p {",
        "  display: flex;",
        "}"),
        "[[p]{[[/* @alternate */]display:[[-webkit-box]];[/* @alternate */]display:[[-moz-box]];"
        + "[/* @alternate */]display:[[-webkit-flex]];[/* @alternate */]display:[[-ms-flexbox]];"
        + "[/* @alternate */]display:[[flex]];]}]");
  }

  public void testMatchOnPropertyName() {
    testTreeConstruction(linesToString(
        "p {",
        "  flex-grow: 1;",
        "}"),
        "[[p]{[[/* @alternate */]-webkit-box-flex:[[1]];[/* @alternate */]box-flex:[[1]];"
        + "[/* @alternate */]-ms-flex-positive:[[1]];[/* @alternate */]-webkit-flex-grow:[[1]];"
        + "[/* @alternate */]flex-grow:[[1]];]}]");
  }

  public void testMatchFunction() {
    testTreeConstruction(linesToString(
        "@def GRADIENT top, #f8f8f8, #f1f1f1;",
        "p {",
        "  background-image: linear-gradient(GRADIENT);",
        "}"),
        "[[p]{[[/* @alternate */]"
        + "background-image:[-webkit-linear-gradient([[top],[#f8f8f8],[#f1f1f1]])];"
        + "[/* @alternate */]background-image:[-moz-linear-gradient([[top],[#f8f8f8],[#f1f1f1]])];"
        + "[/* @alternate */]background-image:[-ms-linear-gradient([[top],[#f8f8f8],[#f1f1f1]])];"
        + "[/* @alternate */]background-image:[-o-linear-gradient([[top],[#f8f8f8],[#f1f1f1]])];"
        + "[/* @alternate */]background-image:[linear-gradient([[top],[#f8f8f8],[#f1f1f1]])];]}]");
  }

  public void testMatchValueOnlyFunction() {
    testTreeConstruction(
        "p { margin: calc(100% - 24px) auto; }",
        ""
            + "[[p]{[[/* @alternate */]margin:[-webkit-calc([[100%] - [24px]]) [auto]];"
            + "[/* @alternate */]margin:[-moz-calc([[100%] - [24px]]) [auto]];"
            + "[/* @alternate */]margin:[calc([[100%] - [24px]]) [auto]];]}]");
  }

  public void testMatchValueOnlyFunctionLast() {
    testTreeConstruction(
        "p { margin: 10px calc(100% - 24px); }",
        ""
            + "[[p]{[[/* @alternate */]margin:[[10px]-webkit-calc([[100%] - [24px]])];"
            + "[/* @alternate */]margin:[[10px]-moz-calc([[100%] - [24px]])];"
            + "[/* @alternate */]margin:[[10px]calc([[100%] - [24px]])];]}]");
  }

  public void testMatchValueOnlyMultipleFunctions() {
    testTreeConstruction(
        "p { margin: calc(100% - 24px) calc(50% + 16px); }",
        ""
            + "[[p]{[[/* @alternate */]margin:[-webkit-calc([[100%] - [24px]]) "
            + "-webkit-calc([[50%] + [16px]])];"
            + "[/* @alternate */]margin:[-moz-calc([[100%] - [24px]]) -moz-calc([[50%] + [16px]])];"
            + "[/* @alternate */]margin:[calc([[100%] - [24px]]) calc([[50%] + [16px]])];]}]");
  }

  public void testDefMixinUnaffected() {
    testTreeConstruction(linesToString(
        "@defmixin display_flex() {",
        "  display: flex;",
        "}",
        "p {",
        "  @mixin display_flex();",
        "}",
        "q {",
        "  display: flex;",
        "}"),
        "[[p]{[display:[[flex]];]}[q]{[[/* @alternate */]display:[[-webkit-box]];[/* @alternate */]"
        + "display:[[-moz-box]];[/* @alternate */]display:[[-webkit-flex]];[/* @alternate */]"
        + "display:[[-ms-flexbox]];[/* @alternate */]display:[[flex]];]}]");
  }

  @Override
  protected void runPass() {
    // These passes have to run before.
    new CreateMixins(tree.getMutatingVisitController(), errorManager).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();

    // These passes should run before to produce the expected behavior.
    // They are needed for testMixinReplacementComponents.
    new CreateDefinitionNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    new CreateComponentNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    new AutoExpandBrowserPrefix(tree.getMutatingVisitController(),
        errorManager).runPass();

    // The passes tested here.
    CollectMixinDefinitions collectDefinitions = new CollectMixinDefinitions(
        tree.getMutatingVisitController(), errorManager);
    collectDefinitions.runPass();
    new ReplaceMixins(tree.getMutatingVisitController(), errorManager,
        collectDefinitions.getDefinitions()).runPass();

    // These passes should run afterwards to produce the expected behavior.
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
