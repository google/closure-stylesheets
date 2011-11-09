/*
 * Copyright 2010 Google Inc.
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

import com.google.common.css.compiler.ast.BackDoorNodeMutation;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssHexColorNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.MutatingVisitController;

import junit.framework.TestCase;

import org.easymock.Capture;
import org.easymock.EasyMock;

import java.util.List;

/**
 */
public class AbbreviatePositionalValuesTest extends TestCase {

  public void testEnterDeclaration() {
    MutatingVisitController mockVisitController = EasyMock.createMock(
        MutatingVisitController.class);

    AbbreviatePositionalValues pass
        = new AbbreviatePositionalValues(mockVisitController);

    CssDeclarationNode declaration = new CssDeclarationNode(
        new CssPropertyNode("padding"),
        new CssPropertyValueNode());
    BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
        new CssLiteralNode("A"));
    BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
        new CssLiteralNode("A"));
    BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
        new CssLiteralNode("A"));
    BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
        new CssLiteralNode("A"));

    Capture<List<CssNode>> capturedResults = new Capture<List<CssNode>>();
    mockVisitController.replaceCurrentBlockChildWith(
        EasyMock.capture(capturedResults), EasyMock.anyBoolean());
    EasyMock.replay(mockVisitController);

    pass.enterDeclaration(declaration);

    List<CssNode> replacements = capturedResults.getValue();
    assertEquals(1, replacements.size());
    assertTrue(replacements.get(0) instanceof CssDeclarationNode);
    CssDeclarationNode replacement = (CssDeclarationNode) replacements.get(0);
    assertEquals(1, replacement.getPropertyValue().numChildren());
    assertEquals("A", replacement.getPropertyValue().getChildAt(0).getValue());
  }

  public void testEqualLiterals() {
    CssLiteralNode v1 = new CssLiteralNode("auto");
    CssLiteralNode v2 = new CssLiteralNode("auto");
    CssLiteralNode v3 = new CssLiteralNode("blah");

    assertTrue(AbbreviatePositionalValues.equalValues(v1, v2));
    assertFalse(AbbreviatePositionalValues.equalValues(v1, v3));
  }

  public void testEqualNumerics() {
    CssNumericNode v1 = new CssNumericNode("5", "%");
    CssNumericNode v2 = new CssNumericNode("5", "%");
    CssNumericNode v3 = new CssNumericNode("5", "");

    assertTrue(AbbreviatePositionalValues.equalValues(v1, v2));
    assertFalse(AbbreviatePositionalValues.equalValues(v1, v3));
  }

  public void testEqualColors() {
    CssHexColorNode v1 = new CssHexColorNode("#ccc");
    CssHexColorNode v2 = new CssHexColorNode("#ccc");
    CssHexColorNode v3 = new CssHexColorNode("#fff");

    assertTrue(AbbreviatePositionalValues.equalValues(v1, v2));
    assertFalse(AbbreviatePositionalValues.equalValues(v1, v3));
  }
}
