/*
 * Copyright 2013 Google Inc.
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

import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

/**
 * Unit tests for {@link HandleMissingConstantDefinitions}.
 */
public class HandleMissingConstantDefinitionsTest
    extends NewFunctionalTestBase {

  private static final String TEST_GOOD_CODE = linesToString(
      "@def TEXT_COLOR blue;",
      ".rule {",
      "  color: TEXT_COLOR",
      "}");
  private static final String TEST_BAD_CODE = linesToString(
      ".rule {",
      "  color: MISSING_TEXT_COLOR",
      "}");

  public void testGoodCodeNoErrors() throws Exception {
    parseAndRun(TEST_GOOD_CODE);
  }

  public void testBadCodeThrowsErrors() throws Exception {
    parseAndRun(TEST_BAD_CODE, HandleMissingConstantDefinitions.ERROR_MESSAGE);
  }

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(tree.getMutatingVisitController(), errorManager)
        .runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
    CollectConstantDefinitions collectDefinitions =
        new CollectConstantDefinitions(tree.getVisitController());
    collectDefinitions.runPass();
    new HandleMissingConstantDefinitions(
        tree.getVisitController(),
        errorManager,
        collectDefinitions.getConstantDefinitions())
        .runPass();
  }
}
