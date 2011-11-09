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
import com.google.common.css.compiler.ast.CssMixinNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 * Unit tests for {@link CreateMixins}.
 *
 */
public class CreateMixinsTest extends PassesTestBase{

  @Override
  protected void runPass() {
    CreateMixins pass = new CreateMixins(tree.getMutatingVisitController(),
        errorManager);
    pass.runPass();
  }

  public void testCreateMixin() throws Exception {
    parseAndRun("div { @mixin test(10px, 20%); }");
    assertTrue(getFirstActualNode() instanceof CssRulesetNode);
    CssRulesetNode ruleset = (CssRulesetNode) getFirstActualNode();
    assertEquals(1, ruleset.getDeclarations().numChildren());
    assertTrue(ruleset.getDeclarations().getChildAt(0) instanceof CssMixinNode);
    CssMixinNode mixin =
        (CssMixinNode) ruleset.getDeclarations().getChildAt(0);
    assertEquals("test", mixin.getDefinitionName());
    assertEquals(3, mixin.getArguments().numChildren());
  }

  public void testMixinWithBlockError() throws Exception {
    parseAndRun("div { @mixin test(10px, 20%) {} }",
        CreateMixins.BLOCK_ERROR_MESSAGE);
  }

  public void testMixinWithInvalidParameter() throws Exception {
    parseAndRun("div { @mixin test; }",
        CreateMixins.INVALID_PARAMETERS_ERROR_MESSAGE);
  }

  public void testCreateMixinDefinition() throws Exception {
    parseAndRun("@defmixin test(A,B) { a:b }");
    assertTrue(getFirstActualNode() instanceof CssMixinDefinitionNode);
    CssMixinDefinitionNode mixinDefinition =
        (CssMixinDefinitionNode) getFirstActualNode();
    assertEquals("test", mixinDefinition.getDefinitionName());
    // The comma counts as an argument.
    assertEquals(3, mixinDefinition.getArguments().numChildren());
  }

  public void testMixinDefinitionWithoutBlockError() throws Exception {
    parseAndRun("@defmixin test(A, B);",
        CreateMixins.NO_BLOCK_ERROR_MESSAGE);
  }

  public void testMixinDefinitionWithInvalidParameter() throws Exception {
    parseAndRun("@defmixin test {}",
        CreateMixins.INVALID_PARAMETERS_ERROR_MESSAGE);
  }
}
