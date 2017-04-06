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

package com.google.common.css.compiler.passes;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;

/**
 * Unit tests for {@link CreateVendorPrefixedKeyframes}.
 */
public class CreateVendorPrefixedKeyframesTest extends NewFunctionalTestBase {
  private String compactPrintedResult;

  @Override
  protected void runPass() {
    CreateVendorPrefixedKeyframes pass = new CreateVendorPrefixedKeyframes(
        tree.getMutatingVisitController(), errorManager);
    pass.runPass();
    CompactPrinter compactPrinterPass = new CompactPrinter(tree);
    compactPrinterPass.runPass();
    compactPrintedResult = compactPrinterPass.getCompactPrintedString();
  }

  public void testCreateWebkitKeyframes() throws Exception {
    parseAndRun(
        "/* @gen-webkit-keyframes */"
        + "@keyframes A{"
        + "0%{top:0}"
        + "100%{top:1%}"
        + "}");
    assertThat(compactPrintedResult)
        .isEqualTo(
            "@keyframes A{"
                + "0%{top:0}"
                + "100%{top:1%}"
                + "}"
                + "@-webkit-keyframes A{"
                + "0%{top:0}"
                + "100%{top:1%}"
                + "}");
  }

  public void testWithoutComment() throws Exception {
    parseAndRun(
        "@keyframes A{"
        + "0%{top:0}"
        + "100%{top:1%}"
        + "}");
    assertThat(compactPrintedResult)
        .isEqualTo("@keyframes A{" + "0%{top:0}" + "100%{top:1%}" + "}");
  }
}
