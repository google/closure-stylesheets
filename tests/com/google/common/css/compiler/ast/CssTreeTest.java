/*
 * Copyright 2008 Google Inc.
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

import junit.framework.TestCase;

/**
 * Unit tests for {@link CssTree}
 *
 *
 */
public class CssTreeTest extends TestCase {

  public void testSimple() {
    CssTree tree = new CssTree(new SourceCode("testfile", ""));
    CssRootNode root = tree.getRoot();
    assertNotNull(root);
    assertNull(root.getParent());
    assertEquals(root, root.getBody().getParent());
    assertTrue(tree.getRoot().getBody().isEmpty());
    assertNull(root.getCharsetRule());
    assertTrue(root.getImportRules().isEmpty());
    assertNotNull(root.getBody());
  }

  public void testCopyConstructor() {
    CssTree tree1 = new CssTree(new SourceCode("testfile", ""));
    CssTree tree2 = new CssTree(tree1);

    assertNotSame(tree1.getRoot(), tree2.getRoot());
    assertNotSame(tree1.getRoot().getBody(), tree2.getRoot().getBody());
    assertTrue(tree1.getRoot().getBody().isEmpty());
    assertTrue(tree2.getRoot().getBody().isEmpty());
    assertEquals(tree1.getSourceCode(), tree2.getSourceCode());

    tree1.getRulesetNodesToRemove().addRulesetNode(new CssRulesetNode());
    assertTrue(tree2.getRulesetNodesToRemove().getRulesetNodes().isEmpty());
  }
}
