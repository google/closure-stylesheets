/*
 * Copyright 2011 Google Inc.
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
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.GssFunctionException;

import junit.framework.TestCase;

import java.util.List;

/**
 * unit tests for {@link ArithmeticGssFunctions}.
 *
 */
public class ArithmeticGssFunctionsTest extends TestCase {

  public void testNumberOfArgs() {
    ArithmeticGssFunctions.Plus funct = new ArithmeticGssFunctions.Plus();
    Integer argNumber = funct.getNumExpectedArguments();
    assertNull("plus() should accept a variable number of arguments",
        argNumber);
  }

  public void testPlus() {
    ArithmeticGssFunctions.Plus funct = new ArithmeticGssFunctions.Plus();
    Float twoArgResult = funct.compute(ImmutableList.of(new Float(1.5),
        new Float(3.6)));
    assertEquals(new Float(5.1), twoArgResult);
    Float threeArgResult = funct.compute(ImmutableList.of(new Float(5.1),
        new Float(6.3), new Float(-2.0)));
    assertEquals(new Float(9.4), threeArgResult);
  }

  public void testMinus() {
    ArithmeticGssFunctions.Minus funct = new ArithmeticGssFunctions.Minus();
    Float result = funct.compute(ImmutableList.of(new Float(1.5),
        new Float(3.6)));
    assertEquals(new Float(-2.1), result);
  }

  public void testMult() {
    ArithmeticGssFunctions.Mult funct = new ArithmeticGssFunctions.Mult();
    Float result = funct.compute(ImmutableList.of(new Float(2.0),
        new Float(2.0)));
    assertEquals(new Float(4.0), result);
    Float twoArgResultWithZero = funct.compute(ImmutableList.of(new Float(5.1),
        new Float(0.0)));
    assertEquals(new Float(0.0), twoArgResultWithZero);
  }

  public void testDiv() {
    ArithmeticGssFunctions.Div funct = new ArithmeticGssFunctions.Div();
    Float twoArgResult = funct.compute(ImmutableList.of(new Float(-5.0),
        new Float(2.0)));
    assertEquals(twoArgResult, new Float(-2.5));
    Float twoArgResultWithDivideByZero = funct.compute(ImmutableList.of(
        new Float(5.1), new Float(0.0)));
    assertTrue(Float.isInfinite(twoArgResultWithDivideByZero));
  }

  public void testMin() {
    ArithmeticGssFunctions.Min funct = new ArithmeticGssFunctions.Min();
    Float result = funct.compute(ImmutableList.of(new Float(2.2),
        new Float(6.0)));
    assertEquals(new Float(2.2), result);
  }

  public void testMax() {
    ArithmeticGssFunctions.Max funct = new ArithmeticGssFunctions.Max();
    Float result = funct.compute(ImmutableList.of(new Float(-2.2),
        new Float(-4.0)));
    assertEquals(new Float(-2.2), result);
  }

  public void testWithThreeNodes() throws GssFunctionException {
    ArithmeticGssFunctions.Max funct = new ArithmeticGssFunctions.Max();
    List<CssValueNode> args = Lists.newArrayList();
    args.add(new CssNumericNode("3", null));
    args.add(new CssNumericNode("5", null));
    args.add(new CssLiteralNode("px", null));

    List<CssValueNode> resultList = funct.getCallResultNodes(args, null);
    int resultListSize = resultList.size();
    assertEquals(1, resultListSize);
    CssValueNode resultNode = resultList.get(0);
    assertEquals("5px", resultNode.toString());
  }

  public void testWithFiveNodes() throws GssFunctionException {
    ArithmeticGssFunctions.Mult funct = new ArithmeticGssFunctions.Mult();
    List<CssValueNode> args = Lists.newArrayList();
    args.add(new CssNumericNode("2", null));
    args.add(new CssNumericNode("3", null));
    args.add(new CssNumericNode("4", null));
    args.add(new CssNumericNode("5", null));
    args.add(new CssLiteralNode("em", null));

    List<CssValueNode> resultList = funct.getCallResultNodes(args, null);
    int resultListSize = resultList.size();
    assertEquals(1, resultListSize);
    CssValueNode resultNode = resultList.get(0);
    assertEquals("120em", resultNode.toString());
  }

  public void testGetCallResultString() throws GssFunctionException {
    ArithmeticGssFunctions.Minus funct = new ArithmeticGssFunctions.Minus();
    assertEquals("8ex",
        funct.getCallResultString(ImmutableList.of("20", "2", "10", "ex")));
  }
}
