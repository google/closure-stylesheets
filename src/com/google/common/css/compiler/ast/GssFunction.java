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

import java.util.List;

/**
 * Encapsulates a custom function callable from GSS stylesheet files.
 *
 */

public interface GssFunction {

  /**
   * Returns the number of parsed arguments that this function takes,
   * or {@code null} if the number of arguments may vary.
   */
  Integer getNumExpectedArguments();

  /**
   * Processes a list of function call arguments and returns a list of
   * CssNodes representing this call's output, which replace the input
   * nodes in the AST. Errors will be reported to the ErrorManager.
   */
  List<CssValueNode> getCallResultNodes(
      List<CssValueNode> args, ErrorManager errorManager)
      throws GssFunctionException;

  /**
   * Processes a list of strings as function arguments and returns a string
   * result. Errors are reported by throwing {@link GssFunctionException}.
   */
  String getCallResultString(List<String> args) throws GssFunctionException;
}
