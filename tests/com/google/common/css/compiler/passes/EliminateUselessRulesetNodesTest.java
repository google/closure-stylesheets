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

import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.MutatingVisitController;

import junit.framework.TestCase;

import java.util.List;

/**
 * Unit tests for {@link EliminateUselessRulesetNodes}.
 *
 * TODO(oana): Added a task in tracker for fixing the dependencies and
 * making the mocking of objects easier.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class EliminateUselessRulesetNodesTest extends TestCase {

  public void testRunPass() {
    MutatingVisitController visitController = createMock(
        MutatingVisitController.class);
    CssTree tree = createMock(CssTree.class);
    expect(tree.getMutatingVisitController())
        .andReturn(visitController).anyTimes();
    tree.resetRulesetNodesToRemove();
    replay(tree);

    EliminateUselessRulesetNodes pass = new EliminateUselessRulesetNodes(tree);
    visitController.startVisit(pass);
    replay(visitController);

    pass.runPass();
    verify(visitController);
  }

  public void testEnterRulesetNode() {
    CssRulesetNode node = new CssRulesetNode();
    List<CssRulesetNode> rulesList = Lists.newArrayList(node);

    MutatingVisitController visitController = createMock(
        MutatingVisitController.class);
    CssTree tree = createMock(CssTree.class);
    expect(tree.getMutatingVisitController())
        .andReturn(visitController).anyTimes();
    CssTree.RulesetNodesToRemove nodesToRemove = createMock(
        CssTree.RulesetNodesToRemove.class);
    expect(tree.getRulesetNodesToRemove()).andReturn(nodesToRemove)
        .anyTimes();
    expect(nodesToRemove.getRulesetNodes())
        .andReturn(rulesList).anyTimes();
    replay(nodesToRemove);
    replay(tree);

    EliminateUselessRulesetNodes pass = new EliminateUselessRulesetNodes(tree);

    visitController.removeCurrentNode();
    replay(visitController);

    pass.enterRuleset(node);
    verify(visitController);
  }
}
