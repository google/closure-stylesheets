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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;
import java.util.Locale;
import junit.framework.TestCase;

/**
 * Unit tests for {@link GssFunctions}. Specifically checks for correctness of getCallResultString
 * results from the GssFunctions since {@link DefaultGssFunctionMapProviderTest} tests
 * getCallResultNodes results.
 *
 */
public class GssFunctionsTest extends TestCase {

  public void testAddGetCallResultString() throws GssFunctionException {
    GssFunctions.AddToNumericValue funct = new GssFunctions.AddToNumericValue();
    assertThat(funct.getCallResultString(ImmutableList.of("12px", "30px"))).isEqualTo("42px");
    assertThat(funct.getCallResultString(ImmutableList.of("10px", "-10px"))).isEqualTo("0px");
    assertThat(funct.getCallResultString(ImmutableList.of("10", "12"))).isEqualTo("22");
  }

  public void testAddGetCallResultString_mismatchedUnitsFail() {
    GssFunctions.AddToNumericValue funct = new GssFunctions.AddToNumericValue();
    testFunctionCallFail(funct, ImmutableList.of("12em", "30px"));
  }

  public void testSubGetCallResultString() throws GssFunctionException {
    GssFunctions.SubtractFromNumericValue funct =
        new GssFunctions.SubtractFromNumericValue();
    assertThat(funct.getCallResultString(ImmutableList.of("60px", "30px"))).isEqualTo("30px");
    assertThat(funct.getCallResultString(ImmutableList.of("0px", "30px"))).isEqualTo("-30px");
    assertThat(funct.getCallResultString(ImmutableList.of("1", "9"))).isEqualTo("-8");
  }

  public void testSubGetCallResultString_mismatchedUnitsFail() {
    GssFunctions.SubtractFromNumericValue funct =
        new GssFunctions.SubtractFromNumericValue();
    testFunctionCallFail(funct, ImmutableList.of("60px", "30em"));
  }

  public void testMultGetCallResultString() throws GssFunctionException {
    GssFunctions.Mult funct = new GssFunctions.Mult();
    assertThat(funct.getCallResultString(ImmutableList.of("21px", "2"))).isEqualTo("42px");
    assertThat(funct.getCallResultString(ImmutableList.of("21px", "-2"))).isEqualTo("-42px");
  }

  public void testMultGetCallResultString_noUnits() throws GssFunctionException {
    GssFunctions.Mult funct = new GssFunctions.Mult();
    assertThat(funct.getCallResultString(ImmutableList.of("21", "2"))).isEqualTo("42");
    assertThat(funct.getCallResultString(ImmutableList.of("21", "-2"))).isEqualTo("-42");
  }

  public void testDivGetCallResultString() throws GssFunctionException {
    GssFunctions.Div funct = new GssFunctions.Div();
    assertThat(funct.getCallResultString(ImmutableList.of("60px", "2"))).isEqualTo("30px");
    assertThat(funct.getCallResultString(ImmutableList.of("60px", "-2"))).isEqualTo("-30px");
    assertThat(funct.getCallResultString(ImmutableList.of("100800px", "978")))
        .isEqualTo("103.06748466px");
    assertThat(funct.getCallResultString(ImmutableList.of("30%", "2"))).isEqualTo("15%");
  }


  public void testDivGetCallResultString_otherLocale() throws GssFunctionException {
    Locale.setDefault(Locale.FRANCE);
    try {
      GssFunctions.Div funct = new GssFunctions.Div();
      assertThat(funct.getCallResultString(ImmutableList.of("100800px", "978")))
          .isEqualTo("103.06748466px");
    } finally {
      Locale.setDefault(Locale.US);
    }
  }

  public void testDivGetCallResultString_noUnits() throws GssFunctionException {
    GssFunctions.Div funct = new GssFunctions.Div();
    assertThat(funct.getCallResultString(ImmutableList.of("60", "2"))).isEqualTo("30");
    assertThat(funct.getCallResultString(ImmutableList.of("60", "-2"))).isEqualTo("-30");
    assertThat(funct.getCallResultString(ImmutableList.of("100800", "978")))
        .isEqualTo("103.06748466");
  }

