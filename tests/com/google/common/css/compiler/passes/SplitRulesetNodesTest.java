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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.BackDoorNodeMutation;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.MutatingVisitController;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link SplitRulesetNodes}.
 *
 * @author oana@google.com (Oana Florescu)
 */
@RunWith(JUnit4.class)
public class SplitRulesetNodesTest {

  @Test
  public void testRunPass() {
    MutatingVisitController visitController = mock(MutatingVisitController.class);
    SplitRulesetNodes pass = new SplitRulesetNodes(visitController);
    visitController.startVisit(pass);

    pass.runPass();
  }

  @Test
  public void testEnterRulesetNode() {
    MutatingVisitController visitController = mock(MutatingVisitController.class);
    SplitRulesetNodes pass = new SplitRulesetNodes(visitController);

    CssRulesetNode node = new CssRulesetNode();
    List<CssNode> replacementNodes = Lists.newArrayList();
    
    visitController.replaceCurrentBlockChildWith(replacementNodes, false);

    pass.enterRuleset(node);
  }

  @Test
  public void testPassResult() {
    CssPropertyNode prop1 = new CssPropertyNode("padding", null);
    CssPropertyNode prop2 = new CssPropertyNode("color", null);

    CssDeclarationNode decl1 = new CssDeclarationNode(prop1);
    CssDeclarationNode decl2 = new CssDeclarationNode(prop2);
    
    CssRulesetNode ruleset = new CssRulesetNode();
    CssSelectorNode sel = new CssSelectorNode("foo", null);
    ruleset.addSelector(sel);
    ruleset.addDeclaration(decl1);
    ruleset.addDeclaration(decl2);
    
    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset);
    
    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);
    
    List<CssNode> replacementNodes = Lists.newArrayList();
    CssRulesetNode rule = new CssRulesetNode();
    rule.addDeclaration(decl1);
    rule.addSelector(sel);
    replacementNodes.add(rule);
    rule = new CssRulesetNode();
    rule.addDeclaration(decl1);
    rule.addSelector(sel);
    replacementNodes.add(rule);
    
    SplitRulesetNodes pass = new SplitRulesetNodes(
        tree.getMutatingVisitController());

    pass.runPass();
    assertThat(tree.getRoot().getBody().toString())
        .isEqualTo("[[foo]{[padding:[]]}, [foo]{[color:[]]}]");

    assertThat(getFirstSelectorByRuleIndex(tree, 0))
        .isNotEqualTo(getFirstSelectorByRuleIndex(tree, 1));
  }

  private CssSelectorNode getFirstSelectorByRuleIndex(CssTree tree, int index) {
    CssRulesetNode rule =
        (CssRulesetNode) tree.getRoot().getBody().getChildren().get(index);
    return rule.getSelectors().getChildren().get(0);
  }
}
