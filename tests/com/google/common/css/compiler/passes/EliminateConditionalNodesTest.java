/*
 * Copyright 2008 Google Inc.
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
import com.google.common.css.compiler.ast.FunctionalTestBase;

import java.util.Set;

/**
 * Unit tests for {@link EliminateConditionalNodes}.
 *
 */
public class EliminateConditionalNodesTest extends FunctionalTestBase {

  private Set<String> trueConditions;

  public void testSimple1() {
    trueConditions = ImmutableSet.of("COND");
    testTreeConstruction(
        "@if COND {"
        + "  foo { top : expression('cond') }"
        + "} @else {"
        + "  foo { top : expression('!cond') }"
        + "}",
        "[[foo]{[top:[expression('cond')]]}]");
  }

  public void testSimple2() {
    trueConditions = ImmutableSet.<String>of();
    testTreeConstruction(
        "@if COND {"
        + "  foo { top : expression('cond') }"
        + "} @else {"
        + "  foo { top : expression('!cond') }"
        + "}",
        "[[foo]{[top:[expression('!cond')]]}]");
  }

  public void testSimple3() {
    trueConditions = ImmutableSet.of("!COND1");
    testTreeConstruction(
        "@if COND1 {"
        + "  foo { top : expression('cond1') }"
        + "} @elseif COND2 {"
        + "  foo { top : expression('cond2') }"
        + "} @else {"
        + "  foo { top : expression }"
        + "}",
        "[[foo]{[top:[expression]]}]");
  }

  public void testSimple4() {
    trueConditions = ImmutableSet.of("COND3");
    testTreeConstruction(
        "@if COND1 {"
        + "  foo { top : expression('cond1') }"
        + "} @elseif COND2 {"
        + "  foo { top : expression('cond2') }"
        + "} @else {"
        + "  foo { top : expression }"
        + "}",
        "[[foo]{[top:[expression]]}]");
  }

  public void testSimple5() {
    trueConditions = ImmutableSet.<String>of();
    testTreeConstruction(
        "@if COND1 {"
        + "  foo { top : expression('cond1') }"
        + "} @elseif COND2 {"
        + "  foo { top : expression('cond2') }"
        + "} @else {"
        + "  foo { top : expression }"
        + "}",
        "[[foo]{[top:[expression]]}]");
  }

  public void testSimple6() {
    trueConditions = ImmutableSet.of("COND3");
    testTreeConstruction(
        "@if COND1 {"
        + "  foo { top : expression('cond1') }"
        + "} @elseif COND2 {"
        + "  foo { top : expression('cond2') }"
        + "} @else {"
        + "  foo { top : expression }"
        + "}",
        "[[foo]{[top:[expression]]}]");
  }

  public void testSimple7() {
    trueConditions = ImmutableSet.of("COND1");
    testTreeConstruction(
        "@if COND1 {"
        + "  foo { top : expression('cond1') }"
        + "}"
        + "@if !COND2 {"
        + "  foo { top : expression }"
        + "}",
        "[[foo]{[top:[expression('cond1')]]}, "
        + "[foo]{[top:[expression]]}]");
  }

  public void testComplex1() {
    trueConditions = ImmutableSet.of("COND1", "!COND2");
    testTreeConstruction(
        "@if COND1 {"
        + "  @if COND2 {"
        + "    @def COLOR red;"
        + "    foo { color: COLOR }"
        + "  } @else {"
        + "    foo { border: 2px }"
        + "  }"
        + "} @elseif COND2 {"
        + "  foo { top : expression('cond2') }"
        + "} @else {"
        + "  foo { top : expression }"
        + "}",
        "[[foo]{[border:[2px]]}]");
  }

  public void testComplex2() {
    trueConditions = ImmutableSet.of("COND1", "COND2");
    testTreeConstruction(
        "@if COND1 {"
        + "  @if COND2 {"
        + "    @def COLOR red;"
        + "    foo { color: COLOR }"
        + "  } @else {"
        + "    foo { border: 2px }"
        + "  }"
        + "} @elseif COND2 {"
        + "  foo { top : expression('cond2') }"
        + "} @else {"
        + "  foo { top : expression }"
        + "}",
        "[@def COLOR [red], [foo]{[color:[COLOR]]}]");
  }

  public void testComplex3() {
    trueConditions = ImmutableSet.of("COND2");
    testTreeConstruction(
        "@if COND1 {"
        + "  @if COND2 {"
        + "    @def COLOR red;"
        + "    foo { color: COLOR }"
        + "  } @else {"
        + "    foo { border: 2px }"
        + "  }"
        + "} @elseif COND2 {"
        + "  foo { top : expression('cond2') }"
        + "} @else {"
        + "  foo { top : expression }"
        + "}",
        "[[foo]{[top:[expression('cond2')]]}]");
  }

  public void testComplex4() {
    trueConditions = ImmutableSet.of("COND1", "COND3");
    testTreeConstruction(
        "@if COND1 {"
        + "  @if COND2 {"
        + "    @def COLOR red;"
        + "    foo { color: COLOR }"
        + "  } @elseif COND3 {"
        + "    foo { border: 2px }"
        + "  }"
        + "} @elseif COND2 {"
        + "  foo { top : expression('cond2') }"
        + "} @else {"
        + "  foo { top : expression }"
        + "}",
        "[[foo]{[border:[2px]]}]");
  }

  public void testComplex5() {
    trueConditions = ImmutableSet.of("COND1", "COND3");
    testTreeConstruction(
        "@media print {"
        + "  @if COND1 {"
        + "    @if COND2 {"
        + "      @def COLOR red;"
        + "      foo { color: COLOR }"
        + "    } @elseif COND3 {"
        + "      foo { border: 2px }"
        + "    }"
        + "  } @elseif COND2 {"
        + "    foo { top : expression('cond2') }"
        + "  } @else {"
        + "    foo { top : expression }"
        + "  }"
        + "}",
        "[@media[print]{[[foo]{[border:[2px]]}]}]");
  }

  @Override
  protected void runPass() {
    EliminateConditionalNodes pass = new EliminateConditionalNodes(
        tree.getMutatingVisitController(), trueConditions);
    pass.runPass();
  }
}
