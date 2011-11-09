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

import static org.easymock.EasyMock.expect;

import com.google.common.css.SubstitutionMap;
import com.google.common.css.compiler.ast.BackDoorNodeMutation;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssIdSelectorNode;
import com.google.common.css.compiler.ast.CssRefinerListNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.passes.testing.AstPrinter;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * Unit tests the {@link CssClassRenaming} compiler pass.
 * 
 * @author oana@google.com (Oana Florescu)
 */
public class CssClassRenamingTest extends TestCase {

  public void testRunPass() {
    IMocksControl controller = EasyMock.createStrictControl();
    MutatingVisitController visitController = controller.createMock(
        MutatingVisitController.class);
    
    CssClassRenaming pass = 
        new CssClassRenaming(visitController, null, null);
    visitController.startVisit(pass);
    controller.replay();

    pass.runPass();
    controller.verify();
  }

  public void testNoSubstitutionWithNullMap() {
    CssClassSelectorNode node = new CssClassSelectorNode("FOO", null);
    CssClassRenaming pass = new CssClassRenaming(null, null, null);
    pass.enterClassSelector(node);
  }

  public void testNoClassSubstitutionWhenClassNotFoundInMap() {
    CssClassSelectorNode refinerNode = new CssClassSelectorNode("FOO", null);
    IMocksControl controller = EasyMock.createStrictControl();
    SubstitutionMap cssClassRenamingMap = controller.createMock(
        SubstitutionMap.class);
    expect(cssClassRenamingMap.get("FOO")).andReturn(null).anyTimes();
    controller.replay();
    
    CssClassRenaming pass
        = new CssClassRenaming(null, cssClassRenamingMap, null);
    pass.enterClassSelector(refinerNode);
  }

  public void testNoIdSubstitutionWhenIdNotFoundInMap() {
    CssIdSelectorNode refinerNode = new CssIdSelectorNode("ID", null);
    IMocksControl controller = EasyMock.createStrictControl();
    SubstitutionMap idRenamingMap = controller.createMock(
        SubstitutionMap.class);
    expect(idRenamingMap.get("ID")).andReturn(null).anyTimes();
    controller.replay();
    CssClassRenaming pass = new CssClassRenaming(null, null, idRenamingMap);
    pass.enterIdSelector(refinerNode);
  }
  
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
    assertEquals("[[.CSS_FOO_]{[]}]", AstPrinter.print(tree));
  }

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
    assertEquals("[[#ID_FOO_]{[]}]", AstPrinter.print(tree));
  }
}
