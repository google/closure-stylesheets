/*
 * Copyright 2011 Google Inc.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.css.AbstractCommandLineCompiler;
import com.google.common.css.DefaultExitCodeHandler;
import com.google.common.css.ExitCodeHandler;
import com.google.common.css.GssFunctionMapProvider;
import com.google.common.css.JobDescription;
import com.google.common.css.JobDescription.InputOrientation;
import com.google.common.css.JobDescription.OutputOrientation;
import com.google.common.css.JobDescription.SourceMapDetailLevel;
import com.google.common.css.JobDescriptionBuilder;
import com.google.common.css.OutputRenamingMapFormat;
import com.google.common.css.SourceCode;
import com.google.common.css.Vendor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * {@link ClosureCommandLineCompiler} is the command-line compiler for Closure
 * Stylesheets.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class ClosureCommandLineCompiler extends DefaultCommandLineCompiler {

  protected ClosureCommandLineCompiler(JobDescription job,
      ExitCodeHandler exitCodeHandler, ErrorManager errorManager) {
    super(job, exitCodeHandler, errorManager);
  }

  @VisibleForTesting
  static class Flags {
    private static final String USAGE_PREAMBLE =
        Joiner.on("\n").join(new String[] {
        "Closure Stylesheets",
        "",
        "One or more CSS/GSS files must be supplied as inputs.",
        "Output will be written to standard out unless --output_file is "
            + "specified.",
        "",
        "command line options:",
        ""
        });

    @Option(name = "--output-file", aliases = {"-o"},
        usage = "The output CSS filename. If empty, standard output will be"
        + " used. The output is always UTF-8 encoded.")
    private String outputFile = null;

    @Option(name = "--input-orientation", usage =
        "This specifies the display orientation the input files were written"
        + " for. You can choose between: LTR, RTL. LTR is the default and means"
        + " that the input style sheets were designed for use with left to"
        + " right display User Agents. RTL sheets are designed for use with"
        + " right to left UAs. Currently, all input files must have the same"
        + " orientation, as there is no way to specify the orientation on a"
        + " per-file or per-library basis.")
    private InputOrientation inputOrientation = InputOrientation.LTR;

    @Option(name = "--output-orientation", usage =
        "Specify this option to perform automatic right to left conversion of"
        + " the input. You can choose between: LTR, RTL, NOCHANGE. NOCHANGE"
        + " means the input will not be changed in any way with respect to"
        + " direction issues. LTR outputs a sheet suitable for left to right"
        + " display and RTL outputs a sheet suitable for right to left"
        + " display. If the input orientation is different than the requested"
        + " output orientation, 'left' and 'right' values in direction"
        + " sensitive style rules are flipped. If the input already has the"
        + " desired orientation, this option effectively does nothing except"
        + " for defining GSS_LTR and GSS_RTL, respectively. The input is LTR"
        + " by default and can be changed with the input_orientation flag.")
    private OutputOrientation outputOrientation = OutputOrientation.LTR;

    @Option(name = "--pretty-print",
        usage = "Whether to format the output with newlines and indents so that"
        + " it is more readable.")
    private boolean prettyPrint = false;

    @Option(name = "--output-renaming-map", usage = "The output from"
        + " the CSS class renaming. Provides a map of class names to what they"
        + " were renammed to.")
    private String renameFile = null;

    @Option(name = "--output-renaming-map-format", usage = "How to format the"
        + " output from the CSS class renaming.")
    private OutputRenamingMapFormat outputRenamingMapFormat =
        OutputRenamingMapFormat.JSON;

    @Option(name = "--input-renaming-map", usage = "The input filename for"
        + " the CSS class renaming. The file must provide a map of class names"
        + " that will be used for renaming. If a class name is not found in"
        + " file, a new name will be generated.")
    private String inputRenamingMapFileName = null;

    @Option(name = "--input-renaming-map-format", usage = "How the input"
        + " renaiming map file is formatted. Default value is value used in"
        + " --output-renaming-map-format.")
    private OutputRenamingMapFormat inputRenamingMapFormat = null;

    @Option(name = "--output-source-map", usage = "The source map output."
        + " Provides a mapping from the generated output to their original"
        + " source code location.")
    private String sourceMapFile = "";

    @Option(name = "--source_map_output_level", usage = "The level to generate "
        + "source maps. You could choose between DEFAULT, which will generate "
        + "source map only for selectors, blocks, rules, variables and symbol "
        + "mappings, and ALL, which outputs mappings for all elements.")
    private SourceMapDetailLevel sourceMapLevel = SourceMapDetailLevel.DEFAULT;

    @Option(name = "--copyright-notice",
        usage = "Copyright notice to prepend to the output")
    private String copyrightNotice = null;

    @Option(name = "--define", usage = "Specifies the name of a true condition."
        + " The condition name can be used in @if boolean expressions."
        + " The conditions are ignored if GSS extensions are not enabled.")
    private List<String> trueConditions = Lists.newArrayList();

    @Option(name = "--allow-def-propagation", usage = "Allows @defs and @mixins"
        + " from one file to propagate to other files.")
    private boolean allowDefPropagation = true;

    @Option(name = "--allow-unrecognized-functions", usage =
        "Allow unrecognized functions.")
    private boolean allowUnrecognizedFunctions = false;

    @Option(name = "--allow-duplicate-declarations", usage =
        "Allow duplicate declarations.")
    private boolean allowDuplicateDeclarations = false;

    @Option(name = "--allowed-non-standard-function", usage =
        "Specify a non-standard function to whitelist, like alpha()")
    private List<String> allowedNonStandardFunctions = Lists.newArrayList();

    @Option(name = "--allowed-unrecognized-property", usage =
        "Specify an unrecognized property to whitelist")
    private List<String> allowedUnrecognizedProperties = Lists.newArrayList();

    @Option(name = "--allow-unrecognized-properties", usage =
        "Allow unrecognized properties.")
    private boolean allowUnrecognizedProperties = false;

    @Option(name = "--vendor", usage =
        "Creates browser-vendor-specific output by stripping all proprietary "
        + "browser-vendor properties from the output except for those "
        + "associated with this vendor.")
    private Vendor vendor = null;

    @Option(name = "--excluded-classes-from-renaming", usage =
        "Pass the compiler a list of CSS class names that shoudn't be renamed.")
    private List<String> excludedClassesFromRenaming = Lists.newArrayList();

    // For enum values, args4j automatically lists all possible values when it
    // prints the usage information for the flag, so including them in the usage
    // message defined here would be redundant.
    @Option(name = "--rename",
        usage = "How CSS classes should be renamed. Defaults to NONE.")
    private RenamingType renamingType = RenamingType.NONE;

    @Option(name = "--gss-function-map-provider",
        usage = "The fully qualified class name of a map provider of custom GSS"
        + " functions to resolve.")
    private String gssFunctionMapProviderClassName =
        "com.google.common.css.compiler.gssfunctions."
        + "DefaultGssFunctionMapProvider";

    @Option(name = "--css-renaming-prefix",
        usage = "Add a prefix to all renamed css class names.")
    private String cssRenamingPrefix = "";

    @Option(name = "--preserve-comments", usage =
        "Preserve comments from sources into pretty printed output css.")
    private boolean preserveComments = false;

    @Option(name = "--const",
        usage = "Specify integer constants to be used in for loops. Invoke for each const, e.g.: "
        + "--const=VAR1=VALUE1 --const=VAR2=VALUE2")
    private Map<String, String> compileConstants = new HashMap<>();

    @Option(name = "--preserve-important-comments", usage = "Preserve important comments from "
        + "sources into minified output css. Important comments are marked with "
        + "/*! */, @license, or @preserve.")
    private boolean preserveImportantComments = false;

    /**
     * All remaining arguments are considered input CSS files.
     */
    @Argument
    private List<String> arguments = Lists.newArrayList();

    /**
     * @return a new {@link JobDescription} using this class's flag values
     */
    @VisibleForTesting
    JobDescription createJobDescription() {
      JobDescriptionBuilder builder = new JobDescriptionBuilder();
      builder.setInputOrientation(inputOrientation);
      builder.setOutputOrientation(outputOrientation);
      builder.setOutputFormat(prettyPrint
          ? JobDescription.OutputFormat.PRETTY_PRINTED
          : JobDescription.OutputFormat.COMPRESSED);
      builder.setCopyrightNotice(copyrightNotice);
      builder.setTrueConditionNames(trueConditions);
      builder.setAllowDefPropagation(allowDefPropagation);
      builder.setAllowUnrecognizedFunctions(allowUnrecognizedFunctions);
      builder.setAllowDuplicateDeclarations(allowDuplicateDeclarations);
      builder.setAllowedNonStandardFunctions(allowedNonStandardFunctions);
      builder.setAllowedUnrecognizedProperties(allowedUnrecognizedProperties);
      builder.setAllowUnrecognizedProperties(allowUnrecognizedProperties);
      builder.setVendor(vendor);
      builder.setAllowKeyframes(true);
      builder.setAllowWebkitKeyframes(true);
      builder.setProcessDependencies(true);
      builder.setExcludedClassesFromRenaming(excludedClassesFromRenaming);
      builder.setSimplifyCss(true);
      builder.setEliminateDeadStyles(true);
      builder.setCssSubstitutionMapProvider(renamingType
          .getCssSubstitutionMapProvider());
      builder.setCssRenamingPrefix(cssRenamingPrefix);
      builder.setPreserveComments(preserveComments);
      builder.setOutputRenamingMapFormat(outputRenamingMapFormat);
      builder.setCompileConstants(parseCompileConstants(compileConstants));
      builder.setPreserveImportantComments(preserveImportantComments);

      GssFunctionMapProvider gssFunctionMapProvider =
          getGssFunctionMapProviderForName(gssFunctionMapProviderClassName);
      builder.setGssFunctionMapProvider(gssFunctionMapProvider);
      builder.setSourceMapLevel(sourceMapLevel);
      builder.setCreateSourceMap(!Strings.isNullOrEmpty(sourceMapFile));

      if (inputRenamingMapFileName != null) {
        if (inputRenamingMapFormat == null) {
          inputRenamingMapFormat = outputRenamingMapFormat;
        }

        try {
          File inputRenamingMapFile = new File(inputRenamingMapFileName);
          Reader inputRenamingMapReader = Files.newReader(inputRenamingMapFile, UTF_8);
          ImmutableMap<String, String> inputRenamingMap =
              inputRenamingMapFormat.readRenamingMap(inputRenamingMapReader);
          builder.setInputRenamingMap(inputRenamingMap);
        } catch (IOException e) {
          throw new RuntimeException(String.format(
              "Input renaming map file %s can not be read", inputRenamingMapFileName), e);
        }
      }

      for (String fileName : arguments) {
        File file = new File(fileName);
        if (!file.exists()) {
          throw new RuntimeException(String.format(
              "Input file %s does not exist", fileName));
        }

        String fileContents;
        try {
          fileContents = Files.asCharSource(file, UTF_8).read();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        builder.addInput(new SourceCode(fileName, fileContents));
      }
      return builder.getJobDescription();
    }

    private OutputInfo createOutputInfo() {
      return new OutputInfo(
          (outputFile == null) ? null : new File(outputFile),
          (renameFile == null) ? null : new File(renameFile),
          (sourceMapFile == null) ? null : new File(sourceMapFile));
    }

    /**
     * Parses the values in the compile constants to integers.
     */
    private Map<String, Integer> parseCompileConstants(
        Map<String, String> compileConstants) {
      Map<String, Integer> parsedConstants = new HashMap<>(compileConstants.size());
      for (Map.Entry<String, String> entry : compileConstants.entrySet()) {
        parsedConstants.put(entry.getKey(), Integer.parseInt(entry.getValue()));
      }
      return parsedConstants;
    }
  }

  /**
   * @param gssFunctionMapProviderClassName such as
   *     "com.google.common.css.compiler.gssfunctions.DefaultGssFunctionMapProvider"
   * @return a new instance of the {@link GssFunctionMapProvider} that
   *     corresponds to the specified class name, or a new instance of
   *     {@link com.google.common.css.compiler.gssfunctions.DefaultGssFunctionMapProvider}
   *     if the class name is {@code null}.
   */
  private static GssFunctionMapProvider getGssFunctionMapProviderForName(
      String gssFunctionMapProviderClassName) {
    // Verify that a class with the given name exists.
    Class<?> clazz;
    try {
      clazz = Class.forName(gssFunctionMapProviderClassName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(String.format(
          "Class does not exist: %s", gssFunctionMapProviderClassName), e);
    }

    // The class must implement GssFunctionMapProvider.
    if (!GssFunctionMapProvider.class.isAssignableFrom(clazz)) {
      throw new RuntimeException(String.format(
          "%s does not implement GssFunctionMapProvider",
          gssFunctionMapProviderClassName));
    }

    // Create the GssFunctionMapProvider using reflection.
    try {
      return (GssFunctionMapProvider) clazz.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static class OutputInfo {
    @Nullable public final File outputFile;
    @Nullable public final File renameFile;
    @Nullable public final File sourceMapFile;

    private OutputInfo(File outputFile, File renameFile, File sourceMapFile) {
      this.outputFile = outputFile;
      this.renameFile = renameFile;
      this.sourceMapFile = sourceMapFile;
    }
  }

  private static void executeJob(
      JobDescription job, ExitCodeHandler exitCodeHandler, OutputInfo outputInfo) {
    CompilerErrorManager errorManager = new CompilerErrorManager();

    ClosureCommandLineCompiler compiler =
        new ClosureCommandLineCompiler(job, exitCodeHandler, errorManager);

    String compilerOutput = compiler.execute(outputInfo.renameFile, outputInfo.sourceMapFile);

    if (outputInfo.outputFile == null) {
      System.out.print(compilerOutput);
    } else {
      try {
        Files.asCharSink(outputInfo.outputFile, UTF_8).write(compilerOutput);
      } catch (IOException e) {
        AbstractCommandLineCompiler.exitOnUnhandledException(e, exitCodeHandler);
      }
    }
  }

  /**
   * Processes the specified args to construct a corresponding
   * {@link Flags}. If the args are invalid, prints an appropriate error
   * message, invokes
   * {@link ExitCodeHandler#processExitCode(int)}, and returns null.
   */
  @VisibleForTesting
  static @Nullable Flags parseArgs(String[] args,
      ExitCodeHandler exitCodeHandler) {
    Flags flags = new Flags();
    CmdLineParser argsParser = new CmdLineParser(flags) {
      @Override
      public void printUsage(OutputStream out) {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        writer.write(Flags.USAGE_PREAMBLE);

        // Because super.printUsage() creates its own PrintWriter to wrap the
        // OutputStream, we call flush() on our PrinterWriter first to make sure
        // that everything from this PrintWriter is written to the OutputStream
        // before any usage information.
        writer.flush();
        super.printUsage(out);
      }
    };

    try {
      argsParser.parseArgument(args);
    } catch (CmdLineException e) {
      argsParser.printUsage(System.err);
      exitCodeHandler.processExitCode(ERROR_MESSAGE_EXIT_CODE);
      return null;
    }

    if (flags.arguments.isEmpty()) {
      System.err.println("\nERROR: No input files specified.\n");
      argsParser.printUsage(System.err);
      exitCodeHandler.processExitCode(
          AbstractCommandLineCompiler.ERROR_MESSAGE_EXIT_CODE);
      return null;
    } else {
      return flags;
    }
  }

  public static void main(String[] args) {
    ExitCodeHandler exitCodeHandler = new DefaultExitCodeHandler();
    Flags flags = parseArgs(args, exitCodeHandler);
    if (flags == null) {
      return;
    }

    JobDescription job = flags.createJobDescription();
    OutputInfo info = flags.createOutputInfo();
    executeJob(job, exitCodeHandler, info);
  }
}
