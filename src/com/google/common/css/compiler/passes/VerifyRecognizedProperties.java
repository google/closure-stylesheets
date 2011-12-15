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
 * CSS Compiler pass that checks whether certain features of CSS, as exercised
 * by the stylesheet, are permitted according to the compilation options.
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
    CssPropertyNode propertyNode = declarationNode.getPropertyName();
    Property property = propertyNode.getProperty();
    if (!property.isRecognizedProperty() &&
        !allowedUnrecognizedProperties.contains(property.getName())) {
      reportError(String.format("%s is an unrecognized property",
          property.getName()), propertyNode);
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
