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
package com.google.common.css.compiler.commandline;

import com.google.common.css.ExitCodeHandler;
import com.google.common.css.JobDescription;
import com.google.common.css.JobDescriptionBuilder;
import com.google.common.css.SourceCode;
import com.google.common.css.SplittingSubstitutionMap;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

import junit.framework.TestCase;

public class ClosureCommandLineCompilerTest extends TestCase {

  private static final String TEST_SPLITTER = "-";

  static final ExitCodeHandler EXIT_CODE_HANDLER =
      new ExitCodeHandler() {
        @Override
        public void processExitCode(int exitCode) {
          fail("compiler exited with code: " + exitCode);
        }
      };

  public void testMixinPropagation() throws Exception {
    ErrorManager errorManager = new NewFunctionalTestBase.TestErrorManager(new String[0]);

    SourceCode def =
        new SourceCode(
            "def.gss", "@defmixin spriteUrl() {  background: url('sprite.png') no-repeat; }");
    SourceCode call = new SourceCode("call.gss", ".example { @mixin spriteUrl(); }");

    JobDescription job =
        new JobDescriptionBuilder()
            .addInput(def)
            .addInput(call)
            .setAllowDefPropagation(true)
            .getJobDescription();

    ClosureCommandLineCompiler compiler =
        new ClosureCommandLineCompiler(job, EXIT_CODE_HANDLER, errorManager);

    String output = compiler.execute(null /* renameFile */, null /* sourcemapFile */);

    assertEquals(".example{background:url('sprite.png') no-repeat}", output);
  }


  public void testAllowDefPropagationDefaultsToTrue() throws Exception {
    ClosureCommandLineCompiler.Flags flags =
        ClosureCommandLineCompiler.parseArgs(new String[] {"/dev/null"}, EXIT_CODE_HANDLER);
    JobDescription jobDescription = flags.createJobDescription();
    assertTrue(jobDescription.allowDefPropagation);
  }
  
  public void testAssignCustomizedSplitter() throws Exception {
    ClosureCommandLineCompiler.Flags flags =
        ClosureCommandLineCompiler.parseArgs(new String[] {"--allow-unrecognized-functions", 
            "--allow-unrecognized-properties", "--rename", "CLOSURE", 
            "--css-class-splitter", TEST_SPLITTER, "/dev/null"}, EXIT_CODE_HANDLER);
    flags.registerSplitterIfExist();
    assertEquals("Type of RenamingType should be CLOSURE", 
        flags.getRenamingType().name(), "CLOSURE");
    assertEquals("The passed splitter should be the same as it in test case.", 
        TEST_SPLITTER.charAt(0), 
        ((SplittingSubstitutionMap)(flags.getRenamingType().getCssSubstitutionMapProvider().get())).getSplitterChar());
  }
}
