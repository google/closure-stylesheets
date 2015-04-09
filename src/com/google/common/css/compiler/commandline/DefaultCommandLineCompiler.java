/*
 * Copyright 2008 Google Inc.
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

package com.google.common.css.compiler.commandline;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Preconditions;
import com.google.common.css.AbstractCommandLineCompiler;
import com.google.common.css.ExitCodeHandler;
import com.google.common.css.JobDescription;
import com.google.common.css.JobDescription.OutputFormat;
import com.google.common.css.RecordingSubstitutionMap;
import com.google.common.css.compiler.ast.BasicErrorManager;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.CompactPrinter;
import com.google.common.css.compiler.passes.DefaultGssSourceMapGenerator;
import com.google.common.css.compiler.passes.GssSourceMapGenerator;
import com.google.common.css.compiler.passes.NullGssSourceMapGenerator;
import com.google.common.css.compiler.passes.PassRunner;
import com.google.common.css.compiler.passes.PrettyPrinter;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * {@link DefaultCommandLineCompiler} provides the CSS parser from command line interface to users.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class DefaultCommandLineCompiler extends AbstractCommandLineCompiler {

  /**
   * The compiler will limit the number of error messages it outputs to this
   * number.
   */
  protected static final int MAXIMUM_ERRORS_TO_OUTPUT = 100;

  private CssTree cssTree;
  private final ErrorManager errorManager;
  private final PassRunner passRunner;
  private final GssSourceMapGenerator gssSourceMapGenerator;

  /**
   * Constructs a {@code DefaultCommandLineCompiler}.
   *
   * @param job The inputs the compiler should process and the options to use.
   * @param errorManager The error manager to use for error reporting.
   */
  protected DefaultCommandLineCompiler(JobDescription job,
      ExitCodeHandler exitCodeHandler, ErrorManager errorManager) {
    super(job, exitCodeHandler);
    this.errorManager = errorManager;
    this.passRunner = new PassRunner(job, errorManager);
    this.gssSourceMapGenerator = createSourceMapGenerator(job);
  }

  private GssSourceMapGenerator createSourceMapGenerator(JobDescription job) {
    if (!job.createSourceMap) {
      return new NullGssSourceMapGenerator();
    }
    return new DefaultGssSourceMapGenerator(job.sourceMapLevel);
  }

  /**
   * Parses all the inputs, reports error messages and combines the parsed
   * inputs into one stylesheet.
   *
   * @return the resulting stylesheet in string format
   */
  public String compile() throws GssParserException {
    Preconditions.checkState(!compilerWasUsed);
    compilerWasUsed = true;

    // Reserving the input length might not be enough for pretty printed output,
    // but it will certainly save resizing the buffers for compressed output.
    // The length of the copyright notice is also included in the total length.
    int copyrightNoticeSize = job.copyrightNotice != null
        ? job.copyrightNotice.length() : 0;
    StringBuilder result = new StringBuilder(job.getAllInputsLength()
        + copyrightNoticeSize);
    if (job.copyrightNotice != null) {
      result.append(job.copyrightNotice);
    }

      GssParser parser = new GssParser(job.inputs);
      parseAndPrint(result, parser);

    return result.toString();
  }

  /**
   * Helper method for parsing and outputting the result.
   */
  private void parseAndPrint(StringBuilder result, GssParser parser)
      throws GssParserException {
    cssTree = parser.parse();
    if (job.outputFormat != OutputFormat.DEBUG) {
      passRunner.runPasses(cssTree);
    }

    if (job.outputFormat == OutputFormat.COMPRESSED) {
      CompactPrinter compactPrinterPass = new CompactPrinter(cssTree, gssSourceMapGenerator);
      compactPrinterPass.runPass();
      result.append(compactPrinterPass.getCompactPrintedString());
    } else {
      PrettyPrinter prettyPrinterPass = new PrettyPrinter(cssTree
          .getVisitController(),
          null /* use external buffer */,
          gssSourceMapGenerator);
      prettyPrinterPass
          .setPreserveComments(job.preserveComments)
          .runPass();
      result.append(prettyPrinterPass.getPrettyPrintedString());
    }
  }

  /**
   * Executes the job associated with this compiler and returns the compiled CSS
   * as a string. If {@code renameFile} is specified along with a
   * {@link RecordingSubstitutionMap}, then the renaming file will be written,
   * as well.
   */
  protected String execute(@Nullable File renameFile, @Nullable File sourcemapFile) {
    try {
      String compilerOutput = compile();

      // Print any errors or warnings.
      errorManager.generateReport();

      // If there were errors, fail.
      if (errorManager.hasErrors()) {
        exitCodeHandler.processExitCode(
            AbstractCommandLineCompiler.ERROR_MESSAGE_EXIT_CODE);
      }

      // Write the class substitution map to file, using same format as
      // VariableMap in jscomp.
      RecordingSubstitutionMap recordingSubstitutionMap = passRunner
          .getRecordingSubstitutionMap();
      if (recordingSubstitutionMap != null && renameFile != null) {
        PrintWriter renamingMapWriter = new PrintWriter(
            Files.newWriter(renameFile, UTF_8));
        Map<String, String> renamingMap = recordingSubstitutionMap
            .getMappings();
        writeRenamingMap(renamingMap, renamingMapWriter);
        renamingMapWriter.close();
      }

      if (job.createSourceMap) {
        PrintWriter sourceMapWriter = new PrintWriter(
            Files.newWriter(sourcemapFile, UTF_8));
        gssSourceMapGenerator.appendOutputTo(sourceMapWriter, sourcemapFile.getName());
        sourceMapWriter.close();
      }

      return compilerOutput;
    } catch (IOException e) {
      AbstractCommandLineCompiler.exitOnUnhandledException(e, exitCodeHandler);
    } catch (GssParserException e) {
      System.err.println("Compiler parsing error: " + e.getMessage());
      e.printStackTrace();
      exitCodeHandler.processExitCode(
          AbstractCommandLineCompiler.ERROR_MESSAGE_EXIT_CODE);
    } catch (RuntimeException e) {
      System.err.println("Compiler internal error: " + e.getMessage());
      e.printStackTrace();
      exitCodeHandler.processExitCode(
          AbstractCommandLineCompiler.INTERNAL_ERROR_EXIT_CODE);
    }

    // This line is unreachable because all paths through the above code block
    // result in calling System.exit().
    return null;
  }

  /**
   * Writes the mappings to the specified writer. By default, mappings are
   * written (one per line) as:
   * <pre>
   * key:value
   * </pre>
   * <p>Subclasses may override this method to provide alternate output formats.
   * Subclasses <em>must not</em> close the writer.
   */
  protected void writeRenamingMap(Map<String, String> renamingMap,
      PrintWriter renamingMapWriter) {
    job.outputRenamingMapFormat.writeRenamingMap(renamingMap,
        renamingMapWriter);
  }

  /**
   * An error message handler.
   */
  protected static final class CompilerErrorManager extends BasicErrorManager {
    private boolean warningsAsErrors = false;

    @Override
    public void print(String msg) {
      System.err.println(msg);
    }

    @Override
    public void reportWarning(GssError warning) {
      if (warningsAsErrors) {
        report(warning);
      } else {
        super.reportWarning(warning);
      }
    }

    public void setWarningsAsErrors(boolean state) {
      warningsAsErrors = state;
    }
  }
}
