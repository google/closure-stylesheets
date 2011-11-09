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
import com.google.common.css.compiler.ast.CssAtRuleNode.Type;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.testing.UtilityTestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * Unit tests for {@link HasConditionalNodes}.
 *
 */
public class HasConditionalNodesTest extends UtilityTestCase {
  public void testEnterConditionalBlock() {
    IMocksControl controller = EasyMock.createStrictControl();
    MutatingVisitController visitController = controller.createMock(
        MutatingVisitController.class);
    visitController.stopVisit();
    controller.replay();

    HasConditionalNodes pass
        = new HasConditionalNodes(visitController);

    CssConditionalBlockNode node = new CssConditionalBlockNode();
    CssConditionalRuleNode rule = new CssConditionalRuleNode(Type.IF,
        new CssLiteralNode("condition"));
    BackDoorNodeMutation.addRuleToConditionalBlock(node, rule);

    pass.enterConditionalBlock(node);
    controller.verify();
  }

  public void testRunPassAndHasConditionalNodes() {
    IMocksControl controller = EasyMock.createStrictControl();
    MutatingVisitController visitController = controller.createMock(
        MutatingVisitController.class);

    HasConditionalNodes pass
        = new HasConditionalNodes(visitController);

    visitController.startVisit(pass);
    controller.replay();

    pass.runPass();
    assertFalse(pass.hasConditionalNodes());

    controller.verify();
  }
}
