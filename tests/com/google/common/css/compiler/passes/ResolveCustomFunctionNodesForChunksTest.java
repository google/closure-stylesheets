/*
 * Copyright 2010 Google Inc.
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
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.CssDefinitionNode;

/**
 * Unit tests for {@link ResolveCustomFunctionNodesForChunks}.
 *
 */
public class ResolveCustomFunctionNodesForChunksTest
      extends ResolveCustomFunctionNodesTest {

  private static final String TEST_CHUNK = "testChunk";

  private ResolveCustomFunctionNodesForChunks<String> resolveForChunksPass;

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(
        tree.getMutatingVisitController(), errorManager).runPass();

    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();

    new MapChunkAwareNodesToChunk<String>(
        tree, ImmutableMap.of(TEST_FILENAME, TEST_CHUNK)).runPass();

    resolveForChunksPass =
        new ResolveCustomFunctionNodesForChunks<String>(
            tree.getMutatingVisitController(),
            errorManager,
            createTestFunctionMap(),
            allowUnknownFunctions,
            ImmutableSet.<String>of() /* allowedNonStandardFunctions */,
            new Function<String, String>() {
              private int count = 0;

              @Override
              public String apply(String chunk) {
                assertThat(chunk).isNotNull();
                return String.valueOf(count++);
              }
            });

    resolveForChunksPass.runPass();
  }

  public void testCreateDef1() {
    assertConstants(
        "@def A 1; .A { width: plus(A, 2, px);}",
        "plus(A,2,px)");
  }

  public void testCreateDef2() {
    assertConstants(
        "@def A 1; .A { width: plus(A, 1, px); height: plus(A, 2, px);}",
        "plus(A,1,px)", "plus(A,2,px)");
  }

  public void testFunctionWithNoDefInFunction() {
    assertConstants(
        "@def A 1px; .A { width: plus(A, plus(0, 2px));}",
        "plus(A,2px)");
  }

  public void testFunctionWithDefInFunction() {
    assertConstants(
        "@def A 1; .A { width: plus(A, plus(A, 2, \"\"), px);}",
        "plus(A,2,\"\")", "plus(A,__F0,px)");
  }

  public void testAlreadyDef1() {
    assertConstants("@def A 1; @def B plus(A,2,px);",
        "plus(A,2,px)");
  }

  public void testAlreadyDef2() {
    assertConstants("@def A 1; @def B plus(A, plus(A, 2, \"\"),px);",
        "plus(A,2,\"\")", "plus(A,__F0,px)");
  }

  public void testAlreadyDef3() {
    assertConstants("@def A 1; @def B plus(A,2,px) plus(A, 1, px);",
        "plus(A,2,px)", "plus(A,1,px)");
  }

  public void testNoFunctions() {
    assertConstants("@def A 1; .A { width: A }");
  }

  /**
   * Asserts that the pass creates the given constant definition
   * values, in that order and no more, for the given code.
   */
  private void assertConstants(String code, String... constants) {
    parseAndBuildTree(code);
    ConstantDefinitions constantDefinitions =
      resolveForChunksPass.getConstantDefinitions().get(TEST_CHUNK);

    int defCount = 0;
    for (String constant : constants) {
      assertWithMessage("Definitions expected").that(constantDefinitions).isNotNull();

      String defName = getDefName(defCount);
      CssDefinitionNode defNode =
        constantDefinitions.getConstantDefinition(defName);

      assertWithMessage("Missing definition " + defCount).that(defNode).isNotNull();
      assertThat(defNode.toString()).isEqualTo("@def " + defName + " [" + constant + "]");

      defCount++;
    }

    if (defCount == 0) {
      assertWithMessage("No definitions expected").that(constantDefinitions).isNull();
    } else {
      assertWithMessage("Too many definitions! Expected " + defCount)
          .that(constantDefinitions.getConstantDefinition(getDefName(defCount)))
          .isNull();
    }
  }

  private String getDefName(int n) {
    return "__F" + n;
  }
}
