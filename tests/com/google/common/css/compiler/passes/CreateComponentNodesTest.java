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

import com.google.common.css.compiler.ast.CssComponentNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

import java.util.List;

/**
 * Unit tests for {@link CreateComponentNodes}.
 *
 */
public class CreateComponentNodesTest extends NewFunctionalTestBase {

  @Override
  protected void runPass() {
    new CreateComponentNodes(tree.getMutatingVisitController(), errorManager).runPass();
  }

  public void testCreateComponentNode1() throws Exception {
    parseAndRun("@component CSS_X { @def X Y; }");
    assertTrue(getFirstActualNode() instanceof CssComponentNode);
    CssComponentNode comp = (CssComponentNode) getFirstActualNode();
    assertEquals("CSS_X", comp.getName().getValue());
    assertNull(comp.getParentName());
    assertFalse(comp.isAbstract());
    assertEquals("[@def[X, Y]]", comp.getBlock().toString());
  }

  public void testCreateComponentNode2() throws Exception {
    parseAndRun("@abstract_component CSS_X { @def X Y; }");
    assertTrue(getFirstActualNode() instanceof CssComponentNode);
    CssComponentNode comp = (CssComponentNode) getFirstActualNode();
    assertEquals("CSS_X", comp.getName().getValue());
    assertNull(comp.getParentName());
    assertTrue(comp.isAbstract());
    assertEquals("[@def[X, Y]]", comp.getBlock().toString());
  }

  public void testCreateComponentNode3() throws Exception {
    parseAndRun("@abstract_component CSS_X { @def X Y; }\n" +
        "@component CSS_Y extends CSS_X { @def X Y; }");
    List<CssNode> children = tree.getRoot().getBody().getChildren();
    assertEquals(2, children.size());
    assertTrue(children.get(0) instanceof CssComponentNode);
    assertTrue(children.get(1) instanceof CssComponentNode);
    CssComponentNode comp = (CssComponentNode) children.get(1);
    assertEquals("CSS_Y", comp.getName().getValue());
    assertNotNull(comp.getParentName());
    assertEquals("CSS_X", comp.getParentName().getValue());
    assertFalse(comp.isAbstract());
    assertEquals("[@def[X, Y]]", comp.getBlock().toString());
  }

  public void testImplicitlyNamedComponent() throws Exception {
    parseAndRun("@component { @def X Y; }");
    assertTrue(getFirstActualNode() instanceof CssComponentNode);
    CssComponentNode comp = (CssComponentNode) getFirstActualNode();
    assertSame(CssComponentNode.IMPLICIT_NODE_NAME, comp.getName().getValue());
    assertNull(comp.getParentName());
    assertFalse(comp.isAbstract());
    assertEquals("[@def[X, Y]]", comp.getBlock().toString());
  }

  public void testBlockError() throws Exception {
    parseAndRun("@component CSS_X;", "@component without block");
    assertTrue(isEmptyBody());
  }

  public void testNameError() throws Exception {
    parseAndRun("@component 1px {}", "@component without a valid literal as name");
    assertTrue(isEmptyBody());
  }

  public void testExtendsError1() throws Exception {
    parseAndRun("@component CSS_X 1px CSS_Y {}",
        "@component with invalid second parameter (expects 'extends')");
    assertTrue(isEmptyBody());
  }

  public void testExtendsError2() throws Exception {
    parseAndRun("@component CSS_X foo CSS_Y {}",
        "@component with invalid second parameter (expects 'extends')");
    assertTrue(isEmptyBody());
  }

  public void testParentNameError() throws Exception {
    parseAndRun("@component CSS_X extends 1px {}",
        "@component with invalid literal as parent name");
    assertTrue(isEmptyBody());
  }

  public void testInvalidParamNumError1() throws Exception {
    parseAndRun("@component CSS_X extends {}", "@component with invalid number of parameters");
    assertTrue(isEmptyBody());
  }

  public void testInvalidParamNumError2() throws Exception {
    parseAndRun("@component CSS_X extends CSS_Y CSS_Z {}",
        "@component with invalid number of parameters");
    assertTrue(isEmptyBody());
  }
}
