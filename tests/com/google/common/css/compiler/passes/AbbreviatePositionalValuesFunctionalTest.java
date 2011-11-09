/*
 * Copyright 2010 Google Inc.
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

import com.google.common.css.compiler.ast.FunctionalTestBase;

/**
 * Functional tests for {@link AbbreviatePositionalValues}.
 *
 */
public class AbbreviatePositionalValuesFunctionalTest
    extends FunctionalTestBase {

  public void test1() {
    testTreeConstruction(
        "foo { margin: -2px -2px; }",
        "[[foo]{[margin:[-2px]]}]");
  }

  public void test2() {
    testTreeConstruction(
        "foo { padding: 1px 2px 1px 2px; }",
        "[[foo]{[padding:[1px, 2px]]}]");
  }

  public void test3() {
    testTreeConstruction(
        "foo { padding: 1px auto 3px auto; }",
        "[[foo]{[padding:[1px, auto, 3px]]}]");
  }

  public void test4() {
    testTreeConstruction(
        "foo { padding: 1px 2px 1px 3px; }",
        "[[foo]{[padding:[1px, 2px, 1px, 3px]]}]");
  }

  public void test5() {
    testTreeConstruction(
        "foo { padding: 4px 4px 4px 4px; }",
        "[[foo]{[padding:[4px]]}]");
  }

  public void test6() {
    testTreeConstruction(
        "foo { border-color: red #fff red #fff}",
        "[[foo]{[border-color:[red, #fff]]}]");
  }

  @Override
  protected void runPass() {
    AbbreviatePositionalValues pass
        = new AbbreviatePositionalValues(
            tree.getMutatingVisitController());
    pass.runPass();
  }
}
