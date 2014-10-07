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
    l.add(new CreateMixins(tree.getMutatingVisitController(), errorMgr));
    l.add(new CreateDefinitionNodes(tree.getMutatingVisitController(), errorMgr));
    l.add(new CreateConstantReferences(tree.getMutatingVisitController()));
    l.add(new CheckDependencyNodes(tree.getMutatingVisitController(), errorMgr));
    l.add(new CreateComponentNodes(tree.getMutatingVisitController(), errorMgr));
    l.add(new ProcessComponents<Object>(tree.getMutatingVisitController(), errorMgr, null));
    for (CssCompilerPass pass : l) {
      pass.runPass();
    }
    CollectProvideNamespaces collectProvides = new CollectProvideNamespaces(
        tree.getMutatingVisitController(), errorMgr);
    collectProvides.runPass();
    new CheckMissingRequire(
        tree.getMutatingVisitController(),
        errorMgr,
        collectProvides.getFilenameProvideMap(),
        collectProvides.getFilenameRequireMap(),
        collectProvides.getDefProvideMap(),
        collectProvides.getDefmixinProvideMap()).runPass();
  }

  public void testBaseCase1() throws GssParserException {
    String base =  ""
        + "@provide 'foo.base';"
        + "@def OZ_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'foo.streamitem';"
        + "@require 'foo.base';"
        + ".nav {"
        + "  color: FOO_BASE_COLOR;"
        + "}";
    String streamcomponent = ""
        + "@provide 'foo.streamcomponent';"
        + "@require 'foo.base';"
        + "@component {"
        + "  .nav {"
        + "    color: FOO_BASE_COLOR;"
        + "  }"
        + "}";
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem,
        "streamcomponent.gss", streamcomponent);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testBaseCase2() throws GssParserException {
    String base =  ""
        + "@provide 'foo.base';"
        + "@def FOO_BASE_COLOR     #fff;"
        + "@def FOO_BASE_BG_COLOR  FOO_BASE_COLOR;";
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
        + "@provide 'foo.base';"
        + "@def FOO_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'foo.streamitem';"
        + ".nav {"
        + "  color: FOO_BASE_COLOR;"
        + "}";
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {"Missing @require for constant FOO_BASE_COLOR."};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testMissingRequireFromComponent() throws GssParserException {
    String base =  ""
        + "@provide 'foo.base';"
        + "@def FOO_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'foo.streamitem';"
        + "@component {"
        + "  @def NAV_COLOR  FOO_BASE_COLOR;"
        + "  .nav {"
        + "    color: NAV_COLOR;"
        + "  }"
        + "}";
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {"Missing @require for constant FOO_BASE_COLOR."};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testMissingRequireOfComponent() throws GssParserException {
    String basecomponent =  ""
        + "@provide 'foo.basecomponent';"
        + "@component {"
        + "  @def COLOR     #fff;"
        + "}";
    String streamitem =  ""
        + "@provide 'foo.streamitem';"
        + ".nav {"
        + "  color: FOO_BASECOMPONENT_COLOR;"
        + "}";
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "basecomponent.gss", basecomponent,
        "streamitem.gss", streamitem);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {"Missing @require for constant FOO_BASECOMPONENT_COLOR."};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testMissingRequireInDef() throws GssParserException {
    String base =  ""
        + "@provide 'foo.base';"
        + "@def FOO_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'foo.streamitem';"
        + "@def FOO_STREAM_ITEM_COLOR  FOO_BASE_COLOR;"
        + "@def FOO_BASE_FONT_SIZE     10px;";
    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem);

    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {"Missing @require for constant FOO_BASE_COLOR."};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testMissingRequireDefMixin() throws GssParserException {
    String base =  ""
        + "@provide 'foo.base';"
        + "@def FOO_BASE_COLOR     #fff;"
        + "@defmixin background_color(FALLBACK_BG_COLOR) {"
        + "  background-color: FALLBACK_BG_COLOR;"
        + "}";
    String streamitem =  ""
        + "@provide 'foo.streamitem';"
        + "@def FOO_OVERLAY_BG_COLOR  #fff;"
        + "@def FOO_OVERLAY_OPAQUE_BG_COLOR  #fee;"
        + ".ozStreamOverlay {"
        + "@mixin background_color(FOO_OVERLAY_BG_COLOR, FOO_OVERLAY_OPAQUE_BG_COLOR);"
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

  public void testMissingOverrideSelectorNamespace() throws GssParserException {
    String base =  ""
        + "@provide 'foo.base';"
        + "@def FOO_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'foo.streamitem';"
        + "@require 'foo.base';"
        + "/* @overrideSelector {foo.foo} */ .nav {"
        + "  color: FOO_BASE_COLOR;"
        + "}";

    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem);
    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {"Missing @require for @overrideSelector"};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

  public void testMissingOverrideDefNamespace() throws GssParserException {
    String base =  ""
        + "@provide 'foo.base';"
        + "@def FOO_BASE_COLOR     #fff;";
    String streamitem =  ""
        + "@provide 'foo.streamitem';"
        + "@require 'foo.bar';"
        + "/* @overrideDef {foo.base} */ @def FOO_BASE_COLOR  #ffe;";

    ImmutableMap<String, String> fileNameToGss = ImmutableMap.of(
        "base.gss", base,
        "streamitem.gss", streamitem);
    parseAndBuildTree(fileNameToGss);
    String[] expectedMessages = {"Missing @require for @overrideDef"};
    TestErrorManager errorManager = new TestErrorManager(false, expectedMessages);
    runPasses(errorManager);
    errorManager.generateReport();
    assertTrue("Encountered all errors.", errorManager.hasEncounteredAllErrors());
  }

}
