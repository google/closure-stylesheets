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

import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link ValidatePropertyValues}.
 */
@RunWith(JUnit4.class)
public class ValidatePropertyValuesTest extends NewFunctionalTestBase {

  @Override
  protected void runPass() {
    ValidatePropertyValues pass =
        new ValidatePropertyValues(tree.getVisitController(), errorManager);
    pass.runPass();
  }

  @Test
  public void testUnicodeRangeLegalPatterns() throws Exception {
    parseAndRun("@font-face { unicode-range: U+010a;}");
    parseAndRun("@font-face { unicode-range: U+010a-0230;}");
    parseAndRun("@font-face { unicode-range: U+23??;}");
    parseAndRun("@font-face { unicode-range: U+23??, U+010a;}");
  }

  @Test
  public void testUnicodeRangeOutOfRangePatterns() throws Exception {
    parseAndRun("@font-face { unicode-range: U+110000;}",
        ValidatePropertyValues.UNICODE_ILLEGAL_CODEPOINT_ERROR);
    parseAndRun("@font-face { unicode-range: U+110000-00AA;}",
        ValidatePropertyValues.UNICODE_ILLEGAL_CODEPOINT_ERROR);
    parseAndRun("@font-face { unicode-range: U+0026-110000;}",
        ValidatePropertyValues.UNICODE_ILLEGAL_CODEPOINT_ERROR);
    parseAndRun("@font-face { unicode-range: U+1100??;}",
        ValidatePropertyValues.UNICODE_ILLEGAL_CODEPOINT_ERROR);
  }
}
