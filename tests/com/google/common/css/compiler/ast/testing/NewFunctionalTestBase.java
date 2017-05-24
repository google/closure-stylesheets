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

package com.google.common.css.compiler.ast.testing;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.BasicErrorManager;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.PrettyPrinter;
import com.google.common.css.compiler.passes.TemplateCompactPrinter;
import java.util.List;
import java.util.Map;
import org.junit.Assert;

/**
 * Base class for testing the passes which use an {@link ErrorManager}.
 *
 */
public class NewFunctionalTestBase extends FunctionalTestCommonBase {

  protected static final String TEST_FILENAME = "test";

  protected ErrorManager errorManager;

  @Override
  public void parseAndBuildTree(String sourceCode) {
    parseAndBuildTree(ImmutableMap.of(TEST_FILENAME, sourceCode));
  }

  /**
   * Parses GSS style sheets and calls fail if an exception is thrown.
   *
   * @param fileNameToGss a map connecting names to GSS style sheets
   */
  public void parseAndBuildTree(ImmutableMap<String, String> fileNameToGss) {
    try {
      parseAndRun(fileNameToGss);
    } catch (GssParserException e) {
      Assert.fail(e.getMessage());
    }
  }

  /**
   * Parses a GSS style sheet and returns the tree. In addition, it checks
   * whether the actual error messages exactly match the expected ones.
   *
   * @param gss the GSS style sheet
   * @param expectedMessages the expected error messages in the right order
   * @return the CSS tree created by the parser
   * @throws GssParserException
   */
  protected CssTree parseAndRun(String gss, String ... expectedMessages)
      throws GssParserException {
    return parseAndRun(
        ImmutableMap.of(TEST_FILENAME, gss), true /* exactMatch */,
        expectedMessages);
  }

  /**
   * Parses a GSS style sheet and returns the tree. In addition, it checks
   * whether the actual error messages match the expected ones.
   *
   * @param gss the GSS style sheet
   * @param exactMatch Determines if the actual error messages have to exactly
   *     match the expected ones or only have to contain them.
   * @param expectedMessages the expected error messages or parts of the
   *     messages in the right order
   * @return the CSS tree created by the parser
   * @throws GssParserException
   */
  protected CssTree parseAndRun(String gss, boolean exactMatch,
      String... expectedMessages) throws GssParserException {
    return parseAndRun(
        ImmutableMap.of(TEST_FILENAME, gss), exactMatch, expectedMessages);
}

  /**
   * Parses GSS style sheets and returns one tree containing everything.
   * In addition, it checks whether the actual error messages exactly match
   * the expected ones.
   *
   * @param fileNameToGss a map connecting names to GSS style sheets
   * @param expectedMessages the expected error messages in the right order
   * @return the CSS tree created by the parser
   * @throws GssParserException
   */
  protected CssTree parseAndRun(
      ImmutableMap<String, String> fileNameToGss,
      String... expectedMessages) throws GssParserException {
    return parseAndRun(fileNameToGss, true /* exactMatch */, expectedMessages);
  }

  /**
   * Parses GSS style sheets and returns one tree containing everything.
   * In addition, it checks whether the actual error messages exactly match
   * the expected ones.
   *
   * @param fileNameToGss a map connecting names to GSS style sheets
   * @param exactMatch Determines if the actual error messages have to exactly
   *     match the expected ones or only have to contain them.
   * @param expectedMessages the expected error messages or parts of the
   *     messages in the right order
   * @return the CSS tree created by the parser
   * @throws GssParserException
   */
  protected CssTree parseAndRun(
      ImmutableMap<String, String> fileNameToGss, boolean exactMatch,
      String... expectedMessages) throws GssParserException {
    tree = parse(fileNameToGss);
    errorManager = new TestErrorManager(exactMatch, expectedMessages);
    runPass();
    errorManager.generateReport();
    assertWithMessage("Encountered all errors.")
        .that(((TestErrorManager) errorManager).hasEncounteredAllErrors())
        .isTrue();
    return tree;
  }

  /**
   * Parses GSS style sheets and returns one tree containing everything.
   *
   * @param fileNameToGss a map connecting names to GSS style sheets
   * @return the CSS tree created by the parser
   * @throws GssParserException
   */
  protected CssTree parse(ImmutableMap<String, String> fileNameToGss)
      throws GssParserException {
    ImmutableList.Builder<SourceCode> builder = ImmutableList.builder();
    for (Map.Entry<String, String> entry : fileNameToGss.entrySet()) {
      builder.add(new SourceCode(entry.getKey(), entry.getValue()));
    }
    return new GssParser(builder.build()).parse();
  }

