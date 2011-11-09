/*
 * Copyright 2011 Google Inc.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import junit.framework.TestCase;

import java.util.List;

/**
 * Unit tests for {@link CssAbstractBlockNode}.
 *
 */
public class CssAbstractBlockNodeTest extends TestCase {
  private ImmutableList<Class<? extends CssNode>> validSuperclasses =
      ImmutableList.<Class<? extends CssNode>>of(CssValueNode.class);

  public void testConstructor() {
    List<CssNode> children = Lists.newArrayList();
    children.add(new CssLiteralNode("a"));
    children.add(new CssNumericNode("20", "px"));
    try {
      new TestBlock(children, validSuperclasses);
    } catch (IllegalStateException e) {
      fail(e.getMessage());
    }
  }

  public void testDeepCopy() {
    List<CssNode> children = Lists.newArrayList();
    children.add(new CssLiteralNode("a"));
    children.add(new CssNumericNode("20", "px"));
    try {
      TestBlock testBlock = new TestBlock(children, validSuperclasses);
      testBlock.deepCopy();
    } catch (IllegalStateException e) {
      fail(e.getMessage());
    }
  }

  public void testAddChild() {
    TestBlock testBlock = new TestBlock(validSuperclasses);
    try {
      testBlock.addChildToBack(new CssLiteralNode("a"));
    } catch (IllegalStateException e) {
      fail(e.getMessage());
    }
  }

  public void testAddChildren() {
    TestBlock testBlock = new TestBlock(validSuperclasses);
    List<CssNode> children = Lists.newArrayList();
    children.add(new CssLiteralNode("a"));
    children.add(new CssNumericNode("20", "px"));
    try {
      testBlock.setChildren(children);
    } catch (IllegalStateException e) {
      fail(e.getMessage());
    }
  }

  public void testAddChildError() {
    TestBlock testBlock = new TestBlock(validSuperclasses);
    try {
      testBlock.addChildToBack(new CssBlockNode());
      fail("An IllegalStateException should have been thrown.");
    } catch (IllegalStateException e) {
      // expected exception
    }
  }

  public void testAddChildrenError() {
    TestBlock testBlock = new TestBlock(validSuperclasses);
    List<CssNode> children = Lists.newArrayList();
    children.add(new CssLiteralNode("a"));
    children.add(new CssNumericNode("20", "px"));
    children.add(new CssBlockNode());
    try {
      testBlock.setChildren(children);
      fail("An IllegalStateException should have been thrown.");
    } catch (IllegalStateException e) {
      // expected exception
    }
  }

  private class TestBlock extends CssAbstractBlockNode {

    public TestBlock(
        ImmutableList<Class<? extends CssNode>> validSuperclasses) {
      super(true /* isEnclosedWithBraces */, validSuperclasses);
    }

    public TestBlock(List<CssNode> children,
        ImmutableList<Class<? extends CssNode>> validSuperclasses) {
      super(true /* isEnclosedWithBraces */, children, null /* comments */,
          validSuperclasses);
    }

    public TestBlock(TestBlock node) {
      super(node);
    }

    @Override
    public TestBlock deepCopy() {
      return new TestBlock(this);
    }
  }
}
