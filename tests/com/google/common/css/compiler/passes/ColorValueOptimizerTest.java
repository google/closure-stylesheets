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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.BackDoorNodeMutation;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssHexColorNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.MutatingVisitController;

import junit.framework.TestCase;

import java.util.List;

/**
 * Unit tests for {@link ColorValueOptimizer}.
 * 
 * @author oana@google.com (Oana Florescu)
 */
public class ColorValueOptimizerTest extends TestCase {
  
  public void testRunPass() {
    MutatingVisitController visitController = createMock(
        MutatingVisitController.class);
    
    ColorValueOptimizer pass = new ColorValueOptimizer(visitController);
    visitController.startVisit(pass);
    replay(visitController);
    
    pass.runPass();
    verify(visitController);
  }

  public void testEnterValueNode1() {
    CssPropertyNode prop = new CssPropertyNode("color", null);
    CssPropertyValueNode value = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value, new CssHexColorNode("#112233", null));

    CssDeclarationNode decl = new CssDeclarationNode(prop);
    decl.setPropertyValue(value);

    CssRulesetNode ruleset = new CssRulesetNode();
    CssSelectorNode sel = new CssSelectorNode("foo", null);
    ruleset.addSelector(sel);
    ruleset.addDeclaration(decl);

    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset);

    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);

    ColorValueOptimizer pass = new ColorValueOptimizer(
        tree.getMutatingVisitController());
    pass.runPass();
    assertEquals(tree.getRoot().getBody().toString(),
        "[[foo]{[color:[#123]]}]");
  }

  public void testEnterValueNode2() {
    CssPropertyNode prop = new CssPropertyNode("color", null);
    CssPropertyValueNode value = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value, new CssHexColorNode("#123344", null));

    CssDeclarationNode decl = new CssDeclarationNode(prop);
    decl.setPropertyValue(value);

    CssRulesetNode ruleset = new CssRulesetNode();
    CssSelectorNode sel = new CssSelectorNode("foo", null);
    ruleset.addSelector(sel);
    ruleset.addDeclaration(decl);

    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset);

    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);

    ColorValueOptimizer pass = new ColorValueOptimizer(
        tree.getMutatingVisitController());
    pass.runPass();
    assertEquals(tree.getRoot().getBody().toString(),
        "[[foo]{[color:[#123344]]}]");
  }

  private CssNumericNode createNumericNode(String value) {
    if (value.endsWith("%")) {
      return new CssNumericNode(value.substring(0, value.length() - 1), "%");
    }
    return new CssNumericNode(value, "");
  }

  private CssFunctionNode createRgbFunctionNode(String r, String g, String b)  {
    CssFunctionNode function = new CssFunctionNode(
        CssFunctionNode.Function.byName("rgb"), null /* sourceCodeLocation */);
    List<CssValueNode> values = Lists.newArrayList();
    values.add(createNumericNode(r));
    values.add(createNumericNode(g));
    values.add(createNumericNode(b));
    CssFunctionArgumentsNode args = new CssFunctionArgumentsNode(values);
    function.setArguments(args);
    return function;
  }

  public void testEnterFunctionNode1() {
    CssPropertyNode prop = new CssPropertyNode("color", null);
    CssPropertyValueNode value = new CssPropertyValueNode();
    CssFunctionNode function = createRgbFunctionNode("0%", "100%", "0%");
    BackDoorNodeMutation.addChildToBack(value, function);

    CssDeclarationNode decl = new CssDeclarationNode(prop);
    decl.setPropertyValue(value);

    CssRulesetNode ruleset = new CssRulesetNode();
    CssSelectorNode sel = new CssSelectorNode("foo", null);
    ruleset.addSelector(sel);
    ruleset.addDeclaration(decl);

    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset);

    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);

    ColorValueOptimizer pass = new ColorValueOptimizer(
        tree.getMutatingVisitController());
    pass.runPass();
    assertEquals(tree.getRoot().getBody().toString(),
        "[[foo]{[color:[#0f0]]}]");
  }

  public void testCanShortenHex() {
    assertTrue(ColorValueOptimizer.canShortenHexString("#000000"));
    assertTrue(ColorValueOptimizer.canShortenHexString("#00aa22"));
    assertFalse(ColorValueOptimizer.canShortenHexString("#000001"));
    assertFalse(ColorValueOptimizer.canShortenHexString("#000"));
  }

  public void testShortenHex() {
    assertEquals("#000", ColorValueOptimizer.shortenHexString("#000000"));
    assertEquals("#0a2", ColorValueOptimizer.shortenHexString("#00aa22"));
  }

  public void testParseRgbArguments() {
    CssFunctionNode function = createRgbFunctionNode("0", "15", "255");
    assertEquals("#000fff", ColorValueOptimizer.parseRgbArguments(function));
    function = createRgbFunctionNode("0%", "50%", "100%");
    assertEquals("#0080ff", ColorValueOptimizer.parseRgbArguments(function));
  }

  public void testParseRgbArgumentsOutOfRange() {
    // Surprisingly, these are valid according to W3C and should be clamped
    // to valid values.
    CssFunctionNode function = createRgbFunctionNode("1", "1", "300");
    assertEquals("#0101ff", ColorValueOptimizer.parseRgbArguments(function));
    function = createRgbFunctionNode("-10", "1", "1");
    assertEquals("#000101", ColorValueOptimizer.parseRgbArguments(function));
  }

  public void testParseRgbArgumentsBadArgs() {
    CssFunctionNode function = new CssFunctionNode(
        CssFunctionNode.Function.byName("rgb"), null /* sourceCodeLocation */);
    List<CssValueNode> values = Lists.newArrayList();
    values.add(createNumericNode("0"));
    CssFunctionArgumentsNode args = new CssFunctionArgumentsNode(values);
    function.setArguments(args);

    try {
      ColorValueOptimizer.parseRgbArguments(function);
      fail("Too few arguments to rgb function; should have thrown.");
    } catch (NumberFormatException expected) {
      // Exception is expected.
    }
  }
}
