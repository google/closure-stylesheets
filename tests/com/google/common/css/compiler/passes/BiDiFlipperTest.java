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

import com.google.common.css.compiler.ast.BackDoorNodeMutation;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssFunctionNode.Function;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.MutatingVisitController;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * Unit tests for {@link BiDiFlipper}.
 *
 */
public class BiDiFlipperTest extends TestCase {

  public void testRunPass() {
    IMocksControl controller = EasyMock.createStrictControl();
    MutatingVisitController visitController = controller.createMock(
        MutatingVisitController.class);

    BiDiFlipper pass = new BiDiFlipper(visitController, true, true);
    visitController.startVisit(pass);
    controller.replay();

    pass.runPass();
    controller.verify();
  }

  // Test when the node is set to be non-flippable.
  public void testEnterDeclaration1() {
    IMocksControl controller = EasyMock.createStrictControl();
    MutatingVisitController visitController = controller.createMock(
        MutatingVisitController.class);
    CssDeclarationNode node = new CssDeclarationNode(new CssPropertyNode("foo"));
    node.setShouldBeFlipped(false);

    controller.replay();

    // Perform action.
    BiDiFlipper pass
        = new BiDiFlipper(visitController, true, true);
    pass.enterDeclaration(node);

    // Verify.
    controller.verify();
  }

  // Test when the node is set to be flippable.
  public void testEnterDeclaration2() {
    // padding: 5px 1px 2px 3px;
    CssPropertyNode prop1 = new CssPropertyNode("padding", null);
    CssPropertyValueNode value1 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value1, new CssNumericNode("5", "px"));
    BackDoorNodeMutation.addChildToBack(value1, new CssNumericNode("1", "px"));
    BackDoorNodeMutation.addChildToBack(value1, new CssNumericNode("2", "px"));
    BackDoorNodeMutation.addChildToBack(value1, new CssNumericNode("3", "px"));

