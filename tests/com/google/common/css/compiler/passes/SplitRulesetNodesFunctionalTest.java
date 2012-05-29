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

import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 * Functional tests for {@link SplitRulesetNodes}.
 * 
 * @author oana@google.com (Oana Florescu)
 */
public class SplitRulesetNodesFunctionalTest extends PassesTestBase {

  public void test1() {
    testTreeConstruction(
        "foo { border: 0px; padding: 5px; }",
        "[[foo]{[border:[[0px]];]}[foo]{[padding:[[5px]];]}]");
  }

  public void test2() {
    testTreeConstruction(
        "foo {"
        + "  border: 0px; padding: 5px 3px 1px;"
        + "  background: red;"
        + "}",
        "[[foo]{[border:[[0px]];]}"
        + "[foo]{[padding:[[5px][3px][1px]];]}"
        + "[foo]{[background:[[red]];]}]");
  }

  public void test3() {
    testTreeConstruction(
        "foo, bar { border: 0px; padding: 5px }",
        "[[foo]{[border:[[0px]];]}[foo]{[padding:[[5px]];]}"
        + "[bar]{[border:[[0px]];]}[bar]{[padding:[[5px]];]}]");
  }

  public void test4() {
    testTreeConstruction(
        linesToString(
            ".foo .bar, .foobar ",
            "{ border: 0px; padding: 5px }"),
        "[[.foo .bar]{[border:[[0px]];]}[.foo .bar]{[padding:[[5px]];]}"
        + "[.foobar]{[border:[[0px]];]}[.foobar]{[padding:[[5px]];]}]");
  }

  public void test5() {
    testTreeConstruction(
        ".foo.bar, .foobar+a "
        + "{ border: 0px; padding: 5px }",
        "[[.foo.bar]{[border:[[0px]];]}[.foo.bar]{[padding:[[5px]];]}"
        + "[.foobar+a]{[border:[[0px]];]}[.foobar+a]{[padding:[[5px]];]}]");
  }

  public void test6() {
    testTreeConstruction(
        ".foo.bar "
        + "{ border: 0px; padding: 5px; display: inline }",
        "[[.foo.bar]{[border:[[0px]];padding:[[5px]];"
        + "display:[[inline]];]}]");
  }

  @Override
  protected void runPass() {
    SplitRulesetNodes pass
        = new SplitRulesetNodes(tree.getMutatingVisitController(), true);
    pass.runPass();
  }
}
