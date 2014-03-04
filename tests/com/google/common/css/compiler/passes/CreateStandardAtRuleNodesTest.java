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

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssImportBlockNode;
import com.google.common.css.compiler.ast.CssImportRuleNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssPageRuleNode;
import com.google.common.css.compiler.ast.CssPageSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 * Unit tests for {@link CreateStandardAtRuleNodes}.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CreateStandardAtRuleNodesTest extends PassesTestBase {

  @Override
  protected void runPass() {
    CreateStandardAtRuleNodes pass = new CreateStandardAtRuleNodes(
        tree.getMutatingVisitController(), errorManager);
    pass.runPass();
  }

  public void testCharsetRemoval() throws Exception {
    parseAndRun("@charset \"x\";", "@charset removed");
    assertTrue(isEmptyBody());
  }

  public void testCreateSimpleImportNode() throws Exception {
    parseAndRun("@import \"name\" ;");
    CssImportRuleNode importRule = findFirstNodeOf(CssImportRuleNode.class);
    assertEquals("import", importRule.getName().getValue());
    assertEquals(1, importRule.getParametersCount());
    assertTrue(
        "Import rules should occur in the import block.",
        Iterables.any(
            importRule.ancestors(),
            Predicates.instanceOf(CssImportBlockNode.class)));
  }

  public void testCreateUriImportNode() throws Exception {
    parseAndRun("@import url('/js/closure/css/common.css');");
    CssImportRuleNode importRule = findFirstNodeOf(CssImportRuleNode.class);
    assertEquals("import", importRule.getName().getValue());
    assertEquals(1, importRule.getParametersCount());
  }

  public void testCreateComplexImportNode() throws Exception {
    parseAndRun("@import \"name\" param1, param2, param3;");
    CssImportRuleNode importRule = findFirstNodeOf(CssImportRuleNode.class);
    assertEquals("import", importRule.getName().getValue());
    assertEquals(2, importRule.getParametersCount());
  }

  public void testImportWithoutParamError() throws Exception {
    parseAndRun("@import ;", "@import without a following string or uri");
    assertTrue(isEmptyBody());
  }

  public void testImportWithWrongParamError() throws Exception {
    parseAndRun("@import abc;", "@import's first parameter has to be a string or an url");
    assertTrue(isEmptyBody());
  }

  public void testImportWithTooManyParamsError() throws Exception {
    parseAndRun("@import \"A\" b c,d;" , "@import with too many parameters");
    assertTrue(isEmptyBody());
  }

  public void testMisplacedImportWarnings() throws Exception {
    CssTree t = parseAndRun("div { font-family: sans } @import 'a';",
        CreateStandardAtRuleNodes.IGNORE_IMPORT_WARNING_MESSAGE,
        CreateStandardAtRuleNodes.IGNORED_IMPORT_WARNING_MESSAGE);
    assertEquals(
        "This pass should not reorder misplaced nodes.",
        "(com.google.common.css.compiler.ast.CssRootNode "
        + "(com.google.common.css.compiler.ast.CssImportBlockNode)"
        + "(com.google.common.css.compiler.ast.CssBlockNode "
        + "(com.google.common.css.compiler.ast.CssRulesetNode "
        + "(com.google.common.css.compiler.ast.CssSelectorListNode "
        + "(com.google.common.css.compiler.ast.CssSelectorNode))"
        + "(com.google.common.css.compiler.ast.CssDeclarationBlockNode "
        + "(com.google.common.css.compiler.ast.CssDeclarationNode "
        + "(com.google.common.css.compiler.ast.CssPropertyValueNode "
        + "(com.google.common.css.compiler.ast.CssLiteralNode sans)))))"
        + "(com.google.common.css.compiler.ast.CssImportRuleNode)))",
        SExprPrinter.print(false /* includeHashCodes */, t));
  }

  public void testPrintableImports() throws Exception {
    String css = "@import url('foo');div{font-family:sans}";
    assertEquals(
        css,
        CompactPrinter.printCompactly(parseAndRun(css).getRoot()));
  }

  public void testCreateMediaNode1() throws Exception {
    parseAndRun("@media a, b, c { e.f { a:b } } ");
    assertTrue(getFirstActualNode() instanceof CssMediaRuleNode);
    CssMediaRuleNode mediaRule = (CssMediaRuleNode) getFirstActualNode();
    assertEquals("media", mediaRule.getName().getValue());
    assertEquals(1, mediaRule.getParametersCount());
  }

  public void testCreateMediaNode2() throws Exception {
    parseAndRun("@media not screen { e.f { a:b } } ");
    assertTrue(getFirstActualNode() instanceof CssMediaRuleNode);
  }

  public void testCreateMediaNode3() throws Exception {
    parseAndRun("@media only screen { e.f { a:b } } ");
    assertTrue(getFirstActualNode() instanceof CssMediaRuleNode);
  }

  public void testCreateMediaNode4() throws Exception {
    parseAndRun("@media screen and (device-width:800px),"
        + "tv and (scan:progressive),"
        + "handheld and grid and (max-width:15em)"
        + "{ e.f { a:b } } ");
    assertTrue(getFirstActualNode() instanceof CssMediaRuleNode);
  }

  public void testCreateMediaNodeWithConditional() throws Exception {
    parseAndRun("@media screen {"
        + " @if (A) { e.f { a:b } }"
        + "@elseif (B) { e.f { a:b } }"
        + "@else { e.f { a:b } } }");
    assertTrue(getFirstActualNode() instanceof CssMediaRuleNode);
  }

  public void testMediaWithoutBlockError() throws Exception {
    parseAndRun("@media a;",
        CreateStandardAtRuleNodes.NO_BLOCK_ERROR_MESSAGE);
    assertTrue(isEmptyBody());
  }

  public void testMediaWithWrongBlockError() throws Exception {
    parseAndRun("@media a { @def a b; }",
        CreateStandardAtRuleNodes.MEDIA_INVALID_CHILD_ERROR_MESSAGE);
    assertTrue(isEmptyBody());
  }

  public void testMediaWithoutParamError() throws Exception {
    parseAndRun("@media { }",
        CreateStandardAtRuleNodes.MEDIA_WITHOUT_PARAMETERS_ERROR_MESSAGE);
    assertTrue(isEmptyBody());
  }

  public void testMediaInvalidParameterError1() throws Exception {
    parseAndRun("@media screen print {}",
        CreateStandardAtRuleNodes.INVALID_PARAMETERS_ERROR_MESSAGE);
    assertTrue(isEmptyBody());
  }

  public void testMediaInvalidParameterError2() throws Exception {
    parseAndRun("@media screen a_d print {}",
        CreateStandardAtRuleNodes.INVALID_PARAMETERS_ERROR_MESSAGE);
    assertTrue(isEmptyBody());
  }

  public void testMediaInvalidParameterError3() throws Exception {
    parseAndRun("@media screen, print a_d x {}",
        CreateStandardAtRuleNodes.INVALID_PARAMETERS_ERROR_MESSAGE);
    assertTrue(isEmptyBody());
  }

  public void testMediaInvalidParameterError4() throws Exception {
    parseAndRun("@media not screen, print a_d x {}",
        CreateStandardAtRuleNodes.INVALID_PARAMETERS_ERROR_MESSAGE);
    assertTrue(isEmptyBody());
  }

  public void testMediaInvalidParameterError5() throws Exception {
    parseAndRun("@media screen and (device-width:800px),"
        + "tv and (scan:progressive),"
        + "handheld and grid and (max-width:15em),"
        + "X Y"
        + "{ e.f { a:b } } ",
        CreateStandardAtRuleNodes.INVALID_PARAMETERS_ERROR_MESSAGE);
    assertTrue(isEmptyBody());
  }

  public void testCreatePageNode() throws Exception {
    parseAndRun("@page { a:b }");
    assertTrue(getFirstActualNode() instanceof CssPageRuleNode);
    CssPageRuleNode pageRule = (CssPageRuleNode) getFirstActualNode();
    assertEquals("page", pageRule.getName().getValue());
    assertEquals(0, pageRule.getParametersCount());
  }

  public void testCreatePageWithPseudoClassNode1() throws Exception {
    parseAndRun("@page :left { a:b }");
    assertTrue(getFirstActualNode() instanceof CssPageRuleNode);
    CssPageRuleNode pageRule = (CssPageRuleNode) getFirstActualNode();
    assertEquals("page", pageRule.getName().getValue());
    assertEquals(1, pageRule.getParametersCount());
    assertEquals(":left", pageRule.getParameters().get(0).getValue());
  }

  public void testCreatePageWithPseudoClassNode2() throws Exception {
    parseAndRun("@page artsy:right { a:b }");
    assertTrue(getFirstActualNode() instanceof CssPageRuleNode);
    CssPageRuleNode pageRule = (CssPageRuleNode) getFirstActualNode();
    assertEquals("page", pageRule.getName().getValue());
    assertEquals(2, pageRule.getParametersCount());
    assertEquals("artsy", pageRule.getParameters().get(0).getValue());
    assertEquals(":right", pageRule.getParameters().get(1).getValue());
  }

  public void testCreatePageWithTypeSelector() throws Exception {
    parseAndRun("@page artsy { a:b }");
    assertTrue(getFirstActualNode() instanceof CssPageRuleNode);
    CssPageRuleNode pageRule = (CssPageRuleNode) getFirstActualNode();
    assertEquals("page", pageRule.getName().getValue());
    assertEquals(1, pageRule.getParametersCount());
    assertEquals("artsy", pageRule.getParameters().get(0).getValue());
  }

  public void testCreatePageInMedia() throws Exception {
    parseAndRun("@media print { @page { a:b } } ");
    assertTrue(getFirstActualNode() instanceof CssMediaRuleNode);
    CssMediaRuleNode mediaRule = (CssMediaRuleNode) getFirstActualNode();
    assertEquals("media", mediaRule.getName().getValue());
    assertEquals(1, mediaRule.getParametersCount());
    assertEquals(1, mediaRule.getBlock().numChildren());
    assertTrue(mediaRule.getBlock().getChildAt(0) instanceof CssPageRuleNode);
    CssPageRuleNode pageRule =
        (CssPageRuleNode) mediaRule.getBlock().getChildAt(0);
    assertEquals("page", pageRule.getName().getValue());
    assertEquals(0, pageRule.getParametersCount());
  }

  public void testCreatePageInMediaPseudoClass1() throws Exception {
    parseAndRun("@media print { @page XY:first { a:b } } ");
    assertTrue(getFirstActualNode() instanceof CssMediaRuleNode);
    CssMediaRuleNode mediaRule = (CssMediaRuleNode) getFirstActualNode();
    assertEquals("media", mediaRule.getName().getValue());
    assertEquals(1, mediaRule.getParametersCount());
    assertEquals(1, mediaRule.getBlock().numChildren());
    assertTrue(mediaRule.getBlock().getChildAt(0) instanceof CssPageRuleNode);
    CssPageRuleNode pageRule =
        (CssPageRuleNode) mediaRule.getBlock().getChildAt(0);
    assertEquals("page", pageRule.getName().getValue());
    assertEquals(2, pageRule.getParametersCount());
    assertEquals("XY", pageRule.getParameters().get(0).getValue());
    assertEquals(":first", pageRule.getParameters().get(1).getValue());
  }

  public void testCreatePageInMediaPseudoClass2() throws Exception {
    parseAndRun("@media print { @page :first { a:b } } ");
    assertTrue(getFirstActualNode() instanceof CssMediaRuleNode);
    CssMediaRuleNode mediaRule = (CssMediaRuleNode) getFirstActualNode();
    assertEquals("media", mediaRule.getName().getValue());
    assertEquals(1, mediaRule.getParametersCount());
    assertEquals(1, mediaRule.getBlock().numChildren());
    assertTrue(mediaRule.getBlock().getChildAt(0) instanceof CssPageRuleNode);
    CssPageRuleNode pageRule =
        (CssPageRuleNode) mediaRule.getBlock().getChildAt(0);
    assertEquals("page", pageRule.getName().getValue());
    assertEquals(1, pageRule.getParametersCount());
    assertEquals(":first", pageRule.getParameters().get(0).getValue());
  }

  public void testPageRuleWithoutBlockError() throws Exception {
    parseAndRun("@page;",
        CreateStandardAtRuleNodes.NO_BLOCK_ERROR_MESSAGE);
  }

  public void testPageRuleInvalidParameters1() throws Exception {
    parseAndRun("@page one two three {}",
        CreateStandardAtRuleNodes.INVALID_PARAMETERS_ERROR_MESSAGE);
  }

  public void testPageRuleInvalidParameters2() throws Exception {
    parseAndRun("@page one two three {}",
        CreateStandardAtRuleNodes.INVALID_PARAMETERS_ERROR_MESSAGE);
  }

  public void testPageRuleInvalidParameters3() throws Exception {
    parseAndRun("@page one:invalidpseudopage {}",
        CreateStandardAtRuleNodes.INVALID_PARAMETERS_ERROR_MESSAGE);
  }

  public void testPageRuleInvalidParameters4() throws Exception {
    parseAndRun("@page :invalidpseudopage {}",
        CreateStandardAtRuleNodes.INVALID_PARAMETERS_ERROR_MESSAGE);
  }

  public void testCreatePageSelectorNode() throws Exception {
    createPageSelector("top-left");
    createPageSelector("left-middle");
    createPageSelector("bottom-right-corner");
    createPageSelector("bottom-center");
  }

  private void createPageSelector(String name) throws GssParserException {
    parseAndRun("@page { @" + name + " { a:b } }");
    assertTrue(getFirstActualNode() instanceof CssPageRuleNode);
    CssPageRuleNode pageRule = (CssPageRuleNode) getFirstActualNode();
    assertEquals("page", pageRule.getName().getValue());
    assertEquals(1, pageRule.getBlock().numChildren());
    assertTrue(
        pageRule.getBlock().getChildAt(0) instanceof CssPageSelectorNode);
    CssPageSelectorNode pageSelector =
        (CssPageSelectorNode) pageRule.getBlock().getChildAt(0);
    assertEquals(name, pageSelector.getName().getValue());
  }

  public void testPageSelectorWithoutBlockError() throws Exception {
    parseAndRun("@page { @top-left; }",
        CreateStandardAtRuleNodes.NO_BLOCK_ERROR_MESSAGE);
  }

  public void testPageSelectorWithParametersError() throws Exception {
    parseAndRun("@page { @top-left param { } }",
        CreateStandardAtRuleNodes.PAGE_SELECTOR_PARAMETERS_ERROR_MESSAGE);
  }

  public void testCreateFontNode() throws GssParserException {
    parseAndRun("@font-face { font-family: Gentium }");
    assertTrue(getFirstActualNode() instanceof CssFontFaceNode);
    CssFontFaceNode fontFace = (CssFontFaceNode) getFirstActualNode();
    assertEquals("font-face", fontFace.getName().getValue());
    assertEquals(0, fontFace.getParametersCount());
  }

  public void testCreateFontNodeWithoutBlockError() throws GssParserException {
    parseAndRun("@font-face;",
        CreateStandardAtRuleNodes.NO_BLOCK_ERROR_MESSAGE);
  }

  public void testCreateFontNodeWithParametersError() throws Exception {
    parseAndRun("@font-face param { font-family: Gentium }",
        CreateStandardAtRuleNodes.FONT_FACE_PARAMETERS_ERROR_MESSAGE);
  }

  public void testMonochrome() throws Exception {
    parseAndRun(
        "@media (monochrome) {\n"
        + "  .test { text-decoration: underline; }\n"
        + "}");
  }

  public void testMediaAndAnd() throws Exception {
    parseAndRun(
        "@media (monochrome) and (min-width:800px) and (scan:progressive) {\n"
        + "  .test { text-decoration: underline; }\n"
        + "}");
  }

  public void testMinColorIndex() throws Exception {
    parseAndRun(
        "@media (min-color-index: 256) {\n"
        + "  .test { font-color: red; }\n"
        + "}");
  }
}
