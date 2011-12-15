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

import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

import java.util.Set;

/**
 * Unit test for {@link VerifyRecognizedProperties}.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class VerifyRecognizedPropertiesTest extends NewFunctionalTestBase {

  private Set<String> allowedUnrecognizedProperties;

  @Override
  public void setUp() {
    allowedUnrecognizedProperties = ImmutableSet.of();
  }

  @Override
  protected void runPass() {
    VerifyRecognizedProperties pass = new VerifyRecognizedProperties(
        allowedUnrecognizedProperties, tree.getVisitController(), errorManager);
    pass.runPass();
  }

  public void testAllValidProperties() throws GssParserException {
    parseAndRun("div {color: blue; background-color: green; }");
  }

  public void testUnrecognizedProperties() throws GssParserException {
    parseAndRun("div {colour: blue; background-colour: green; }",
        "colour is an unrecognized property",
        "background-colour is an unrecognized property");
  }

  public void testAllowedUnrecognizedProperties() throws GssParserException {
    allowedUnrecognizedProperties = ImmutableSet.of("-webkit-new-fanciness");
    parseAndRun("div { -webkit-new-fanciness: red; }");
  }
}
