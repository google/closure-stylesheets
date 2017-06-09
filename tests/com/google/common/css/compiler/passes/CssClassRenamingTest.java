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
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.css.SubstitutionMap;
import com.google.common.css.compiler.ast.BackDoorNodeMutation;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssIdSelectorNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssRefinerListNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.passes.testing.AstPrinter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests the {@link CssClassRenaming} compiler pass.
 *
 * @author oana@google.com (Oana Florescu)
 */
@RunWith(JUnit4.class)
public class CssClassRenamingTest {

  @Test
  public void testRunPass() {
    MutatingVisitController visitController = mock(MutatingVisitController.class);

    CssClassRenaming pass = new CssClassRenaming(visitController, null, null);
    visitController.startVisit(pass);
    pass.runPass();
  }

  @Test
  public void testNoSubstitutionWithNullMap() {
    CssClassSelectorNode node = new CssClassSelectorNode("FOO", null);
    CssClassRenaming pass = new CssClassRenaming(null, null, null);
    pass.enterClassSelector(node);
  }

  @Test
  public void testNoClassSubstitutionWhenClassNotFoundInMap() {
    CssClassSelectorNode refinerNode = new CssClassSelectorNode("FOO", null);
    SubstitutionMap cssClassRenamingMap = mock(SubstitutionMap.class);

    CssClassRenaming pass
        = new CssClassRenaming(null, cssClassRenamingMap, null);
    pass.enterClassSelector(refinerNode);

    verify(cssClassRenamingMap).get("FOO");
  }

  @Test
  public void testNoIdSubstitutionWhenIdNotFoundInMap() {
    CssIdSelectorNode refinerNode = new CssIdSelectorNode("ID", null);
    SubstitutionMap idRenamingMap = mock(SubstitutionMap.class);

    CssClassRenaming pass = new CssClassRenaming(null, null, idRenamingMap);
    pass.enterIdSelector(refinerNode);

    verify(idRenamingMap).get("ID");
  }
  
  @Test
  public void testEnterClassRefiner() {
    CssClassSelectorNode refinerNode = new CssClassSelectorNode("CSS_FOO",
        null);
    CssRefinerListNode refiners = new CssRefinerListNode();
    BackDoorNodeMutation.addChildToBack(refiners, refinerNode);

    CssRulesetNode ruleset = new CssRulesetNode();
    CssSelectorNode sel = new CssSelectorNode("", null);
    sel.setRefiners(refiners);
    ruleset.addSelector(sel);
    
    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset);
    
    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);
    
    SubstitutionMap classMap = new SubstitutionMap() {
        /** {@inheritDoc} */
        @Override
        public String get(String key) {
          return key.startsWith("CSS_") ? key + '_' : key;
        }
    };
    CssClassRenaming pass = new CssClassRenaming(
        tree.getMutatingVisitController(), classMap, null);
    pass.runPass();
    assertThat(AstPrinter.print(tree)).isEqualTo("[[.CSS_FOO_]{[]}]");
  }

  @Test
  public void testCustomPropertyRenaming() {
    CssFunctionNode value = new CssFunctionNode(CssFunctionNode.Function.byName("var"), null);
    value.setArguments(new CssFunctionArgumentsNode(ImmutableList.of((CssValueNode) new CssLiteralNode("--myvar-b"))));
    CssDeclarationNode declarationNode = new CssDeclarationNode(new CssPropertyNode("--myvar-a"),
        new CssPropertyValueNode(ImmutableList.of((CssValueNode)value)));
    CssClassSelectorNode refinerNode = new CssClassSelectorNode("CSS_FOO",
        null);
    CssRefinerListNode refiners = new CssRefinerListNode();
    BackDoorNodeMutation.addChildToBack(refiners, refinerNode);

    CssDeclarationBlockNode ruleBody = new CssDeclarationBlockNode();
    ruleBody.addChildToBack(declarationNode);
    CssRulesetNode ruleset = new CssRulesetNode(ruleBody);
    CssSelectorNode sel = new CssSelectorNode("FOO");
    ruleset.addSelector(sel);

    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset);

    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);
    
    SubstitutionMap classMap = new SubstitutionMap() {
        /** {@inheritDoc} */
        @Override
        public String get(String key) {
          return key.startsWith("--") ? key + '_' : key;
        }
    };
    CssClassRenaming pass = new CssClassRenaming(
        tree.getMutatingVisitController(), classMap, null);
    pass.runPass();
    assertThat(AstPrinter.print(tree)).isEqualTo("[[FOO]{[--myvar-a_:[var(--myvar-b_)];]}]");
  }


  
  @Test
  public void testEnterIdRefiner() {
    CssIdSelectorNode refinerNode = new CssIdSelectorNode("ID_FOO", null);
    CssRefinerListNode refiners = new CssRefinerListNode();
    BackDoorNodeMutation.addChildToBack(refiners, refinerNode);

    CssRulesetNode ruleset = new CssRulesetNode();
    CssSelectorNode sel = new CssSelectorNode("", null);
    sel.setRefiners(refiners);
    ruleset.addSelector(sel);
    
    CssBlockNode body = new CssBlockNode(false);
    BackDoorNodeMutation.addChildToBack(body, ruleset);
    
    CssRootNode root = new CssRootNode(body);
    CssTree tree = new CssTree(null, root);
    
    SubstitutionMap idMap = new SubstitutionMap() {
        /** {@inheritDoc} */
        @Override
        public String get(String key) {
          return key.startsWith("ID_") ? key + '_' : key;
        }
    };
    CssClassRenaming pass
        = new CssClassRenaming(tree.getMutatingVisitController(), null, idMap);
    pass.runPass();
    assertThat(AstPrinter.print(tree)).isEqualTo("[[#ID_FOO_]{[]}]");
  }
}
