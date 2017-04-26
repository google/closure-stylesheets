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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Functional tests for {@link EliminateUnitsFromZeroNumericValues}.
 *
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
@RunWith(JUnit4.class)
public class EliminateUnitsFromZeroNumericValuesFunctionalTest extends PassesTestBase {

  @Test
  public void test1() {
    testTreeConstruction(
        "foo { border: 0px 0.0px; }",
        "[[foo]{[border:[[0][0]];]}]");
  }

  @Test
  public void test2() {
    testTreeConstruction(
        "foo { padding: 0px 3px; }",
        "[[foo]{[padding:[[0][3px]];]}]");
  }

  @Test
  public void test3() {
    testTreeConstruction(
        "foo { padding: 000.000px 3.000px; }",
        "[[foo]{[padding:[[0][3px]];]}]");
  }

  @Test
  public void test4() {
    testTreeConstruction(
        "foo { padding: 00.5px 3.5px; }",
        "[[foo]{[padding:[[.5px][3.5px]];]}]");
  }

  @Test
  public void test5() {
    testTreeConstruction(
        "foo { padding: 050.05px 03.050px; }",
        "[[foo]{[padding:[[50.05px][3.05px]];]}]");
  }

  @Test
  public void testRemovableUnit1() {
    testTreeConstruction(
        "foo { width: 0in }",
        "[[foo]{[width:[[0]];]}]");
  }

  @Test
  public void testRemovableUnit2() {
    testTreeConstruction(
        "foo { width: 0em }",
        "[[foo]{[width:[[0]];]}]");
  }

  @Test
  public void testRemovableUnit3() {
    testTreeConstruction(
        "foo { width: 0cm }",
        "[[foo]{[width:[[0]];]}]");
  }

  @Test
  public void testUnitInTimesOperandOfCalcExprIsRemoved() {
    testTreeConstruction(
        "foo { width: calc(0px * 42) }",
        "[[foo]{[width:[calc([[0]*[42]])];]}]");
  }

  @Test
  public void testUnitInDivOperandOfCalcExprIsRemoved() {
    testTreeConstruction(
        "foo { width: calc(0px / 42) }",
        "[[foo]{[width:[calc([[0]/[42]])];]}]");
  }

  @Test
  public void testUnremovableUnit1() {
    testTreeConstruction(
        ".html5-progress-item { -webkit-transition:all 0s linear 0s }",
        "[[.html5-progress-item]{[-webkit-transition:[[all][0s][linear][0s]];]}]");
  }

  @Test
  public void testUnremovableUnit2() {
    testTreeConstruction(
        "foo { width: 0.0% }",
        "[[foo]{[width:[[0%]];]}]");
  }

  @Test
  public void testUnitInPlusOperandOfCalcExprIsNotRemoved() {
    testTreeConstruction(
        "foo { width: calc(0px + 32) }",
        "[[foo]{[width:[calc([[0px] + [32]])];]}]");
  }

  @Test
  public void testUnitInMinusOperandOfCalcExprIsNotRemoved() {
    testTreeConstruction(
        "foo { width: calc(32 - 0px) }",
        "[[foo]{[width:[calc([[32] - [0px]])];]}]");
  }

  @Test
  public void testZeroesInComplexCalcExprs() {
    testTreeConstruction(
        "foo { width: calc(0px + (0px * (35 - 0px))) }",
        "[[foo]{[width:[calc([[0px] + [([0]*[([35] - [0px])])]])];]}]");
  }

  @Override
  protected void runPass() {
    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(
            tree.getMutatingVisitController());
    pass.runPass();
  }
}
