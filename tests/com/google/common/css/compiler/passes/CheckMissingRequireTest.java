/*
 * Copyright 2013 Google Inc.
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

import java.util.List;

/**
 * Tests for {@link CheckMissingRequire}.
 *
 */
public class CheckMissingRequireTest extends NewFunctionalTestBase {

  protected void runPasses(TestErrorManager errorMgr) {
    List<CssCompilerPass> l = Lists.newArrayList();
    l.add(new CreateMixins(tree.getMutatingVisitController(), errorManager));
    l.add(new CreateDefinitionNodes(tree.getMutatingVisitController(), errorManager));
    l.add(new CreateConstantReferences(tree.getMutatingVisitController()));
    l.add(new CheckDependencyNodes(tree.getMutatingVisitController(), errorMgr));
    l.add(new CheckMissingRequire(tree.getMutatingVisitController(), errorMgr));
    for (CssCompilerPass pass : l) {
      pass.runPass();
    }
  }

  public void testBaseCase1() throws GssParserException {
    String base =  ""
        + "@provide 'oz.base';"
        + "@def OZ_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'oz.streamitem';"
        + "@require 'oz.base';"
        + ".nav {"
        + "  color: OZ_BASE_COLOR;"
        + "}";
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testBaseCase2() throws GssParserException {
    String base =  ""
        + "@provide 'oz.base';"
        + "@def OZ_BASE_COLOR     #fff;"
        + "@def OZ_BASE_BG_COLOR  OZ_BASE_COLOR;";
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of("base.gss", base);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testMissingRequire() throws GssParserException {
    String base =  ""
        + "@provide 'oz.base';"
        + "@def OZ_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'oz.streamitem';"
        + ".nav {"
        + "  color: OZ_BASE_COLOR;"
        + "}";
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {"Missing @require for constant OZ_BASE_COLOR."};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testMissingRequireInDef() throws GssParserException {
    String base =  ""
        + "@provide 'oz.base';"
        + "@def OZ_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'oz.streamitem';"
        + "@def OZ_STREAM_ITEM_COLOR  OZ_BASE_COLOR;"
        + "@def OZ_BASE_FONT_SIZE     10px;";
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {"Missing @require for constant OZ_BASE_COLOR."};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testMissingRequireDefMixin() throws GssParserException {
    String base =  ""
        + "@provide 'oz.base';"
        + "@def OZ_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'oz.streamitem';"
        + "@def OZ_OVERLAY_BG_COLOR  #fff;"
        + "@def OZ_OVERLAY_OPAQUE_BG_COLOR  #fee;"
        + ".ozStreamOverlay {"
        + "@mixin background_color(OZ_OVERLAY_BG_COLOR, OZ_OVERLAY_OPAQUE_BG_COLOR);"
        + "}";

    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {"Missing @require for mixin background_color."};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }
}
