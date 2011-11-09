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

import com.google.common.css.compiler.ast.FunctionalTestBase;

/**
 * Functional tests for {@link HasConditionalNodes}.
 *
 */
public class HasConditionalNodesFunctionalTest extends FunctionalTestBase {

  private boolean passResult;

  public void testSimpleTrue() {
    parseAndBuildTree(linesToString(
        "@if COND {",
        "  foo { top : expression('cond') }",
        "} @else {",
        "  foo { top : expression('!cond') }",
        "}"));
    runPass();
    assertEquals(true, passResult);
  }

  public void testComplexTrue() {
    parseAndBuildTree(linesToString(
        "@media print {",
        "  @if COND1 {",
        "    @if COND2 {",
        "      @def COLOR red;",
        "      foo { color: COLOR }",
        "    } @elseif COND3 {",
        "      foo { border: 2px }",
        "    }",
        "  } @elseif COND2 {",
        "    foo { top : expression('cond2') }",
        "  } @else {",
        "    foo { top : expression }",
        "  }",
        "}"));
    runPass();
    assertEquals(true, passResult);
  }

  public void testSimpleFalse() {
    parseAndBuildTree(linesToString(
        "@media print /* @noflip */{",
        "  .CSS_RULE_1, .CSS_RULE_2:hover a {",
        "     border: thickBorder(red, 2px);",
        "  }",
        "}",
        "@media tv {",
        "  .CSS_RULE_1, .CSS_RULE_2:hover a /* @noflip */{",
        "     border: thickBorder(green, 2px);",
        "  }",
        "}",
        ".CSS_RULE_3 { /* @noflip */top : expression('cond') }"
        ));
    runPass();
    assertEquals(false, passResult);
  }

  @Override
  protected void runPass() {
    HasConditionalNodes pass = new HasConditionalNodes(
        tree.getVisitController());
    pass.runPass();
    passResult = pass.hasConditionalNodes();
  }
}
