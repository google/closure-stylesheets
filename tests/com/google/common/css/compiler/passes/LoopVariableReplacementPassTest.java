/*
 * Copyright 2015 Google Inc.
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

import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.passes.testing.PassesTestBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LoopVariableReplacementPass}. */
@RunWith(JUnit4.class)
public class LoopVariableReplacementPassTest extends PassesTestBase {

  private static final int LOOP_ID = 3;

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
  }

  @Test
  public void testClassRenaming() throws Exception {
    parseAndRun(".foo-$i {}");
    new LoopVariableReplacementPass(
        "$i", 3, Collections.<String>emptySet(), tree.getMutatingVisitController(), LOOP_ID)
        .runPass();
    checkTreeDebugString("[[.foo-3]{[]}]");
  }

  @Test
  public void testVariableInProperty() throws Exception {
    parseAndRun(".foo { top: $i; }");
    new LoopVariableReplacementPass(
        "$i", 3, Collections.<String>emptySet(), tree.getMutatingVisitController(), LOOP_ID)
        .runPass();
    checkTreeDebugString("[[.foo]{[top:[[3]];]}]");
  }

  @Test
  public void testVariableInArgument() throws Exception {
    parseAndRun(".foo { top: mult($i, 4); }");
    new LoopVariableReplacementPass(
        "$i", 5, Collections.<String>emptySet(), tree.getMutatingVisitController(), LOOP_ID)
        .runPass();
    checkTreeDebugString("[[.foo]{[top:[mult(5,4)];]}]");
  }

  @Test
  public void testDefinitionReplacement() throws Exception {
    parseAndRun("@def XXX $i; .foo { top: XXX; }");
    new LoopVariableReplacementPass(
        "$i", 2, ImmutableSet.of("XXX"), tree.getMutatingVisitController(), LOOP_ID).runPass();
    checkTreeDebugString("[@def XXX__LOOP3__2 [[2]];[.foo]{[top:[[XXX__LOOP3__2]];]}]");
  }

  @Test
  public void testBasicPseudoClassReplacement() throws Exception {
    parseAndRun(".foo:nth_child($i) { top: 0; }");
    new LoopVariableReplacementPass(
        "$i", 2, Collections.<String>emptySet(), tree.getMutatingVisitController(), LOOP_ID)
        .runPass();
    checkTreeDebugString("[[.foo:nth_child(2)]{[top:[[0]];]}]");
  }

  @Test
  public void testArgumentAOnlyPseudoClassReplacement() throws Exception {
    parseAndRun(".foo:nth_child(-$in) { top: 0; }");
    new LoopVariableReplacementPass(
        "$i", 2, Collections.<String>emptySet(), tree.getMutatingVisitController(), LOOP_ID)
        .runPass();
    checkTreeDebugString("[[.foo:nth_child(-2n)]{[top:[[0]];]}]");
  }

  @Test
  public void testBothArgumentPseudoClassReplacement() throws Exception {
    parseAndRun(".foo:nth_child($in + $i) { top: 0; }");
    new LoopVariableReplacementPass(
        "$i", 2, Collections.<String>emptySet(), tree.getMutatingVisitController(), LOOP_ID)
        .runPass();
    checkTreeDebugString("[[.foo:nth_child(2n+2)]{[top:[[0]];]}]");
  }
}
