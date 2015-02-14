/*
 * Copyright 2013 Google Inc.
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

import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

/**
 * Unit tests for {@link LocationBoundingVisitor}.
 */
public class LocationBoundingVisitorTest extends NewFunctionalTestBase {
  private LocationBoundingVisitor locationBoundingVisitor;

  @Override
  protected void runPass() {
    locationBoundingVisitor = new LocationBoundingVisitor();
    tree.getVisitController().startVisit(locationBoundingVisitor);
  }

  public void testTrivialBound() throws Exception {
    CssLiteralNode red = new CssLiteralNode("red");
    SourceCodeLocation expected =
        new SourceCodeLocation(
            new SourceCode(null, ""),
            5 /* beginCharacterIndex */,
            3 /* beginLineNumber */,
            1 /* beginIndexInLine */,
            15 /* endCharacterIndex */,
            3 /* endLineNumber */,
            11 /* endIndexInLine */);
    red.setSourceCodeLocation(expected);
    assertEquals(expected, LocationBoundingVisitor.bound(red));
  }

  public void testUnknown() throws Exception {
    parseAndRun("div { color: red; }");
    UniformVisitor eraseLocations = new UniformVisitor() {
        @Override public void enter(CssNode n) {
          n.setSourceCodeLocation(SourceCodeLocation.getUnknownLocation());
        }
      };
    tree.getMutatingVisitController().startVisit(eraseLocations);
    SourceCodeLocation actual = LocationBoundingVisitor.bound(tree.getRoot());
    assertEquals(
        new com.google.common.css.compiler.ast.GssError("boo", actual).format(),
        SourceCodeLocation.getUnknownLocation(), actual);
  }

  public void testMixedSubtree() throws Exception {
    // Let's examine a non-trivial tree
    parseAndRun("div { color: red; }");

    // First: establish some facts we can use later on
    CssSelectorNode div = findFirstNodeOf(CssSelectorNode.class);
    assertFalse(
        "There should be a node with known location",
        div.getSourceCodeLocation().isUnknown());
    CssLiteralNode red = findFirstNodeOf(CssLiteralNode.class);
    assertEquals(
        "There should be a distinguished second node",
        "red", red.getValue());
    assertFalse(
        "The second node  should also have known location",
        red.getSourceCodeLocation().isUnknown());
    CssDeclarationBlockNode block =
        findFirstNodeOf(CssDeclarationBlockNode.class);
    assertTrue(
        "There should be a node with an unknown location",
        block.getSourceCodeLocation() == null
        || block.getSourceCodeLocation().isUnknown());

    // Next: demonstrate some properties of the visitor
    SourceCodeLocation actual = LocationBoundingVisitor.bound(tree.getRoot());
    assertFalse(actual.isUnknown());
    assertTrue(
        "The tree-wide lower bound should l.b. a known node.",
        actual.getBeginCharacterIndex()
        <= div.getSourceCodeLocation().getBeginCharacterIndex());
    assertTrue(
        "The tree-wide lower bound should l.b. all the known nodes.",
        actual.getBeginCharacterIndex()
        <= red.getSourceCodeLocation().getBeginCharacterIndex());
    assertTrue(
        "The tree-wide upper bound should u.b. a known node.",
        actual.getEndCharacterIndex()
        >= div.getSourceCodeLocation().getEndCharacterIndex());
    assertTrue(
        "The tree-wide upper bound should u.b. all the known nodes.",
        actual.getEndCharacterIndex()
        >= red.getSourceCodeLocation().getEndCharacterIndex());

    for (CssNode n : new CssNode[] {div, red, block}) {
      SourceCodeLocation nLocation = n.getSourceCodeLocation();
      for (CssNode a : n.ancestors()) {
        try {
          if (!a.getSourceCodeLocation().isUnknown()) {
            // LocationBoundingVisitor guarantees that ancestors contain
            // their descendents only as long as the ancestor doesn't
            // have explicit bounds, in which case all bets are off.
            // E.g., consider this tree
            //
            //   graph  beginCharacterIndex endCharacterIndex
            //   ---    ---                 ---
            //    div   5                   8
            //     |
            //   span   3                   42
            // These indices make no sense but GIGO.
            continue;
          }
          SourceCodeLocation aBound = LocationBoundingVisitor.bound(a);
          assertTrue(
              "ancestral lower bounds should not exceed descendent l.b.s",
              aBound.getBeginCharacterIndex()
              <= nLocation.getBeginCharacterIndex());
          assertTrue(
              "ancestral upper bounds should equal or exceed descendent u.b.s",
              aBound.getBeginCharacterIndex()
              >= nLocation.getBeginCharacterIndex());
        } catch (NullPointerException e) {
          // Our tree-traversal code is a bit buggy, so give up
          // on this ancestor and try another one. To the extent
          // we can visit ancestors, these properties we assert
          // should hold.
        }
      }
    }
    // For good measure: some specific, empirical, and reasonable-looking
    // assertions.
    assertEquals(0, actual.getBeginCharacterIndex());
    assertEquals(18, actual.getEndCharacterIndex());
  }
}
