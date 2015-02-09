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

import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssForLoopRuleNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A compiler pass that unrolls loops.
 *
 * <p>For every loop, we iterate over the different values of the loop variables, create a copy of
 * the loop block and invoke {@link LoopVariableReplacementPass} to replace all references of the
 * loop variable with the current iteration value. So for a loop with N iterations, we'll replace
 * the loop block with N blocks which only differ in the replacement to the loop variable value.
 *
 * <p><strong>NOTE:<strong> There is special treatment for {@code @def}s in this pass. Since
 * definition references might appear before declaration, it's easier for
 * {@link LoopVariableReplacementPass} to know all definitions declarations beforehand (the latter
 * pass needs to do name replacement for definitions declared inside the loop). That's why this pass
 * scrapes them prior to calling {@link LoopVariableReplacementPass}.
 */
public class UnrollLoops extends DefaultTreeVisitor implements CssCompilerPass {

  private final MutatingVisitController visitController;

  public UnrollLoops(MutatingVisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public boolean enterForLoop(CssForLoopRuleNode node) {
    GatherLoopDefinitions definitionsGatherer = new GatherLoopDefinitions();
    node.getVisitController().startVisit(definitionsGatherer);
    Set<String> definitions = definitionsGatherer.getLoopDefinitions();
    List<CssNode> blocks = Lists.newArrayListWithCapacity(node.getToValue() - node.getFromValue());
    for (int i = node.getFromValue(); i <= node.getToValue(); i += node.getStepValue()) {
      blocks.addAll(
          makeBlock(node, i, definitions, node.getLoopId()).getChildren());
    }

    visitController.replaceCurrentBlockChildWith(blocks, true /* visitTheReplacementNodes */);
    return true;
  }

  /**
   * Copies the node's block and replaces appearances of the loop variable with the given value.
   */
  private CssBlockNode makeBlock(
      CssForLoopRuleNode node, int value, Set<String> definitions, int loopId) {
    CssBlockNode newBlock = new CssBlockNode(false, node.getBlock().deepCopy().getChildren());
    newBlock.setSourceCodeLocation(node.getSourceCodeLocation());
    CssTree tree = new CssTree(null, new CssRootNode(newBlock));
    new LoopVariableReplacementPass(
        node.getVariableName(), value, definitions, tree.getMutatingVisitController(), loopId)
        .runPass();
    return newBlock;
  }

  /**
   * A visitor gathers all the definitions ({@code @def}) inside the loop.
   * That is needed to properly add the iteration suffix.
   */
  private class GatherLoopDefinitions extends DefaultTreeVisitor {

    private final Set<String> loopDefinitions = new HashSet<>();

    public Set<String> getLoopDefinitions() {
      return loopDefinitions;
    }

    @Override
    public boolean enterDefinition(CssDefinitionNode node) {
      loopDefinitions.add(node.getName().getValue());
      return true;
    }
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
