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

import com.google.common.css.SubstitutionMap;
import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 *  Functional tests the {@link CssClassRenaming} compiler pass.
 *  
 * @author oana@google.com (Oana Florescu)
 */
public class CssClassRenamingFunctionalTest extends PassesTestBase {

  public void testCssClassRenaming1() {
    testTreeConstruction(linesToString(
        ".CSS_RULE_1 {",
        "  padding: 1px;",
        "}",
        ".CSS_RULE_2 {",
        "  border: 2px;",
        "}"),
        "[[.CSS_RULE_1_]{[padding:[1px];]}" +
        "[.CSS_RULE_2_]{[border:[2px];]}]");
  }

  public void testCssClassRenaming2() {
    testTreeConstruction(linesToString(
        ".CSS_RULE_1 .CSS_RULE_2 {",
        "  padding: 1px;",
        "}",
        ".CSS_RULE_2 {",
        "  border: 2px;",
        "}"),
        "[[.CSS_RULE_1_ .CSS_RULE_2_]{[padding:[1px];]}" +
        "[.CSS_RULE_2_]{[border:[2px];]}]");
  }

  public void testCssClassRenaming3() {
    testTreeConstruction(linesToString(
        "div.CSS_RULE_1 {",
        "  padding: 1px;",
        "}"),
        "[[div.CSS_RULE_1_]{[padding:[1px];]}]");
  }

  public void testCssClassRenaming4() {
    testTreeConstruction(linesToString(
        ".CSS_RULE_1#ID_ID {",
        "  padding: 1px;",
        "}"),
        "[[.CSS_RULE_1_#ID_ID^]{[padding:[1px];]}]");
  }

  @Override
  protected void runPass() {
    SubstitutionMap classMap = new SubstitutionMap() {
        /** {@inheritDoc} */
        @Override
        public String get(String key) {
          return key.startsWith("CSS_") ? key + '_' : key;
        }
    };
    SubstitutionMap idMap = new SubstitutionMap() {
      /** {@inheritDoc} */
      @Override
      public String get(String key) {
        return key.startsWith("ID_") ? key + '^' : key;
      }
  };
    CssClassRenaming pass = 
        new CssClassRenaming(tree.getMutatingVisitController(), classMap, idMap);
    pass.runPass();
  }
}
