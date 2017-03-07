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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.CssAttributeSelectorNode.MatchType;
import com.google.common.css.compiler.ast.CssCompositeValueNode.Operator;
import com.google.common.css.compiler.ast.DefaultVisitController.RootVisitAfterChildrenState;
import com.google.common.css.compiler.ast.DefaultVisitController.RootVisitBeforeChildrenState;
import com.google.common.css.compiler.ast.DefaultVisitController.RootVisitBodyState;
import com.google.common.css.compiler.ast.DefaultVisitController.RootVisitCharsetState;
import com.google.common.css.compiler.ast.DefaultVisitController.RootVisitImportBlockState;
import com.google.common.css.compiler.ast.DefaultVisitController.VisitBlockChildrenState;
import com.google.common.css.compiler.ast.DefaultVisitController.VisitDefinitionState;
import com.google.common.css.compiler.ast.DefaultVisitController.VisitImportBlockChildrenState;
import com.google.common.css.compiler.ast.DefaultVisitController.VisitImportRuleState;
import com.google.common.css.compiler.ast.DefaultVisitController.VisitReplaceChildrenState;
import java.util.List;
import junit.framework.TestCase;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Unit tests for {@link DefaultVisitController}.
 *
 * TODO(oana): Add more unit tests.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class DefaultVisitControllerTest extends TestCase {

  DefaultTreeVisitor testVisitor = mock(DefaultTreeVisitor.class);

  @Override
  protected void setUp() throws Exception {
    when(testVisitor.enterTree(Matchers.<CssRootNode>any())).thenReturn(true);
    when(testVisitor.enterImportBlock(Matchers.<CssImportBlockNode>any())).thenReturn(true);
    when(testVisitor.enterBlock(Matchers.<CssBlockNode>any())).thenReturn(true);
    when(testVisitor.enterDefinition(Matchers.<CssDefinitionNode>any())).thenReturn(true);
    when(testVisitor.enterRuleset(Matchers.<CssRulesetNode>any())).thenReturn(true);
    when(testVisitor.enterSelectorBlock(Matchers.<CssSelectorListNode>any())).thenReturn(true);
    when(testVisitor.enterSelector(Matchers.<CssSelectorNode>any())).thenReturn(true);
    when(testVisitor.enterClassSelector(Matchers.<CssClassSelectorNode>any())).thenReturn(true);
    when(testVisitor.enterIdSelector(Matchers.<CssIdSelectorNode>any())).thenReturn(true);
    when(testVisitor.enterPseudoClass(Matchers.<CssPseudoClassNode>any())).thenReturn(true);
    when(testVisitor.enterPseudoElement(Matchers.<CssPseudoElementNode>any())).thenReturn(true);
    when(testVisitor.enterAttributeSelector(Matchers.<CssAttributeSelectorNode>any()))
        .thenReturn(true);
    when(testVisitor.enterDeclarationBlock(Matchers.<CssDeclarationBlockNode>any()))
        .thenReturn(true);
    when(testVisitor.enterDeclaration(Matchers.<CssDeclarationNode>any())).thenReturn(true);
    when(testVisitor.enterPropertyValue(Matchers.<CssPropertyValueNode>any())).thenReturn(true);
    when(testVisitor.enterValueNode(Matchers.<CssValueNode>any())).thenReturn(true);
    when(testVisitor.enterUnknownAtRule(Matchers.<CssUnknownAtRuleNode>any())).thenReturn(true);
    when(testVisitor.enterMediaTypeListDelimiter(
            Matchers.<CssNodesListNode<? extends CssNode>>any()))
        .thenReturn(true);
    when(testVisitor.enterForLoop(Matchers.<CssForLoopRuleNode>any())).thenReturn(true);
    when(testVisitor.enterComponent(Matchers.<CssComponentNode>any())).thenReturn(true);
  }

  public void testConstructor() {
    DefaultVisitController visitController = new DefaultVisitController(
        new CssTree((SourceCode) null), false);

    assertTrue(visitController.getStateStack().isEmpty());
  }

  public void testVisitBlock() {
    CssLiteralNode literal = new CssLiteralNode("");
    CssDefinitionNode def = new CssDefinitionNode(literal);
    CssBlockNode block = new CssBlockNode(false);
    block.addChildToBack(def);
    CssRootNode root = new CssRootNode(block);
    CssTree tree = new CssTree(null, root);

    DefaultVisitController controller = new DefaultVisitController(tree, true);
    controller.startVisit(testVisitor);

    InOrder inOrder = Mockito.inOrder(testVisitor);

    // Enter Tree gets the root - there is no enterRoot.
    inOrder.verify(testVisitor).enterTree(root);
    // There are blocks that get created even if you don't add them.
    inOrder.verify(testVisitor).enterImportBlock(Matchers.<CssImportBlockNode>any());
    // Then we enter the block.
    inOrder.verify(testVisitor).enterBlock(block);
    // Then another node that we created.
    inOrder.verify(testVisitor).enterDefinition(def);
  }

  public void testVisitProperties() {

    CssValueNode first = new CssLiteralNode("one", null);
    CssValueNode second = new CssLiteralNode("two", null);
    CssValueNode third = new CssLiteralNode("three", null);
    CssPropertyNode propName = new CssPropertyNode("prop");

    CssPropertyValueNode propValue = new CssPropertyValueNode();
    propValue.addChildToBack(first);
    propValue.addChildToBack(second);
    propValue.addChildToBack(third);

    CssDeclarationNode decl = new CssDeclarationNode(propName, propValue);
    CssDeclarationBlockNode dBlock = new CssDeclarationBlockNode();
    dBlock.addChildToBack(decl);

    CssClassSelectorNode classSelector = new CssClassSelectorNode("foo", null);
    CssIdSelectorNode idSelector = new CssIdSelectorNode("bar", null);
    CssPseudoClassNode pseudoClass = new CssPseudoClassNode("foo", null);
    CssPseudoElementNode pseudoElement = new CssPseudoElementNode("bar", null);
    CssAttributeSelectorNode attrSelector = new CssAttributeSelectorNode(
        MatchType.EXACT, "hreflang",
        new CssStringNode(
            CssStringNode.Type.DOUBLE_QUOTED_STRING, "en"), null);

    CssSelectorNode selector = new CssSelectorNode("name", null);
    selector.getRefiners().addChildToBack(classSelector);
    selector.getRefiners().addChildToBack(idSelector);
    selector.getRefiners().addChildToBack(pseudoClass);
    selector.getRefiners().addChildToBack(pseudoElement);
    selector.getRefiners().addChildToBack(attrSelector);

    CssRulesetNode ruleset = new CssRulesetNode(dBlock);
    ruleset.addSelector(selector);

    CssBlockNode block = new CssBlockNode(false);
    block.addChildToBack(ruleset);

    CssRootNode root = new CssRootNode(block);
    CssTree tree = new CssTree(null, root);

    DefaultVisitController controller = new DefaultVisitController(tree, true);
    controller.startVisit(testVisitor);

    InOrder inOrder = Mockito.inOrder(testVisitor);

    inOrder.verify(testVisitor).enterTree(root);
    inOrder.verify(testVisitor).enterImportBlock(Matchers.<CssImportBlockNode>any());
    inOrder.verify(testVisitor).enterBlock(block);
    inOrder.verify(testVisitor).enterRuleset(ruleset);
    inOrder.verify(testVisitor).enterSelectorBlock(Matchers.<CssSelectorListNode>any());
    inOrder.verify(testVisitor).enterSelector(selector);
    inOrder.verify(testVisitor).enterClassSelector(classSelector);
    inOrder.verify(testVisitor).enterIdSelector(idSelector);
    inOrder.verify(testVisitor).enterPseudoClass(pseudoClass);
    inOrder.verify(testVisitor).enterPseudoElement(pseudoElement);
    inOrder.verify(testVisitor).enterAttributeSelector(attrSelector);
    inOrder.verify(testVisitor).enterDeclarationBlock(dBlock);
    inOrder.verify(testVisitor).enterDeclaration(decl);
    inOrder.verify(testVisitor).enterPropertyValue(propValue);
    inOrder.verify(testVisitor).enterValueNode(first);
    inOrder.verify(testVisitor).enterValueNode(second);
    inOrder.verify(testVisitor).enterValueNode(third);
  }

  public void testStateStack() {
    CssTree tree = new CssTree((SourceCode) null);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, false);

    RootVisitBeforeChildrenState state
        = visitController.new RootVisitBeforeChildrenState(tree.getRoot());
    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());
    assertEquals(1, visitController.getStateStack().size());

    visitController.getStateStack().pop();
    assertTrue(visitController.getStateStack().isEmpty());
  }

  public void testRootVisitBeforeChildrenState() {
    CssTree tree = new CssTree((SourceCode) null);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, false);
    RootVisitBeforeChildrenState state
        = visitController.new RootVisitBeforeChildrenState(tree.getRoot());

    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());

    state.transitionToNextState();
    assertEquals(1, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof RootVisitCharsetState);
  }

  public void testRootVisitCharsetState() {
    CssTree tree = new CssTree((SourceCode) null);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, true);
    RootVisitCharsetState state =
        visitController.new RootVisitCharsetState(tree.getRoot(), tree.getRoot().getCharsetRule());

    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());

    visitController.removeCurrentNode();
    assertNull(tree.getRoot().getCharsetRule());

    state.transitionToNextState();
    assertEquals(1, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof RootVisitImportBlockState);
  }

  public void testRootVisitImportBlockState() {
    CssTree tree = new CssTree((SourceCode) null);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, true);
    RootVisitImportBlockState state =
        visitController
        .new RootVisitImportBlockState(tree.getRoot(), tree.getRoot().getImportRules());

    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());

    state.transitionToNextState();
    assertEquals(2, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof VisitImportBlockChildrenState);

    state.transitionToNextState();
    assertEquals(2, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof RootVisitBodyState);
  }

  public void testVisitImportBlockChildrenState() {
    CssTree tree = new CssTree((SourceCode) null);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, true);
    visitController.visitor = new DefaultTreeVisitor();
    CssImportBlockNode cssImportBlockNode = new CssImportBlockNode();
    cssImportBlockNode.setChildren(Lists.newArrayList(new CssImportRuleNode()));
    VisitImportBlockChildrenState state
        = visitController.new VisitImportBlockChildrenState(cssImportBlockNode);

    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());

    state.transitionToNextState();
    assertEquals(2, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof VisitImportRuleState);

    visitController.getStateStack().getTop().transitionToNextState();
    assertEquals(1, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof VisitImportBlockChildrenState);

    state.transitionToNextState();
    assertTrue(visitController.getStateStack().isEmpty());
  }

  public void testVisitImportRuleState() {
    CssTree tree = new CssTree((SourceCode) null);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, true);
    visitController.visitor = new DefaultTreeVisitor();
    VisitImportRuleState state
        = visitController.new VisitImportRuleState(new CssImportRuleNode());

    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());

    state.transitionToNextState();
    assertTrue(visitController.getStateStack().isEmpty());
  }

  public void testRootVisitBodyState() {
    CssTree tree = new CssTree((SourceCode) null);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, true);
    visitController.visitor = new DefaultTreeVisitor();
    RootVisitBodyState state =
        visitController.new RootVisitBodyState(tree.getRoot(), tree.getRoot().getBody());

    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());

    state.transitionToNextState();
    assertEquals(2, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof VisitBlockChildrenState);

    visitController.getStateStack().getTop().transitionToNextState();
    assertEquals(1, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof RootVisitBodyState);

    state.transitionToNextState();
    assertEquals(1, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof RootVisitAfterChildrenState);
  }

  public void testVisitBlockChildrenState1() {
    CssDefinitionNode def = new CssDefinitionNode(new CssLiteralNode(""));
    CssBlockNode block = new CssBlockNode(false);
    block.addChildToBack(def);

    CssRootNode root = new CssRootNode(block);
    CssTree tree = new CssTree(null, root);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, true);
    visitController.visitor = new DefaultTreeVisitor();
    VisitReplaceChildrenState<CssNode> state
        = visitController.new VisitBlockChildrenState(tree.getRoot().getBody());

    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());
    assertEquals(-1, state.currentIndex);

    state.transitionToNextState();
    assertEquals(2, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof VisitDefinitionState);
    assertEquals(0, state.currentIndex);

    visitController.getStateStack().getTop().transitionToNextState();
    assertEquals(3, visitController.getStateStack().size());

    state.transitionToNextState();
    state.transitionToNextState();
    state.transitionToNextState();
    assertTrue(visitController.getStateStack().isEmpty());
  }

  public void testVisitBlockChildrenState2() {
    CssDefinitionNode def = new CssDefinitionNode(new CssLiteralNode(""));
    CssBlockNode block = new CssBlockNode(false);
    block.addChildToBack(def);
    def = new CssDefinitionNode(new CssLiteralNode(""));
    block.addChildToBack(def);

    CssRootNode root = new CssRootNode(block);
    CssTree tree = new CssTree(null, root);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, true);
    visitController.visitor = new DefaultTreeVisitor();
    VisitReplaceChildrenState<CssNode> state
        = visitController.new VisitBlockChildrenState(tree.getRoot().getBody());

    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());
    assertEquals(-1, state.currentIndex);

    state.transitionToNextState();
    assertEquals(2, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof VisitDefinitionState);
    assertEquals(0, state.currentIndex);

    state.removeCurrentChild();
    assertEquals(0, state.currentIndex);

    state.removeCurrentChild();
    assertEquals(0, state.currentIndex);

    visitController.getStateStack().getTop().transitionToNextState();
    assertEquals(3, visitController.getStateStack().size());

    state.transitionToNextState();
    state.transitionToNextState();
    state.transitionToNextState();
    assertTrue(visitController.getStateStack().isEmpty());
    assertEquals(0, state.currentIndex);
  }

  public void testVisitBlockChildrenState3() {
    CssDefinitionNode def = new CssDefinitionNode(new CssLiteralNode(""));
    CssBlockNode block = new CssBlockNode(false);
    block.addChildToBack(def);
    def = new CssDefinitionNode(new CssLiteralNode(""));
    block.addChildToBack(def);

    CssRootNode root = new CssRootNode(block);
    CssTree tree = new CssTree(null, root);
    DefaultVisitController visitController = new DefaultVisitController(
        tree, true);
    visitController.visitor = new DefaultTreeVisitor();
    VisitReplaceChildrenState<CssNode> state
        = visitController.new VisitBlockChildrenState(tree.getRoot().getBody());

    visitController.getStateStack().push(state);
    assertEquals(state, visitController.getStateStack().getTop());
    assertEquals(-1, state.currentIndex);

    state.transitionToNextState();
    assertEquals(2, visitController.getStateStack().size());
    assertTrue(visitController.getStateStack()
        .getTop() instanceof VisitDefinitionState);
    assertEquals(0, state.currentIndex);

    state.replaceCurrentBlockChildWith(
        Lists.<CssNode>newArrayList(
            new CssDefinitionNode(new CssLiteralNode("")),
            new CssDefinitionNode(new CssLiteralNode(""))),
        true);
    assertEquals(0, state.currentIndex);

    state.removeCurrentChild();
    assertEquals(0, state.currentIndex);

    visitController.getStateStack().getTop().transitionToNextState();
    assertEquals(3, visitController.getStateStack().size());
  }

  public void testVisitSimpleUnknownAtRule() {
    CssLiteralNode defLit = new CssLiteralNode("def");
    CssUnknownAtRuleNode atDef = new CssUnknownAtRuleNode(defLit, false);
    CssLiteralNode xLit = new CssLiteralNode("x");
    CssLiteralNode yLit = new CssLiteralNode("y");
    List<CssValueNode> defParameters = Lists.newArrayList((CssValueNode) xLit, (CssValueNode) yLit);
    atDef.setParameters(defParameters);
    CssBlockNode block = new CssBlockNode(false);
    block.addChildToBack(atDef);
    CssRootNode root = new CssRootNode(block);
    CssTree tree = new CssTree(null, root);

    DefaultVisitController controller = new DefaultVisitController(tree, true);
    controller.startVisit(testVisitor);

    InOrder inOrder = Mockito.inOrder(testVisitor);

    // Enter Tree gets the root - there is no enterRoot.
    inOrder.verify(testVisitor).enterTree(root);
    // There are blocks that get created even if you don't add them.
    inOrder.verify(testVisitor).enterImportBlock(Matchers.<CssImportBlockNode>any());
    // Then we enter the block.
    verify(testVisitor).enterBlock(block);
    // Then we enter the unknown at-rule node that we created.
    verify(testVisitor).enterUnknownAtRule(atDef);
    // Then the media type list params
    for (int i = 0; i < defParameters.size(); ++i) {
      inOrder.verify(testVisitor).enterValueNode(defParameters.get(i));
      if (i < defParameters.size() - 1) {
        inOrder.verify(testVisitor).enterMediaTypeListDelimiter(atDef);
      }
    }
    // We've got no block associated with this at rule.
  }

  public void testVisitComplexUnknownAtRule() {
    CssLiteralNode defLit = new CssLiteralNode("def");
    CssUnknownAtRuleNode atDef = new CssUnknownAtRuleNode(defLit, false);
    CssLiteralNode xLit = new CssLiteralNode("x");
    CssLiteralNode yLit = new CssLiteralNode("y");
    List<CssValueNode> defParameters = Lists.newArrayList((CssValueNode) xLit, (CssValueNode) yLit);
    atDef.setParameters(defParameters);
    CssBlockNode defBlock = new CssBlockNode(true);
    defBlock.addChildToBack(atDef);

    CssLiteralNode ifLit = new CssLiteralNode("if");
    CssUnknownAtRuleNode atIf = new CssUnknownAtRuleNode(ifLit, true);
    CssBooleanExpressionNode ifCondition = new CssBooleanExpressionNode(
        CssBooleanExpressionNode.Type.CONSTANT, "some condition", null, null);
    List<CssValueNode> ifParameters = Lists.newArrayList((CssValueNode) ifCondition);
    atIf.setParameters(ifParameters);
    atIf.setBlock(defBlock);

    CssBlockNode block = new CssBlockNode(false);
    block.addChildToBack(atIf);
    CssRootNode root = new CssRootNode(block);
    CssTree tree = new CssTree(null, root);

    DefaultVisitController controller = new DefaultVisitController(tree, true);
    controller.startVisit(testVisitor);

    InOrder inOrder = Mockito.inOrder(testVisitor);

    // Enter Tree gets the root - there is no enterRoot.
    inOrder.verify(testVisitor).enterTree(root);
    // There are blocks that get created even if you don't add them.
    inOrder.verify(testVisitor).enterImportBlock(Matchers.<CssImportBlockNode>any());
    // Then we enter the block.
    verify(testVisitor).enterBlock(block);
    // Then we enter the unknown 'if' at-rule node that we created.
    verify(testVisitor).enterUnknownAtRule(atIf);

    // Then the media type list params for 'if'
    for (int i = 0; i < ifParameters.size(); ++i) {
      inOrder.verify(testVisitor).enterValueNode(ifParameters.get(i));
      if (i < ifParameters.size() - 1) {
        inOrder.verify(testVisitor).enterMediaTypeListDelimiter(atIf);
      }
    }

    // Then we enter the defBlock.
    inOrder.verify(testVisitor).enterBlock(defBlock);

    // Then we enter the unknown 'def' at-rule node within the 'if'.
    inOrder.verify(testVisitor).enterUnknownAtRule(atDef);

    // Then the media type list params for 'def'
    for (int i = 0; i < defParameters.size(); ++i) {
      inOrder.verify(testVisitor).enterValueNode(defParameters.get(i));
      if (i < defParameters.size() - 1) {
        inOrder.verify(testVisitor).enterMediaTypeListDelimiter(atDef);
      }
    }
  }

  public void testVisitComponent() {
    CssLiteralNode x = new CssLiteralNode("FOO");
    CssDefinitionNode def = new CssDefinitionNode(x);

    CssBlockNode compBlock = new CssBlockNode(true);
    compBlock.addChildToBack(def);

    CssLiteralNode compLit = new CssLiteralNode("CSS_BAR");
    CssComponentNode comp = new CssComponentNode(compLit, null, false,
        CssComponentNode.PrefixStyle.LITERAL, compBlock);

    CssBlockNode block = new CssBlockNode(false);
    block.addChildToBack(comp);
    CssRootNode root = new CssRootNode(block);
    CssTree tree = new CssTree(null, root);

    DefaultVisitController controller = new DefaultVisitController(tree, true);
    controller.startVisit(testVisitor);

    InOrder inOrder = Mockito.inOrder(testVisitor);
    // Enter Tree gets the root - there is no enterRoot.
    inOrder.verify(testVisitor).enterTree(root);
    // There are blocks that get created even if you don't add them.
    inOrder.verify(testVisitor).enterImportBlock(Matchers.<CssImportBlockNode>any());
    // Then we enter the block.
    inOrder.verify(testVisitor).enterBlock(block);
    // Then we enter the component that we created.
    inOrder.verify(testVisitor).enterComponent(comp);
    // Then we enter the definition within the component.
    inOrder.verify(testVisitor).enterDefinition(def);
  }

  public void testVisitValueNodes() {
    List<CssValueNode> simpleValues = Lists.newLinkedList();
    for (String v : new String[] {"a", "b", "c"}) {
      simpleValues.add(new CssLiteralNode(v, null));
    }
    CssCompositeValueNode parent =
        new CssCompositeValueNode(
            simpleValues, CssCompositeValueNode.Operator.COMMA, null);

    CssPropertyValueNode propValue = new CssPropertyValueNode();
    propValue.addChildToBack(parent);
    CssDeclarationNode decl =
        new CssDeclarationNode(
            new CssPropertyNode("prop"),
            propValue);
    CssDeclarationBlockNode db = new CssDeclarationBlockNode();
    db.addChildToBack(decl);
    CssRulesetNode ruleset = new CssRulesetNode(db);
    ruleset.addSelector(new CssSelectorNode("name", null));
    CssBlockNode b = new CssBlockNode(false);
    b.addChildToBack(ruleset);
    CssTree t = new CssTree(null, new CssRootNode(b));

    final List<CssValueNode> cNodes = Lists.newLinkedList();
    final List<CssValueNode> evnNodes = Lists.newLinkedList();
    DefaultTreeVisitor testVisitor =
        new DefaultTreeVisitor() {
          @Override
          public boolean enterCompositeValueNode(CssCompositeValueNode c) {
            cNodes.add(c);
            return true;
          }

          @Override
          public boolean enterValueNode(CssValueNode n) {
            evnNodes.add(n);
            return true;
          }
        };
    DefaultVisitController controller = new DefaultVisitController(t, true);
    controller.startVisit(testVisitor);

    assertEquals(simpleValues.size(), evnNodes.size());
    for (CssValueNode i : simpleValues) {
      assertTrue(evnNodes.contains(i));
    }
    assertEquals(1, cNodes.size());
    assertTrue(cNodes.contains(parent));
  }

  public void testVisitCompositeValueNodeWithFunction() {
    List<CssValueNode> simpleValues = Lists.newLinkedList();
    simpleValues.add(
        new CssFunctionNode(CssFunctionNode.Function.byName("url"), null));
    simpleValues.add(
        new CssFunctionNode(CssFunctionNode.Function.byName("url"), null));

    CssCompositeValueNode parent =
        new CssCompositeValueNode(
            simpleValues, CssCompositeValueNode.Operator.COMMA, null);

    CssPropertyValueNode propValue = new CssPropertyValueNode();
    propValue.addChildToBack(parent);
    CssDeclarationNode decl =
        new CssDeclarationNode(
            new CssPropertyNode("prop"),
            propValue);
    CssDeclarationBlockNode db = new CssDeclarationBlockNode();
    db.addChildToBack(decl);
    CssRulesetNode ruleset = new CssRulesetNode(db);
    ruleset.addSelector(new CssSelectorNode("name", null));
    CssBlockNode b = new CssBlockNode(false);
    b.addChildToBack(ruleset);
    CssTree t = new CssTree(null, new CssRootNode(b));

    final List<CssValueNode> compositeNode = Lists.newLinkedList();
    final List<CssValueNode> functionNodes = Lists.newLinkedList();
    DefaultTreeVisitor testVisitor =
        new DefaultTreeVisitor() {
          @Override
          public boolean enterCompositeValueNode(CssCompositeValueNode c) {
            compositeNode.add(c);
            return true;
          }

          @Override
          public boolean enterFunctionNode(CssFunctionNode n) {
            functionNodes.add(n);
            return true;
          }
        };
    DefaultVisitController controller = new DefaultVisitController(t, true);
    controller.startVisit(testVisitor);

    assertEquals(2, functionNodes.size());
    assertEquals(1, compositeNode.size());
    assertTrue(compositeNode.contains(parent));
  }

  public void verifyRemoveablePropertyValueElement(String backgroundValue) {
    try {
      CssTree t = new GssParser(
          new com.google.common.css.SourceCode(null,
              String.format("p { background: %s; }", backgroundValue)))
          .parse();
      assertTrue(
          "This test assumes we start with a stylesheet containing detectable " + "function nodes.",
          FunctionDetector.detect(t));
      final DefaultVisitController vc =
          new DefaultVisitController(t, true /* allowMutating */);
      CssTreeVisitor functionRemover =
          new DefaultTreeVisitor() {
            @Override
            public boolean enterFunctionNode(CssFunctionNode node) {
              System.err.println(node.getParent().getClass().getName());
              vc.removeCurrentNode();
              return true;
            }
          };
      vc.startVisit(functionRemover);
      assertFalse(
          "We should be able to remove function nodes that occur as property " + "values.",
          FunctionDetector.detect(t));
      assertTrue(
          "Removing one composite element within a property value should not "
              + "affect its siblings.",
          ValueDetector.detect(t, "red"));
      assertTrue(
          "Removing one composite element within a property value should not "
              + "affect its siblings.",
          ValueDetector.detect(t, "fixed"));
    } catch (GssParserException e) {
      throw new RuntimeException(e);
    }
  }

  public void testRemoveCompositePropertyValueElement() {
    verifyRemoveablePropertyValueElement(
        "url(http://www.google.com/logo), red fixed");
  }

  public void testRemoveCompositePropertyValueElementMiddle() {
    verifyRemoveablePropertyValueElement(
        "red, url(http://www.google.com/logo), fixed");
  }

  public void testRemoveCompositePropertyValueElementEnd() {
    verifyRemoveablePropertyValueElement(
        "red fixed, url(http://www.google.com/logo)");
  }

  public void testVisitForLoop() {
    CssLiteralNode x = new CssLiteralNode("FOO");
    CssDefinitionNode def = new CssDefinitionNode(x);

    CssBlockNode loopBlock = new CssBlockNode(true);
    loopBlock.addChildToBack(def);

    CssValueNode from = new CssNumericNode("1", CssNumericNode.NO_UNITS);
    CssValueNode to = new CssNumericNode("5", CssNumericNode.NO_UNITS);
    CssValueNode step = new CssNumericNode("2", CssNumericNode.NO_UNITS);

    CssLiteralNode variableNode = new CssLiteralNode("for");
    CssForLoopRuleNode loop = new CssForLoopRuleNode(
        variableNode, loopBlock, null, from, to, step, "i", 0, null);

    CssBlockNode block = new CssBlockNode(false);
    block.addChildToBack(loop);
    CssRootNode root = new CssRootNode(block);
    CssTree tree = new CssTree(null, root);

    DefaultVisitController controller = new DefaultVisitController(tree, true);
    controller.startVisit(testVisitor);

    InOrder inOrder = Mockito.inOrder(testVisitor);

    // Enter Tree gets the root - there is no enterRoot.
    inOrder.verify(testVisitor).enterTree(root);
    // There are blocks that get created even if you don't add them.
    inOrder.verify(testVisitor).enterImportBlock(Matchers.<CssImportBlockNode>any());
    // Then we enter the block.
    inOrder.verify(testVisitor).enterBlock(block);
    // Then we enter the for loop node.
    inOrder.verify(testVisitor).enterForLoop(loop);
    // Then we enter the definition within the for loop.
    inOrder.verify(testVisitor).enterDefinition(def);
  }

  public void testCssCompositeValueNodeBecomesParentForNewChildren() {
    CssLiteralNode foo = new CssLiteralNode("foo");
    CssLiteralNode bar = new CssLiteralNode("bar");
    CssCompositeValueNode composite =
        new CssCompositeValueNode(ImmutableList.<CssValueNode>of(foo, bar), Operator.COMMA, null);

    final MutatingVisitController controller = new DefaultVisitController(composite, true);
    controller.startVisit(
        new DefaultTreeVisitor() {
          @Override
          public boolean enterValueNode(CssValueNode value) {
            if (value.getValue().equals("bar")) {
              CssLiteralNode baz = new CssLiteralNode("baz");
              CssLiteralNode quux = new CssLiteralNode("quux");
              CssCompositeValueNode newNode =
                  new CssCompositeValueNode(ImmutableList.<CssValueNode>of(baz, quux), Operator.COMMA, null);
              controller.replaceCurrentBlockChildWith(ImmutableList.of(newNode), false);
            }
            return true;
          }
        });

    assertThat(composite.toString()).isEqualTo("foo,baz,quux");

    CssValueNode fooValue = composite.getValues().get(0);
    assertThat(fooValue.getParent()).isSameAs(composite);

    CssValueNode bazValue = composite.getValues().get(1);
    assertThat(bazValue.getParent()).isSameAs(composite);

    CssValueNode quuxValue = composite.getValues().get(1);
    assertThat(quuxValue.getParent()).isSameAs(composite);
  }

  private static class ValueDetector extends DefaultTreeVisitor {

    private final String quarry;
    private final boolean[] foundValue = {false};

    private ValueDetector(String quarry) {
      this.quarry = quarry;
    }

    public static boolean detect(CssTree t, String quarry) {
      ValueDetector detector = new ValueDetector(quarry);
      new DefaultVisitController(t, false /* allowMutating */).startVisit(detector);
      return detector.foundValue[0];
    }

    @Override
    public boolean enterValueNode(CssValueNode node) {
      if (quarry.equals(node.getValue())) {
        foundValue[0] = true;
      }
      return true;
    }
  }

  private static class FunctionDetector extends DefaultTreeVisitor {

    private final boolean[] foundValue = {false};

    public static boolean detect(CssTree t) {
      FunctionDetector detector = new FunctionDetector();
      new DefaultVisitController(t, false /* allowMutating */).startVisit(detector);
      return detector.foundValue[0];
    }

    @Override
    public boolean enterFunctionNode(CssFunctionNode node) {
      foundValue[0] = true;
      return true;
    }
  }
}
