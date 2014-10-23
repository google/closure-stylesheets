/*
 * Copyright 2014 Google Inc.
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
package com.google.common.css.compiler.ast;

import com.google.common.collect.ImmutableList;
import com.google.common.css.SourceCode;

import java.util.List;

/**
 * Base parser implementation that delegates management of the underlying
 * JavaCC parser to the subclass. This class does not expose any public
 * methods; subclasses should expose an appropriate interface.
 *
 * @see GssParser
 */
public abstract class AbstractGssParser {

  protected static final StringCharStream EMPTY_CHAR_STREAM =
      new StringCharStream("");

  /**
   * Parses a list of GSS sources. Subclasses should use this method to do
   * the actual parsing of {@code SourceCode} objects. It will in turn call
   * the subclass's implementation of {@link #getParser()} as necessary.
   *
   * @param sources a list of GSS {@link SourceCode} objects to parse
   * @param errorHandling if error handling should be enabled. If this is
   *     {@code false}, no {@code GssParserException}s will be returned in
   *     the result.
   * @return ParseResult the result containing the {@link CssTree} and
   *     parsing errors
   */
  protected final ParseResult parseInternal(List<SourceCode> sources,
      boolean errorHandling) throws GssParserException {
    SourceCode globalSourceCode = new SourceCode("global", null);
    CssBlockNode globalBlock =
        new CssBlockNode(false /* isEnclosedWithBraces */);
    CssTree tree = new CssTree(globalSourceCode, new CssRootNode(globalBlock));
    ImmutableList.Builder<GssParserException> builder =
        ImmutableList.builder();
    for (SourceCode source : sources) {
      getParser().parse(globalBlock, source, errorHandling, builder);
    }
    return new ParseResult(tree, builder.build());
  }

  /**
   * Implementations must return an empty {@code GssParserCC} object. Whether
   * this object is actually new or not isn't important which allows pooling.
   */
  protected abstract GssParserCC getParser();

  protected static class ParseResult {
    private final CssTree cssTree;
    private final ImmutableList<GssParserException> handledErrors;

    private ParseResult(CssTree cssTree,
        ImmutableList<GssParserException> handledErrors) {
      this.cssTree = cssTree;
      this.handledErrors = handledErrors;
    }

    protected CssTree getCssTree() {
      return cssTree;
    }

    protected ImmutableList<GssParserException> getHandledErrors() {
      return handledErrors;
    }
  }
}
