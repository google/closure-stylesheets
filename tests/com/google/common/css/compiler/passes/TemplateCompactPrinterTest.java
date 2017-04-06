/*
 * Copyright 2010 Google Inc.
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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import java.util.Map;

/**
 * Unit tests for {@link TemplateCompactPrinter}.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class TemplateCompactPrinterTest extends ChunkCompactPrinterTest {

  private static final char rS = TemplateCompactPrinter.RULE_START;
  private static final char rE = TemplateCompactPrinter.RULE_END;
  private static final char dS = TemplateCompactPrinter.DECLARATION_START;
  private static final char dE = TemplateCompactPrinter.DECLARATION_END;

  public void testChunkOutput_initialChunk() {
    setupTestTree();

    TemplateCompactPrinter<String> printer = createPrinter("foo");
    printer.runPass();

    assertThat(printer.getCompactPrintedString())
        .isEqualTo(
            rS
                + "foo{}"
                + rE
                + rS
                + "a{}"
                + rE
                + rS
                + "a#a{}"
                + rE
                + rS
                + "a#a b{}"
                + rE
                + rS
                + "b+i{}"
                + rE
                + rS
                + "@media print{"
                + rS
                + "foo{}"
                + rE
                + "}"
                + rE
                + rS
                + "@font-face"
                + "{"
                + dS
                + "font-family:'Roboto'"
                + dE
                + "}"
                + rE);
  }

  public void testChunkOutput_middleChunk() {
    setupTestTree();

    TemplateCompactPrinter<String> printer = createPrinter("bar");
    printer.runPass();

    assertThat(printer.getCompactPrintedString())
        .isEqualTo(
            rS
                + ".bar{}"
                + rE
                + rS
                + "b{}"
                + rE
                + rS
                + "b#b{}"
                + rE
                + rS
                + "b>i+em{}"
                + rE
                + "@keyframes my-animation{"
                + rS
                + "0%{}"
                + rE
                + "}");
  }

  public void testChunkOutput_endChunk() {
    setupTestTree();

    TemplateCompactPrinter<String> printer = createPrinter("baz");
    printer.runPass();

    assertThat(printer.getCompactPrintedString())
        .isEqualTo(
            rS + "hr,i{}" + rE + rS + "i{}" + rE + rS + "hr{}" + rE + rS + "i,hr{}" + rE + rS
                + "a i{}" + rE + rS + "a+i{}" + rE);
  }

  public void testMarkedComments_unpreservedByDefault() {
    String sourceCode =
        "/* Header comment\n"
            + " * @license MIT */\n"
            + "foo{} "
            + "/* @preserve Preserved comment 1 */ a{} "
            + "@media print { foo { /* @preserve Preserved comment 2 */ color: red } } "
            + "/*! this is important */\n"
            + "foo{} ";

    parseStyleSheet(sourceCode);

    TemplateCompactPrinter<String> printer = createPrinter("foo");
    printer.runPass();

    assertThat(printer.getCompactPrintedString())
        .isEqualTo(
            rS
                + "foo{}"
                + rE
                + rS
                + "a{}"
                + rE
                + rS
                + "@media print{"
                + rS
                + "foo{"
                + dS
                + "color:red"
                + dE
                + "}"
                + rE
                + "}"
                + rE
                + rS
                + "foo{}"
                + rE);
  }

  public void testMarkedComments_unpreservedExplicitly() {
    String sourceCode =
        "/* Header comment\n"
            + " * @license MIT */\n"
            + "foo{} "
            + "/* @preserve Preserved comment 1 */ a{} "
            + "@media print { foo { /* @preserve Preserved comment 2 */ color: red } }\n"
            + "/*! this is important */\n"
            + "foo{}";

    parseStyleSheet(sourceCode);

    TemplateCompactPrinter<String> printer = createNonCommentPreservingPrinter("foo");
    printer.runPass();

    assertThat(printer.getCompactPrintedString())
        .isEqualTo(
            rS
                + "foo{}"
                + rE
                + rS
                + "a{}"
                + rE
                + rS
                + "@media print{"
                + rS
                + "foo{"
                + dS
                + "color:red"
                + dE
                + "}"
                + rE
                + "}"
                + rE
                + rS
                + "foo{}"
                + rE);
  }

  public void testMarkedComments_preserved() {
    String sourceCode =
        "/* Header comment\n"
            + " * @license MIT */\n"
            + "foo{} "
            + "/* @preserve Preserved comment 1 */ a{} "
            + "@media print { foo { /* @preserve Preserved comment 2 */ color: red } }\n"
            + "/*! this is important */\n"
            + "foo{}";

    parseStyleSheet(sourceCode);

    TemplateCompactPrinter<String> printer = createCommentPreservingPrinter("foo");
    printer.runPass();

    assertEquals(
        rS + "\n/* Header comment\n * @license MIT */\n" + "foo{}" + rE
            + rS + "\n/* @preserve Preserved comment 1 */\n" + "a{}" + rE
            + rS + "@media print{" + rS
            // TODO(flan): The declaration start should be *before* the comment, not after.
            // The problem is that the preserved comment printing visitor visits before the
            // TemplateCompactPrinter.
            + "foo{\n/* @preserve Preserved comment 2 */\n" + dS + "color:red" + dE + "}" + rE
            + "}" + rE
            + rS + "\n/*! this is important */\nfoo{}" + rE,
        printer.getCompactPrintedString());
  }

  public void testMarkedComments_multipleAdjacentPreserved() {
    String sourceCode =
        "/* @license MIT */\n"
            + "/* @preserve Keep this comment, too */\n"
            + "/*! Me, too! */\n\n"
            + "/* Not me, I'm just a comment, not important. :-( */\n"
            + "foo{}";

    parseStyleSheet(sourceCode);

    TemplateCompactPrinter<String> printer = createCommentPreservingPrinter("foo");
    printer.runPass();

    assertThat(printer.getCompactPrintedString())
        .isEqualTo(
            rS
                + "\n/* @license MIT *//* @preserve Keep this comment, too *//*! Me, too! */\n"
                + "foo{}"
                + rE);
  }

  public void testMarkedComments_preservedWithFooterBeforeNextFile() {
    String sourceCode1 = "/* Header comment\n" + " * @license MIT */\n" + "foo{}";

    String sourceCode2 = "bar{}";

    Map<String, String> selectorToChunk = ImmutableMap.of("foo", "foo");

    newTestBase = new NewFunctionalTestBase();
    newTestBase.parseAndBuildTree(ImmutableMap.of("fooFile", sourceCode1, "barFile", sourceCode2));
    newTree = newTestBase.getTree();
    tree = newTestBase.getTree();
    runPassesOnNewTree();

    new SetSelectorChunk(newTree, selectorToChunk).runPass();

    TemplateCompactPrinter<String> printer = createCommentPreservingPrinter("foo");
    printer.runPass();
    
    System.out.println(printer.getCompactPrintedString());

    assertThat(printer.getCompactPrintedString())
        .isEqualTo(
            rS
                + "\n/* Header comment\n"
                + " * @license MIT */\n"
                + "foo{}"
                + rE
                + "/* END OF LICENSED CSS FILE */\n");
  }

  public void testMarkedComments_preservedWithFooterAfterPreviousFile() {
    String sourceCode1 = "foo{}";
    String sourceCode2 = "/* Header comment\n" + " * @license MIT */\n" + "bar{}";

    Map<String, String> selectorToChunk = ImmutableMap.of("foo", "foo", "bar", "bar");

    newTestBase = new NewFunctionalTestBase();
    newTestBase.parseAndBuildTree(ImmutableMap.of("fooFile", sourceCode1, "barFile", sourceCode2));
    newTree = newTestBase.getTree();
    tree = newTestBase.getTree();
    runPassesOnNewTree();

    new SetSelectorChunk(newTree, selectorToChunk).runPass();

    TemplateCompactPrinter<String> printer = createCommentPreservingPrinter("bar");
    printer.runPass();
    
    System.out.println(printer.getCompactPrintedString());

    assertThat(printer.getCompactPrintedString())
        .isEqualTo(rS + "\n/* Header comment\n" + " * @license MIT */\n" + "bar{}" + rE);
  }

  public void testMarkedComments_preservedButWithBadAnnotations() {
    String sourceCode = "/* ! !Header comment @licenseless *! @preservement !*/\n" + "foo{}";

    parseStyleSheet(sourceCode);

    TemplateCompactPrinter<String> printer = createCommentPreservingPrinter("foo");
    printer.runPass();

    assertThat(printer.getCompactPrintedString()).isEqualTo(rS + "foo{}" + rE);
  }

  public void testMarkedComments_preservedButWithSomeBadAnnotationsOneGood() {
    String sourceCode = "/* ! Header comment @licenseless /*! @preserve */\n" + "foo{}";

    parseStyleSheet(sourceCode);

    TemplateCompactPrinter<String> printer = createCommentPreservingPrinter("foo");
    printer.runPass();

    assertThat(printer.getCompactPrintedString())
        .isEqualTo(rS + "\n/* ! Header comment @licenseless /*! @preserve */\n" + "foo{}" + rE);
  }

  private TemplateCompactPrinter<String> createPrinter(String chunkId) {
    return new TemplateCompactPrinter<String>(newTree, chunkId);
  }


  private TemplateCompactPrinter<String> createCommentPreservingPrinter(String chunkId) {
    TemplateCompactPrinter<String> printer = new TemplateCompactPrinter<String>(newTree, chunkId);
    printer.setPreserveMarkedComments(true);
    return printer;
  }

  private TemplateCompactPrinter<String> createNonCommentPreservingPrinter(String chunkId) {
    TemplateCompactPrinter<String> printer = new TemplateCompactPrinter<String>(newTree, chunkId);
    printer.setPreserveMarkedComments(false);
    return printer;
  }
}
