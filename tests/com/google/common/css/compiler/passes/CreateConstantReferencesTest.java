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

import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

/**
 * Tests for {@link CreateConstantReferences}.
 *
 */
public class CreateConstantReferencesTest extends NewFunctionalTestBase {

  public void testCreateSimpleRef() throws Exception {
    parseAndRun(".X { color: SOME_COLOR }");

    CssValueNode colorValue = getFirstPropertyValue().getChildAt(0);
    assertTrue(colorValue instanceof CssConstantReferenceNode);
    assertEquals("SOME_COLOR", colorValue.getValue());
  }

  public void testCreateFunRef() throws Exception {
    parseAndRun(".X { background-url:  image(X0,Y0) }");

    CssFunctionNode funCall = (CssFunctionNode) getFirstPropertyValue().getChildAt(0);

    CssValueNode x0Value = funCall.getArguments().getChildAt(0);
    assertTrue(x0Value instanceof CssConstantReferenceNode);
    assertEquals("X0", x0Value.getValue());

    CssValueNode commaValue = funCall.getArguments().getChildAt(1);
    assertTrue(commaValue instanceof CssLiteralNode);

    CssValueNode y0Value = funCall.getArguments().getChildAt(2);
    assertTrue(y0Value instanceof CssConstantReferenceNode);
    assertEquals("Y0", y0Value.getValue());
  }

  @Override
  protected void runPass() {
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
  }
}
