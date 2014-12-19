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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import com.google.common.base.Joiner;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.BackDoorNodeMutation;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.MutatingVisitController;

import junit.framework.TestCase;

/**
 *  Unit tests for {@link MergeAdjacentRulesetNodesWithSameDeclarations}.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class MergeAdjacentRulesetNodesWithSameDeclarationsTest
    extends TestCase {

  public void testRunPass() {
    MutatingVisitController visitController = createMock(
        MutatingVisitController.class);
    CssTree tree = createMock(CssTree.class);
    expect(tree.getMutatingVisitController()).andReturn(visitController)
        .anyTimes();
    replay(tree);

    MergeAdjacentRulesetNodesWithSameDeclarations pass =
        new MergeAdjacentRulesetNodesWithSameDeclarations(tree);
    visitController.startVisit(pass);
    replay(visitController);

    pass.runPass();
    verify(visitController);
  }

  public void testEnterTree() {
    CssTree tree = new CssTree((SourceCode) null);
    tree.getRulesetNodesToRemove().addRulesetNode(new CssRulesetNode());
    assertFalse(tree.getRulesetNodesToRemove().getRulesetNodes().isEmpty());

    MergeAdjacentRulesetNodesWithSameDeclarations pass =
        new MergeAdjacentRulesetNodesWithSameDeclarations(tree);
    pass.enterTree(tree.getRoot());
    assertTrue(tree.getRulesetNodesToRemove().getRulesetNodes().isEmpty());
  }

  public void testPassResult() throws Exception {
    CssTree tree = new GssParser(new SourceCode(null, lines(
      "@-moz-document url-prefix() {",
      "  foo {",
      "    padding: 6px;",
      "   }",
      "  bar {",
      "    padding: 6px;",
      "   }",
      "}",
      "foo {",
      "  padding: 5px;",
      "}",
      "bar {",
      "  padding: 5px;",
      "}"))).parse();

    assertEquals(tree.getRoot().getBody().toString(),
        "[@-moz-document[url-prefix()]{[[foo]{[padding:[6px]]}, [bar]{[padding:[6px]]}]}, " +
        "[foo]{[padding:[5px]]}, [bar]{[padding:[5px]]}]");

    MergeAdjacentRulesetNodesWithSameDeclarations pass =
        new MergeAdjacentRulesetNodesWithSameDeclarations(tree);
    pass.runPass();
    assertEquals(tree.getRoot().getBody().toString(),
        "[@-moz-document[url-prefix()]{[[foo, bar]{[padding:[6px]]}, [bar]{[padding:[6px]]}]}, " +
        "[foo, bar]{[padding:[5px]]}, [bar]{[padding:[5px]]}]");
  }

  public void testPassResult2() {
    CssPropertyNode prop1 = new CssPropertyNode("padding", null);
    CssPropertyValueNode value1 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value1, new CssNumericNode("5", "px"));
    CssDeclarationNode decl1 = new CssDeclarationNode(prop1);
    decl1.setPropertyValue(value1);

    CssPropertyNode propD = new CssPropertyNode("display", null);
    CssPropertyValueNode valueD = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(valueD, new CssNumericNode("5", "px"));
    CssDeclarationNode declD = new CssDeclarationNode(propD);
    declD.setPropertyValue(valueD);

    CssPropertyNode prop2 = new CssPropertyNode("padding", null);
    CssPropertyValueNode value2 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value2, new CssNumericNode("5", "px"));
    CssDeclarationNode decl2 = new CssDeclarationNode(prop2);
    decl2.setPropertyValue(value2);

    CssPropertyNode propD2 = new CssPropertyNode("display", null);
    CssPropertyValueNode valueD2 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(valueD2, new CssNumericNode("5", "px"));
    CssDeclarationNode declD2 = new CssDeclarationNode(propD2);
    declD2.setPropertyValue(valueD2);

    CssRulesetNode ruleset1 = new CssRulesetNode();
    CssSelectorNode sel1 = new CssSelectorNode("foo", null);
    ruleset1.addSelector(sel1);
    ruleset1.addDeclaration(decl1);
    ruleset1.addDeclaration(declD);

    CssRulesetNode ruleset2 = new CssRulesetNode();
    CssSelectorNode sel2 = new CssSelectorNode("bar", null);
    ruleset2.addSelector(sel2);
    ruleset2.addDeclaration(decl2);
    ruleset2.addDeclaration(declD2);

    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset1);
    BackDoorNodeMutation.addChildToBack(body, ruleset2);

    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);
    assertEquals(tree.getRoot().getBody().toString(),
        "[[foo]{[padding:[5px], display:[5px]]}, "
        + "[bar]{[padding:[5px], display:[5px]]}]");

    MergeAdjacentRulesetNodesWithSameDeclarations pass =
        new MergeAdjacentRulesetNodesWithSameDeclarations(tree, true);
    pass.runPass();
    // skip merging rules with display -> we expect output == input
    assertEquals(tree.getRoot().getBody().toString(),
        "[[foo]{[padding:[5px], display:[5px]]}, "
        + "[bar]{[padding:[5px], display:[5px]]}]");
  }

  public void testDoNotMergePseudoElements() throws Exception {
    CssTree tree = new GssParser(new SourceCode(null, lines(
      "foo {",
      "  padding: 5px;",
      "}",
      ".bar {",
      "  padding: 5px;",
      "}",
      "baz::-ms-clear {",
      "  padding: 5px;",
      "}",
      ".bez {",
      "  padding: 5px;",
      "}",
      "biz {",
      "  padding: 5px;",
      "}"))).parse();

    MergeAdjacentRulesetNodesWithSameDeclarations pass =
        new MergeAdjacentRulesetNodesWithSameDeclarations(tree);
    pass.runPass();
    assertEquals(
        "[[foo, .bar]{[padding:[5px]]}, [.bar]{[padding:[5px]]}, " +
        "[baz::-ms-clear]{[padding:[5px]]}, " +
        "[.bez, biz]{[padding:[5px]]}, [biz]{[padding:[5px]]}]",
        tree.getRoot().getBody().toString());
  }

  private String lines(String... lines) {
    return Joiner.on("\n").join(lines);
  }
}
