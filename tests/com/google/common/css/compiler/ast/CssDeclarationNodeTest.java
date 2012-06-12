/*
 * Copyright 2008 Google Inc.
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

import com.google.common.css.compiler.ast.testing.AstUtilityTestCase;

import junit.framework.TestCase;

/**
 * Unit tests for {@link CssDeclarationNode}
 *
 * @author oana@google.com (Oana Florescu)
 *
 */
public class CssDeclarationNodeTest extends TestCase {

  public void testDeclarationNodeCreation() {
    CssPropertyNode propertyName = new CssPropertyNode("color", null);
    CssDeclarationNode node = new CssDeclarationNode(propertyName);

    assertNull(node.getParent());
    assertNull(node.getSourceCodeLocation());
    assertEquals(propertyName.toString(), node.getPropertyName().toString());
    assertTrue(node.getPropertyValue().isEmpty());
  }

  public void testCompleteDeclarationNodeCreation() {
    CssPropertyNode propertyName = new CssPropertyNode("color", null);
    CssLiteralNode colorValue = new CssLiteralNode("red");
    CssPropertyValueNode propertyValue = new CssPropertyValueNode();
    propertyValue.addChildToBack(colorValue);

    CssDeclarationNode node = new CssDeclarationNode(propertyName,
                                                     propertyValue);

    assertNull(node.getParent());
    assertNull(node.getSourceCodeLocation());
    assertEquals(propertyValue, node.getPropertyValue());
    assertEquals("color:[red]", node.toString());
  }
  
  public void testDeepCopyOfDeclarationNode() throws Exception {
    AstUtilityTestCase tester = new AstUtilityTestCase();
    CssPropertyNode propertyName = new CssPropertyNode("color", null);
    CssDeclarationNode node1 = new CssDeclarationNode(propertyName);
    node1.setStarHack(false);
    
    CssDeclarationNode node2 = new CssDeclarationNode(node1);
    
    tester.deepEquals(node1, node2);
  }

  public void testDeepCopyOfDeclarationNode2() throws Exception {
    AstUtilityTestCase tester = new AstUtilityTestCase();
    CssPropertyNode propertyName = new CssPropertyNode("color", null);
    CssDeclarationNode node1 = new CssDeclarationNode(propertyName);
    node1.setStarHack(true);
    
    CssDeclarationNode node2 = new CssDeclarationNode(node1);
    
    tester.deepEquals(node1, node2);
  }

  public void testDeepCopyOfDeclarationNode3() throws Exception {
    AstUtilityTestCase tester = new AstUtilityTestCase();
    CssPropertyNode propertyName = new CssPropertyNode("color", null);
    CssDeclarationNode node1 = new CssDeclarationNode(propertyName);
    node1.setStarHack(true);
    node1.setShouldBeFlipped(true);
    
    CssDeclarationNode node2 = new CssDeclarationNode(node1);
    
    tester.deepEquals(node1, node2);
  }
}
