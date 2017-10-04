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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.css.SourceCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link CssTree}
 *
 */
@RunWith(JUnit4.class)
public class CssTreeTest {

  @Test
  public void testSimple() {
    CssTree tree = new CssTree(new SourceCode("testfile", ""));
    CssRootNode root = tree.getRoot();
    assertThat(root).isNotNull();
    assertThat(root.getParent()).isNull();
    assertThat(root.getBody().getParent()).isEqualTo(root);
    assertThat(tree.getRoot().getBody().isEmpty()).isTrue();
    assertThat(root.getCharsetRule()).isNull();
    assertThat(root.getImportRules().isEmpty()).isTrue();
    assertThat(root.getBody()).isNotNull();
  }

  @Test
  public void testCopyConstructor() {
    CssTree tree1 = new CssTree(new SourceCode("testfile", ""));
    CssTree tree2 = new CssTree(tree1);

    assertThat(tree2.getRoot()).isNotSameAs(tree1.getRoot());
    assertThat(tree2.getRoot().getBody()).isNotSameAs(tree1.getRoot().getBody());
    assertThat(tree1.getRoot().getBody().isEmpty()).isTrue();
    assertThat(tree2.getRoot().getBody().isEmpty()).isTrue();
    assertThat(tree2.getSourceCode()).isEqualTo(tree1.getSourceCode());

    tree1.getRulesetNodesToRemove().addRulesetNode(new CssRulesetNode());
    assertThat(tree2.getRulesetNodesToRemove().getRulesetNodes()).isEmpty();
  }
}
