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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.MutatingVisitController;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link EliminateUselessRulesetNodes}.
 *
 * <p>TODO(oana): Added a task in tracker for fixing the dependencies and making the mocking of
 * objects easier.
 *
 * @author oana@google.com (Oana Florescu)
 */
@RunWith(MockitoJUnitRunner.class)
public class EliminateUselessRulesetNodesTest {

  @Mock MutatingVisitController mockVisitController;
  @Mock CssTree mockTree;

  @Before
  public void setUp() {
    when(mockTree.getMutatingVisitController()).thenReturn(mockVisitController);
  }

  @Test
  public void testRunPass() {
    EliminateUselessRulesetNodes pass = new EliminateUselessRulesetNodes(mockTree);
    mockVisitController.startVisit(pass);

    pass.runPass();
  }

  @Test
  public void testEnterRulesetNode() {
    CssRulesetNode node = new CssRulesetNode();
    List<CssRulesetNode> rulesList = Lists.newArrayList(node);

    CssTree.RulesetNodesToRemove nodesToRemove = mock(CssTree.RulesetNodesToRemove.class);
    when(nodesToRemove.getRulesetNodes()).thenReturn(rulesList);
    when(mockTree.getRulesetNodesToRemove()).thenReturn(nodesToRemove);

    EliminateUselessRulesetNodes pass = new EliminateUselessRulesetNodes(mockTree);

    pass.enterRuleset(node);
  }
}
