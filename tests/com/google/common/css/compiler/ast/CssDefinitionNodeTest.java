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

import com.google.common.collect.Lists;

import junit.framework.TestCase;

/**
 * Unit tests for {@link CssDefinitionNode}.
 * 
 * @author oana@google.com (Oana Florescu)
 */
public class CssDefinitionNodeTest extends TestCase {

  public void testDefinitionNodeCreation() {
    CssDefinitionNode definition = new CssDefinitionNode(
        new CssLiteralNode("COLOR"));

    assertNull(definition.getParent());
    assertNull(definition.getSourceCodeLocation());
    assertEquals("@def", definition.getType().toString());
    assertEquals("@def COLOR []", definition.toString());
  }

  public void testDefinitionNodeCopy() {
    CssDefinitionNode definition1 = new CssDefinitionNode(
        new CssLiteralNode("COLOR"), 
        Lists.newArrayList(new CssCommentNode("/* foo */", null)));
    CssDefinitionNode definition2 = new CssDefinitionNode(definition1);
    
    assertNull(definition1.getParent());
    assertNull(definition2.getParent());
    
    assertNull(definition1.getSourceCodeLocation());
    assertNull(definition2.getSourceCodeLocation());
    
    assertEquals("@def", definition1.getType().toString());
    assertEquals("@def", definition2.getType().toString());
    
    assertEquals("@def COLOR []", definition1.toString());
    assertEquals("@def COLOR []", definition2.toString());
    
    assertTrue(definition1.hasComment("/* foo */"));
    assertTrue(definition2.hasComment("/* foo */"));
  }
}
