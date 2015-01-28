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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;

/**
 * A compiler pass that automatically detects certain properties that need additional
 * browser specific property declarations, and adds them.
 * The properties to be matched for expansion are provided by the BrowserPrefixGenerator.
 *
 * <p>This mechanism is an alternative to using conventional mixins.
 * Problems with conventional mixins:
 * - developers have to always remember to use the mixin consistently
 * - they have to go find the appropriate mixins
 * - the framework has to to verify the code and ensure mixins were used correctly
 * Automation addresses all of the above issues.
 *
 * <p>Currently three most common cases are handled:
 * #1 Matching and replacing only the property name. Eg. flex-grow: VALUE;
 * #2 Matching property name and value, replacing the value. Eg. display: flex;
 * #3 Matching property name and value where value is a function, replacing the function name.
 *    Eg. background-image: linear-gradient(ARGS);
 *
 */
public final class AutoExpandBrowserPrefix extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;
  private final ImmutableList<BrowserPrefixRule> expansionRules;
  private boolean inDefMixinBlock;

  public AutoExpandBrowserPrefix(
      MutatingVisitController visitController, ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
    this.expansionRules = BrowserPrefixGenerator.getExpansionRules();
  }

  @Override
  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    inDefMixinBlock = true;
    return true;
  }

  @Override
  public void leaveMixinDefinition(CssMixinDefinitionNode node) {
    inDefMixinBlock = false;
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    // Do not auto expand properties inside @defmixin blocks.
    // To enable compatibility with existing mixin expansion, don't apply the rules to the
    // mixin definitions. This leaves the mixin expansion unaffected.
    if (inDefMixinBlock) {
      return true;
    }
    List<CssDeclarationNode> expansionNodes = Lists.newArrayList();
    for (BrowserPrefixRule rule : expansionRules) {
      if (rule.getMatchPropertyName().equals(declaration.getPropertyName().getPropertyName())) {
        // Handle case #1 when no property value is available.
        if (rule.getMatchPropertyValue() == null) {
          for (CssDeclarationNode ruleExpansionNode : rule.getExpansionNodes()) {
            CssDeclarationNode expansionNode = ruleExpansionNode.deepCopy();
            expansionNode.setPropertyValue(declaration.getPropertyValue().deepCopy());
            expansionNode.setSourceCodeLocation(declaration.getSourceCodeLocation());
            expansionNodes.add(expansionNode);
          }
        } else {
          if (!rule.isFunction()) {
            // Handle case #2 where the property value is not a function.
            // Ensure that the property value matches exactly.
            if (!(declaration.getPropertyValue().getChildren().size() == 1
                && rule.getMatchPropertyValue().equals(
                    declaration.getPropertyValue().getChildAt(0).getValue()))) {
              continue;
            }
            for (CssDeclarationNode ruleExpansionNode : rule.getExpansionNodes()) {
              CssDeclarationNode expansionNode = ruleExpansionNode.deepCopy();
              expansionNode.setSourceCodeLocation(declaration.getSourceCodeLocation());
              expansionNodes.add(expansionNode);
            }
          } else {
            // Handle case #3 where the property value is a function. Eg. linear-gradient().
            CssValueNode matchValueNode = declaration.getPropertyValue().getChildAt(0);
            if (!(matchValueNode instanceof CssFunctionNode)) {
              continue;
            }
            CssFunctionNode matchFunctionNode = (CssFunctionNode) matchValueNode;
            if (matchFunctionNode.getFunctionName() == rule.getMatchPropertyValue()) {
              for (CssDeclarationNode ruleExpansionNode : rule.getExpansionNodes()) {
                CssDeclarationNode expansionNode = ruleExpansionNode.deepCopy();
                CssValueNode expandValueNode = expansionNode.getPropertyValue().getChildAt(0);
                CssFunctionNode expandFunctionNode = (CssFunctionNode) expandValueNode;
                expandFunctionNode.setArguments(matchFunctionNode.getArguments().deepCopy());
                expansionNode.setSourceCodeLocation(declaration.getSourceCodeLocation());
                expansionNodes.add(expansionNode);
              }
            }
          }
        }

        if (!expansionNodes.isEmpty()) {
          visitController.replaceCurrentBlockChildWith(expansionNodes, false);
          return true;
        }
      }
    }
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
