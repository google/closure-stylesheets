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

package com.google.common.css.compiler.ast;

import com.google.common.css.compiler.ast.CssFunctionNode.Function;

import junit.framework.TestCase;

/**
 * Unit tests for {@link CssFunctionNode}.
 * 
 * @author oana@google.com (Your Name Here)
 */
public class CssFunctionNodeTest extends TestCase {

  private static final CssFunctionNode.Function RGB =
      CssFunctionNode.Function.byName("rgb");

  public void testConstructor() {
    CssFunctionNode function = new CssFunctionNode((Function) null, null);
    assertNull(function.getFunction());
    assertNull(function.getSourceCodeLocation());
    assertNotNull(function.getArguments());
    assertTrue(function.getArguments().getChildren().isEmpty());
  }
  
  public void testFunctionName() {
    CssFunctionNode function = new CssFunctionNode(RGB, null);
    assertNotNull(function.getFunction());
    assertEquals(RGB, function.getFunction());
    assertEquals("rgb", function.getFunctionName());
    assertNull(function.getSourceCodeLocation());
    assertNotNull(function.getArguments());
    assertTrue(function.getArguments().getChildren().isEmpty());
  }
  
  public void testFunctionArguments() {
    CssFunctionNode function = new CssFunctionNode(RGB, null);
    CssFunctionArgumentsNode args = new CssFunctionArgumentsNode();
    args.addChildToBack(new CssLiteralNode("test"));
    function.setArguments(args);
    assertNotNull(function.getFunction());
    assertEquals(RGB, function.getFunction());
    assertEquals("rgb", function.getFunctionName());
    assertNull(function.getSourceCodeLocation());
    assertNotNull(function.getArguments());
    assertFalse(function.getArguments().getChildren().isEmpty());
    assertEquals("rgb(test)", function.toString());
    assertEquals("rgb(test)", function.getValue());
  }
}
