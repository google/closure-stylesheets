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

import static com.google.common.css.compiler.passes.ResolveCustomFunctionNodesForChunks.DEF_PREFIX;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.passes.testing.PassesTestBase;

import java.util.List;

/**
 * Unit tests for {@link ProcessComponents}.
 *
 */
public class ProcessComponentsTest extends PassesTestBase {

  private static final String FILE1 = "file1";
  private static final String FILE2 = "file2";
  private static final String FILE3 = "file3";
  private static final String TEST_CHUNK = "testChunk";
  private static final String CHUNK1 = "chunk1";
  private static final String CHUNK2 = "chunk2";
  private static final String CHUNK3 = "chunk3";

  private static final ImmutableMap<String, String> FILE_TO_CHUNK =
    ImmutableMap.of(
        TEST_FILENAME, TEST_CHUNK,
        FILE1, CHUNK1,
        FILE2, CHUNK2,
        FILE3, CHUNK3);

  private static final String FAKE_FUNCTION_REF = "~fake~function~ref~";

  private final String globalInput = "@def GLOBAL_COLOR white;";
  private final String globalOutput = "@def GLOBAL_COLOR [[white]]";

  private final ImmutableList<String> topComponentPrefixInput = ImmutableList.of(
      "@component CSS_TOP {");

  private final ImmutableList<String> topComponentInputConstants = ImmutableList.of(
      "  @def SOME_COLOR     black;",
      "  @def OTHER_BG_COLOR GLOBAL_COLOR;",
      "  @def OTHER_COLOR someColorFunction(SOME_COLOR, OTHER_BG_COLOR);");
  private final String topComponentOutputConstantsTemplate =
      "@def CSS_TOP__SOME_COLOR [[black]];" +
      "@def CSS_TOP__OTHER_BG_COLOR [[GLOBAL_COLOR]];" +
      "@def CSS_TOP__OTHER_COLOR [[" + FAKE_FUNCTION_REF + "]];";


  private final ImmutableList<String> topComponentInputRules = ImmutableList.of(
      "  .CSS_SOME_CLASS, .CSS_SOME_OTHER_CLASS {",
      "    color: SOME_COLOR;",
      "    background-color: OTHER_BG_COLOR;",
      "    width: 100px;",
      "    border-color: someColorFunction(SOME_COLOR, OTHER_BG_COLOR);",
      "  }");
  private final String topComponentOutputRulesTemplate =
      "[.CSS_TOP-CSS_SOME_CLASS,.CSS_TOP-CSS_SOME_OTHER_CLASS]{" +
      "[color:[[CSS_TOP__SOME_COLOR]];" +
      "background-color:[[CSS_TOP__OTHER_BG_COLOR]];" +
      "width:[[100px]];" +
      "border-color:[[" + FAKE_FUNCTION_REF + "]];]}";


  private final String abstractTopComponentInput = joinNl(Iterables.concat(
      ImmutableList.of("@abstract_component CSS_TOP {"),
      topComponentInputConstants,
      topComponentInputRules,
      ImmutableList.of("}")));
  private final String abstractTopComponentOutput =
      replaceFunction(topComponentOutputConstantsTemplate,
          "someColorFunction(CSS_TOP__SOME_COLOR,CSS_TOP__OTHER_BG_COLOR)");
  private final String abstractTopComponentOutputResolved =
      replaceFunction(topComponentOutputConstantsTemplate, "[" + DEF_PREFIX + "0]");


  private final String topComponentInput = joinNl(Iterables.concat(
      topComponentPrefixInput,
      topComponentInputConstants,
      topComponentInputRules,
      ImmutableList.of("}")));
  private final String topComponentOutput =
      abstractTopComponentOutput +
      replaceFunction(topComponentOutputRulesTemplate,
          "someColorFunction(CSS_TOP__SOME_COLOR,CSS_TOP__OTHER_BG_COLOR)");
  private final String topComponentOutputResolved =
      globalOutput + ";" +
      abstractTopComponentOutputResolved +
      // This is the second occurrence of the function.
      replaceFunction(topComponentOutputRulesTemplate, "[" + DEF_PREFIX + "1]");

