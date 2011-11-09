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

import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.testing.PassesTestBase;
import com.google.testing.util.MoreAsserts;

import java.util.Map;

/**
 * Unit tests for the {@link CollectMixinDefinitions} compiler pass.
 *
 */
public class CollectMixinDefinitionsTest extends PassesTestBase {
  private Map<String, CssMixinDefinitionNode> definitions;

  public void testSimpleMixinDefinition() {
    parseAndBuildTree(
        "@defmixin test(PAR1, PAR2) { width: PAR1; height: PAR2; }");
  }

  public void testCollectedMixinDefinitions() {
    parseAndBuildTree(linesToString(
        "@defmixin test1(PAR1, PAR2) { width: PAR1; height: PAR2; }",
        "@defmixin test2() {}",
        "@defmixin test3(PAR1) { color: PAR1; }"));
    assertNotNull(definitions);
    MoreAsserts.assertContentsAnyOrder(definitions.keySet(), "test1", "test2",
        "test3");
  }

  public void testDupilicateMixinDefinitionNames() throws GssParserException {
    parseAndRun("@defmixin test() {} @defmixin test() {}",
        CollectMixinDefinitions.DUPLICATE_MIXIN_DEFINITION_NAME_ERROR_MESSAGE);
  }

  public void testDupilicateArgumentNames() throws GssParserException {
    parseAndRun("@defmixin test(PAR, PAR) {}",
        CollectMixinDefinitions.DUPLICATE_ARGUMENT_NAME_ERROR_MESSAGE);
  }

  public void testInvalidArgument() throws GssParserException {
    parseAndRun("@defmixin test(Par) {}",
        CollectMixinDefinitions.INVALID_ARGUMENT_ERROR_MESSAGE);
  }

  public void testInvalidBlock1() throws GssParserException {
    parseAndRun("@if (COND) { @defmixin test(PAR) {} }",
        CollectMixinDefinitions.INVALID_BLOCK_ERROR_MESSAGE);
  }

  public void testInvalidBlock2() throws GssParserException {
    parseAndRun("@component X { @defmixin test(PAR) {} }",
        CollectMixinDefinitions.INVALID_BLOCK_ERROR_MESSAGE);
  }

  @Override
  protected void runPass() {
    // This pass has to run before.
    new CreateMixins(tree.getMutatingVisitController(), errorManager).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();

    CollectMixinDefinitions collectDefinitions = new CollectMixinDefinitions(
        tree.getMutatingVisitController(), errorManager);
    collectDefinitions.runPass();
    definitions = collectDefinitions.getDefinitions();
  }
}
