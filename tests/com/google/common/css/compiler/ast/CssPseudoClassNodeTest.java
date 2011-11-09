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

package com.google.common.css.compiler.ast;

import com.google.common.css.compiler.ast.CssPseudoClassNode.FunctionType;

import junit.framework.TestCase;

/**
 * Unit tests for {@link CssPseudoClassNode}
 *
 */
public class CssPseudoClassNodeTest extends TestCase {

  public void testNonFunction() {
    String name = "foo";
    CssPseudoClassNode node = new CssPseudoClassNode(name, null);
    assertEquals(FunctionType.NONE, node.getFunctionType());
    assertEquals(node.getPrefix(), ":");
    assertEquals(node.getSuffix(), "");
    assertEquals(name, node.getRefinerName());
    assertNull(node.getArgument());
    assertNull(node.getNotSelector());
  }

  public void testLangFunction() {
    String name = "foo";
    String arg = "en";
    CssPseudoClassNode node = new CssPseudoClassNode(FunctionType.LANG, name,
        arg, null);
    assertEquals(FunctionType.LANG, node.getFunctionType());
    assertEquals(name, node.getRefinerName());
    assertEquals(arg, node.getArgument());
    assertNull(node.getNotSelector());
  }

  public void testNthFunction() {
    String name = "foo";
    String arg = "2n+1";
    CssPseudoClassNode node = new CssPseudoClassNode(FunctionType.NTH, name,
        arg, null);
    assertEquals(FunctionType.NTH, node.getFunctionType());
    assertEquals(name, node.getRefinerName());
    assertEquals(arg, node.getArgument());
    assertNull(node.getNotSelector());
  }

  public void testNotFunction() {
    String name = "not";
    CssSelectorNode selector = new CssSelectorNode("foo");
    CssPseudoClassNode node = new CssPseudoClassNode(name, selector, null);
    assertEquals(FunctionType.NOT, node.getFunctionType());
    assertEquals(name, node.getRefinerName());
    assertEquals(selector, node.getNotSelector());
    assertNull(node.getArgument());
  }
}
