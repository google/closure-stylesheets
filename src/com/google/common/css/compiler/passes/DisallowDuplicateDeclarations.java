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

package com.google.common.css.compiler.passes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.VisitController;

import java.util.Set;

/**
 * Compiler pass that reports an error if a ruleset has two of the same
 * declarations that are not marked as alternate. This must be run before
 * the SplitRulesetNodes pass.
 *
 * @author henrywong@google.com (Henry Wong)
 */
public class DisallowDuplicateDeclarations extends DefaultTreeVisitor
    implements CssCompilerPass {

  private static final String ERROR_STR =
      "Detected multiple identical, non-alternate declarations in the same "
      + "ruleset. If this is intentional please use the /* @alternate */ "
      + "annotation. ";

  private final VisitController visitController;
  private final ErrorManager errorManager;

  private final Set<String> propertyNames = Sets.newHashSet();

  public DisallowDuplicateDeclarations(VisitController visitController,
      ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterRuleset(CssRulesetNode node) {
    for (CssNode child : node.getDeclarations().childIterable()) {
      // CssPropertyNodes don't get their location set, so just use the
      // location of the containing parent.
      SourceCodeLocation location = node.getSourceCodeLocation();
      if (location == null && !node.getSelectors().isEmpty()) {
        // If the ruleset doesn't have a location set, then use location in the selector within.
        location = node.getSelectors().getChildAt(0).getSourceCodeLocation();
      }
      processDeclaration((CssDeclarationNode) child, location);
    }

    // Clear the map for future re-use
    propertyNames.clear();

    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

  /**
   * Tests a given declaration to see whether or not it has a property name
   * that we've seen before.
   */
  @VisibleForTesting
  void processDeclaration(CssDeclarationNode declaration,
      SourceCodeLocation location) {

    String propertyName = declaration.getPropertyName().getPropertyName();

    // The MarkRemovableRulesetNodes pass special cases these so there's no
    // reason to check them here.
    // TODO(henrywong): Move PROPERTIES_NOT_TO_BE_CHECKED somewhere else.
    if (MarkRemovableRulesetNodes.PROPERTIES_NOT_TO_BE_CHECKED
        .contains(propertyName)) {
      return;
    }

    // If the declaration is star-hacked then we make the star be part of
    // the property name to ensure that we do not consider hacked
    // declarations as overridden by the non-hacked ones.
    // TODO(henrywong): This is copied from MarkRemovableRulesetNodes. We
    // should refactor this logic probably.
    if (declaration.hasStarHack()) {
      propertyName = "*" + propertyName;
    }

    // Ignore rules w/ the @alternate annotation.
    if (PassUtil.hasAlternateAnnotation(declaration)) {
      return;
    }

    if (propertyNames.contains(propertyName)) {
      errorManager.report(new GssError(ERROR_STR + declaration, location));
    } else {
      propertyNames.add(propertyName);
    }
  }
}
