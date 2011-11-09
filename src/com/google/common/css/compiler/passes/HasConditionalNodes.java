/*
 * Copyright 2008 Google Inc.
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
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;

/**
 */
public class HasConditionalNodes extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final VisitController visitController;

  private boolean hasConditionalNodes = false;

  private boolean passWasRun = false;

  HasConditionalNodes(VisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public boolean enterConditionalBlock(CssConditionalBlockNode block) {
    // assert !block.isEmpty();
    hasConditionalNodes = true;
    visitController.stopVisit();
    return true;
  }

  @Override
  public boolean enterConditionalRule(CssConditionalRuleNode node) {
    // This should never get called as the visit should stop at the enclosing
    // conditional block of the first conditional rule.
    // assert false;
    return false;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
    passWasRun = true;
  }

  public boolean hasConditionalNodes() {
    Preconditions.checkState(passWasRun);
    return hasConditionalNodes;
  }
}
