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

package com.google.common.css;

/**
 * {@link ExitCodeHandler} handles a request to exit with a specified exit code.
 * The default implementation, {@link DefaultExitCodeHandler} calls {@link
 * System#exit(int)}, but other implementations could throw an un-caught
 * Throwable or equivalent to terminate execution of the compiler. In <em>no
 * case</em> should an implementation return normally from {@link
 * #processExitCode}.
 * 
 * @author bolinfest@google.com (Michael Bolin)
 */
public interface ExitCodeHandler {

  /**
   * Process the request to exit with the specified exit code.
   * Implementations of this method <em>must never</em> return normally.
   */
  public void processExitCode(int exitCode);

}
