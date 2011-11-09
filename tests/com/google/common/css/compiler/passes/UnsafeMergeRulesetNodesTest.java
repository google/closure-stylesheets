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

import static com.google.common.css.compiler.passes.UnsafeMergeRulesetNodes.DECLARATION_COMPARATOR;
import static com.google.common.css.compiler.passes.UnsafeMergeRulesetNodes.TO_STRING_COMPARATOR;
import static com.google.common.css.compiler.passes.UnsafeMergeRulesetNodes.TO_STRING_ITERABLE_COMPARATOR;

import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.*;

import junit.framework.TestCase;

/**
 * Unit tests for {@link UnsafeMergeRulesetNodes}.
 *
 */
public class UnsafeMergeRulesetNodesTest extends TestCase {

  /**
   * Tests for {@link UnsafeMergeRulesetNodes#TO_STRING_COMPARATOR}.
   */
  public void testToStringComparator() {
    testEquals(TO_STRING_COMPARATOR.compare(
        new CssSelectorNode("a"), new CssSelectorNode("a")));
    testSmaller(TO_STRING_COMPARATOR.compare(
        new CssSelectorNode("a"), new CssSelectorNode("b")));
    CssSelectorNode n = new CssSelectorNode("a");
  }

  /**
   * Tests for {@link UnsafeMergeRulesetNodes#TO_STRING_ITERABLE_COMPARATOR}.
   */
  public void testToStringIterableComparator() {
    testEquals(TO_STRING_ITERABLE_COMPARATOR.compare(
        ImmutableList.of("a", "b"), ImmutableList.of("a", "b")));
    testGreater(TO_STRING_ITERABLE_COMPARATOR.compare(
        ImmutableList.of("b", "a"), ImmutableList.of("a", "b")));
    testSmaller(TO_STRING_ITERABLE_COMPARATOR.compare(
            ImmutableList.of("a"), ImmutableList.of("a", "b")));
  }

  /**
   * Tests for {@link UnsafeMergeRulesetNodes#DECLARATION_COMPARATOR}.
   */
  public void testDeclarationComparator() {
    CssPropertyNode padding = new CssPropertyNode("padding");
    CssPropertyNode paddingLeft = new CssPropertyNode("padding-left");
    CssPropertyNode margin = new CssPropertyNode("margin");
    CssPropertyNode marginLeft = new CssPropertyNode("margin-left");

    CssValueNode px1 = new CssNumericNode("1", "px");
    CssValueNode px2 = new CssNumericNode("2", "px");

    CssPropertyValueNode v1 = new CssPropertyValueNode(ImmutableList.of(px1));
    CssPropertyValueNode v2 = new CssPropertyValueNode(ImmutableList.of(px2));
    CssPropertyValueNode v1s =
        new CssPropertyValueNode(ImmutableList.of(px1, px1, px1, px1));
    CssPropertyValueNode v2s =
            new CssPropertyValueNode(ImmutableList.of(px2, px2, px2, px2));

    CssDeclarationNode padding1 =
        new CssDeclarationNode(padding.deepCopy(), v1s.deepCopy());
    CssDeclarationNode padding2 =
        new CssDeclarationNode(padding.deepCopy(), v2s.deepCopy());
    CssDeclarationNode paddingLeft1 =
        new CssDeclarationNode(paddingLeft.deepCopy(), v1.deepCopy());

    CssDeclarationNode margin1 =
        new CssDeclarationNode(margin.deepCopy(), v1s.deepCopy());
    CssDeclarationNode marginLeft1 =
        new CssDeclarationNode(marginLeft.deepCopy(), v1.deepCopy());

    testEquals(DECLARATION_COMPARATOR.compare(
        margin1, margin1.deepCopy()));
    testSmaller(DECLARATION_COMPARATOR.compare(
        margin1, marginLeft1));
    testSmaller(DECLARATION_COMPARATOR.compare(
        margin1, paddingLeft1));
    testSmaller(DECLARATION_COMPARATOR.compare(
        padding1, padding2));
    testSmaller(DECLARATION_COMPARATOR.compare(
        marginLeft1, padding2));
    testGreater(DECLARATION_COMPARATOR.compare(
        paddingLeft1, padding2));

  }

  private void testEquals(int i) {
    assertEquals(0, i);
  }

  private void testSmaller(int i) {
    assertTrue(i < 0);
  }

  private void testGreater(int i) {
    assertTrue(i > 0);
  }
}
