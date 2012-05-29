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

import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.Property;
import com.google.common.css.compiler.ast.VisitController;

import java.util.Set;

/**
 * CSS Compiler pass that checks that all properties in the stylesheet are
 * either on the built-in recognized property list, or were whitelisted
 * explicitly.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class VerifyRecognizedProperties extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final Set<String> allowedUnrecognizedProperties;
  private final VisitController visitController;
  private final ErrorManager errorManager;

  public VerifyRecognizedProperties(Set<String> allowedUnrecognizedProperties,
      VisitController visitController, ErrorManager errorManager) {
    this.allowedUnrecognizedProperties = ImmutableSet.copyOf(
        allowedUnrecognizedProperties);
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  /**
   * Checks whether the {@code Property} of {@code declarationNode} is a
   * recognized property. If not, an error is reported.
   */
  @Override
  public boolean enterDeclaration(CssDeclarationNode declarationNode) {
    // TODO(bolinfest): Associate CSS version with a Property so that this check
    // can also verify the "compliance level." For example, it may want to
    // enforce that all properties are CSS 2.1 or earlier.
    CssPropertyNode propertyNode = declarationNode.getPropertyName();
    Property property = propertyNode.getProperty();
    String propertyName = property.getName();

    // TODO(bolinfest): Have separate options to specify whether the star hack
    // or underscore hack should be allowed. See:
    // http://en.wikipedia.org/wiki/CSS_filter#Star_hack
    // http://en.wikipedia.org/wiki/CSS_filter#Underscore_hack
    //
    // If the star or underscore hack is employed, consider the name without the
    // hack character in determining whether it is a "recognized property."
    if (propertyName.startsWith("*") || propertyName.startsWith("_")) {
      propertyName = propertyName.substring(1);
      property = Property.byName(propertyName);
    }

    if (!property.isRecognizedProperty() &&
        !allowedUnrecognizedProperties.contains(property.getName())) {
      reportError(String.format("%s is an unrecognized property",
          property.getName()), propertyNode);
    } else if (property.hasWarning()) {
      errorManager.reportWarning(new GssError(
          String.format(
              "WARNING for use of CSS property %s: %s\n",
              property.getName(), property.getWarning()),
          propertyNode.getSourceCodeLocation()));
    }
    return true;
  }

  private void reportError(String message, CssNode node) {
    errorManager.report(new GssError(message, node.getSourceCodeLocation()));
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
