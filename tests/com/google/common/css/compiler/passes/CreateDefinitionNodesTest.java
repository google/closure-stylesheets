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

import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

/**
 * Unit tests for {@link CreateDefinitionNodes}.
 *
 */
public class CreateDefinitionNodesTest extends NewFunctionalTestBase {

  @Override
  protected void runPass() {
    CreateDefinitionNodes pass = new CreateDefinitionNodes(
        tree.getMutatingVisitController(), errorManager);
    pass.runPass();
  }

  public void testCreateDefNode1() throws Exception {
    parseAndRun("@def X Y;");
    assertTrue(getFirstActualNode() instanceof CssDefinitionNode);
    CssDefinitionNode def = (CssDefinitionNode) getFirstActualNode();
    assertEquals("X", def.getName().getValue());
    assertEquals(1, def.getParametersCount());
  }

  public void testBlockError() throws Exception {
    parseAndRun("@def X { a {b: c} }", "@def with block");
    assertTrue(isEmptyBody());
  }

  public void testNoNameError() throws Exception {
    parseAndRun("@def;", "@def without name");
    assertTrue(isEmptyBody());
  }

  public void testNameError() throws Exception {
    parseAndRun("@def 1px 2px 3px;",
        "@def without a valid literal as name");
    assertTrue(isEmptyBody());
  }

  public void testNameSyntacticallyInvalid() throws Exception {
    parseAndRun("@def FOO-BAR 1;",
        "WARNING for invalid @def name FOO-BAR. We will ignore this.");
  }
}
