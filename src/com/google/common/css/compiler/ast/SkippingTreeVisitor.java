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

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Any compiler pass which derives from this is able to skip
 * processing rules that contain specific property names.
 *
 * TODO(user): Should we move these functions to the DefaultVisitController?
 *
 */
public class SkippingTreeVisitor extends DefaultTreeVisitor {

  private boolean skipping = false;
  /**
   * The rules which contain any of these property names will be skipped,
   * if skipping is set to true.
   */
  Set<String> propertyNamesToSkip = ImmutableSet.of("display");
  // TODO(user): Possible more needed property names (which can trigger
  // hasLayout in IE): zoom, position, float

  /**
   * Constructor of a skipping tree visitor which sets the skipping property.
   * @param skip
   */
  public SkippingTreeVisitor(boolean skip) {
    skipping = skip;
  }

  /**
   * This method checks if the given ruleset is safe to be changed.
   * @return {@code false} if the ruleset is not safe to be modified (because
   * it contains any of the pre-defined property names when skipping is turned
   * on), true otherwise.
   */
  public boolean canModifyRuleset(CssRulesetNode ruleset) {
    // If skipping is on and the rule contains a property from the set : skip.
    if (skipping) {
      for (String propNameToSkip : propertyNamesToSkip) {
        for (CssNode child : ruleset.getDeclarations().childIterable()) {
          if (!(child instanceof CssDeclarationNode)) {
            continue;
          }
          CssDeclarationNode decl = (CssDeclarationNode) child;
          if (decl.getPropertyName().getPropertyName().equals(propNameToSkip)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public boolean isSkipping() {
    return skipping;
  }

}
