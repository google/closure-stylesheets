/*
 * Copyright 2012 Google Inc.
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

package com.google.common.css.compiler.gssfunctions;

import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;

import junit.framework.TestCase;

/**
 * Unit tests for {@link GssFunctions}. Specifically checks for correctness of
 * getCallResultString results from the GssFunctions since
 * {@link DefaultGssFunctionMapProviderTest} tests getCallResultNodes results.
 *
 */
public class GssFunctionsTest extends TestCase {

  public void testAddGetCallResultString() throws GssFunctionException {
    GssFunctions.AddToNumericValue funct = new GssFunctions.AddToNumericValue();
    assertEquals("42px",
        funct.getCallResultString(ImmutableList.of("12px", "30px")));
    assertEquals("0px",
        funct.getCallResultString(ImmutableList.of("10px", "-10px")));
    assertEquals("22",
        funct.getCallResultString(ImmutableList.of("10", "12")));
  }

  public void testAddGetCallResultString_mismatchedUnitsFail() {
    GssFunctions.AddToNumericValue funct = new GssFunctions.AddToNumericValue();
    testFunctionCallFail(funct, ImmutableList.of("12em", "30px"));
  }

  public void testSubGetCallResultString() throws GssFunctionException {
    GssFunctions.SubtractFromNumericValue funct =
        new GssFunctions.SubtractFromNumericValue();
    assertEquals("30px",
        funct.getCallResultString(ImmutableList.of("60px", "30px")));
    assertEquals("-30px",
        funct.getCallResultString(ImmutableList.of("0px", "30px")));
    assertEquals("-8",
        funct.getCallResultString(ImmutableList.of("1", "9")));
  }

  public void testSubGetCallResultString_mismatchedUnitsFail() {
    GssFunctions.SubtractFromNumericValue funct =
        new GssFunctions.SubtractFromNumericValue();
    testFunctionCallFail(funct, ImmutableList.of("60px", "30em"));
  }

  public void testMultGetCallResultString() throws GssFunctionException {
    GssFunctions.Mult funct = new GssFunctions.Mult();
    assertEquals("42px",
        funct.getCallResultString(ImmutableList.of("21px", "2")));
    assertEquals("-42px",
        funct.getCallResultString(ImmutableList.of("21px", "-2")));
  }

  public void testMultGetCallResultString_noUnits() throws GssFunctionException {
    GssFunctions.Mult funct = new GssFunctions.Mult();
    assertEquals("42",
        funct.getCallResultString(ImmutableList.of("21", "2")));
    assertEquals("-42",
        funct.getCallResultString(ImmutableList.of("21", "-2")));
  }

  public void testDivGetCallResultString() throws GssFunctionException {
    GssFunctions.Div funct = new GssFunctions.Div();
    assertEquals("30px",
        funct.getCallResultString(ImmutableList.of("60px", "2")));
    assertEquals("-30px",
        funct.getCallResultString(ImmutableList.of("60px", "-2")));
    assertEquals("103.06748466px",
        funct.getCallResultString(ImmutableList.of("100800px", "978")));
    assertEquals("15%",
        funct.getCallResultString(ImmutableList.of("30%", "2")));
  }

  public void testDivGetCallResultString_noUnits() throws GssFunctionException {
    GssFunctions.Div funct = new GssFunctions.Div();
    assertEquals("30",
        funct.getCallResultString(ImmutableList.of("60", "2")));
    assertEquals("-30",
        funct.getCallResultString(ImmutableList.of("60", "-2")));
    assertEquals("103.06748466",
        funct.getCallResultString(ImmutableList.of("100800", "978")));
  }

  public void testMaxGetCallResultString() throws GssFunctionException {
    GssFunctions.MaxValue funct = new GssFunctions.MaxValue();
    assertEquals("42px",
        funct.getCallResultString(ImmutableList.of("42px", "-42px")));
    assertEquals("0px",
        funct.getCallResultString(ImmutableList.of("0px", "0px")));
    assertEquals("11",
        funct.getCallResultString(ImmutableList.of("-1", "11")));
  }

  public void testMaxGetCallResultString_mismatchedUnitsFail() {
    GssFunctions.MaxValue funct = new GssFunctions.MaxValue();
    testFunctionCallFail(funct, ImmutableList.of("60px", "30em"));
  }

  public void testMinGetCallResultString() throws GssFunctionException {
    GssFunctions.MinValue funct = new GssFunctions.MinValue();
    assertEquals("-42px",
        funct.getCallResultString(ImmutableList.of("42px", "-42px")));
    assertEquals("0px",
        funct.getCallResultString(ImmutableList.of("0px", "0px")));
    assertEquals("5",
        funct.getCallResultString(ImmutableList.of("5", "10")));
  }

  public void testMinGetCallResultString_mismatchedUnitsFail() {
    GssFunctions.MinValue funct = new GssFunctions.MinValue();
    testFunctionCallFail(funct, ImmutableList.of("60px", "30em"));
  }

  public void testScalarLeftAssociativeOperator_unexpectedUnitsFail() {
    GssFunctions.Mult mult = new GssFunctions.Mult();
    testFunctionCallFail(mult, ImmutableList.of("42", "2px"));
    testFunctionCallFail(mult, ImmutableList.of("42px", "2px"));
    testFunctionCallFail(mult, ImmutableList.of("42px", "2em"));

    GssFunctions.Div div = new GssFunctions.Div();
    testFunctionCallFail(div, ImmutableList.of("42", "2px"));
    testFunctionCallFail(div, ImmutableList.of("42px", "2px"));
    testFunctionCallFail(div, ImmutableList.of("42px", "2em"));
  }


  /*
   * Test that calling the function with the given arguments throws a
   * GssFunctionException.
   */
  private void testFunctionCallFail(GssFunction funct,
      ImmutableList<String> args) {
    try {
      funct.getCallResultString(args);
      fail();
    } catch (GssFunctionException expected) {
      // Expected to fail.
    }
  }
}
