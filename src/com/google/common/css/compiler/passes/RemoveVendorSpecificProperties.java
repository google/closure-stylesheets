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

import com.google.common.base.Preconditions;
import com.google.common.css.Vendor;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.ast.Property;

import javax.annotation.Nonnull;

/**
 * A {@link CssCompilerPass} that removes all vendor-specific properties, except
 * for one {@link Vendor} that is specified in the constructor. This is
 * designed to facilitate creating a vendor-specific stylesheet. For example,
 * suppose you have the following CSS:
 * <pre>
 * .button {
 *   border-radius: 2px;
 *   -moz-border-radius: 2px;
 *   -webkit-border-radius: 2px;
 * }
 * </pre>
 * <p>If {@link Vendor#WEBKIT} were specified as the "vendor to keep" when running
 * this pass, then the resulting CSS would be:
 * <pre>
 * .button {
 *   border-radius: 2px;
 *   -webkit-border-radius: 2px;
 * }
 * </pre>
 * {@code border-radius} would not be removed because it is not a
 * vendor-specific property, and {@code -webkit-border-radius} would not be
 * removed because {@link Vendor#WEBKIT} was specified as the "vendor to keep."
 * Only {@code -moz-border-radius} is removed because it is vendor-specific, but
 * it is not the whitelisted vendor. If there were properties prefixed with
 * "-o-" for Opera or "-ms-" for Microsoft, then those would have been removed,
 * as well.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class RemoveVendorSpecificProperties extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final Vendor vendorToKeep;
  private final MutatingVisitController visitController;

  /**
   * @param vendorToKeep determines the vendor for whose vendor-specific
   *     properties will not be stripped. This parameter may not be null: if
   *     there is no such venor, then this pass should not be used.
   * @param visitController to facilitate traversing the AST
   */
  public RemoveVendorSpecificProperties(@Nonnull Vendor vendorToKeep,
      MutatingVisitController visitController) {
    Preconditions.checkNotNull(vendorToKeep);
    this.vendorToKeep = vendorToKeep;
    this.visitController = visitController;
  }

  /**
   * Checks whether the {@code Property} of {@code declarationNode} is a
   * vendor-specific property that does not match {@code vendorToKeep}. If so,
   * then the declaration is removed.
   */
  @Override
  public boolean enterDeclaration(CssDeclarationNode declarationNode) {
    CssPropertyNode propertyNode = declarationNode.getPropertyName();
    Property property = propertyNode.getProperty();
    Vendor vendor = property.getVendor();
    if (vendor != null && !vendor.equals(vendorToKeep)) {
      visitController.removeCurrentNode();
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
