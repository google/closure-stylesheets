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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import com.google.common.css.compiler.gssfunctions.GssFunctions;

import java.util.Map;

/**
 * Unit tests for {@link ResolveCustomFunctionNodes}.
 *
 */
public class ResolveCustomFunctionNodesTest extends NewFunctionalTestBase {

  protected boolean allowUnknownFunctions = false;

  protected Map<String, GssFunction> createTestFunctionMap() {
    return new ImmutableMap.Builder<String, GssFunction>()
        .put("plus", new GssFunctions.AddToNumericValue())
        .put("minus", new GssFunctions.SubtractFromNumericValue())
        .put("mult", new GssFunctions.Mult())
        .put("div", new GssFunctions.Div())
        .put("min", new GssFunctions.MinValue())
        .put("max", new GssFunctions.MaxValue())
        .build();
  }

  @Override
  protected void runPass() {
    new ResolveCustomFunctionNodes(
        tree.getMutatingVisitController(), errorManager,
        createTestFunctionMap(), allowUnknownFunctions,
        ImmutableSet.<String>of() /* allowedNonStandardFunctions */).runPass();
  }

  public void testAcceptBuiltInFunction() throws Exception {
    parseAndRun("A { color: rgb(0,0,0) }");
  }

  public void testUnknownFunctionError() throws Exception {
    parseAndRun("A { width: -example(a,b) }", "Unknown function \"-example\"");
  }

  public void testUnknownFunctionAllowed() throws Exception {
    allowUnknownFunctions = true;
    parseAndRun("A { width: f(a,b) }");
    assertEquals("[f(a,b)]", getFirstPropertyValue().toString());
  }

  public void testWrongNumberOfArgsError() throws Exception {
    parseAndRun("A { width: plus(2px); }",
        "Not enough arguments");
  }

  public void testWrongArgumentError1() throws Exception {
    parseAndRun("A { width: max(2,bar,foo) }",
        "Size must be a CssNumericNode with a unit or 0; was: bar");
  }

  public void testPlus() throws Exception {
    parseAndRun("A { width: plus(2px, 3px) }");
    assertEquals("[5px]", getFirstPropertyValue().toString());
  }

  public void testMinus() throws Exception {
    parseAndRun("A { width: minus(2em, 5.5em) }");
    assertEquals("[-3.5em]", getFirstPropertyValue().toString());
  }

  public void testMax() throws Exception {
    parseAndRun("A { width: max(-2%, -5%) }");
    assertEquals("[-2%]", getFirstPropertyValue().toString());
  }

  public void testMultiply() throws Exception {
    parseAndRun("A { width: mult(-2, -5) }");
    assertEquals("[10]", getFirstPropertyValue().toString());
  }

  public void testFunctionWithinFunction() throws Exception {
    parseAndRun("A { width: max(10px, max(2px, 30px)) }");
    assertEquals("[30px]", getFirstPropertyValue().toString());
  }
}
