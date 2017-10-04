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

package com.google.common.css.compiler.passes;

import com.google.common.css.Vendor;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit test for {@link RemoveVendorSpecificProperties}.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
@RunWith(JUnit4.class)
public class RemoveVendorSpecificPropertiesTest extends NewFunctionalTestBase {

  private Vendor vendor;

  @Before
  public void setUp() {
    vendor = null;
  }

  @Override
  protected void runPass() {
    RemoveVendorSpecificProperties pass = new RemoveVendorSpecificProperties(
        vendor, tree.getMutatingVisitController());
    pass.runPass();
  }

  @Test
  public void testAllowWebkit() throws GssParserException {
    vendor = Vendor.WEBKIT;
    test(linesToString(
        ".button {",
        "  -moz-transform: matrix(1, 0, 0.6, 1, 15em, 0);", 
        "  -webkit-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  -o-transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "  -ms-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "}"),
        linesToString(
        ".button {",
        "  -webkit-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "}"));
  }

  @Test
  public void testAllowMozilla() throws GssParserException {
    vendor = Vendor.MOZILLA;
    test(linesToString(
        ".button {",
        "  -moz-transform: matrix(1, 0, 0.6, 1, 15em, 0);", 
        "  -webkit-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  -o-transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "  -ms-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "}"),
        linesToString(
        ".button {",
        "  -moz-transform: matrix(1, 0, 0.6, 1, 15em, 0);", 
        "  transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "}"));
  }

  @Test
  public void testAllowMicrosoft() throws GssParserException {
    vendor = Vendor.MICROSOFT;
    test(linesToString(
        ".button {",
        "  -moz-transform: matrix(1, 0, 0.6, 1, 15em, 0);", 
        "  -webkit-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  -o-transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "  -ms-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "}"),
        linesToString(
        ".button {",
        "  -ms-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "}"));
  }

  @Test
  public void testAllowOpera() throws GssParserException {
    vendor = Vendor.OPERA;
    test(linesToString(
        ".button {",
        "  -moz-transform: matrix(1, 0, 0.6, 1, 15em, 0);", 
        "  -webkit-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  -o-transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "  -ms-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "}"),
        linesToString(
        ".button {",
        "  -o-transform: matrix(1, 0, 0.6, 1, 250, 0);", 
        "  transform: matrix(1, 0, 0.6, 1, 250, 0);",
        "}"));
  }
}