  public ErrorManager getErrorManager() {
    return errorManager;
  }

  public CssTree getTree() {
    return tree;
  }

  /**
   * Normalizes the compiled CSS to a pretty-printed form that can be compared
   * with the result of {@link #normalizeExpectedCss(String)}.
   */
  private String getCompiledCss() {
    PrettyPrinter prettyPrinterPass = new PrettyPrinter(tree
        .getVisitController());
    prettyPrinterPass.runPass();
    return prettyPrinterPass.getPrettyPrintedString();
  }


  /**
   * Normalizes the expected CSS to a pretty-printed form that can be compared
   * with the result of {@link #getCompiledCss()}.
   */
  private static String normalizeExpectedCss(String expectedCss)
      throws GssParserException {
    List<SourceCode> inputs = ImmutableList.of(
        new SourceCode("expectedCss", expectedCss));
    CssTree tree = new GssParser(inputs).parse();
    PrettyPrinter prettyPrinterPass = new PrettyPrinter(tree
        .getVisitController());
    prettyPrinterPass.runPass();
    return prettyPrinterPass.getPrettyPrintedString();
  }

  /**
   * Takes a string of GSS to compile and the CSS that should be produced as a
   * result of compilation. The compiled GSS and expected CSS will be normalized
   * to a pretty-printed format for comparison, so it is not necessary to format
   * either by hand.
   */
  protected void test(String inputGss, String expectedCss)
      throws GssParserException {
    parseAndRun(inputGss);
    assertThat(getCompiledCss()).isEqualTo(normalizeExpectedCss(expectedCss));
  }

  /**
   * Used to check whether the actual error messages match the expected ones.
   */
  public static class TestErrorManager extends BasicErrorManager {
    private final String[] expectedMessages;
    private int currentIndex = 0;
    private boolean exactMatch;

    public TestErrorManager(String[] expectedMessages) {
      this(true, expectedMessages);
    }

    /**
     * Used to check whether the actual error messages match the expected ones.
     *
     * @param exactMatch Determines if the actual error messages have to exactly
     *     match the expected ones or only have to contain them.
     * @param expectedMessages the expected error messages or parts of the
     *     messages in the right order
     */
    public TestErrorManager(boolean exactMatch, String[] expectedMessages) {
      this.exactMatch = exactMatch;
      this.expectedMessages = expectedMessages;
    }

    @Override
    public void generateReport() {
      for (GssError error : errors) {
        print(error);
      }
      for (GssError warning : warnings) {
        print(warning);
      }
    }

    public void print(GssError error) {
      assertWithMessage("Unexpected extra error: " + error.format())
          .that(currentIndex)
          .isLessThan(expectedMessages.length);
      print(error.getMessage());
    }

    @Override
    public void print(String message) {
      assertWithMessage("Unexpected extra error: " + message)
          .that(currentIndex)
          .isLessThan(expectedMessages.length);
      if (exactMatch) {
        assertThat(message).isEqualTo(expectedMessages[currentIndex]);
      } else {
        assertWithMessage(
                "Expected error '"
                    + message
                    + "' to contain '"
                    + expectedMessages[currentIndex]
                    + "'.")
            .that(message)
            .contains(expectedMessages[currentIndex]);
      }
      currentIndex++;
    }

    public boolean hasEncounteredAllErrors() {
      return currentIndex == expectedMessages.length;
    }
  }

  /**
   * Helper pass to mark all selectors as belonging to a single chunk.
   * (The official printer for compiled CSS, {@link TemplateCompactPrinter}, requires
   * at least one chunk, so this is useful for tests that want to assert against the official
   * compiled output but don't care directly about chunking.)
   */
  private final class AllSelectorsInOneChunk extends DefaultTreeVisitor implements CssCompilerPass {

    static final String THE_CHUNK = "THE_CHUNK";

    @Override
    public boolean enterSelector(CssSelectorNode selector) {
      selector.setChunk(THE_CHUNK);
      return true;
    }

    @Override
    public boolean enterMediaRule(CssMediaRuleNode media) {
      media.setChunk(THE_CHUNK);
      return true;
    }

    @Override
    public boolean enterKeyframesRule(CssKeyframesNode keyframes) {
      keyframes.setChunk(THE_CHUNK);
      return true;
    }

    @Override
    public boolean enterFontFace(CssFontFaceNode cssFontFaceNode) {
      cssFontFaceNode.setChunk(THE_CHUNK);
      return true;
    }

    @Override
    public void runPass() {
      tree.getVisitController().startVisit(this);
    }
  }
}
