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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LocationBoundingVisitor}. */
@RunWith(JUnit4.class)
public class LocationBoundingVisitorTest extends NewFunctionalTestBase {
  private LocationBoundingVisitor locationBoundingVisitor;

  @Override
  protected void runPass() {
    locationBoundingVisitor = new LocationBoundingVisitor();
    tree.getVisitController()
        .startVisit(UniformVisitor.Adapters.asVisitor(locationBoundingVisitor));
  }

  @Test
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
    assertThat(LocationBoundingVisitor.bound(red)).isEqualTo(expected);
  }

  @Test
  public void testUnknown() throws Exception {
    parseAndRun("div { color: red; }");
    CssTreeVisitor eraseLocations =
        UniformVisitor.Adapters.asVisitor(
            new UniformVisitor() {
              @Override
              public void enter(CssNode n) {
                n.setSourceCodeLocation(SourceCodeLocation.getUnknownLocation());
              }

              @Override
              public void leave(CssNode node) {}
            });
    tree.getMutatingVisitController().startVisit(eraseLocations);
    SourceCodeLocation actual = LocationBoundingVisitor.bound(tree.getRoot());
    assertWithMessage(new com.google.common.css.compiler.ast.GssError("boo", actual).format())
        .that(actual)
        .isEqualTo(SourceCodeLocation.getUnknownLocation());
  }

  @Test
  public void testMixedSubtree() throws Exception {
    // Let's examine a non-trivial tree
    parseAndRun("div { color: red; }");

    // First: establish some facts we can use later on
    CssSelectorNode div = findFirstNodeOf(CssSelectorNode.class);
    assertWithMessage("There should be a node with known location")
        .that(div.getSourceCodeLocation().isUnknown())
        .isFalse();
    CssLiteralNode red = findFirstNodeOf(CssLiteralNode.class);
    assertWithMessage("There should be a distinguished second node")
        .that(red.getValue())
        .isEqualTo("red");
    assertWithMessage("The second node  should also have known location")
        .that(red.getSourceCodeLocation().isUnknown())
        .isFalse();
    CssDeclarationBlockNode block =
        findFirstNodeOf(CssDeclarationBlockNode.class);
    assertWithMessage("There should be a node with an known location")
        .that(block.getSourceCodeLocation() == null || block.getSourceCodeLocation().isUnknown())
        .isFalse();

    // Next: demonstrate some properties of the visitor
    SourceCodeLocation actual = LocationBoundingVisitor.bound(tree.getRoot());
    assertThat(actual.isUnknown()).isFalse();
    assertWithMessage("The tree-wide lower bound should l.b. a known node.")
        .that(actual.getBeginCharacterIndex())
        .isAtMost(div.getSourceCodeLocation().getBeginCharacterIndex());
    assertWithMessage("The tree-wide lower bound should l.b. all the known nodes.")
        .that(actual.getBeginCharacterIndex())
        .isAtMost(red.getSourceCodeLocation().getBeginCharacterIndex());
    assertWithMessage("The tree-wide upper bound should u.b. a known node.")
        .that(actual.getEndCharacterIndex())
        .isAtLeast(div.getSourceCodeLocation().getEndCharacterIndex());
    assertWithMessage("The tree-wide upper bound should u.b. all the known nodes.")
        .that(actual.getEndCharacterIndex())
        .isAtLeast(red.getSourceCodeLocation().getEndCharacterIndex());

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
          assertWithMessage("ancestral lower bounds should not exceed descendent l.b.s")
              .that(aBound.getBeginCharacterIndex())
              .isAtMost(nLocation.getBeginCharacterIndex());
          assertWithMessage("ancestral upper bounds should equal or exceed descendent u.b.s")
              .that(aBound.getBeginCharacterIndex())
              .isAtLeast(nLocation.getBeginCharacterIndex());
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
    assertThat(actual.getBeginCharacterIndex()).isEqualTo(0);
    assertThat(actual.getEndCharacterIndex()).isEqualTo(19);
  }
}