  private final String childComponentInput = linesToString(
      "@component CSS_CHILD extends CSS_TOP {",
      "  @def SOME_COLOR     yellow;",
      "  .CSS_SOME_CLASS {",
      "    border: 1px solid SOME_COLOR",
      "  }",
      "}");
  private final String childComponentOutputTemplate =
      "@def CSS_CHILD__SOME_COLOR [[CSS_TOP__SOME_COLOR]];" +
      "@def CSS_CHILD__OTHER_BG_COLOR [[CSS_TOP__OTHER_BG_COLOR]];" +
      "@def CSS_CHILD__OTHER_COLOR [[CSS_TOP__OTHER_COLOR]];" +
      "[.CSS_CHILD-CSS_SOME_CLASS,.CSS_CHILD-CSS_SOME_OTHER_CLASS]{" +
      "[color:[[CSS_CHILD__SOME_COLOR]];" +
      "background-color:[[CSS_CHILD__OTHER_BG_COLOR]];" +
      "width:[[100px]];" +
      "border-color:[[" + FAKE_FUNCTION_REF + "]];]}" +
      "@def CSS_CHILD__SOME_COLOR [[yellow]];" +
      "[.CSS_CHILD-CSS_SOME_CLASS]{" +
      "[border:[[1px][solid][CSS_CHILD__SOME_COLOR]];]}";
  private final String childComponentOutput =
      replaceFunction(childComponentOutputTemplate,
          "someColorFunction(CSS_CHILD__SOME_COLOR,CSS_CHILD__OTHER_BG_COLOR)");

  private final String grandChildComponentInput = linesToString(
      "@component CSS_GRAND_CHILD extends CSS_CHILD {",
      "  @def NEW_BORDER_COLOR   green;",
      "  .CSS_SOME_OTHER_CLASS {",
      "    border: 1px solid NEW_BORDER_COLOR;",
      "  }",
      "}");
  private final String grandChildComponentOutputTemplate =
      "@def CSS_GRAND_CHILD__SOME_COLOR [[CSS_CHILD__SOME_COLOR]];" +
      "@def CSS_GRAND_CHILD__OTHER_BG_COLOR [[CSS_CHILD__OTHER_BG_COLOR]];" +
      "@def CSS_GRAND_CHILD__OTHER_COLOR [[CSS_CHILD__OTHER_COLOR]];" +
      "[.CSS_GRAND_CHILD-CSS_SOME_CLASS,.CSS_GRAND_CHILD-CSS_SOME_OTHER_CLASS]{" +
      "[color:[[CSS_GRAND_CHILD__SOME_COLOR]];" +
      "background-color:[[CSS_GRAND_CHILD__OTHER_BG_COLOR]];" +
      "width:[[100px]];" +
      "border-color:[[" + FAKE_FUNCTION_REF + "]];]}" +
      "@def CSS_GRAND_CHILD__SOME_COLOR [[CSS_CHILD__SOME_COLOR]];" +
      "[.CSS_GRAND_CHILD-CSS_SOME_CLASS]{" +
      "[border:[[1px][solid][CSS_GRAND_CHILD__SOME_COLOR]];]}" +
      "@def CSS_GRAND_CHILD__NEW_BORDER_COLOR [[green]];" +
      "[.CSS_GRAND_CHILD-CSS_SOME_OTHER_CLASS]{" +
      "[border:[[1px][solid][CSS_GRAND_CHILD__NEW_BORDER_COLOR]];]}";
  private final String grandChildComponentOutput =
    replaceFunction(grandChildComponentOutputTemplate,
        "someColorFunction(CSS_GRAND_CHILD__SOME_COLOR,CSS_GRAND_CHILD__OTHER_BG_COLOR)");

  private final ImmutableList<String> topComponentWithRefWorkaroundInputConstants =
      ImmutableList.of(
          "  @def SOME_COLOR     black;",
          "  @def OTHER_BG_COLOR GLOBAL_COLOR;",
          "  @def OTHER_COLOR someColorFunction(CSS_TOP__SOME_COLOR, CSS_TOP__OTHER_BG_COLOR);");
  private final String childComponentWithRefWorkaroundInput = linesToString(
      "@component CSS_CHILD extends CSS_TOP {",
      "  @def SOME_COLOR     yellow;",
      "  .CSS_SOME_CLASS {",
      "    border: 1px solid CSS_CHILD__SOME_COLOR",
      "  }",
      "}");

  private final String ifComponentInput = linesToString(
      "@component CSS_IF {" +
      "  @if (BROWSER_IE) {",
      "    @if (RTL_LANG) {",
      "      .CSS_BAR {",
      "        border: 1px;",
      "      }",
      "    } @else {",
      "      .CSS_BAR {",
      "        border: 2px;",
      "      }",
      "    }",
      "  }",
      "}");

  private final String ifComponentOutput =
      "@if[BROWSER_IE]{" +
      "@if[RTL_LANG]{[.CSS_IF-CSS_BAR]{[border:[[1px]];]}}" +
        "@else[]{[.CSS_IF-CSS_BAR]{[border:[[2px]];]}}}";

