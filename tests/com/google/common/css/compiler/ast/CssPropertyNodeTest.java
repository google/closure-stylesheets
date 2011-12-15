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

import com.google.common.collect.ImmutableSet;
import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;

import junit.framework.TestCase;

/**
 * Unit tests for {@link CssPropertyNode}.
 * 
 * @author oana@google.com (Oana Florescu)
 */
public class CssPropertyNodeTest extends TestCase {

  public void testPropertyNodeCreation1() {
    CssPropertyNode property = new CssPropertyNode("color", null);

    assertNull(property.getParent());
    assertNull(property.getSourceCodeLocation());
    assertEquals("color", property.toString());
    assertFalse(property.getProperty().hasPositionalParameters());
  }

  public void testPropertyNodeCreation2() {
    CssPropertyNode property = new CssPropertyNode("cOloR", null);

    assertNull(property.getParent());
    assertNull(property.getSourceCodeLocation());
    assertEquals("color", property.toString());
    assertFalse(property.getProperty().hasPositionalParameters());
 }

  public void testPropertyNodeCreation3() {
    SourceCodeLocation codeLoc = new SourceCodeLocation(
        new SourceCode("file.css", null), 1, 1, 1, 1, 1, 1);
    CssPropertyNode property = new CssPropertyNode("color", codeLoc);

    assertNull(property.getParent());
    assertEquals(codeLoc, property.getSourceCodeLocation());
    assertEquals("color", property.toString());
    assertFalse(property.getProperty().hasPositionalParameters());
  }

  public void testPropertyNodePositionDependentValues() {
    CssPropertyNode borderColor = new CssPropertyNode("border-color", null);
    CssPropertyNode borderStyle = new CssPropertyNode("border-style", null);
    CssPropertyNode borderWidth = new CssPropertyNode("border-width", null);
    CssPropertyNode margin = new CssPropertyNode("margin", null);
    CssPropertyNode padding = new CssPropertyNode("padding", null);

    assertEquals("border-color", borderColor.toString());
    assertTrue(borderColor.getProperty().hasPositionalParameters());

    assertEquals("border-style", borderStyle.toString());
    assertTrue(borderStyle.getProperty().hasPositionalParameters());

    assertEquals("border-width", borderWidth.toString());
    assertTrue(borderWidth.getProperty().hasPositionalParameters());

    assertEquals("margin", margin.toString());
    assertTrue(margin.getProperty().hasPositionalParameters());

    assertEquals("padding", padding.toString());
    assertTrue(padding.getProperty().hasPositionalParameters());
  }

  public void testPropertyNodeShorthands() {
    assertEquals(ImmutableSet.<String>of(),
            new CssPropertyNode("foo").getProperty().getShorthands());

    assertEquals(ImmutableSet.<String>of(),
        new CssPropertyNode("color").getProperty().getShorthands());

    assertEquals(ImmutableSet.<String>of(),
        new CssPropertyNode("caption-side").getProperty().getShorthands());

    assertEquals(ImmutableSet.<String>of(),
        new CssPropertyNode("border-collapse").getProperty().getShorthands());

    assertEquals(ImmutableSet.of("background"),
        new CssPropertyNode("background-color").getProperty().getShorthands());

    assertEquals(ImmutableSet.of("border"),
        new CssPropertyNode("border-color").getProperty().getShorthands());

    assertEquals(ImmutableSet.of("list-style"),
        new CssPropertyNode("list-style-type").getProperty().getShorthands());

    assertEquals(ImmutableSet.of("border", "border-left", "border-style"),
        new CssPropertyNode("border-left-style").getProperty().getShorthands());       
  }

  public void testPropertyNodePartition() {
    assertEquals("foo", new CssPropertyNode("foo").getPartition());

    assertEquals("color", new CssPropertyNode("color").getPartition());

    assertEquals("caption-side",
        new CssPropertyNode("caption-side").getPartition());

    assertEquals("border-collapse",
        new CssPropertyNode("border-collapse").getPartition());

    assertEquals("background",
        new CssPropertyNode("background-color").getPartition());

    assertEquals("border", new CssPropertyNode("border-color").getPartition());

    assertEquals("list-style",
        new CssPropertyNode("list-style-type").getPartition());

    assertEquals("border",
        new CssPropertyNode("border-left-style").getPartition());
  }

  public void testPropertyNodeCopy() {
    CssPropertyNode property = new CssPropertyNode("color", null);
    CssPropertyNode propertyCopy = new CssPropertyNode(property);
    CssPropertyNode property1 = new CssPropertyNode("border-color", null);
    CssPropertyNode property1Copy = new CssPropertyNode(property1);

    assertNull(property.getParent());
    assertNull(propertyCopy.getParent());
    assertNull(property1.getParent());
    assertNull(property1Copy.getParent());

    assertNull(property.getSourceCodeLocation());
    assertNull(propertyCopy.getSourceCodeLocation());
    assertNull(property1.getSourceCodeLocation());
    assertNull(property1Copy.getSourceCodeLocation());

    assertEquals("color", property.toString());
    assertEquals("color", propertyCopy.toString());
    assertEquals("border-color", property1.toString());
    assertEquals("border-color", property1Copy.toString());

    assertTrue(property.getProperty().isRecognizedProperty());
    assertTrue(propertyCopy.getProperty().isRecognizedProperty());
    assertTrue(property1.getProperty().isRecognizedProperty());
    assertTrue(property1Copy.getProperty().isRecognizedProperty());

    assertFalse(property.getProperty().hasPositionalParameters());
    assertFalse(propertyCopy.getProperty().hasPositionalParameters());
    assertTrue(property1.getProperty().hasPositionalParameters());
    assertTrue(property1Copy.getProperty().hasPositionalParameters());
  }

}
