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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssMixinNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.passes.testing.PassesTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link CreateMixins}.
 *
 * @author fbenz@google.com (Florian Benz)
 */
@RunWith(JUnit4.class)
public class CreateMixinsTest extends PassesTestBase {

  @Override
  protected void runPass() {
    CreateMixins pass = new CreateMixins(tree.getMutatingVisitController(),
        errorManager);
    pass.runPass();
  }

  @Test
  public void testCreateMixin() throws Exception {
    parseAndRun("div { @mixin test(10px, 20%); }");
    assertThat(getFirstActualNode()).isInstanceOf(CssRulesetNode.class);
    CssRulesetNode ruleset = (CssRulesetNode) getFirstActualNode();
    assertThat(ruleset.getDeclarations().numChildren()).isEqualTo(1);
    assertThat(ruleset.getDeclarations().getChildAt(0)).isInstanceOf(CssMixinNode.class);
    CssMixinNode mixin =
        (CssMixinNode) ruleset.getDeclarations().getChildAt(0);
    assertThat(mixin.getDefinitionName()).isEqualTo("test");
    assertThat(mixin.getArguments().numChildren()).isEqualTo(3);
  }

  @Test
  public void testMixinWithBlockError() throws Exception {
    parseAndRun("div { @mixin test(10px, 20%) {} }",
        CreateMixins.BLOCK_ERROR_MESSAGE);
  }

  @Test
  public void testMixinWithInvalidParameter() throws Exception {
    parseAndRun("div { @mixin test; }",
        CreateMixins.INVALID_PARAMETERS_ERROR_MESSAGE);
  }

  @Test
  public void testCreateMixinDefinition() throws Exception {
    parseAndRun("@defmixin test(A,B) { a:b }");
    assertThat(getFirstActualNode()).isInstanceOf(CssMixinDefinitionNode.class);
    CssMixinDefinitionNode mixinDefinition =
        (CssMixinDefinitionNode) getFirstActualNode();
    assertThat(mixinDefinition.getDefinitionName()).isEqualTo("test");
    // The comma counts as an argument.
    assertThat(mixinDefinition.getArguments().numChildren()).isEqualTo(3);
  }

  @Test
  public void testMixinDefinitionWithoutBlockError() throws Exception {
    parseAndRun("@defmixin test(A, B);",
        CreateMixins.NO_BLOCK_ERROR_MESSAGE);
  }

  @Test
  public void testMixinDefinitionWithInvalidParameter() throws Exception {
    parseAndRun("@defmixin test {}",
        CreateMixins.INVALID_PARAMETERS_ERROR_MESSAGE);
  }
}
