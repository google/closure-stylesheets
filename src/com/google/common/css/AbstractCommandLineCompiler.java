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

package com.google.common.css;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * An abstract class that is designed to be extended by classes that provide a
 * command line interface to the CSS parser.
 *
 */
public class AbstractCommandLineCompiler {

  /**
   * Exit code for success (normal operation of the compiler, possibly with
   * warnings and informational messages, but with no errors whatsoever).
   */
  public static final int SUCCESS_EXIT_CODE = 0;

  /**
   * Exit code for compilation errors. The compiler processed the input
   * normally, but the input was wrong in some way.
   */
  public static final int ERROR_MESSAGE_EXIT_CODE = 1;

  /**
   * Exit code for unhandled exceptions (such as IOException).
   */
  // Ideally there should be none, we should catch potential exceptions and
  // convert them to our own error messages. However, when it makes code less
  // complicated, we accept unhandled exceptions.
  public static final int UNHANDLED_EXCEPTION_EXIT_CODE = 2;

  /**
   * Exit code for abnormal compiler behavior, such as violated invariants,
   * infinite loops and the likes.
   */
  // Abnormal compiler behavior is indicated by RuntimeExceptions (assertion
  // failures, stack overflows, etc.). We can potentially have guards for
  // sensitive locations (potentially infinite loops) and throw an exception
  // if something is wrong.
  public static final int INTERNAL_ERROR_EXIT_CODE = 3;

  /** The job to process: the inputs and the options. */
  protected final JobDescription job;

  /** Whether the compiler was called. We don't allow calling it twice. */
  protected boolean compilerWasUsed = false;

  /** Used to manage calls to System.exit(). */
  protected final ExitCodeHandler exitCodeHandler;

  /**
   * Constructs an {@code AbstractCommandLineCompiler}.
   *
   * @param job the inputs the compiler should process and the options to use
   */
  @VisibleForTesting
  public AbstractCommandLineCompiler(JobDescription job,
      ExitCodeHandler exitCodeHandler) {
    Preconditions.checkNotNull(job);
    Preconditions.checkNotNull(exitCodeHandler);
    this.job = job;
    this.exitCodeHandler = exitCodeHandler;
  }

  /**
   * Prints a message announcing an unhandled exception and exits.
   */
  protected static void exitOnUnhandledException(Exception e,
      ExitCodeHandler exitCodeHandler) {
    System.err.println(
        "The compiler encountered an unhadled error condition. " + e);
    e.printStackTrace();
    exitCodeHandler.processExitCode(UNHANDLED_EXCEPTION_EXIT_CODE);
  }
}
