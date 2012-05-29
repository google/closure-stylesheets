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

package com.google.common.css.compiler.passes;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.testing.AstPrinter;

import junit.framework.TestCase;

/**
 * Unit tests for {@link FixupFontDeclarations}.
 *
 */
public class FixupFontDeclarationsTest extends TestCase {
  private DummyErrorManager errorManager;

  public void testIdSequenceFontFamilies() throws Exception {
    CssTree t =
        runPass(parse("a { font-family: a, b c d, e f g h i, 'jkl'; }"));
    CssRootNode r = t.getRoot();
    CssRulesetNode rules = (CssRulesetNode) r.getBody().getChildAt(0);
    CssDeclarationNode decl =
        (CssDeclarationNode) rules.getDeclarations().getChildAt(0);
    assertEquals("font-family", decl.getPropertyName().getPropertyName());
    CssPropertyValueNode valueNode = decl.getPropertyValue();
    assertEquals(1, valueNode.numChildren());
    CssCompositeValueNode alternatives =
        (CssCompositeValueNode) valueNode.getChildAt(0);
    assertEquals(4, alternatives.getValues().size());
    assertEquals("a", alternatives.getValues().get(0).getValue());
    assertEquals("b c d", alternatives.getValues().get(1).getValue());
    assertEquals("e f g h i", alternatives.getValues().get(2).getValue());
    assertEquals("'jkl'", alternatives.getValues().get(3).getValue());
  }

  // TODO(user): when we have string parsing, test that this pass
  // rejects string sequences e.g.
  //   font-family: Helvetica, "Adobe Garamond" "Pro", Times New Roman, serif
  //                                            ^
  //  error: font alternatives must be strings or identifier-sequences

  public void testFontFamilyAssociativity() throws Exception {
    testTree("div { font-family: a b, c; }",
             "[[div]{[font-family:[[[a b],[c]]];]}]");
  }

  public void testFontFamilyInherit() throws Exception {
    testValid("div { font-family: inherit; }");
  }

  public void testFontShorthandGssUse() throws Exception {
    testTree(FixupFontDeclarations.InputMode.GSS,
             "div { font: FG_COLOR; }",
             "[[div]{[font:[[FG_COLOR]];]}]");
    testTree(FixupFontDeclarations.InputMode.GSS,
             "div { font: FGCOLOR }",
             "[[div]{[font:[[FGCOLOR]];]}]");
    testTree(FixupFontDeclarations.InputMode.GSS,
             "div { font: FGCOLOR  }",
             "[[div]{[font:[[FGCOLOR]];]}]");
  }

  public void testFontShorthandSystem() throws Exception {
    testTree("div { font: status-bar; }",
             "[[div]{[font:[[status-bar]];]}]");
    testTree("div { font: status-bar }",
             "[[div]{[font:[[status-bar]];]}]");
  }

  public void testFontJustInherit() throws Exception {
    testValid("div { font: inherit; }");
  }

  public void testIdSeqInFontShorthand() throws Exception {
    testTree("div { font: bold 12px arial, serif; }",
             "[[div]{[font:[[bold][12px][[arial],[serif]]];]}]");
  }

  public void testFontShorthandSizeFamily() throws Exception {
    testTree("div { font: 100% \"Adobe Garamond Premier Pro\"; }",
             "[[div]{[font:[[100%][[\"Adobe Garamond Premier Pro\"]]];]}]");
  }

  public void testFontShorthandSizeFamilyNoSemi() throws Exception {
    testTree("div { font: 100% \"Adobe Garamond Premier Pro\" }",
             "[[div]{[font:[[100%][[\"Adobe Garamond Premier Pro\"]]];]}]");
  }

  public void testFontShorthandPresize() throws Exception {
    testTree("div { font:normal 13px Frutiger; }",
             "[[div]{[font:[[normal][13px][[Frutiger]]];]}]");
  }

  public void testFontShorthandVarFirst() throws Exception {
    testValid(FixupFontDeclarations.InputMode.GSS,
              "div { font: FONT_STYLE 14px arial,sans-serif; }");
  }

  public void testFontShorthandNoTailSwizzle() throws Exception {
    testTree(FixupFontDeclarations.InputMode.GSS,
             "div { font: bold APP_MENU_FONT; }",
             "[[div]{[font:[[bold][APP_MENU_FONT]];]}]");
  }

  public void testFontShorthandSizeAndLineHeight() throws Exception {
    testTree("div { font: 1em/1.3em \"Roboto\"; }",
             "[[div]{[font:[[1em][/][1.3em][[\"Roboto\"]]];]}]");
  }

  public void testFontShorthandSizeAndLineHeight2() throws Exception {
    testValid("a { font: 10px/12px Arial; }");
  }

  public void testFontShorthandImportant() throws Exception {
    testValid(FixupFontDeclarations.InputMode.GSS,
              "div { font: normal SEARCHAC !important; }");
  }

  public void testFontShorthandImportant2() throws Exception {
    testTree("a { font: medium \"Computer Modern\" !important; }",
             "[[a]{[font:[[medium][[\"Computer Modern\"]][!important]];]}]");
  }

  public void testFontFamilyImportant() throws Exception {
    testTree("a { font-family: \"Computer Modern\" !important; }",
             "[[a]{[font-family:[[[\"Computer Modern\"]][!important]];]}]");
  }

  public void testFontInheritFamily() throws Exception {
    // this isn't allowed by the CSS 2.1 grammar but it's common enough
    // and we support it anyway.
    testValid("div { font: normal 95% inherit; }");
  }

  public void testFontWithNumericWeight() throws Exception {
    testValid("div { font: normal normal 400 13px arial,sans,sans-serif; }");
  }

