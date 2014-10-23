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
package com.google.common.css.compiler.ast;

import com.google.common.collect.ImmutableList;
import com.google.common.css.SourceCode;

import java.util.List;

/**
 * Provides thread local GssParserCC instances.
 */
public class PerThreadGssParser extends AbstractGssParser {

  private static final ThreadLocal<GssParserCC> THREAD_LOCAL_PARSERS =
      new ThreadLocal<GssParserCC>() {
        @Override
        protected GssParserCC initialValue() {
          return new GssParserCC(EMPTY_CHAR_STREAM);
        }
      };
  private ImmutableList<GssParserException> handledErrors = ImmutableList.of();

  public CssTree parse(SourceCode source) throws GssParserException {
    return parse(source, false);
  }

  public CssTree parse(List<SourceCode> sources) throws GssParserException {
    return parse(sources, false);
  }

  public CssTree parse(SourceCode source, boolean errorHandling)
      throws GssParserException {
    return parse(ImmutableList.of(source), errorHandling);
  }

  public CssTree parse(List<SourceCode> sources, boolean errorHandling)
      throws GssParserException {
    ParseResult result = parseInternal(sources, errorHandling);
    this.handledErrors = result.getHandledErrors();
    return result.getCssTree();
  }

  /**
   * Returns errors from previous call to parse().
   */
  public List<GssParserException> getHandledErrors() {
    return handledErrors;
  }

  @Override
  protected GssParserCC getParser() {
    return THREAD_LOCAL_PARSERS.get();
  }
}
