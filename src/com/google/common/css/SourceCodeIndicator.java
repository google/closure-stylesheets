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

import javax.annotation.Nullable;
import com.google.common.base.Preconditions;

/**
 * Represents a line of source code, together with an associated "indicator"
 * line. If the indicator line is displayed immediately after (or before) the
 * source code line it should have the effect of visually indicating a portion
 * of the source code line. Some examples:
 * <pre class="code>
 * int i = 0.1;
 * ^^^     ^^^
 * </pre>
 * <pre class="code>
 *     vvvvvvv
 * int i = 0.1;
 * </pre>
 * <p>Note that the indicator line is not required to indicate continuous
 * portions of the code line. However, in our application it is only going to be
 * used for continuous portions.
 *
 * <p>Support for special characters (such as tab, backspace and end-of-line) in
 * the code line is the responsibility of the class that displays the two lines.
 * The indicator in this class assumes a one-to-one mapping of code characters
 * to display characters and to indicator line characters. The meaning of "line"
 * is left to the users of this class; it does not enforce that its "lines"
 * contain no newline characters. In our application we should make sure we
 * don't pass newline characters to this class.
 *
 * <p>The indicator line is not required to have the same length as the code
 * line. This means that trailing whitespace can be absent. However, uses such
 * as:
 * <pre class="code>
 * int i = 0;
 * ____#####_
 * </pre>
 * are considered valid. Note that the indicator line contains no whitespace and
 * that both lines are the same size. As long as the visual indication is
 * achieved, the indicator line is unconstrained.
 *
 * <p>The indicator line might be absent. If that is the case,
 * {@link #getIndicatorLine()} returns {@code null}.
 *
 */
public class SourceCodeIndicator {
  private final String codeLine;
  private final String indicatorLine;

  /**
   * Constructs a SourceCodeIndicator.
   *
   * @param codeLine the line of source code to be displayed
   * @param indicatorLine The line that indicates a portion of the source code
   *     line. Can be {@code null} if this information is not available or not
   *     required for display.
   */
  SourceCodeIndicator(String codeLine, @Nullable String indicatorLine) {
    Preconditions.checkNotNull(codeLine);
    this.codeLine = codeLine;
    this.indicatorLine = indicatorLine;
  }

  /**
   * @return the code line
   */
  public String getCodeLine() {
    return codeLine;
  }

  /**
   * @return the indicator line
   */
  public String getIndicatorLine() {
    return indicatorLine;
  }
}
