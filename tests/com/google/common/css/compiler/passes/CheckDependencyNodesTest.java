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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

/**
 * Tests for {@link CheckDependencyNodes}.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class CheckDependencyNodesTest extends NewFunctionalTestBase {

  private CheckDependencyNodes processDependencyNodes;

  @Override
  protected void runPass() {
    processDependencyNodes = new CheckDependencyNodes(
        tree.getMutatingVisitController(), errorManager);
    processDependencyNodes.runPass();
  }

  public void testOrdinaryProvideRequire() throws GssParserException {
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "first.css", "@provide 'foo.bar';",
        "second.css", "@require 'foo.bar';");
    parseAndRun(fileNameToGss);
    assertEquals(ImmutableList.of("foo.bar"),
        processDependencyNodes.getProvidesInOrder());
  }

  public void testMissingProvide() throws GssParserException {
    parseAndRun("@require 'foo.bar';", "Missing provide for: foo.bar");
  }

  public void testDuplicateProvide() throws GssParserException {
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "first.css", "@provide 'foo.bar';",
        "second.css", "@provide 'foo.bar';");
    parseAndRun(fileNameToGss, "Duplicate provide for: foo.bar");
  }

  public void testDependencyOrder() throws GssParserException {
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "first.css", "@provide 'foo';",
        "second.css", "@provide 'bar';",
        "third.css", "@provide 'baz'; @require 'foo'; @require 'bar';",
        "fourth.css", "@provide 'buzz'; @require 'baz';",
        "fifth.css", "@require 'buzz';");
    parseAndRun(fileNameToGss);
    assertEquals(ImmutableList.of("foo", "bar", "baz", "buzz"),
        processDependencyNodes.getProvidesInOrder());
  }
}
