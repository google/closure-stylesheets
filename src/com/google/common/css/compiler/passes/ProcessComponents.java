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

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
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
import com.google.common.css.compiler.ast.CssProvideNode;
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
  private final List<CssProvideNode> provideNodes = Lists.newArrayList();
  private SourceCode lastFile = null;

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
  public boolean enterProvideNode(CssProvideNode node) {
    // Often this pass is called on a bunch of GSS files which have been concatenated
    // together, meaning that there will be multiple @provide declarations. We are only
    // interested in @provide nodes which are in the same source file as the @component.
    SourceCode sourceCode = node.getSourceCodeLocation().getSourceCode();
    if (sourceCode != lastFile) {
      provideNodes.clear();
      lastFile = sourceCode;
    }
    provideNodes.add(node);
    return false;
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
    SourceCode sourceCode = node.getSourceCodeLocation().getSourceCode();
    if (sourceCode != lastFile) {
      provideNodes.clear();
      lastFile = sourceCode;
    }
    if (node.isImplicitlyNamed() && provideNodes.size() != 1) {
      reportError("implicitly-named @components require a single @provide declaration ", node);
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
   * @param current the component for which the nodes are collected
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
        tree.getMutatingVisitController(), errorManager, provideNodes).runPass();
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
    private final String classPrefix;
    private final String defPrefix;
    private final String parentName;
    private final SourceCodeLocation sourceCodeLocation;

    public TransformNodes(Set<String> constants, CssComponentNode current, boolean inAncestorBlock,
        MutatingVisitController visitController, ErrorManager errorManager,
        List<CssProvideNode> provideNodes) {
      this.componentConstants = constants;
      this.inAncestorBlock = inAncestorBlock;
      this.visitController = visitController;
      this.errorManager = errorManager;

      String currentName = current.getName().getValue();
      if (current.isImplicitlyNamed()) {
        currentName = Iterables.getOnlyElement(provideNodes).getProvide();
      }
      this.isAbstract = current.isAbstract();
      // TODO(user): Allow this behavior to work with any component name that is
      // a quoted string.
      if (current.isImplicitlyNamed()) {
        this.classPrefix = getClassPrefixFromDottedName(currentName);
        this.defPrefix = getDefPrefixFromDottedName(currentName);
      } else {
        this.classPrefix = currentName + CLASS_SEP;
        this.defPrefix = currentName + DEF_SEP;
      }
      this.parentName = inAncestorBlock ? current.getParentName().getValue() : null;
      this.sourceCodeLocation = current.getSourceCodeLocation();
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
          classPrefix + node.getRefinerName(),
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
      CssLiteralNode newDefLit = new CssLiteralNode(defPrefix + defName);
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
        newNode = new CssDefinitionNode(ImmutableList.<CssValueNode>of(parentRefNode),
            newDefLit, sourceCodeLocation);
      } else {
        newNode = new CssDefinitionNode(CssAtRuleNode.copyNodes(node.getParameters()),
            newDefLit, sourceCodeLocation);
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
            new CssConstantReferenceNode(defPrefix + node.getValue());
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

    /**
     * Compute the name of the class prefix from the package name. This converts
     * the dot-separated package name to camel case, so foo.bar becomes fooBar.
     *
     * @param packageName the @provide package name
     * @return the converted class prefix
     */
    private String getClassPrefixFromDottedName(String packageName) {
      // CaseFormat doesn't have a format for names separated by dots, so we transform
      // the dots into dashes. Then we can use the regular CaseFormat transformation
      // to camel case instead of having to write our own.
      String packageNameWithDashes = packageName.replace('.', '-');
      return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, packageNameWithDashes);
    }

    /**
     * Compute the name of the def prefix from the package name. This converts the dot-separated
     * package name to uppercase with underscores, so foo.bar becomes FOO_BAR_.
     *
     * @param packageName the @provide package name
     * @return the converted def prefix
     */
    private String getDefPrefixFromDottedName(String packageName) {
      return packageName.replace('.', '_').toUpperCase() + "_";
    }
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
