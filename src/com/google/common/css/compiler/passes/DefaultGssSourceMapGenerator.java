/*
 * Copyright 2015 Google Inc.
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
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.css.JobDescription.SourceMapDetailLevel;
import com.google.common.css.compiler.ast.CssNode;
import com.google.debugging.sourcemap.FilePosition;
import com.google.debugging.sourcemap.SourceMapFormat;
import com.google.debugging.sourcemap.SourceMapGenerator;
import com.google.debugging.sourcemap.SourceMapGeneratorFactory;
import com.google.debugging.sourcemap.SourceMapGeneratorV3;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Class to collect and generate source map(v3) for Gss compiler. It is intended to be used by
 * {@link com.google.common.css.compiler.passes.CodePrinter}.
 *
 * <p>Source Map Revision 3 Proposal:
 * https://docs.google.com/document/d/1U1RGAehQwRypUTovF1KRlpiOFze0b-_2gc6fAH0KY0k/edit?usp=sharing
 *
 * @see com.google.debugging.sourcemap.SourceMapGeneratorV3
 *
 * @author steveyang@google.com (Chenyun Yang)
 */
public final class DefaultGssSourceMapGenerator implements GssSourceMapGenerator {

  /** The underlying source map generator to use. */
  private SourceMapGenerator generator;

  /**
   * Maintains a mapping from a given node's source code position to its generated output.
   * This position is relative to the current run of the CodePrinter and will be normalized
   * later on by the SourceMap.
   */
  static class Mapping {
    CssNode node;
    FilePosition start;
    FilePosition end;
  }

  /**
   * Map used internally to get {@code Predicate<CssNode>}s from {@code DetailLevel}.
   *
   * <ul>
   *   <li>{@code ALL} provides the most details by generating source map for every nodes containing
   *       source locations.
   *   <li>{@code DEFAULT} generates source map for selected nodes and results in a smaller output
   *       suitable to use in production.
   * </ul>
   */
  private static final ImmutableMap<SourceMapDetailLevel, Predicate<CssNode>>
      DETAIL_LEVEL_PREDICATES =
          Maps.immutableEnumMap(
              ImmutableMap.of(
                  SourceMapDetailLevel.ALL,
                  Predicates.<CssNode>alwaysTrue(),
                  SourceMapDetailLevel.DEFAULT,
                  Predicates.<CssNode>alwaysTrue()));

  /** Deque to hold current mappings on stack while visiting the subtree. **/
  private final Deque<Mapping> mappings;

  /** List of all the mappings generated for code visit **/
  private final List<Mapping> allMappings;

  private SourceMapDetailLevel sourceMapDetailLevel;

  /** Predicate to determine whether to include current node under visit into {@code mappings}. **/
  private Predicate<CssNode> detailLevelPredicate;