  public void testMaxGetCallResultString() throws GssFunctionException {
    GssFunctions.MaxValue funct = new GssFunctions.MaxValue();
    assertThat(funct.getCallResultString(ImmutableList.of("42px", "-42px"))).isEqualTo("42px");
    assertThat(funct.getCallResultString(ImmutableList.of("0px", "0px"))).isEqualTo("0px");
    assertThat(funct.getCallResultString(ImmutableList.of("-1", "11"))).isEqualTo("11");
  }

  public void testMaxGetCallResultString_mismatchedUnitsFail() {
    GssFunctions.MaxValue funct = new GssFunctions.MaxValue();
    testFunctionCallFail(funct, ImmutableList.of("60px", "30em"));
  }

  public void testMinGetCallResultString() throws GssFunctionException {
    GssFunctions.MinValue funct = new GssFunctions.MinValue();
    assertThat(funct.getCallResultString(ImmutableList.of("42px", "-42px"))).isEqualTo("-42px");
    assertThat(funct.getCallResultString(ImmutableList.of("0px", "0px"))).isEqualTo("0px");
    assertThat(funct.getCallResultString(ImmutableList.of("5", "10"))).isEqualTo("5");
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
    assertThat(function.getCallResultString(ImmutableList.of("#fff", "1")))
        .isEqualTo("rgba(255,255,255,1)");
    assertThat(function.getCallResultString(ImmutableList.of("#fff", "0.25")))
        .isEqualTo("rgba(255,255,255,0.251)");
    assertThat(function.getCallResultString(ImmutableList.of("#fff", "0.5")))
        .isEqualTo("rgba(255,255,255,0.502)");
    assertThat(function.getCallResultString(ImmutableList.of("#fff", "0")))
        .isEqualTo("rgba(255,255,255,0)");

    // Check that RGB values are preserved
    assertThat(function.getCallResultString(ImmutableList.of("#000", "1")))
        .isEqualTo("rgba(0,0,0,1)");
    assertThat(function.getCallResultString(ImmutableList.of("#123", "1")))
        .isEqualTo("rgba(17,34,51,1)");

    // Check various input formats
    assertThat(function.getCallResultString(ImmutableList.of("#102030", ".251")))
        .isEqualTo("rgba(16,32,48,0.251)");
    assertThat(function.getCallResultString(ImmutableList.of("red", ".251")))
        .isEqualTo("rgba(255,0,0,0.251)");
    assertThat(function.getCallResultString(ImmutableList.of("rgb(1, 2, 3)", ".251")))
        .isEqualTo("rgba(1,2,3,0.251)");
    assertThat(function.getCallResultString(ImmutableList.of("rgba(1, 2, 3, 0)", ".251")))
        .isEqualTo("rgba(1,2,3,0.251)");
    assertThat(function.getCallResultString(ImmutableList.of("rgba(1, 2, 3, .7)", ".2")))
        .isEqualTo("rgba(1,2,3,0.2)");
  }


  public void testMakeTranslucent_otherLocale() {
    Locale.setDefault(Locale.FRANCE);
    try {

      GssFunctions.MakeTranslucent function = new GssFunctions.MakeTranslucent();
      assertThat(function.getCallResultString(ImmutableList.of("#102030", ".251")))
          .isEqualTo("rgba(16,32,48,0.251)");
    } finally {
      Locale.setDefault(Locale.US);
    }
  }

