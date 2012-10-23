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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.*;

import java.util.Set;

/**
 * Compiler pass that handles remaining {@link CssUnknownAtRuleNode} instances
 * by optionally reporting them as errors and optionally removing
 * them.
 *
 */
public class HandleUnknownAtRuleNodes extends DefaultTreeVisitor
    implements CssCompilerPass {
  static final String unknownAtRuleErrorMessage = "unknown @ rule";

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;
  private final Set<String> additionalAtRules;
  private final boolean report;
  private final boolean remove;

  public HandleUnknownAtRuleNodes(MutatingVisitController visitController,
                                  ErrorManager errorManager,
                                  boolean report,
                                  boolean remove) {
    this(visitController, errorManager, ImmutableSet.<String>of(),
         report, remove);
  }

  public HandleUnknownAtRuleNodes(MutatingVisitController visitController,
                                  ErrorManager errorManager,
                                  Set<String> additionalAtRules,
                                  boolean report,
                                  boolean remove) {
    Preconditions.checkArgument(!report || errorManager != null);
    this.visitController = visitController;
    this.errorManager = errorManager;
    this.additionalAtRules = additionalAtRules;
    this.report = report;
    this.remove = remove;
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    if (node.isOkWithoutProcessing()
        || additionalAtRules.contains(node.getName().getValue())) {
      return true;
    }

    if (report) {
      errorManager.report(new GssError(unknownAtRuleErrorMessage, node.getSourceCodeLocation()));
    }
    if (remove) {
      visitController.removeCurrentNode();
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void runPass() {
    if (report || remove) {
      visitController.startVisit(this);
    }
  }
}
