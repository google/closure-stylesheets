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

package com.google.common.css.compiler.passes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssUnicodeRangeNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.VisitController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * A compiler pass that validates values of properties.
 *
 * <p>This pass exists to validate values that are not fully covered by the grammar definition.
 */
public class ValidatePropertyValues extends DefaultTreeVisitor implements CssCompilerPass {

  private static final String UNICODE_RANGE_ERROR = "Invalid expression for unicode range. "
      + "Expected single codepoint (U+25), range (U+0015-00FF), or wildcard range (U+3??)";
  @VisibleForTesting
  static final String UNICODE_ILLEGAL_CODEPOINT_ERROR = "Unicode code point must be within "
      + "0-10FFFF";

  private static final String CODE_POINT_PATTERN = "([\\da-zA-Z]+)";
  private static final Pattern UNICODE_RANGE_PATTERN =
      Pattern.compile(String.format("U\\+%s(?:-%s)?", CODE_POINT_PATTERN, CODE_POINT_PATTERN));
  private static final Pattern UNICODE_WILDCARD_PATTERN =
      Pattern.compile(String.format("U\\+%s(\\?+)", CODE_POINT_PATTERN));

  private final ErrorManager errorManager;

  private final VisitController visitController;

  public ValidatePropertyValues(VisitController visitController, ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterValueNode(CssValueNode value) {
    if (value instanceof CssUnicodeRangeNode) {
      validateUnicodeRangeValue((CssUnicodeRangeNode) value);
    }
    return true;
  }

  /**
   * Verify that a unicode range value is legal.
   *
   *  <p>See <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/unicode-range"></a>.
   */
  private void validateUnicodeRangeValue(CssUnicodeRangeNode value) {
    Matcher rangeMatcher = UNICODE_RANGE_PATTERN.matcher(value.getValue());
    Matcher wildcardMatcher = UNICODE_WILDCARD_PATTERN.matcher(value.getValue());
    if (!rangeMatcher.matches() && !wildcardMatcher.matches()) {
      reportError(UNICODE_RANGE_ERROR, value);
      return;
    }

    if (rangeMatcher.matches()) {
      for (int i = 1; i <= rangeMatcher.groupCount(); ++i) {
        validateSingleCodePoint(rangeMatcher.group(i), value);
      }
    } else {  // wildcardMatcher.matches() == true.
      // The second group contains only question marks (?). We concatenate it as zeros to the number
      // prefix for purpose of validation.
      String codePoint = wildcardMatcher.group(1) + wildcardMatcher.group(2).replaceAll("\\?", "0");
      validateSingleCodePoint(codePoint, value);
    }
  }

  /**
   * Verifies that a code point is within the permissible range (U+0-10FFFF).
   */
  private void validateSingleCodePoint(@Nullable String codePoint, CssUnicodeRangeNode value) {
    if (codePoint == null) {
      return;
    }
    long asLong = Long.parseLong(codePoint, 16);
    if (asLong > 0x10FFFF) {
      reportError(UNICODE_ILLEGAL_CODEPOINT_ERROR, value);
    }
  }

  private void reportError(String message, CssNode node) {
    errorManager.report(new GssError(message, node.getSourceCodeLocation()));
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
