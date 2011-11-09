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

import java.util.List;

/**
 * A compiler pass that moves all comments of each {@link CssDefinitionNode}'s children to the
 * {@link CssDefinitionNode} itself. (This is to ensure that "@default" annotations are attached
 * to the {@link CssDefinitionNode} directly.)
 *
 */
public class RelocateDefaultComments extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;

  public RelocateDefaultComments(MutatingVisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    //TODO(user): Now it moves up every comment to the CssDefinitionNode.
    // Later we should move only the annotations "@default".
    List<CssCommentNode> comments = node.getComments();
    CssLiteralNode name = node.getName();
    List<CssValueNode> params = node.getParameters();
    List<CssCommentNode> nameComments = name.getComments();
    for (CssCommentNode c : nameComments) {
      comments.add(c);
    }
    nameComments.clear();
    for (CssValueNode valueNode : params) {
      List<CssCommentNode> commentsList = valueNode.getComments();
      for (CssCommentNode c : commentsList) {
        comments.add(c);
      }
      commentsList.clear();
    }
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

}
