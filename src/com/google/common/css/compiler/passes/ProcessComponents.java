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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.CssAtRuleNode;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssComponentNode;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public class ProcessComponents<T> extends DefaultTreeVisitor
    implements CssCompilerPass {

  private static final String CLASS_SEP = "-";
  private static final String DEF_SEP = "__";

  private final Map<String, CssComponentNode> components = Maps.newHashMap();

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;
  private final Map<String, T> fileToChunk;

  /**
   * Creates a new pass to process components for the given visit
   * controller, using the given error manager, while ignoring chunks.
   */
  public ProcessComponents(MutatingVisitController visitController, ErrorManager errorManager) {
    this(visitController, errorManager, null);
  }

  /**
   * Creates a new pass to process components for the given visit
   * controller, using the given error manager, while maintaining the
   * chunk ids on the nodes created in the process according to the
   * given map from files to chunks.
   */
  public ProcessComponents(
      MutatingVisitController visitController, ErrorManager errorManager,
      @Nullable Map<String, T> fileToChunk) {
    this.visitController = visitController;
    this.errorManager = errorManager;
    this.fileToChunk = fileToChunk;
  }

  @Override
  public boolean enterComponent(CssComponentNode node) {
    String name = node.getName().getValue();
    if (components.containsKey(name)) {
      reportError("cannot redefine component in chunk ", node);
      return false;
    }
    CssLiteralNode parentName = node.getParentName();
    if ((parentName != null) && !components.containsKey(parentName.getValue())) {
      reportError("parent component is undefined in chunk ", node);
      return false;
    }
    visitController.replaceCurrentBlockChildWith(transformAllNodes(node), false);
    components.put(name, node);
    return false;
  }

  private void reportError(String message, CssNode node) {
    if (fileToChunk != null) {
      message += String.valueOf(
          MapChunkAwareNodesToChunk.getChunk(node, fileToChunk));
    }
    errorManager.report(new GssError(message, node.getSourceCodeLocation()));
    visitController.removeCurrentNode();
  }

  private List<CssNode> transformAllNodes(CssComponentNode current) {
    Set<String> constants = Sets.newHashSet();
    List<CssNode> nodes = Lists.newLinkedList();
    transformAllParentNodes(nodes, constants, current, current.getParentName());
    nodes.addAll(transformNodes(constants, current, current));
    return nodes;
  }

  /**
   * Recursively goes up the component inheritance hierarchy and copies the
   * ancestor component contents.
   *
   * @param nodes the list of copied child nodes collected from ancestor
   *     components
   * @param constants the set of names of constants defined in the ancestor
   *     components, used to differentiate local constant names from global
   *     constant names
   * @param current the component for which the the nodes are collected
   * @param parentLiteralNode the node which contains the name of the ancestor
   *     node to process, may be {@code null} if we reached the root of the
   *     inheritance tree
   */
  private void transformAllParentNodes(List<CssNode> nodes, Set<String> constants,
      CssComponentNode current, @Nullable CssLiteralNode parentLiteralNode) {
    if (parentLiteralNode == null) {
      return;
    }
    String parentName = parentLiteralNode.getValue();
    CssComponentNode parentComponent = components.get(parentName);
    transformAllParentNodes(nodes, constants, current, parentComponent.getParentName());
    nodes.addAll(transformNodes(constants, current, parentComponent));
  }

  /**
   * Copies and transforms the contents of the source component block for
   * inclusion in the expanded version of the target component.
   *
   * <p>The transformation of the source component block is basically a renaming
   * of the local constant references to their global equivalent.  Their names
   * are prefixed with the expanded component name.  Additionally ancestor
   * component contents are also emitted with appropriate renaming, although the
   * {@code @def} values are replaced with a reference to the ancestor
   * component.  For examples look at {@link ProcessComponentsTest}.
   *
   * @param constants the set of names of constants defined in the ancestor
   *     components, used to differentiate local constant names from global
   *     constant names
   * @param target the component for which the block contents are copied
   * @param source the component from which the block contents are taked
   * @return the list of transformed nodes
   */
  private List<CssNode> transformNodes(
      Set<String> constants, CssComponentNode target, CssComponentNode source) {
    CssBlockNode sourceBlock = source.getBlock();
    CssBlockNode copyBlock = new CssBlockNode(false, sourceBlock.deepCopy().getChildren());
    CssTree tree = new CssTree(
        target.getSourceCodeLocation().getSourceCode(), new CssRootNode(copyBlock));
    new TransformNodes(constants, target, target != source,
        tree.getMutatingVisitController(), errorManager).runPass();
    if (fileToChunk != null) {
      T chunk = MapChunkAwareNodesToChunk.getChunk(target, fileToChunk);
      new SetChunk(tree, chunk).runPass();
    }
    return tree.getRoot().getBody().getChildren();
  }

  private static class SetChunk extends DefaultTreeVisitor
      implements CssCompilerPass {

    private final CssTree tree;
    private final Object chunk;

    public SetChunk(CssTree tree, Object chunk) {
      this.tree = tree;
      this.chunk = chunk;
    }

    @Override
    public boolean enterDefinition(CssDefinitionNode definition) {
      definition.setChunk(chunk);
      return false;
    }

    @Override
    public boolean enterSelector(CssSelectorNode selector) {
      selector.setChunk(chunk);
      return true;
    }

    @Override
    public boolean enterFunctionNode(CssFunctionNode function) {
      function.setChunk(chunk);
      return super.enterFunctionNode(function);
    }

    @Override
    public void runPass() {
      tree.getVisitController().startVisit(this);
    }
  }

  private static class TransformNodes extends DefaultTreeVisitor
    implements CssCompilerPass {

    private final boolean inAncestorBlock;
    private final MutatingVisitController visitController;
    private final ErrorManager errorManager;

    private final Set<CssDefinitionNode> renamedDefinitions = Sets.newHashSet();

    private final Set<String> componentConstants;
    private final boolean isAbstract;
    private final String currentName;
    private final String parentName;

    public TransformNodes(Set<String> constants, CssComponentNode current, boolean inAncestorBlock,
                          MutatingVisitController visitController, ErrorManager errorManager) {
      this.componentConstants = constants;
      this.inAncestorBlock = inAncestorBlock;
      this.visitController = visitController;
      this.errorManager = errorManager;

      this.isAbstract = current.isAbstract();
      this.currentName = current.getName().getValue();
      this.parentName = inAncestorBlock ? current.getParentName().getValue() : null;
    }

    @Override
    public boolean enterComponent(CssComponentNode node) {
      if (!inAncestorBlock) {
        errorManager.report(
            new GssError("nested components are not allowed", node.getSourceCodeLocation()));
      }
      visitController.removeCurrentNode();
      return false;
    }

    @Override
    public boolean enterRuleset(CssRulesetNode node) {
      if (isAbstract) {
        visitController.removeCurrentNode();
      }
      return !isAbstract;
    }

    @Override
    public boolean enterClassSelector(CssClassSelectorNode node) {
      Preconditions.checkState(!isAbstract);
      CssClassSelectorNode newNode = new CssClassSelectorNode(
          currentName + CLASS_SEP + node.getRefinerName(),
          node.getSourceCodeLocation());
      visitController.replaceCurrentBlockChildWith(ImmutableList.of(newNode), false);
      return true;
    }

    @Override
    public boolean enterDefinition(CssDefinitionNode node) {
      // Do not modify the renamed node created below, but descend and modify
      // its children.
      if (renamedDefinitions.contains(node)) {
        return true;
      }
      String defName = node.getName().getValue();
      CssLiteralNode newDefLit = new CssLiteralNode(currentName + DEF_SEP + defName);
      CssDefinitionNode newNode;
      // When copying the ancestor block, we want to replace definition values
      // with a reference to the constant emitted when the parent component was
      // transformed.  This makes it possible to actually inherit values from
      // the parent component (parent component definitions changes will
      // propagate to descendant components).
      if (inAncestorBlock) {
        String parentRefPrefix = parentName + DEF_SEP;
        // workarounds.  Can be removed when all workarounds are removed.
        String parentRefName = defName.startsWith(parentRefPrefix)
            ? defName : parentRefPrefix + defName;
        CssConstantReferenceNode parentRefNode =
            new CssConstantReferenceNode(parentRefName);
        newNode = new CssDefinitionNode(ImmutableList.<CssValueNode>of(parentRefNode), newDefLit);
      } else {
        newNode = new CssDefinitionNode(CssAtRuleNode.copyNodes(node.getParameters()), newDefLit);
      }
      componentConstants.add(defName);
      renamedDefinitions.add(newNode);
      visitController.replaceCurrentBlockChildWith(ImmutableList.of(newNode), true);
      return false;
    }

    @Override
    public boolean enterValueNode(CssValueNode node) {
      if (node instanceof CssConstantReferenceNode
          // Avoid renaming constant references for constants not defined in the
          // component tree.
          && componentConstants.contains(node.getValue())) {
        CssConstantReferenceNode newNode =
            new CssConstantReferenceNode(currentName + DEF_SEP + node.getValue());
        visitController.replaceCurrentBlockChildWith(ImmutableList.of(newNode), false);
      }
      return true;
    }

    @Override
    public boolean enterArgumentNode(CssValueNode node) {
      return enterValueNode(node);
    }

    @Override
    public void runPass() {
      visitController.startVisit(this);
    }
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
