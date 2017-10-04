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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link CssCompositeValueNode}.
 *
 * @author chrishenry@google.com (Chris Henry)
 */
@RunWith(JUnit4.class)
public class CssCompositeValueNodeTest {

  @Test
  public void testDeepCopy() throws Exception {
    CssCompositeValueNode node = new CssCompositeValueNode(
        ImmutableList.<CssValueNode>of(
            new CssLiteralNode("foo"), new CssLiteralNode("bar")),
        CssCompositeValueNode.Operator.SPACE, null);

    CssCompositeValueNode clone = node.deepCopy();
    assertThat(clone).isNotSameAs(node);
    assertThat(clone.getValues()).isNotSameAs(node.getValues());
    // Operator is enum.
    assertThat(clone.getOperator()).isSameAs(node.getOperator());
    assertThat(clone.getValues()).hasSize(2);

    CssValueNode clonedChild1 = clone.getValues().get(0);
    assertThat(clonedChild1).isNotSameAs(node.getValues().get(0));
    assertThat(clonedChild1.getClass()).isEqualTo(node.getValues().get(0).getClass());
    assertThat(clonedChild1.getValue()).isEqualTo("foo");

    CssValueNode clonedChild2 = clone.getValues().get(1);
    assertThat(clonedChild2).isNotSameAs(node.getValues().get(1));
    assertThat(clonedChild2.getClass()).isEqualTo(node.getValues().get(1).getClass());
    assertThat(clonedChild2.getValue()).isEqualTo("bar");
  }
}
