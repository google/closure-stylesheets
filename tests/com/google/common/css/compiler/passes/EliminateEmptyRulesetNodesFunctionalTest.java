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

/**
 * Functional tests for {@link EliminateEmptyRulesetNodes}.
 *
 * @author oana@google.com (Oana Florescu)
 */
@RunWith(JUnit4.class)
public class EliminateEmptyRulesetNodesFunctionalTest extends FunctionalTestBase {

  @Test
  public void testSimple1() {
    testTreeConstruction(
        "foo {}",
        "[]");
  }

  @Test
  public void testSimple2() {
    testTreeConstruction(
        "foo {"
        + "  border: 2px;"
        + "}"
        + "bar {}",
        "[[foo]{[border:[2px]]}]");
  }

  @Test
  public void testSimple3() {
    testTreeConstruction(
        "foo {"
        + "  border: 2px;"
        + "}"
        + "bar {}"
        + "foobar {"
        + "  color: red;"
        + "}",
        "[[foo]{[border:[2px]]}, [foobar]{[color:[red]]}]");
  }

  @Test
  public void testComplex1() {
    testTreeConstruction(
        "@if COND1 {"
        + "  foo {"
        + "    border: 2px;"
        + "  }"
        + "} @elseif COND2 {"
        + "  foo {}"
        + "} @else {"
        + "  bar {}"
        + "}",
        "[[@if[COND1]{[[foo]{[border:[2px]]}]}, "
        + "@elseif[COND2]{[]}, @else[]{[]}]]");
  }

  @Test
  public void testComplex2() {
    testTreeConstruction(
        "@media print {"
        + "  @if COND1 {"
        + "    @if COND2 {"
        + "      @def COLOR red;"
        + "      foo { color: COLOR }"
        + "    } @elseif COND3 {"
        + "      foo {}"
        + "    }"
        + "  } @elseif COND2 {"
        + "    foo {}"
        + "  } @else {"
        + "    foo { top : expression }"
        + "  }"
        + "}",
        "[@media[print]{[[@if[COND1]{[[@if[COND2]"
        + "{[@def COLOR [red], [foo]{[color:[COLOR]]}]}, "
        + "@elseif[COND3]{[]}]]}, @elseif[COND2]{[]}, "
        + "@else[]{[[foo]{[top:[expression]]}]}]]}]");
  }

  @Override
  protected void runPass() {
    EliminateEmptyRulesetNodes pass = new EliminateEmptyRulesetNodes(
        tree.getMutatingVisitController());
    pass.runPass();
  }
}