  /**
   * Constructor to get source map class to use.
   *
   * @param sourceMapDetailLevel used to control the output details of source map
   */
  public DefaultGssSourceMapGenerator(SourceMapDetailLevel sourceMapDetailLevel) {
    Preconditions.checkState(sourceMapDetailLevel != null);
    this.mappings = new ArrayDeque<>();
    this.generator = SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3);
    this.allMappings = new ArrayList<>();
    this.sourceMapDetailLevel = sourceMapDetailLevel;
    this.detailLevelPredicate = DETAIL_LEVEL_PREDICATES.get(this.sourceMapDetailLevel);
  }

  /**
   * Appends the generated source map to {@code out}.
   *
   * @param out an {@link Appendable} object to append the output on
   * @param name filename to be written inside the source map (not the filename where writes to)
   *
   * @see SourceMapGeneratorV3#appendTo
   */
  @Override
  public void appendOutputTo(Appendable out, String name) throws IOException {
    generateSourceMap();
    generator.appendTo(out, name);
  }

  /**
   * Starts the source mapping for the given node at the current position.
   * This is intended to be called before the node is written to the buffer.
   *
   * @param node the {@link CssNode} to be processed
   * @param startLine the first character's line number once it starts writing output
   * @param startCharIndex the first character's character index once it starts writing output
   */
  @Override
  public void startSourceMapping(CssNode node, int startLine, int startCharIndex) {
    Preconditions.checkState(node != null);
    Preconditions.checkState(startLine >= 0);
    Preconditions.checkState(startCharIndex >= 0);
    if (node.getSourceCodeLocation() != null
        && detailLevelPredicate.apply(node)) {
      Mapping mapping = new Mapping();
      mapping.node = node;
      mapping.start = new FilePosition(startLine, startCharIndex);
      mappings.push(mapping);
      allMappings.add(mapping);
    }
  }

  /**
   * Finishes the source mapping for the given node at the current position.
   * This is intended to be called immediately after the whole node is written to the buffer.
   *
   * @param node the {@link CssNode} to be processed
   * @param endLine the last character's line number when it ends writing output
   * @param endCharIndex the last character's character index when it ends writing output
   */
  @Override
  public void endSourceMapping(CssNode node, int endLine, int endCharIndex) {
    Preconditions.checkState(node != null);
    Preconditions.checkState(endLine >= 0);
    // -1 when a node contributes no content at the start of the buffer,
    // as when a CssImportBlockNode is encountered, and there is no
    // copyright comment.
    Preconditions.checkState(endCharIndex >= -1);
    endCharIndex++; //
    if (!mappings.isEmpty() && mappings.peek().node == node) {
      Mapping mapping = mappings.pop();
      mapping.end = new FilePosition(endLine, endCharIndex);
    }
  }

  /**
   * Sets the prefix to be added to the beginning of each source path passed to
   * {@link #addMapping} as debuggers expect (prefix + sourceName) to be a URL
   * for loading the source code.
   *
   * @param path The URL prefix to save in the sourcemap file
   */
  @Override
  public void setSourceRoot(String path){
    ((SourceMapGeneratorV3) generator).setSourceRoot(path);
  }

  /**
   * Generates the source map by passing all mappings to {@link #generator}.
   */
  private void generateSourceMap() {
    List<CompleteMapping> completeMappings = new ArrayList<>(allMappings.size());
    for (Mapping mapping : allMappings) {
      // If the node does not have an associated source file or source location
      // is unknown, then the node does not have sufficient info for source map.
      if (mapping.node.getSourceCodeLocation().isUnknown()) {
        continue;
      }
      CompleteMapping completeMapping = new CompleteMapping(mapping);
      if (completeMapping.sourceFile == null) {
        continue;
      }
      completeMappings.add(completeMapping);
    }
    Collections.sort(completeMappings);
    for (CompleteMapping completeMapping : completeMappings) {
      // TODO: could pass in an optional symbol name
      generator.addMapping(
          completeMapping.sourceFile, null,
          completeMapping.inputStart,
          completeMapping.outputStart, completeMapping.outputEnd);
    }
  }


  private static final class CompleteMapping implements Comparable<CompleteMapping> {
    final String sourceFile;
    final FilePosition inputStart;
    final FilePosition outputStart;
    final FilePosition outputEnd;

    CompleteMapping(Mapping mapping) {
      CssNode node = mapping.node;
      this.sourceFile = getSourceFileName(node);
      this.inputStart = new FilePosition(
          getStartLineno(node), getStartCharIndex(node));
      this.outputStart = mapping.start;
      this.outputEnd = mapping.end;
    }

    @Override
    public int compareTo(CompleteMapping m) {
      int delta = outputStart.getLine() - m.outputStart.getLine();
      if (delta == 0) {
        delta = outputStart.getColumn() - m.outputStart.getColumn();
      }
      return delta;
    }

    /**
     * Gets the source file file for current node.
     */
    private static String getSourceFileName(CssNode node) {
      return node.getSourceCodeLocation().getSourceCode().getFileName();
    }

    /**
     * Gets the start line index in the source code of {@code node} adjusted to 0-based indices.
     *
     * <p>
     * Note: Gss compiler uses a 1-based line number and source map V3 uses a 0-based line number.
     */
    private static int getStartLineno(CssNode node) {
      return node.getSourceCodeLocation().getLineNumber() - 1;
    }

    /**
     * Gets the start character index in the output buffer for current {@code node}.
     */
    private static int getStartCharIndex(CssNode node) {
      return node.getSourceCodeLocation().getCharacterIndex();
    }
  }
}
