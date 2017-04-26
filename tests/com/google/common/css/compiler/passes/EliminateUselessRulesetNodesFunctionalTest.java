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

import com.google.common.css.compiler.ast.FunctionalTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** @author oana@google.com (Oana Florescu) */
@RunWith(JUnit4.class)
public class EliminateUselessRulesetNodesFunctionalTest extends FunctionalTestBase {

  @Test
  public void test() {
    testTreeConstruction(
        linesToString(
            "foo { background-color: blue;}",
            "foo { border-left: 5px;}",
            "foo { background-color: red;}"
            ),
        "[[foo]{[border-left:[5px]]}, "
        + "[foo]{[background-color:[red]]}]");
  }

  @Override
  protected void runPass() {
    MarkRemovableRulesetNodes markPass = new MarkRemovableRulesetNodes(tree);
    markPass.runPass();
    EliminateUselessRulesetNodes pass
        = new EliminateUselessRulesetNodes(tree);
    pass.runPass();
  }
}
