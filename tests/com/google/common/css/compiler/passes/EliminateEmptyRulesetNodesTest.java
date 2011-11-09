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
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.MutatingVisitController;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * Unit tests for {@link EliminateEmptyRulesetNodes}.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class EliminateEmptyRulesetNodesTest extends TestCase {

  public void testRunPass() {
    IMocksControl controller = EasyMock.createStrictControl();
    MutatingVisitController visitController = controller.createMock(
        MutatingVisitController.class);

    EliminateEmptyRulesetNodes pass
        = new EliminateEmptyRulesetNodes(visitController);
    visitController.startVisit(pass);
    controller.replay();

    pass.runPass();
    controller.verify();
  }

  public void testEnterRuleset1() {
    IMocksControl controller = EasyMock.createStrictControl();
    MutatingVisitController visitController = controller.createMock(
        MutatingVisitController.class);
    visitController.removeCurrentNode();
    controller.replay();

    EliminateEmptyRulesetNodes pass
        = new EliminateEmptyRulesetNodes(visitController);

    CssRulesetNode node = new CssRulesetNode();
    pass.enterRuleset(node);

    controller.verify();
  }

  public void testEnterRuleset2() {
    IMocksControl controller = EasyMock.createStrictControl();
    MutatingVisitController visitController = controller.createMock(
        MutatingVisitController.class);
    controller.replay();

    EliminateEmptyRulesetNodes pass
        = new EliminateEmptyRulesetNodes(visitController);

    CssRulesetNode node = new CssRulesetNode();
    CssDeclarationNode declaration = new CssDeclarationNode(
        new CssPropertyNode("property"),
        new CssPropertyValueNode());
    BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
        new CssLiteralNode("value"));
    BackDoorNodeMutation.addDeclarationToRuleset(node,declaration);

    pass.enterRuleset(node);
    controller.verify();
  }
}
