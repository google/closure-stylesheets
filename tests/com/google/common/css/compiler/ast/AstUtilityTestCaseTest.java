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

import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssCombinatorNode.Combinator;
import com.google.common.css.compiler.ast.testing.AstUtilityTestCase;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link AstUtilityTestCase}.
 *
 * @author oana@google.com (Oana Florescu)
 */
@RunWith(JUnit4.class)
public class AstUtilityTestCaseTest extends AstUtilityTestCase {

  @Test
  public void testDeepEquals1() throws Exception {
    CssLiteralNode node1 = new CssLiteralNode("");
    CssLiteralNode node2 = new CssLiteralNode("");
    
    deepEquals(node1, node2);
  }

  @Test
  public void testDeepEquals2() throws Exception {
    CssLiteralNode node1 = new CssLiteralNode("node");
    CssLiteralNode node2 = new CssLiteralNode("node");
    
    deepEquals(node1, node2);
  }

  @Test
  public void testDeepEquals3() throws Exception {
    CssLiteralNode node1 = new CssLiteralNode("node1");
    CssLiteralNode node2 = new CssLiteralNode("node2");
    
    try {
      deepEquals(node1, node2);
      fail("FAIL: Node1 and Node2 should not be equal.");
    } catch (AssertionFailedError e) {
      if (e.getMessage().startsWith("FAIL")) {
        throw e;
      }
    }
  }

  @Test
  public void testDeepEquals4() throws Exception {
    CssCombinatorNode parent1 = new CssCombinatorNode(
        Combinator.DESCENDANT,
        new SourceCodeLocation(
            new SourceCode("filename1", null), 1, 1, 1, 1, 1, 1));
    CssCombinatorNode parent2 = new CssCombinatorNode(
        Combinator.DESCENDANT,
        null);
    CssSelectorNode node1 = new CssSelectorNode("selector");
    CssSelectorNode node2 = new CssSelectorNode("selector");
    
    BackDoorNodeMutation.setParent(node1, parent1);
    BackDoorNodeMutation.setParent(node2, parent2);
    
    try {
      deepEquals(parent1, parent2);
      fail("FAIL: Parent1 and Parent2 should not be equal.");
    } catch (AssertionFailedError e) {
      if (e.getMessage().startsWith("FAIL")) {
        throw e;
      }
    }
  }

  @Test
  public void testDeepEquals5() throws Exception {
    CssPropertyValueNode parent1 = new CssPropertyValueNode();
    CssPropertyValueNode parent2 = new CssPropertyValueNode();
    CssLiteralNode node1 = new CssLiteralNode("node");
    CssLiteralNode node2 = new CssLiteralNode("node");
    
    BackDoorNodeMutation.addChildToBack(parent1, node1);
    BackDoorNodeMutation.addChildToBack(parent2, node2);
    
    deepEquals(parent1, parent2);
  }
}
