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

import com.google.common.collect.ImmutableMap;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;

import java.util.Map;

/**
 * Unit tests for {@link ChunkCompactPrinter}.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class ChunkCompactPrinterTest extends AbstractCompactPrinterTest {

  protected void setupTestTree() {
    String sourceCode =
        "foo,hr,.bar,i{} " +
        "a,i{} " +
        "b,hr{} " +
        "a#a{} " +
        "b#b{} " +
        "i,hr{}" +
        "a i{}" +
        "b > i + em, a#a b {}" +
        "b + i, a+i {}" +
        "@media print { foo {} }" +
        "@keyframes my-animation { 0% {} }" +
        "@font-face { font-family:'Roboto'; }";

    Map<String, String> selectorToChunk =
      new ImmutableMap.Builder<String, String>()
      .put("foo", "foo")
      .put("a", "foo")
      .put("a#a", "foo")
      .put("a#a b", "foo")
      .put("b+i", "foo")
      .put(".bar", "bar")
      .put("b", "bar")
      .put("b#b", "bar")
      .put("b>i+em", "bar")
      .put("hr", "baz")
      .put("i", "baz")
      .put("a i", "baz")
      .put("a+i", "baz")
      .put("my-animation", "bar")
      .put("print", "foo")
      .build();

    parseStyleSheet(sourceCode);
    new SetSelectorChunk(newTree, selectorToChunk).runPass();
  }

  public void testChunkOutput() {
    setupTestTree();
    assertChunkOutput("foo", "foo{}a{}a#a{}a#a b{}b+i{}@media print{foo{}}"
        + "@font-face{font-family:'Roboto'}", newTree);
    assertChunkOutput(
        "bar", ".bar{}b{}b#b{}b>i+em{}@keyframes my-animation{0%{}}", newTree);
    assertChunkOutput("baz", "hr,i{}i{}hr{}i,hr{}a i{}a+i{}", newTree);
  }

  private void assertChunkOutput(String chunk, String expected, CssTree tree) {
    ChunkCompactPrinter<String> printer = new ChunkCompactPrinter<String>(tree, chunk);
    printer.runPass();
    assertEquals(expected, printer.getCompactPrintedString());
  }

  /**
   * Helper pass to mark selectors with a chunk, uses a map from string
   * representation of a selector to a chunk, given selector belongs to.
   */
  private static class SetSelectorChunk extends DefaultTreeVisitor
      implements CssCompilerPass {
    private CssTree tree;
    private Map<String, String> selectorToChunkMap;

    /**
     * The current "top" selector - one which is first in a list of selectors
     * divided by combinators. It is used to correctly set the chunk of
     * combined selectors.
     */
    private CssSelectorNode currentTopSelector;

    /**
     * The chunk of current "top" selector.
     */
    private String topSelectorChunk;

    /**
     * Creates the helper pass for a given CSS AST and selector to chunk
     * mapping.
     *
     * @param tree the CSS AST tree to be mapped
     * @param selectorToChunk a map from string representation of a selector
     *     to a chunk this selector belongs to
     */
    public SetSelectorChunk(
        CssTree tree, Map<String, String> selectorToChunk) {
      this.tree = tree;
      this.selectorToChunkMap = selectorToChunk;
    }

    @Override
    public boolean enterSelector(CssSelectorNode selector) {
      if (currentTopSelector == null) {
        currentTopSelector = selector;
        topSelectorChunk = selectorToChunkMap.get(
            PassUtil.printSelector(selector));
      }
      selector.setChunk(topSelectorChunk);
      return true;
    }

    @Override
    public void leaveSelector(CssSelectorNode selector) {
      if (currentTopSelector == selector) {
        currentTopSelector = null;
        topSelectorChunk = null;
      }
    }

    @Override
    public boolean enterMediaRule(CssMediaRuleNode media) {
      media.setChunk(
          selectorToChunkMap.get(media.getParameters().get(0).getValue()));
      return true;
    }

    @Override
    public boolean enterKeyframesRule(CssKeyframesNode keyframes) {
      keyframes.setChunk(
          selectorToChunkMap.get(keyframes.getParameters().get(0).getValue()));
      return true;
    }

    @Override
    public boolean enterFontFace(CssFontFaceNode cssFontFaceNode) {
      cssFontFaceNode.setChunk("foo");
      return true;
    }

    @Override
    public void runPass() {
      tree.getVisitController().startVisit(this);
    }
  }
}
