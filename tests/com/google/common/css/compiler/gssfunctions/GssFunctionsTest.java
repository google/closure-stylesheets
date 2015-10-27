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

import java.util.Locale;

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


  public void testDivGetCallResultString_otherLocale() throws GssFunctionException {
    Locale.setDefault(Locale.FRANCE);
    try {
      GssFunctions.Div funct = new GssFunctions.Div();
      assertEquals("103.06748466px",
          funct.getCallResultString(ImmutableList.of("100800px", "978")));
    } finally {
      Locale.setDefault(Locale.US);
    }
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

  public void testMakeTranslucent() {
    GssFunctions.MakeTranslucent function = new GssFunctions.MakeTranslucent();
    // Check alpha conversion. Note that .25 and .5 are not exactly
    // representable.
    assertEquals("rgba(255,255,255,1)",
        function.getCallResultString(ImmutableList.of("#fff", "1")));
    assertEquals("rgba(255,255,255,0.251)",
        function.getCallResultString(ImmutableList.of("#fff", "0.25")));
    assertEquals("rgba(255,255,255,0.502)",
        function.getCallResultString(ImmutableList.of("#fff", "0.5")));
    assertEquals("rgba(255,255,255,0)",
        function.getCallResultString(ImmutableList.of("#fff", "0")));

    // Check that RGB values are preserved
    assertEquals("rgba(0,0,0,1)",
        function.getCallResultString(ImmutableList.of("#000", "1")));
    assertEquals("rgba(17,34,51,1)",
        function.getCallResultString(ImmutableList.of("#123", "1")));

    // Check various input formats
    assertEquals("rgba(16,32,48,0.251)",
        function.getCallResultString(ImmutableList.of("#102030", ".251")));
    assertEquals("rgba(255,0,0,0.251)",
        function.getCallResultString(ImmutableList.of("red", ".251")));
    assertEquals("rgba(1,2,3,0.251)",
        function.getCallResultString(ImmutableList.of("rgb(1, 2, 3)", ".251")));
    assertEquals("rgba(1,2,3,0.251)",
        function.getCallResultString(
            ImmutableList.of("rgba(1, 2, 3, 0)", ".251")));
    assertEquals("rgba(1,2,3,0.2)",
        function.getCallResultString(
            ImmutableList.of("rgba(1, 2, 3, .7)", ".2")));
  }


  public void testMakeTranslucent_otherLocale() {
    Locale.setDefault(Locale.FRANCE);
    try {

      GssFunctions.MakeTranslucent function = new GssFunctions.MakeTranslucent();
      assertEquals(
          "rgba(16,32,48,0.251)",
          function.getCallResultString(ImmutableList.of("#102030", ".251")));
    } finally {
      Locale.setDefault(Locale.US);
    }
  }

  public void testSaturateColor() throws GssFunctionException {
    GssFunctions.SaturateColor function = new GssFunctions.SaturateColor();
    assertEquals("#4671EC",
        function.getCallResultString(ImmutableList.of("#5a7bd8", "20")));
    assertEquals("#80FF00",
        function.getCallResultString(ImmutableList.of("#80e619", "20")));
  }

  public void testDesaturateColor() throws GssFunctionException {
    GssFunctions.DesaturateColor function = new GssFunctions.DesaturateColor();
    assertEquals("#6E85C4",
        function.getCallResultString(ImmutableList.of("#5a7bd8", "20")));
    assertEquals("#80CD32",
        function.getCallResultString(ImmutableList.of("#80e619", "20")));
  }

  public void testGreyscale() throws GssFunctionException {
    GssFunctions.Greyscale function = new GssFunctions.Greyscale();
    assertEquals("#999999",
        function.getCallResultString(ImmutableList.of("#5a7bd8")));
    assertEquals("#808080",
        function.getCallResultString(ImmutableList.of("#80e619")));
  }

  public void testLighten() throws GssFunctionException {
    GssFunctions.Lighten function = new GssFunctions.Lighten();
    assertEquals("#839CE2",
        function.getCallResultString(ImmutableList.of("#5a7bd8", "10")));
    assertEquals("#B3F075",
        function.getCallResultString(ImmutableList.of("#80e619", "20")));
  }

  public void testDarken() throws GssFunctionException {
    GssFunctions.Darken function = new GssFunctions.Darken();
    assertEquals("#315ACE",
        function.getCallResultString(ImmutableList.of("#5a7bd8", "10")));
    assertEquals("#4D8A0F",
        function.getCallResultString(ImmutableList.of("#80e619", "20")));
  }

  /*
   * Test that calling the function with the given arguments throws a
   * GssFunctionException.
   */
  private void testFunctionCallFail(GssFunction funct, ImmutableList<String> args) {
    try {
      funct.getCallResultString(args);
      fail();
    } catch (GssFunctionException expected) {
      // Expected to fail.
    }
  }

  public void testConcat() throws Exception {
    GssFunctions.Concat funct = new GssFunctions.Concat();
    assertEquals("'ab'", funct.getCallResultString(ImmutableList.of("a", "b")));
    assertEquals("'a'", funct.getCallResultString(ImmutableList.of("a")));
    assertEquals("'a'", funct.getCallResultString(ImmutableList.of("'a'")));
    assertEquals("'a'", funct.getCallResultString(ImmutableList.of("\"a\"")));
    assertEquals("'abc'", funct.getCallResultString(ImmutableList.of("a", "b", "c")));
    assertEquals("''", funct.getCallResultString(ImmutableList.<String>of()));
    assertEquals("'ab'", funct.getCallResultString(ImmutableList.of("'a'", "'b'")));
    assertEquals("'\"'", funct.getCallResultString(ImmutableList.of("'\"'")));
    assertEquals("'\\''", funct.getCallResultString(ImmutableList.of("'")));
  }
}