  public void testSaturateColor() throws GssFunctionException {
    GssFunctions.SaturateColor function = new GssFunctions.SaturateColor();
    assertThat(function.getCallResultString(ImmutableList.of("#5a7bd8", "20")))
        .isEqualTo("#4671EC");
    // saturation of #80e619 is 80. Adding 20 (to set saturation to 100%)
    // or more return the same result
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "20")))
        .isEqualTo("#80FF00");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "50")))
        .isEqualTo("#80FF00");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "100")))
        .isEqualTo("#80FF00");
  }

  public void testDesaturateColor() throws GssFunctionException {
    GssFunctions.DesaturateColor function = new GssFunctions.DesaturateColor();
    assertThat(function.getCallResultString(ImmutableList.of("#5a7bd8", "20")))
        .isEqualTo("#6E85C4");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "20")))
        .isEqualTo("#80CD32");
    // saturation of #80e619 is 80.4. Removing 81 (to set saturation to 0%)
    // or more return the same result
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "81")))
        .isEqualTo("#808080");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "100")))
        .isEqualTo("#808080");
  }

  public void testGreyscale() throws GssFunctionException {
    GssFunctions.Greyscale function = new GssFunctions.Greyscale();
    assertThat(function.getCallResultString(ImmutableList.of("#5a7bd8"))).isEqualTo("#999999");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619"))).isEqualTo("#808080");
  }

  public void testLighten() throws GssFunctionException {
    GssFunctions.Lighten function = new GssFunctions.Lighten();
    assertThat(function.getCallResultString(ImmutableList.of("#5a7bd8", "10")))
        .isEqualTo("#839CE2");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "20")))
        .isEqualTo("#B3F075");
    // lightness of #80e619 is 50. Adding 50 (to set lightness to 100%
    // which is white) or more return the same result
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "50")))
        .isEqualTo("#FFFFFF");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "60")))
        .isEqualTo("#FFFFFF");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "100")))
        .isEqualTo("#FFFFFF");
  }

  public void testDarken() throws GssFunctionException {
    GssFunctions.Darken function = new GssFunctions.Darken();
    assertThat(function.getCallResultString(ImmutableList.of("#5a7bd8", "10")))
        .isEqualTo("#315ACE");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "20")))
        .isEqualTo("#4D8A0F");
    // lightness of #80e619 is 50. Removing 50 (to set lightness to 0%
    // which is black) or more return the same result
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "50")))
        .isEqualTo("#000000");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "60")))
        .isEqualTo("#000000");
    assertThat(function.getCallResultString(ImmutableList.of("#80e619", "100")))
        .isEqualTo("#000000");
  }

  public void testSpin() throws GssFunctionException {
    GssFunctions.Spin function = new GssFunctions.Spin();
    String color = "#F2330D";
    assertThat(function.getCallResultString(ImmutableList.of(color, "30"))).isEqualTo("#F2A60D");
    assertThat(function.getCallResultString(ImmutableList.of(color, "-30"))).isEqualTo("#F20D5A");
    // Value of Hue is modulo 360
    assertThat(function.getCallResultString(ImmutableList.of(color, "360"))).isEqualTo(color);
    assertThat(function.getCallResultString(ImmutableList.of(color, "-360"))).isEqualTo(color);
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
    assertThat(funct.getCallResultString(ImmutableList.of("a", "b"))).isEqualTo("'ab'");
    assertThat(funct.getCallResultString(ImmutableList.of("a"))).isEqualTo("'a'");
    assertThat(funct.getCallResultString(ImmutableList.of("'a'"))).isEqualTo("'a'");
    assertThat(funct.getCallResultString(ImmutableList.of("\"a\""))).isEqualTo("'a'");
    assertThat(funct.getCallResultString(ImmutableList.of("a", "b", "c"))).isEqualTo("'abc'");
    assertThat(funct.getCallResultString(ImmutableList.<String>of())).isEqualTo("''");
    assertThat(funct.getCallResultString(ImmutableList.of("'a'", "'b'"))).isEqualTo("'ab'");
    assertThat(funct.getCallResultString(ImmutableList.of("'\"'"))).isEqualTo("'\"'");
    assertThat(funct.getCallResultString(ImmutableList.of("'"))).isEqualTo("'\\''");
  }
}
