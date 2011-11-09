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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.BackDoorNodeMutation;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import junit.framework.TestCase;

import org.easymock.Capture;
import org.easymock.EasyMock;

import java.util.List;

/**
 * Unit tests for {@link ReplaceConstantReferences}.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class ReplaceConstantReferencesTest extends TestCase {

  public void testRunPass() {
    MutatingVisitController visitController = createMock(
        MutatingVisitController.class);
    CssTree tree = createMock(CssTree.class);
    expect(tree.getMutatingVisitController())
        .andReturn(visitController).anyTimes();
    // We need to have this replayed because in the constructor of
    // ReplaceConstantReferences pass we need tree.getMutatingVisitController().
    replay(tree);

    ReplaceConstantReferences pass =
        new ReplaceConstantReferences(tree, new ConstantDefinitions(),
            true /* removeDefs */, new DummyErrorManager(),
            true /* allowUndefinedConstants */);
    visitController.startVisit(pass);
    replay(visitController);

    pass.runPass();
    verify(visitController);
  }

  public void testEnterDefinitionNode() {
    MutatingVisitController visitController = createMock(
        MutatingVisitController.class);
    CssTree tree = createMock(CssTree.class);
    expect(tree.getMutatingVisitController())
        .andReturn(visitController).anyTimes();
    // We need to have this replayed because in the constructor of
    // ReplaceConstantReferences pass we need tree.getMutatingVisitController().
    replay(tree);

    ReplaceConstantReferences pass =
      new ReplaceConstantReferences(tree, new ConstantDefinitions(),
          true /* removeDefs */, new DummyErrorManager(),
          true /* allowUndefinedConstants */);

    visitController.removeCurrentNode();
    replay(visitController);

    CssDefinitionNode node = new CssDefinitionNode(new CssLiteralNode("COLOR"));
    pass.enterDefinition(node);
    verify(visitController);
  }

  public void testEnterValueNode() {
    CssDefinitionNode def = new CssDefinitionNode(new CssLiteralNode("COLOR"));
    def.getParameters().add(new CssLiteralNode("red"));

    CssPropertyNode prop1 = new CssPropertyNode("padding", null);
    CssPropertyValueNode value1 = new CssPropertyValueNode();
    BackDoorNodeMutation.addChildToBack(value1, new CssNumericNode("5", "px"));

    CssPropertyNode prop2 = new CssPropertyNode("color", null);
    CssPropertyValueNode value2 = new CssPropertyValueNode();
    CssConstantReferenceNode ref = new CssConstantReferenceNode("COLOR", null);
    BackDoorNodeMutation.addChildToBack(value2, ref);

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
    ConstantDefinitions constantDefinitions = new ConstantDefinitions();
    constantDefinitions.addConstantDefinition(def);

    ReplaceConstantReferences pass =
        new ReplaceConstantReferences(tree, constantDefinitions,
            true /* removeDefs */, new DummyErrorManager(),
            true /* allowUndefinedConstants */);
    pass.runPass();
    assertEquals(tree.getRoot().getBody().toString(),
        "[[foo]{[padding:[5px], color:[red]]}]");
  }

  // TODO(oana): Added a task in tracker for fixing these dependencies and
  // making the mocking of objects easier.
  public void testEnterArgumentNode() {
    CssDefinitionNode def = new CssDefinitionNode(new CssLiteralNode("COLOR"));

    MutatingVisitController visitController = createMock(
        MutatingVisitController.class);
    CssTree tree = createMock(CssTree.class);
    expect(tree.getMutatingVisitController())
        .andReturn(visitController).anyTimes();
    ConstantDefinitions definitions = EasyMock.createMock(
        ConstantDefinitions.class);
    expect(definitions.getConstantDefinition("COLOR"))
        .andReturn(def).anyTimes();
    replay(definitions);
    replay(tree);

    ReplaceConstantReferences pass =
        new ReplaceConstantReferences(tree, definitions, true /* removeDefs */,
            new DummyErrorManager(), true /* allowUndefinedConstants */);

    Capture<List<CssNode>> tempList = new Capture<List<CssNode>>();
    visitController.replaceCurrentBlockChildWith(capture(tempList), eq(true));
    replay(visitController);

    CssConstantReferenceNode node = new CssConstantReferenceNode("COLOR", null);
    pass.enterArgumentNode(node);
    verify(visitController);
    assertEquals(1, tempList.getValue().size());
    assertEquals(
        CssCompositeValueNode.class,
        tempList.getValue().get(0).getClass());
  }

  public void testAllowUndefinedConstants() {
    ConstantDefinitions definitions = EasyMock.createMock(
        ConstantDefinitions.class);
    expect(definitions.getConstantDefinition("Foo")).andStubReturn(null);

    SourceCodeLocation loc = createMock(SourceCodeLocation.class);
    CssConstantReferenceNode refNode = createMock(
        CssConstantReferenceNode.class);
    expect(refNode.getValue()).andStubReturn("Foo");
    expect(refNode.getSourceCodeLocation()).andStubReturn(loc);

    // This should not cause an error to be reported.
    ErrorManager errorManager = createMock(ErrorManager.class);
    replay(definitions, refNode, errorManager);
    ReplaceConstantReferences allowingPass =
        new ReplaceConstantReferences(createMock(CssTree.class), definitions,
            true /* removeDefs */, errorManager,
            true /* allowUndefinedConstants */);
    allowingPass.replaceConstantReference(refNode);
    verify(definitions, refNode, errorManager);
  }

  public void testAllowUndefinedConstantsError() {
    ConstantDefinitions definitions = EasyMock.createMock(
        ConstantDefinitions.class);
    expect(definitions.getConstantDefinition("Foo")).andStubReturn(null);

    SourceCodeLocation loc = createMock(SourceCodeLocation.class);
    CssConstantReferenceNode refNode = createMock(
        CssConstantReferenceNode.class);
    expect(refNode.getValue()).andStubReturn("Foo");
    expect(refNode.getSourceCodeLocation()).andStubReturn(loc);

    // This should cause an error to be reported.
    ErrorManager errorManager = createMock(ErrorManager.class);
    errorManager.report(EasyMock.<GssError>anyObject());
    replay(definitions, refNode, errorManager);

    ReplaceConstantReferences nonAllowingPass =
      new ReplaceConstantReferences(createMock(CssTree.class), definitions,
          true /* removeDefs */, errorManager,
          false /* allowUndefinedConstants */);
    nonAllowingPass.replaceConstantReference(refNode);
    verify(definitions, refNode, errorManager);
  }
}
