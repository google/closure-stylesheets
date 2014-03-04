/*
 * Copyright 2012 Google Inc.
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

import junit.framework.TestCase;

/**
 * Tests for {@link CssCompositeValueNode}.
 * @author chrishenry@google.com (Chris Henry)
 */
public class CssCompositeValueNodeTest extends TestCase {

  public void testDeepCopy() throws Exception {
    CssCompositeValueNode node = new CssCompositeValueNode(
        ImmutableList.<CssValueNode>of(
            new CssLiteralNode("foo"), new CssLiteralNode("bar")),
        CssCompositeValueNode.Operator.SPACE, null);

    CssCompositeValueNode clone = node.deepCopy();
    assertNotSame(node, clone);
    assertNotSame(node.getValues(), clone.getValues());
    // Operator is enum.
    assertSame(node.getOperator(), clone.getOperator());
    assertEquals(2, clone.getValues().size());

    CssValueNode clonedChild1 = clone.getValues().get(0);
    assertNotSame(node.getValues().get(0), clonedChild1);
    assertEquals(node.getValues().get(0).getClass(), clonedChild1.getClass());
    assertEquals("foo", clonedChild1.getValue());

    CssValueNode clonedChild2 = clone.getValues().get(1);
    assertNotSame(node.getValues().get(1), clonedChild2);
    assertEquals(node.getValues().get(1).getClass(), clonedChild2.getClass());
    assertEquals("bar", clonedChild2.getValue());
  }
}