  public void testFontVariantSizeFamily() throws Exception {
    testValid("div { font: small-caps 13px serif; }");
  }

  public void testIdSequenceInShorthand() throws Exception {
    testTree("div { font: 13px Computer Modern, Adobe Caslon Pro, serif; }",
             "[[div]{[font:[[13px][[Computer Modern],"
             + "[Adobe Caslon Pro],[serif]]];]}]");
  }

  public void testSizeAndFamilyRequired() throws Exception {
    testError(FixupFontDeclarations.SIZE_AND_FAMILY_REQUIRED,
              "a { font: 12px; }");
    testError(FixupFontDeclarations.SIZE_AND_FAMILY_REQUIRED,
              "a { font: serif; }");
  }

  public void testTooManyLineHeights() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY.get(
        FixupFontDeclarations.FontProperty.LINE_HEIGHT),
              "a { font: 1ex/2pt/4px serif; }");
  }

  public void testTooManyLineHeightPairs() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY.get(
        FixupFontDeclarations.FontProperty.LINE_HEIGHT),
              "a { font: 1ex/2pt 4px/8em serif; }");
  }

  public void testTooManyFontSizes() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY.get(
        FixupFontDeclarations.FontProperty.SIZE),
              "a { font: large 12pt serif; }");
  }

  public void testTooManyStyles() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY.get(
        FixupFontDeclarations.FontProperty.STYLE),
              "a { font: italic oblique 10px helvetica; }");
  }

  public void testTooManyVariants() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY.get(
        FixupFontDeclarations.FontProperty.VARIANT),
              "a { font: small-caps small-caps 10px helvetica; }");
  }

  public void testTooManyWeights() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY.get(
        FixupFontDeclarations.FontProperty.WEIGHT),
              "a { font: bold 800 72pt serif; }");
  }

  public void testTooManyNormals() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY_NORMALS,
              "a { font: normal normal normal normal 8px sans-serif; }");
  }

  public void testNormalTooLate() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY_PRE_SIZE,
              "a { font: italic bold normal normal 12pt serif; }");
  }

  public void testNormalAsAFontName() throws Exception {
    testValid("a { font: italic 3.14em normal; }");
  }

  public void testNormalAsAFontName2() throws Exception {
    testValid("a { font: italic bold 12pt normal; }");
  }

  public void testSizeHeightSize() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY.get(
        FixupFontDeclarations.FontProperty.SIZE),
              "a { font: 1em/2em 3em serif; }");
  }

  public void testSizeSizeHeight() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY.get(
        FixupFontDeclarations.FontProperty.SIZE),
              "a { font: 3em 1em/2em serif; }");
  }

  public void testTooManyPreLineHeight() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY_PRE_SIZE,
              "a { font: normal italic small-caps bold 18px/20px serif; }");
  }

  public void testTooManyPreSize() throws Exception {
    testError(FixupFontDeclarations.TOO_MANY_PRE_SIZE,
              "a { font: normal italic small-caps bold 18px serif; }");
  }

  public void testPreSizeInterloperSize() throws Exception {
    testError(FixupFontDeclarations.PRE_SIZE_INTERLOPER_SIZE,
              "a { font: normal normal normal serif 12px sans-serif; }");
  }

  private CssTree parse(String source) throws GssParserException {
    return new GssParser(new SourceCode(null, source)).parse();
  }

  private CssTree runPass(CssTree input) {
    return runPass(FixupFontDeclarations.InputMode.CSS, input);
  }

  private CssTree runPass(
      FixupFontDeclarations.InputMode mode, CssTree input) {
    CssTree result =
        new CssTree(input.getSourceCode(), input.getRoot().deepCopy());
    System.err.println(SExprPrinter.print(result));
    errorManager = new DummyErrorManager();
    new FixupFontDeclarations(mode, errorManager, result).runPass();
    // this will be helpful for debugging tests
    System.err.println(SExprPrinter.print(result));
    return result;
  }

  private CssTree testTree(String css, String output) throws Exception {
    return testTree(FixupFontDeclarations.InputMode.CSS, css, output);
  }

  private CssTree testTree(
      FixupFontDeclarations.InputMode mode, String gss, String output)
      throws Exception {
    CssTree tree = testValid(mode, gss);
    assertEquals(output.replace("[", "(").replace("]", ")"),
                 AstPrinter.print(tree).replace("[", "(").replace("]", ")"));
    //assertEquals(output, AstPrinter.print(tree));
    return tree;
  }

  private CssTree testValid(String css) throws Exception {
    return testValid(FixupFontDeclarations.InputMode.CSS, css);
  }

  private CssTree testValid(
      FixupFontDeclarations.InputMode mode, String css) throws Exception {
    CssTree tree = runPass(mode, parse(css));
    assertNotNull(tree);
    assertFalse(Joiner.on("\n").join(
        Iterables.transform(errorManager.getErrors(),
                            new Function<GssError, String>() {
                              @Override public String apply(GssError e) {
                                return e.format();
                              }
                            })),
                errorManager.hasErrors());
    return tree;
  }

  private CssTree testError(String expectedError, String css) throws Exception {
    return testError(FixupFontDeclarations.InputMode.CSS, expectedError, css);
  }

  private CssTree testError(
      FixupFontDeclarations.InputMode mode,
      final String expectedError, String css)
      throws Exception {
    CssTree tree = runPass(mode, parse(css));
    assertNotNull(tree);
    assertTrue(errorManager.hasErrors());
    assertTrue(
        Iterables.any(errorManager.getErrors(),
                      new Predicate<GssError>() {
                        @Override public boolean apply(GssError e) {
                          return expectedError.equals(e.getMessage());
                        }
                      }));
    return tree;
  }
}
