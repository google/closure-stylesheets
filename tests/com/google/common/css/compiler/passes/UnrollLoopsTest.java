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

import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 * Unit tests for {@link UnrollLoops}.
 */
public class UnrollLoopsTest extends PassesTestBase {

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
    new CreateForLoopNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new UnrollLoops(tree.getMutatingVisitController()).runPass();
  }

  public void testSimpleLoopUnroll() throws Exception {
    testTreeConstruction(linesToString(
        "@for $i from 1 to 3 {",
        "  .foo-$i {}",
        "}"),
        "[[.foo-1]{[]}[.foo-2]{[]}[.foo-3]{[]}]");
  }

  public void testNestedLoopUnroll() throws Exception {
    testTreeConstruction(linesToString(
        "@for $i from 1 to 3 {",
        "  @for $j from 1 to $i {",
        "    .foo-$i-$j {}",
        "  }",
        "}"),
        "[[.foo-1-1]{[]}[.foo-2-1]{[]}[.foo-2-2]{[]}[.foo-3-1]{[]}[.foo-3-2]{[]}[.foo-3-3]{[]}]");
  }

  public void testDefinitionRenaming() throws Exception {
    testTreeConstruction(linesToString(
        "@for $i from 1 to 2 {",
        "  @def FOO $i;",
        "  .foo {",
        "    top: FOO;",
        "  }",
        "}"),
        "[@def FOO__LOOP0__1 [[1]];[.foo]{[top:[[FOO__LOOP0__1]];]}"
        + "@def FOO__LOOP0__2 [[2]];[.foo]{[top:[[FOO__LOOP0__2]];]}]");
  }
}
