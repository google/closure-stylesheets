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

import com.google.common.base.Preconditions;
import com.google.common.css.compiler.ast.ChunkAware;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;

import java.util.Map;

/**
 * Pass which marks which {@link ChunkAware} nodes belong to which chunk.
 *
 * @param <T> type of chunk id objects set on {@link ChunkAware} nodes
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class MapChunkAwareNodesToChunk<T> extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final CssTree tree;

  private final Map<String, T> fileToChunk;

  public MapChunkAwareNodesToChunk(
      CssTree tree, Map<String, T> fileToChunk) {
    this.tree = tree;
    this.fileToChunk = fileToChunk;
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode definition) {
    definition.setChunk(getChunk(definition));
    return true;
  }

  /**
   * Marks the selector with an appropriate chunk.
   */
  @Override
  public boolean enterSelector(CssSelectorNode selector) {
    selector.setChunk(getChunk(selector));
    return true;
  }

  @Override
  public boolean enterFunctionNode(CssFunctionNode function) {
    function.setChunk(getChunk(function));
    return true;
  }

  @Override
  public boolean enterMediaRule(CssMediaRuleNode media) {
    media.setChunk(getChunk(media));
    return true;
  }

  @Override
  public boolean enterKeyframesRule(CssKeyframesNode keyframes) {
    keyframes.setChunk(getChunk(keyframes));
    return true;
  }

  @Override
  public boolean enterFontFace(CssFontFaceNode fontFaceNode) {
    fontFaceNode.setChunk(getChunk(fontFaceNode));
    return true;
  }

  /**
   * Determine the chunk for the given node.
   *
   * @param node A css node that is chunk aware.
   */
  private <N extends CssNode & ChunkAware> T getChunk(N node) {
    @SuppressWarnings("unchecked")
    T chunk = (T) node.getChunk();
    if (chunk == null) {
      chunk = getChunk(node, fileToChunk);
    }
    return chunk;
  }

  @Override
  public void runPass() {
    tree.getVisitController().startVisit(this);
  }

  /**
   * Finds the chunk of a node, by looking up the filename of its
   * source code location in the given map from filenames to
   * chunks. Assumes that the map is complete, such that it always
   * contains the filename of any valid source code location.
   *
   * @param node the node to find the chunk for
   * @param fileToChunk a map from filenames to chunks
   * @return the chunk of the node according to the map
   */
  static <T> T getChunk(CssNode node, Map<String, T> fileToChunk) {
    String file = node.getSourceCodeLocation().getSourceCode().getFileName();
    T chunk = fileToChunk.get(file);
    Preconditions.checkNotNull(chunk, "File '%s' does not have chunk mapping",
        file);
    return chunk;
  }
}