  private final ImmutableList<String> namelessComponentPrefixInput = ImmutableList.of(
      "@provide \"some.example.package\";",
      "@component {");

  private final String namelessComponentInput = joinNl(Iterables.concat(
      namelessComponentPrefixInput,
      topComponentInputConstants,
      topComponentInputRules,
      ImmutableList.of("}")));

  private final String namelessComponentOutput =
      "@def SOME_EXAMPLE_PACKAGE_SOME_COLOR [[black]];" +
      "@def SOME_EXAMPLE_PACKAGE_OTHER_BG_COLOR [[GLOBAL_COLOR]];" +
      "@def SOME_EXAMPLE_PACKAGE_OTHER_COLOR [someColorFunction(" +
          "SOME_EXAMPLE_PACKAGE_SOME_COLOR,SOME_EXAMPLE_PACKAGE_OTHER_BG_COLOR)];" +
      "[.someExamplePackageCSS_SOME_CLASS,.someExamplePackageCSS_SOME_OTHER_CLASS]{" +
      "[color:[[SOME_EXAMPLE_PACKAGE_SOME_COLOR]];" +
      "background-color:[[SOME_EXAMPLE_PACKAGE_OTHER_BG_COLOR]];" +
      "width:[[100px]];" +
      "border-color:[someColorFunction(" +
          "SOME_EXAMPLE_PACKAGE_SOME_COLOR,SOME_EXAMPLE_PACKAGE_OTHER_BG_COLOR)];]}";

  @Override
  protected void runPass() {
    new CreateDefinitionNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new MapChunkAwareNodesToChunk<String>(tree, FILE_TO_CHUNK).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
    new CreateConditionalNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new CheckDependencyNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new CreateComponentNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new ProcessComponents<String>(
        tree.getMutatingVisitController(), errorManager, FILE_TO_CHUNK).runPass();
  }

  protected void testTreeConstructionWithResolve(
      ImmutableMap<String, String> fileNameToGss,
      String expectedOutput) {

    parseAndBuildTree(fileNameToGss);
    runPass();
    ResolveCustomFunctionNodesForChunks<String> resolveFunctions =
        new ResolveCustomFunctionNodesForChunks<String>(
            tree.getMutatingVisitController(), errorManager,
            ImmutableMap.of("someColorFunction", SOME_COLOR_FUNCTION),
            false /* allowUnknownFunctions */,
            ImmutableSet.<String>of() /* allowedNonStandardFunctions */,
            new UniqueSuffixFunction());
    resolveFunctions.runPass();
    checkTreeDebugString(expectedOutput);
  }

  public void testTopComponent() throws Exception {
    testTreeConstruction(topComponentInput, "[" + topComponentOutput + "]");
    testTreeConstructionWithResolve(
        ImmutableMap.of(
            FILE1, globalInput,
            FILE2, topComponentInput),
        "[" + topComponentOutputResolved + "]");
  }

  public void testChildComponent() throws Exception {
    testTreeConstruction(
        topComponentInput + "\n" + childComponentInput,
        "[" + topComponentOutput + childComponentOutput + "]");
    testTreeConstructionWithResolve(
        ImmutableMap.of(
            FILE1, globalInput,
            FILE2, topComponentInput,
            FILE3, childComponentInput),
        "[" + topComponentOutputResolved +
            // Suffix is 2 since the top component is also resolved.
            replaceFunction(childComponentOutputTemplate, "[" + DEF_PREFIX + "2]") + "]");
  }

  public void testChildComponentWithReferenceWorkaround() throws Exception {
    String topComponentWithWorkaroundInput = joinNl(Iterables.concat(
        topComponentPrefixInput,
        topComponentWithRefWorkaroundInputConstants,
        topComponentInputRules,
        ImmutableList.of("}")));

    testTreeConstruction(
        topComponentWithWorkaroundInput + "\n" + childComponentWithRefWorkaroundInput,
        "[" + topComponentOutput + childComponentOutput + "]");
    testTreeConstructionWithResolve(
        ImmutableMap.of(
            FILE1, globalInput,
            FILE2, topComponentWithWorkaroundInput,
            FILE3, childComponentWithRefWorkaroundInput),
        "[" + topComponentOutputResolved +
            // Suffix is 2 since the top component is also resolved.
            replaceFunction(childComponentOutputTemplate, "[" + DEF_PREFIX + "2]") + "]");
  }

  public void testChildComponentWithAbstractParent() throws Exception {
    testTreeConstruction(
        abstractTopComponentInput + "\n" + childComponentInput,
        "[" + abstractTopComponentOutput + childComponentOutput + "]");
    testTreeConstructionWithResolve(
        ImmutableMap.of(
            FILE1, globalInput,
            FILE2, abstractTopComponentInput,
            FILE3, childComponentInput),
        "[" + globalOutput + ";" + abstractTopComponentOutputResolved +
            // Suffix is 1 since the abstract top component is also resolved.
            replaceFunction(childComponentOutputTemplate, "[" + DEF_PREFIX + "1]") + "]");
  }