    // font: 90%;
    CssPropertyNode prop2 = new CssPropertyNode("font", null);
    CssPropertyValueNode value2 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value2, new CssNumericNode("90", "%"));

    // background-position-x: 80%;
    CssPropertyNode prop3 = new CssPropertyNode("background-position-x", null);
    CssPropertyValueNode value3 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value3, new CssNumericNode("80", "%"));

    // background: url("/foo/rtl/background.png");
    CssPropertyNode prop4 = new CssPropertyNode("background", null);
    CssPropertyValueNode value4 = new CssPropertyValueNode();
    CssFunctionNode functionNode = new CssFunctionNode(Function.byName("url"),
        null);
    CssFunctionArgumentsNode argsNode = new CssFunctionArgumentsNode();
    BackDoorNodeMutation.addChildToBack(argsNode, new CssLiteralNode("/foo/rtl/background.png"));
    functionNode.setArguments(argsNode);
    BackDoorNodeMutation.addChildToBack(value4, functionNode);

    // margin-left: 5px
    CssPropertyNode prop5 = new CssPropertyNode("margin-left", null);
    CssPropertyValueNode value5 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value5, new CssNumericNode("5", "px"));

    // float: left
    CssPropertyNode prop6 = new CssPropertyNode("float", null);
    CssPropertyValueNode value6 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value6, new CssLiteralNode("left"));

    // left: 5px
    CssPropertyNode prop7 = new CssPropertyNode("left", null);
    CssPropertyValueNode value7 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value7, new CssNumericNode("5", "px"));

    // cursor: e-resize
    CssPropertyNode prop8 = new CssPropertyNode("cursor", null);
    CssPropertyValueNode value8 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value8, new CssLiteralNode("e-resize"));

    // border-top-left-radius: 3px
    CssPropertyNode prop9 = new CssPropertyNode("border-top-left-radius", null);
    CssPropertyValueNode value9 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value9, new CssNumericNode("3", "px"));

    // -moz-border-radius-topleft: 3px
    CssPropertyNode prop10 = new CssPropertyNode("-moz-border-radius-topleft", null);
    CssPropertyValueNode value10 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value10, new CssNumericNode("3", "px"));

    CssDeclarationNode decl1 = new CssDeclarationNode(prop1);
    decl1.setPropertyValue(value1);
    CssDeclarationNode decl2 = new CssDeclarationNode(prop2);
    decl2.setPropertyValue(value2);
    CssDeclarationNode decl3 = new CssDeclarationNode(prop3);
    decl3.setPropertyValue(value3);
    CssDeclarationNode decl4 = new CssDeclarationNode(prop4);
    decl4.setPropertyValue(value4);
    CssDeclarationNode decl5 = new CssDeclarationNode(prop5);
    decl5.setPropertyValue(value5);
    CssDeclarationNode decl6 = new CssDeclarationNode(prop6);
    decl6.setPropertyValue(value6);
    CssDeclarationNode decl7 = new CssDeclarationNode(prop7);
    decl7.setPropertyValue(value7);
    CssDeclarationNode decl8 = new CssDeclarationNode(prop8);
    decl8.setPropertyValue(value8);
    CssDeclarationNode decl9 = new CssDeclarationNode(prop9);
    decl9.setPropertyValue(value9);
    CssDeclarationNode decl10 = new CssDeclarationNode(prop10);
    decl10.setPropertyValue(value10);

    CssRulesetNode ruleset = new CssRulesetNode();
    CssSelectorNode sel = new CssSelectorNode("foo", null);
    ruleset.addSelector(sel);
    ruleset.addDeclaration(decl1);
    ruleset.addDeclaration(decl2);
    ruleset.addDeclaration(decl3);
    ruleset.addDeclaration(decl4);
    ruleset.addDeclaration(decl5);
    ruleset.addDeclaration(decl6);
    ruleset.addDeclaration(decl7);
    ruleset.addDeclaration(decl8);
    ruleset.addDeclaration(decl9);
    ruleset.addDeclaration(decl10);

    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset);

    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);

    BiDiFlipper pass = new BiDiFlipper(tree.getMutatingVisitController(),
                                       true, true);
    pass.runPass();
    assertEquals(tree.getRoot().getBody().toString(),
        "[[foo]{["
        + "padding:[5px, 3px, 2px, 1px], "
        + "font:[90%], "
        + "background-position-x:[20%], "
        + "background:[url(/foo/ltr/background.png)], "
        + "margin-right:[5px], "
        + "float:[right], "
        + "right:[5px], "
        + "cursor:[w-resize], "
        + "border-top-right-radius:[3px], "
        + "-moz-border-radius-topright:[3px]"
        + "]}]");
  }

  public void testSubPercentValues() {
    // background-position-x: 1.12345678%;
    CssPropertyNode prop1 = new CssPropertyNode("background-position-x", null);
    CssPropertyValueNode value1 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value1, new CssNumericNode("1.12345678", "%"));

    // -ms-background-position-x: 2.5%;
    CssPropertyNode prop2 = new CssPropertyNode("-ms-background-position-x", null);
    CssPropertyValueNode value2 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value2, new CssNumericNode("2.5", "%"));

    CssDeclarationNode decl1 = new CssDeclarationNode(prop1);
    decl1.setPropertyValue(value1);
    CssDeclarationNode decl2 = new CssDeclarationNode(prop2);
    decl2.setPropertyValue(value2);

    CssRulesetNode ruleset = new CssRulesetNode();
    CssSelectorNode sel = new CssSelectorNode("foo", null);
    ruleset.addSelector(sel);
    ruleset.addDeclaration(decl1);
    ruleset.addDeclaration(decl2);

    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset);

    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);

    BiDiFlipper pass = new BiDiFlipper(tree.getMutatingVisitController(),
                                       true, true);
    pass.runPass();
    assertEquals(tree.getRoot().getBody().toString(),
        "[[foo]{["
        + "background-position-x:[98.87654322%], "
        + "-ms-background-position-x:[97.5%]"
        + "]}]");
  }
}
