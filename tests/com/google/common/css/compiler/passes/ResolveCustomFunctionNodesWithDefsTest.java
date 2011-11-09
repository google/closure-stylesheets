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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link ResolveCustomFunctionNodes}.
 *
 */
public class ResolveCustomFunctionNodesWithDefsTest
    extends NewFunctionalTestBase {

  protected boolean allowUnknownFunctions = false;

  protected Map<String, GssFunction> createTestFunctionMap() {
    return new ImmutableMap.Builder<String, GssFunction>()
        .put("testMultipleArg", new SampleMultipleArgsFunc())
        .build();
  }

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(
        tree.getMutatingVisitController(), errorManager).runPass();

    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();

    // Collect constant definitions.
    CollectConstantDefinitions collectConstantDefinitionsPass =
        new CollectConstantDefinitions(tree);
    collectConstantDefinitionsPass.runPass();
    // Replace constant references.
    new ReplaceConstantReferences(tree,
        collectConstantDefinitionsPass.getConstantDefinitions(),
        true /* removeDefs */, errorManager,
        true /* allowUndefinedConstants */).runPass();

    new ResolveCustomFunctionNodes(
        tree.getMutatingVisitController(), errorManager,
        createTestFunctionMap(), allowUnknownFunctions,
        ImmutableSet.<String>of() /* allowedNonStandardFunctions */)
        .runPass();
  }

  public void testMultipleArgs() throws Exception {
    parseAndRun("@def BAR 3px left top;" +
        "A { foo: testMultipleArg(first, 30px, BAR) }");
    assertEquals("[(first) (30px) (3px left top)]",
        getFirstPropertyValue().toString());
  }

  /**
   * Sample GssFunction implementation that accept 3 args.
   */
  private static class SampleMultipleArgsFunc implements GssFunction {
    @Override
    public Integer getNumExpectedArguments() {
      return 3;
    }

    @Override
    public List<CssValueNode> getCallResultNodes(
        List<CssValueNode> args, ErrorManager errorManager) {

      Preconditions.checkState(args.size() == 3,
          "Exactly 3 args expected: firstString, secondNumeric, thirdString");

      List<String> argsStr = ImmutableList.of(
          args.get(0).toString(),
          args.get(1).toString(),
          args.get(2).toString());

      CssLiteralNode result = new CssLiteralNode(getCallResultString(argsStr),
          args.get(0).getSourceCodeLocation());
      return ImmutableList.of((CssValueNode) result);
    }

    @Override
    public String getCallResultString(List<String> args) {
      Preconditions.checkState(args.size() == 3,
          "Exactly 3 args expected: startColor, endColor, defaultBg");
      return "(" + args.get(0) + ") (" + args.get(1) +
          ") (" + args.get(2) + ")";
    }
  }
}