  public void testGrandChildComponent() throws Exception {
    testTreeConstruction(
        topComponentInput + "\n" + childComponentInput + "\n" +
        grandChildComponentInput,
        "[" + topComponentOutput +
              childComponentOutput +
              grandChildComponentOutput + "]");
    testTreeConstructionWithResolve(
        ImmutableMap.of(
            FILE1, globalInput,
            FILE2, topComponentInput,
            FILE3, childComponentInput,
            TEST_FILENAME, grandChildComponentInput),
        "[" + topComponentOutputResolved +
            // Suffixes are 2 and 3 since the top component is also resolved.
            replaceFunction(childComponentOutputTemplate, "[" + DEF_PREFIX + "2]") +
            replaceFunction(grandChildComponentOutputTemplate, "[" + DEF_PREFIX +
                "3]") + "]");
  }

  public void testIfComponent() throws Exception {
    testTreeConstruction(ifComponentInput, "[" + ifComponentOutput + "]");
  }

  public void testUndefinedParentComponentError() throws Exception {
    parseAndRun("@component CSS_X extends CSS_Y { }",
        "parent component is undefined in chunk " + TEST_CHUNK);
    assertTrue(isEmptyBody());
  }

  public void testRedefinedComponentError() throws Exception {
    parseAndRun(ImmutableMap.of(
            FILE1, "@component CSS_X { }",
            FILE2, "@component CSS_X { }"),
        "cannot redefine component in chunk " + CHUNK2);
    assertTrue(isEmptyBody());
  }

  public void testNestedComponentsError1() throws Exception {
    parseAndRun("@component CSS_X { @component CSS_Y {} }", "nested components are not allowed");
    assertTrue(isEmptyBody());
  }

  public void testNestedComponentsError2() throws Exception {
    parseAndRun("@component CSS_X { @component CSS_Y {} }\n@component CSS_Z extends CSS_X {}",
        "nested components are not allowed");
    assertTrue(isEmptyBody());
  }

  public void testImplicitlyNamed() throws Exception {
    testTreeConstruction(namelessComponentInput, "[" + namelessComponentOutput + "]");
  }

  public void testImplicitlyNamedNoPackageError() throws Exception {
    parseAndRun("@component { }",
        "implicitly-named @components require a single @provide declaration " + TEST_CHUNK);
    assertTrue(isEmptyBody());
  }

  public void testImplicitlyNamedMultiplePackageError() throws Exception {
    parseAndRun(
        "@provide \"some.example.package\";\n" +
        "@provide \"another.example.package\";\n" +
            "@component { }",
        "implicitly-named @components require a single @provide declaration " + TEST_CHUNK);
  }

  public void testMultiplePackageWithNoComponentError() throws Exception {
    testTreeConstruction(
        "@provide \"some.example.package\";\n" +
        "@provide \"another.example.package\";\n",
        "[]");
  }

  private String joinNl(Iterable<String> lines) {
    return Joiner.on('\n').join(lines);
  }

  /**
   * Replaces the fake function reference in the output.
   * @param componentOutputTemplate the component output with the fake function
   *     reference
   * @param replacement the replacement value to use as the function reference
   * @return the component output with the fake function reference replaced
   */
  private String replaceFunction(String componentOutputTemplate, String replacement) {
    return componentOutputTemplate.replace("[" + FAKE_FUNCTION_REF + "]", replacement);
  }

  private static class UniqueSuffixFunction implements Function<String, String> {
    private int count = 0;
    @Override
    public String apply(String chunk) {
      assertNotNull(chunk);
      return String.valueOf(count++);
    }
  }

  private static final GssFunction SOME_COLOR_FUNCTION = new GssFunction() {
    @Override
    public Integer getNumExpectedArguments() {
      return 2;
    }

    @Override
    public String getCallResultString(List<String> args) {
      return Joiner.on("~").join(args);
    }

    @Override
    public List<CssValueNode> getCallResultNodes(
        List<CssValueNode> args, ErrorManager errorManager) {
      ImmutableList<String> stringArgs = ImmutableList.of(
          args.get(0).getValue(),
          args.get(1).getValue());
      String stringResult = getCallResultString(stringArgs);
      CssLiteralNode nodeResult = new CssLiteralNode(stringResult);
      return ImmutableList.<CssValueNode>of(nodeResult);
    }
  };
}
