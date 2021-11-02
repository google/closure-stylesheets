/*
 * Copyright 2016 Google Inc.
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

import com.google.common.css.JobDescription.SourceMapDetailLevel;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase.TestErrorManager;
import com.google.common.css.compiler.passes.CompactPrinter;
import com.google.common.css.compiler.passes.CreateStandardAtRuleNodes;
import com.google.common.css.compiler.passes.DefaultGssSourceMapGenerator;
import com.google.common.css.compiler.passes.FixupFontDeclarations;
import com.google.common.css.compiler.passes.FixupFontDeclarations.InputMode;
import com.google.common.css.compiler.passes.GssSourceMapGenerator;
import com.google.common.io.Files;
import com.google.debugging.sourcemap.SourceMapConsumerFactory;
import com.google.debugging.sourcemap.SourceMapParseException;
import com.google.debugging.sourcemap.SourceMapping;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Simple framework for creating a parser for testing. */
public class TestParser {

  private CssTree tree;
  private List<SourceCode> sources = new ArrayList<>();
  private TestErrorManager errorManager = new TestErrorManager(new String[0]);
  private GssSourceMapGenerator generator =
      new DefaultGssSourceMapGenerator(SourceMapDetailLevel.ALL, true);
  private String output = null;
  private SourceMapping sourceMap = null;
  private String sourceMapString = null;

  public void addSource(String filename, String code) {
    sources.add(new SourceCode(filename, code));
  }

  /**
   * Parses GSS style sheets and returns one tree containing everything.
   *
   * @return the CSS tree created by the parser
   */
  public CssTree parse() throws GssParserException {
    tree = new GssParser(sources).parse();
    runPasses(tree);
    return tree;
  }

  protected void runPasses(CssTree tree) {
    new CreateStandardAtRuleNodes(tree.getMutatingVisitController(), errorManager).runPass();
    new FixupFontDeclarations(InputMode.CSS, errorManager, tree).runPass();
  }

  public String getOutput() {
    if (output == null) {
      CompactPrinter compactPrinter = new CompactPrinter(tree, generator);
      compactPrinter.runPass();
      output = compactPrinter.getCompactPrintedString();
    }
    return output;
  }

  public SourceMapping getSourceMap() throws IOException, SourceMapParseException {
    if (sourceMap == null) {
      sourceMap = SourceMapConsumerFactory.parse(getSourceMapString());
    }
    return sourceMap;
  }

  private String getSourceMapString() throws IOException {
    if (sourceMapString == null) {
      StringBuffer sourceMapBuffer = new StringBuffer();
      generator.appendOutputTo(sourceMapBuffer, "test");
      sourceMapString = sourceMapBuffer.toString();
    }
    return sourceMapString;
  }

  public void showSourceMap() throws IOException, InterruptedException {
    /* NOTE(flan): This method is used for debugging sourcemaps with
     * https://sokra.github.io/source-map-visualization/, the best (only?) source map visualizer
     * that I've found. You need to install https://www.npmjs.com/package/source-map-visualize to
     * in order to send the source map to the webapp. If you are concerned about leaking data, you
     * should *not* use this method. The meat is commented out to prevent mistakes.
     */
    File tmpDir = Files.createTempDir();
    File sourcemapFile = new File(tmpDir, "sourcemap");
    File compiledFile = new File(tmpDir, "compiled.css");

    Files.asCharSink(sourcemapFile, StandardCharsets.UTF_8).write(getSourceMapString());
    Files.asCharSink(compiledFile, StandardCharsets.UTF_8).write(getOutput());
    for (SourceCode source : sources) {
      Files
          .asCharSink(new File(tmpDir, source.getFileName()), StandardCharsets.UTF_8)
          .write(source.getFileContents());
    }
    Process process =
        new ProcessBuilder() /*
            .directory(tmpDir)
            .command(
                "/usr/local/bin/source-map-visualize",
                "compiled.css",
                "sourcemap") */
            .start();
    process.waitFor();
    assertThat(process.exitValue()).named("visualization exit code").isEqualTo(0);
  }
}
