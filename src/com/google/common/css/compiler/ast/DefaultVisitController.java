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

package com.google.common.css.compiler.ast;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompositeValueNode.Operator;

import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of the MutatingVisitController. The controller is
 * mutating or not depending on a flag passed as a parameter to the constructor.
 *
 */
class DefaultVisitController implements MutatingVisitController {

  /** The CssTree to be visited. */
  private CssTree tree;

  /** Whether mutations of the tree are allowed or not. */
  private boolean allowMutating;

  /** The visitor of the tree. */
  @VisibleForTesting
  CssTreeVisitor visitor;

  /** The stack of states for the controller. */
  private StateStack stateStack = new StateStack();

  /** Whether the visit was required to stop. */
  @SuppressWarnings("unused")
  private boolean stopVisitCalled = false;

  @SuppressWarnings("serial")
  private static class StopVisitRequestedException extends RuntimeException {}

  /**
   * Interface for CSS AST visit states. Visit states are used to track which
   * node and what type of node is currently visited, perform tree modifications
   * and take care of visit state transitions. Visit states should only allow
   * valid state transitions - this can be used as AST structure validation.
   *
   * @param <T> type of the children CSS nodes that can be used as a replacement
   *     for currently visited block node
   */
  @VisibleForTesting
  interface VisitState<T extends CssNode> {
    /**
     * Performs the visit by calling appropriate methods of the visitor
     * (enter and leave).
     */
    void doVisit();

    /**
     * Transitions to next state by putting a new one onto the stack or popping
     * the old one off the state stack. Some implementations can handle children
     * nodes changes and omit visits of children nodes (effectively a subtree).
     */
    void transitionToNextState();

    /**
     * Notifies the state, that the stop tree visit has been requested.<p>
     *
     * <p>NOTE(dgajda): Practically unused, remove?
     */
    void stopVisitCalled();

    /**
     * Notifies the state that removal of current node is requested.
     * Performs the removal by passing the control to the state
     * below current one.
     */
    void removeCurrentNodeCalled();

    /** Removes currently visited child node. */
    void removeCurrentChild();

    /**
     * Used to notify current state and allow the state below of the top state
     * remove the current node with a list of replacement nodes.
     *
     * <p>NOTE(dgajda): This method probably does not need to be in VisitState.
     *
     * @param <S> type od replacement nodes
     * @param replacementNodes nodes used to replace current block
     * @param visitTheReplacementNodes whether new nodes should also be visited
     */
    <S extends CssNode> void replaceCurrentBlockChildWithCalled(
        List<S> replacementNodes,
        boolean visitTheReplacementNodes);

    /**
     * Replaces current node with given replacement nodes.
     * Called by {@link #replaceCurrentBlockChildWithCalled(List, boolean)}.
     *
     * @param replacementNodes nodes used to replace current block
     * @param visitTheReplacementNodes whether new nodes should also be visited
     */
    void replaceCurrentBlockChildWith(List<T> replacementNodes,
        boolean visitTheReplacementNodes);
  }

  /**
   * Base implementation of AST visit state.
   *
   * @param <N> type of the children CSS nodes that can be used as a replacement
   *     for currently visited block node
   */
  @VisibleForTesting
  abstract class BaseVisitState<N extends CssNode>
      implements VisitState<N> {
    @Override
    public void stopVisitCalled() { }

    @Override
    public void removeCurrentChild() {
      // Assume that by default this cannot happen.
      throw new AssertionError("Current child removal is not supported by " +
          this.getClass().getName() + " VisitState class.");
    }

    @Override
    public void removeCurrentNodeCalled() {
      stateStack.pop();
      stateStack.getTop().removeCurrentChild();
    }

    @Override
    public <S extends CssNode> void replaceCurrentBlockChildWithCalled(
        List<S> replacementNodes, boolean visitTheReplacementNodes) {
      stateStack.pop();
      @SuppressWarnings("unchecked")
      VisitState<S> topState = (VisitState<S>) stateStack.getTop();
      topState.replaceCurrentBlockChildWith(
          replacementNodes, visitTheReplacementNodes);
    }

    @Override
    public void replaceCurrentBlockChildWith(
        List<N> replacementNodes, boolean visitTheReplacementNodes) {
      // Assume that by default this cannot happen.
      // assert false;
    }

    public VisitState<? extends CssNode> createFallbackState(N child) {
      return null;
    }
  }

  /**
   * Base class for VisitStates which control visits of {@link CssNodesListNode}
   * children.
   *
   * @param <T> type of the children CSS nodes that can be used as a replacement
   *     for currently visited block node
   */
  abstract class VisitChildrenState<T extends CssNode>
      extends BaseVisitState<CssNode> {

    private final CssNodesListNode<T> block;

    private int currentIndex = -1;

    VisitChildrenState(CssNodesListNode<T> block) {
      this.block = block;
    }

    @Override
    public void transitionToNextState() {
      if (currentIndex == block.numChildren() - 1) {
        stateStack.pop();
        return;
      }
      // Remain in this state to finish visiting all the children
      currentIndex++;
      stateStack.push(getVisitState(block.getChildAt(currentIndex)));
      return;
    }

    /**
     * Returns a visit state for a given child node.
     *
     * @param node child node to create visit state for
     * @return new visit state
     */
    abstract VisitState<CssNode> getVisitState(T node);

    /** {@inheritDoc} */
    @Override
    public void doVisit() {
      // Does nothing.
    }
  }

  /**
   * Base class for VisitStates which control visits of {@link CssNodesListNode}
   * children and can replace currently visited node with replacement nodes.
   *
   * @param <T> type of the children CSS nodes that can be used as a replacement
   *     for currently visited block node
   */
  abstract class VisitReplaceChildrenState<T extends CssNode>
    extends BaseVisitState<T> {

    protected int currentIndex = -1;
    protected boolean doNotIncreaseIndex = false;
    protected final CssNodesListNode<T> node;

    VisitReplaceChildrenState(CssNodesListNode<T> node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      // Does nothing.
    }

    @Override
    public void removeCurrentChild() {
      node.removeChildAt(currentIndex);
      doNotIncreaseIndex = true;
    }

    @Override
    public void replaceCurrentBlockChildWith(
        List<T> replacementNodes, boolean visitTheReplacementNodes) {
      node.replaceChildAt(currentIndex, replacementNodes);
      if (visitTheReplacementNodes) {
        doNotIncreaseIndex = true;
      } else {
        currentIndex += replacementNodes.size() - 1;
      }
    }

    @Override
    public void transitionToNextState() {
      // We get out of this state if we are on the last child and we are allowed
      // to increment the index, which means we arrived here with no special
      // case, or we have just removed the last element.
      if ((currentIndex == node.numChildren() - 1 && !doNotIncreaseIndex)
          || currentIndex == node.numChildren()) {
        stateStack.pop();
        return;
      }
      // Remain in this state to finish visiting all the children.
      if (!doNotIncreaseIndex) {
        currentIndex++;
      } else {
        doNotIncreaseIndex = false;
      }
      VisitState<? extends CssNode> state =
          createVisitState(node.getChildAt(currentIndex), this);
      if (state != null) {
        stateStack.push(state);
      }
    }
  }

  /**
   * Unfinished base class for VisitStates which share the same code to
   * optionally visit child nodes.
   *
   * @param <T> type of the children CSS nodes that can be used as a replacement
   *     for currently visited block node
   */
  abstract class VisitChildrenOptionalState<T extends CssNode>
      extends BaseVisitState<CssNode> {
  }

  @VisibleForTesting
  class RootVisitBeforeChildrenState extends BaseVisitState<CssNode> {

    private final CssRootNode root;

    RootVisitBeforeChildrenState(CssRootNode root) {
      Preconditions.checkNotNull(root);
      this.root = root;
    }

    @Override
    public void doVisit() {
      visitor.enterTree(root);
    }

    @Override
    public void transitionToNextState() {
      stateStack.transitionTo(
          new RootVisitCharsetState(root, root.getCharsetRule()));
    }
  }

  @VisibleForTesting
  class RootVisitAfterChildrenState extends BaseVisitState<CssNode> {

    private final CssRootNode root;

    RootVisitAfterChildrenState(CssRootNode root) {
      Preconditions.checkNotNull(root);
      this.root = root;
    }

    @Override
    public void doVisit() {
      visitor.leaveTree(root);
    }

    @Override
    public void transitionToNextState() {
      stateStack.pop();
      // assert stateStack.isEmpty();
    }
  }

  @VisibleForTesting
  class RootVisitCharsetState extends BaseVisitState<CssNode> {

    private final CssRootNode root;

    private final CssAtRuleNode charsetRule;

    RootVisitCharsetState(CssRootNode root, CssAtRuleNode charsetRule) {
      this.root = root;
      this.charsetRule = charsetRule;
    }

    @Override
    public void doVisit() {
      if (charsetRule == null) {
        // Nothing left to do.
        return;
      }
    }

    @Override
    public void removeCurrentNodeCalled() {
      root.setCharsetRule(null);
    }

    @Override
    public void transitionToNextState() {
      stateStack.transitionTo(new RootVisitImportBlockState(root,
          root.getImportRules()));
    }
  }

  @VisibleForTesting
  class RootVisitImportBlockState extends BaseVisitState<CssNode> {

    private final CssRootNode root;

    private final CssImportBlockNode block;

    private boolean visitedChildren = false;

    RootVisitImportBlockState(CssRootNode root, CssImportBlockNode block) {
      this.root = root;
      this.block = block;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterImportBlock(block);
      } else {
        visitor.leaveImportBlock(block);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        stateStack.push(
            new VisitImportBlockChildrenState(block));
        visitedChildren = true;
      } else {
        stateStack.transitionTo(
            new RootVisitBodyState(root, root.getBody()));
      }
    }
  }

  @VisibleForTesting
  class VisitImportBlockChildrenState
      extends VisitChildrenState<CssImportRuleNode> {

    VisitImportBlockChildrenState(CssImportBlockNode block) {
      super(block);
    }

    @Override
    VisitState<CssNode> getVisitState(CssImportRuleNode node) {
      return new VisitImportRuleState(node);
    }

  }

  @VisibleForTesting
  class VisitImportRuleState extends BaseVisitState<CssNode> {

    private final CssImportRuleNode node;

    VisitImportRuleState(CssImportRuleNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      visitor.enterImportRule(node);
      visitor.leaveImportRule(node);
    }

    @Override
    public void transitionToNextState() {
      stateStack.pop();
    }
  }

  @VisibleForTesting
  class RootVisitBodyState extends VisitChildrenOptionalState<CssNode> {

    private final CssRootNode root;

    private final CssBlockNode body;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    RootVisitBodyState(CssRootNode root, CssBlockNode body) {
      this.root = root;
      this.body = body;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterBlock(body);
      } else {
        visitor.leaveBlock(body);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        stateStack.push(new VisitBlockChildrenState(body));
        visitedChildren = true;
      } else {
        stateStack.transitionTo(new RootVisitAfterChildrenState(root));
      }
    }
  }

  @VisibleForTesting
  class VisitBlockChildrenState extends VisitReplaceChildrenState<CssNode> {

    VisitBlockChildrenState(CssBlockNode block) {
      super(block);
    }
  }

  @VisibleForTesting
  class VisitDefinitionState extends VisitChildrenOptionalState<CssNode> {

    private final CssDefinitionNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitDefinitionState(CssDefinitionNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterDefinition(node);
      } else {
        visitor.leaveDefinition(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        stateStack.push(new VisitDefinitionParametersState(node));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitDefinitionParametersState
      extends VisitReplaceChildrenState<CssValueNode> {

    VisitDefinitionParametersState(CssDefinitionNode def) {
      super(def);
    }

    @Override
    public VisitState<CssValueNode> createFallbackState(CssValueNode child) {
      return new VisitValueNodeState(child);
    }
  }

  @VisibleForTesting
  class VisitMediaRuleState extends VisitChildrenOptionalState<CssNode> {

    private final CssMediaRuleNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitMediaRuleState(CssMediaRuleNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterMediaRule(node);
      } else {
        visitor.leaveMediaRule(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        stateStack.push(new VisitUnknownAtRuleBlockState(node.getBlock()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitPageRuleState extends VisitChildrenOptionalState<CssNode> {

    private final CssPageRuleNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitPageRuleState(CssPageRuleNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterPageRule(node);
      } else {
        visitor.leavePageRule(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        stateStack.push(new VisitUnknownAtRuleBlockState(node.getBlock()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitPageSelectorState extends VisitChildrenOptionalState<CssNode> {

    private final CssPageSelectorNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitPageSelectorState(CssPageSelectorNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterPageSelector(node);
      } else {
        visitor.leavePageSelector(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        stateStack.push(new VisitUnknownAtRuleBlockState(node.getBlock()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitConditionalBlockState extends BaseVisitState<CssNode> {

    private final CssConditionalBlockNode block;

    private boolean visitedChildren = false;

    VisitConditionalBlockState(CssConditionalBlockNode block) {
      this.block = block;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterConditionalBlock(block);
      } else {
        visitor.leaveConditionalBlock(block);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        stateStack.push(
            new VisitConditionalBlockChildrenState(block));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitConditionalBlockChildrenState
      extends VisitChildrenState<CssConditionalRuleNode> {

    VisitConditionalBlockChildrenState(CssConditionalBlockNode block) {
      super(block);
    }

    @Override
    VisitState<CssNode> getVisitState(CssConditionalRuleNode node) {
      return new VisitConditionalRuleState(node);
    }
  }

  @VisibleForTesting
  class VisitConditionalRuleState extends VisitChildrenOptionalState<CssNode> {

    private final CssConditionalRuleNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitConditionalRuleState(CssConditionalRuleNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterConditionalRule(node);
      } else {
        visitor.leaveConditionalRule(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        stateStack.push(
            new VisitConditionalRuleChildrenState(node.getBlock()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitConditionalRuleChildrenState extends VisitReplaceChildrenState<CssNode> {

    VisitConditionalRuleChildrenState(CssBlockNode block) {
      super(block);
    }
  }

  @VisibleForTesting
  class VisitRulesetState extends VisitChildrenOptionalState<CssNode> {

    private final CssRulesetNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitRulesetState(CssRulesetNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterRuleset(node);
      } else {
        visitor.leaveRuleset(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        stateStack.push(new VisitSelectorBlockState(node, node.getSelectors()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitKeyframeRulesetState extends VisitChildrenOptionalState<CssNode> {

    private final CssKeyframeRulesetNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitKeyframeRulesetState(CssKeyframeRulesetNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterKeyframeRuleset(node);
      } else {
        visitor.leaveKeyframeRuleset(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        stateStack.push(new VisitKeyBlockState(node, node.getKeys()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitSelectorBlockState extends BaseVisitState<CssNode> {

    private final CssSelectorListNode block;

    private final CssRulesetNode ruleset;

    private boolean visitedChildren = false;

    VisitSelectorBlockState(CssRulesetNode ruleset,
                            CssSelectorListNode block) {
      this.ruleset = ruleset;
      this.block = block;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterSelectorBlock(block);
      } else {
        visitor.leaveSelectorBlock(block);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        stateStack.push(
            new VisitSelectorBlockChildrenState(block));
        visitedChildren = true;
      } else {
        stateStack.transitionTo(
            new VisitDeclarationBlockState(ruleset.getDeclarations()));
      }
    }
  }

  @VisibleForTesting
  class VisitSelectorBlockChildrenState
      extends VisitChildrenState<CssSelectorNode> {

    VisitSelectorBlockChildrenState(CssSelectorListNode block) {
      super(block);
    }

    @Override
    VisitState<CssNode> getVisitState(CssSelectorNode node) {
      return new VisitSelectorState(node);
    }
  }

  @VisibleForTesting
  class VisitKeyBlockState extends BaseVisitState<CssNode> {

    private final CssKeyListNode block;

    private final CssKeyframeRulesetNode ruleset;

    private boolean visitedChildren = false;

    VisitKeyBlockState(CssKeyframeRulesetNode ruleset,
                            CssKeyListNode block) {
      this.ruleset = ruleset;
      this.block = block;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterKeyBlock(block);
      } else {
        visitor.leaveKeyBlock(block);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        stateStack.push(
            new VisitKeyBlockChildrenState(block));
        visitedChildren = true;
      } else {
        stateStack.transitionTo(
            new VisitDeclarationBlockState(ruleset.getDeclarations()));
      }
    }
  }

  @VisibleForTesting
  class VisitKeyBlockChildrenState
      extends VisitChildrenState<CssKeyNode> {

    VisitKeyBlockChildrenState(CssKeyListNode block) {
      super(block);
    }

    @Override
    VisitState<CssNode> getVisitState(CssKeyNode node) {
      return new VisitKeyState(node);
    }
  }

  @VisibleForTesting
  class VisitSelectorState extends BaseVisitState<CssNode> {

    private final CssSelectorNode node;

    private boolean visitedChildren = false;

    VisitSelectorState(CssSelectorNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterSelector(node);
      } else {
        visitor.leaveSelector(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        // We need to prepare the stack such that the refiners are visited first
        // and then the combinator if there is one.
        if (node.getCombinator() != null) {
          stateStack.push(new VisitCombinatorState(node.getCombinator()));
        }
        stateStack.push(new VisitRefinerListState(node.getRefiners()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitKeyState extends BaseVisitState<CssNode> {

    private final CssKeyNode node;

    private boolean visitedChildren = false;

    VisitKeyState(CssKeyNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterKey(node);
      } else {
        visitor.leaveKey(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitRefinerListState extends VisitReplaceChildrenState<CssRefinerNode> {

    VisitRefinerListState(CssRefinerListNode node) {
      super(node);
    }
  }

  @VisibleForTesting
  class VisitRefinerNodeState extends BaseVisitState<CssNode> {

    private final CssRefinerNode node;

    private boolean visitedChildren = false;

    VisitRefinerNodeState(CssRefinerNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      // TODO(user): Actually each of these nodes should have its own state
      // here but this adds a bunch of similar code that is not really
      // necessary. The problem is the design of the visit controller. I'm
      // going to refactor it so that it doesn't make sense to add all the
      // states.
      if (!visitedChildren) {
        if (node instanceof CssClassSelectorNode) {
          visitor.enterClassSelector((CssClassSelectorNode) node);
        } else if (node instanceof CssIdSelectorNode) {
          visitor.enterIdSelector((CssIdSelectorNode) node);
        } else if (node instanceof CssPseudoClassNode) {
          visitor.enterPseudoClass((CssPseudoClassNode) node);
        } else if (node instanceof CssPseudoElementNode) {
          visitor.enterPseudoElement((CssPseudoElementNode) node);
        } else if (node instanceof CssAttributeSelectorNode) {
          visitor.enterAttributeSelector((CssAttributeSelectorNode) node);
        }
      } else {
        if (node instanceof CssClassSelectorNode) {
          visitor.leaveClassSelector((CssClassSelectorNode) node);
        } else if (node instanceof CssIdSelectorNode) {
          visitor.leaveIdSelector((CssIdSelectorNode) node);
        } else if (node instanceof CssPseudoClassNode) {
          visitor.leavePseudoClass((CssPseudoClassNode) node);
        } else if (node instanceof CssPseudoElementNode) {
          visitor.leavePseudoElement((CssPseudoElementNode) node);
        } else if (node instanceof CssAttributeSelectorNode) {
          visitor.leaveAttributeSelector((CssAttributeSelectorNode) node);
        }
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        if (node instanceof CssPseudoClassNode) {
          CssPseudoClassNode pseudoClass = (CssPseudoClassNode) node;
          if (pseudoClass.getNotSelector() != null) {
            stateStack.push(new VisitSelectorState(
                pseudoClass.getNotSelector()));
          }
        }
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitCombinatorState extends BaseVisitState<CssNode> {

    private final CssCombinatorNode node;

    private boolean visitedChildren = false;

    VisitCombinatorState(CssCombinatorNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterCombinator(node);
      } else {
        visitor.leaveCombinator(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        stateStack.push(new VisitSelectorState(node.getSelector()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitDeclarationBlockState extends BaseVisitState<CssNode> {

    private final CssDeclarationBlockNode node;

    private boolean startedVisitingChildren = false;
    private boolean finishedVisitingChildren = false;

    @VisibleForTesting
    int currentIndex = -1;
    private boolean doNotIncreaseIndex = false;

    VisitDeclarationBlockState(CssDeclarationBlockNode block) {
      this.node = block;
    }

    @Override
    public void doVisit() {
      if (!startedVisitingChildren) {
        visitor.enterDeclarationBlock(node);
        startedVisitingChildren = true;
      } else if (finishedVisitingChildren) {
        visitor.leaveDeclarationBlock(node);
      }
    }

    @Override
    public void removeCurrentChild() {
      node.removeChildAt(currentIndex);
      doNotIncreaseIndex = true;
    }

    @Override
    public void replaceCurrentBlockChildWith(
        List<CssNode> replacementNodes,
        boolean visitTheReplacementNodes) {
      node.replaceChildAt(currentIndex, replacementNodes);
      if (visitTheReplacementNodes) {
        doNotIncreaseIndex = true;
      } else {
        currentIndex += replacementNodes.size() - 1;
      }
    }

    @Override
    public void transitionToNextState() {
      if (finishedVisitingChildren) {
        stateStack.pop();
        return;
      }
      // We finish visiting this state if we are on the last child and we are
      // allowed to increment the index, which means we arrived here with no
      // special case, or we have just removed the last element.
      if ((currentIndex == node.numChildren() - 1 && !doNotIncreaseIndex)
          || currentIndex == node.numChildren()) {
        finishedVisitingChildren = true;
        return;
      }
      // Remain in this state to finish visiting all the children.
      if (!doNotIncreaseIndex) {
        currentIndex++;
      } else {
        doNotIncreaseIndex = false;
      }
      VisitState<? extends CssNode> state =
          createVisitState(node.getChildAt(currentIndex), this);
      if (state != null) {
        stateStack.push(state);
      }
    }
  }

  @VisibleForTesting
  class VisitDeclarationState extends BaseVisitState<CssNode> {

    private final CssDeclarationNode node;

    private boolean visitedChildren = false;

    VisitDeclarationState(CssDeclarationNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterDeclaration(node);
      } else {
        visitor.leaveDeclaration(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        stateStack.push(new VisitPropertyValueState(node.getPropertyValue()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitMixinState extends BaseVisitState<CssNode> {

    private final CssMixinNode node;

    private boolean visitedChildren = false;

    VisitMixinState(CssMixinNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterMixin(node);
      } else {
        visitor.leaveMixin(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        stateStack.push(
            new VisitFunctionArgumentsNodeState(node.getArguments()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitPropertyValueState extends BaseVisitState<CssValueNode> {

    private final CssPropertyValueNode node;

    private boolean visitedChildren = false;
    private boolean visitingChildren = false;

    private int currentIndex = -1;
    private boolean doNotIncreaseIndex = false;

    VisitPropertyValueState(CssPropertyValueNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitingChildren) {
        if (!visitedChildren) {
          visitor.enterPropertyValue(node);
        } else {
          visitor.leavePropertyValue(node);
        }
      }
    }

    @Override
    public void removeCurrentChild() {
      node.removeChildAt(currentIndex);
      doNotIncreaseIndex = true;
    }

    @Override
    public void replaceCurrentBlockChildWith(
        List<CssValueNode> replacementNodes, boolean visitTheReplacementNodes) {
      // If we're replacing the current property with a composite value
      // separated by spaces, we really just want to graft those nodes at the
      // current child's location.
      if (replacementNodes.size() == 1
          && replacementNodes.get(0) instanceof CssCompositeValueNode) {
        CssCompositeValueNode compositeValueNode =
            (CssCompositeValueNode) replacementNodes.get(0);
        if (compositeValueNode.getOperator() == Operator.SPACE) {
          replacementNodes = compositeValueNode.getValues();
        }
      }
      node.replaceChildAt(currentIndex, replacementNodes);
      if (visitTheReplacementNodes) {
        doNotIncreaseIndex = true;
      } else {
        currentIndex += replacementNodes.size() - 1;
      }
    }

    @Override
    public void transitionToNextState() {
      // We get out of this state if we are on the last child and we are allowed
      // to increment the index, which means we arrived here with no special
      // case, or we have just removed the last element.
      if ((currentIndex == node.numChildren() - 1 && !doNotIncreaseIndex)
          || currentIndex == node.numChildren()) {
        if (visitedChildren) {
          stateStack.pop();
        } else {
          visitingChildren = false;
          visitedChildren = true;
        }
        return;
      }
      // Remain in this state to finish visiting all the children.
      visitingChildren = true;
      if (!doNotIncreaseIndex) {
        currentIndex++;
      } else {
        doNotIncreaseIndex = false;
      }
      VisitState<? extends CssNode> state =
          createVisitState(node.getChildAt(currentIndex), this);
      if (state != null) {
        stateStack.push(state);
      }
    }

    @Override
    public VisitState<? extends CssNode> createFallbackState(CssValueNode child) {
      return new VisitValueNodeState(child);
    }
  }

  @VisibleForTesting
  class VisitValueNodeState extends BaseVisitState<CssValueNode> {

    private final CssValueNode node;

    VisitValueNodeState(CssValueNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      visitor.enterValueNode(node);
      visitor.leaveValueNode(node);
    }

    @Override
    public void transitionToNextState() {
      stateStack.pop();
    }
  }

  @VisibleForTesting
  class VisitCompositeValueState extends BaseVisitState<CssValueNode> {

    private final CssCompositeValueNode node;
    private List<CssValueNode> children;
    private int currentIndex = -1;
    private boolean doNotIncreaseIndex = false;
    private boolean visitChildren = true;

    VisitCompositeValueState(CssCompositeValueNode node) {
      this.node = node;
      this.children = node.getValues();
    }

    @Override
    public void transitionToNextState() {
      if (currentIndex == children.size() - 1) {
        stateStack.pop();
        return;
      }

      if (visitChildren == false) {
        currentIndex = children.size() - 1;
        return;
      }

      // Remain in this state to finish visiting all the children
      if (!doNotIncreaseIndex) {
        currentIndex++;
      } else {
        doNotIncreaseIndex = false;
      }
      stateStack.push(getVisitState(children.get(currentIndex)));
      return;
    }

    /**
     * Returns a visit state for a given child node.
     *
     * @param child child node to create visit state for
     * @return new visit state
     */
    VisitState<CssValueNode> getVisitState(CssValueNode child) {
      if (child instanceof CssCompositeValueNode) {
        return new VisitCompositeValueState((CssCompositeValueNode) child);
      } else {
        return new VisitValueNodeState(child);
      }
    }

    /** {@inheritDoc} */
    @Override
    public void doVisit() {
      if (currentIndex < 0) {
        visitChildren = visitor.enterValueNode(node);
      } else if (currentIndex == children.size() - 1) {
        visitor.leaveValueNode(node);
      }
    }

    @Override
    public void replaceCurrentBlockChildWith(
        List<CssValueNode> replacementNodes,
        boolean visitTheReplacementNodes) {
      children.remove(currentIndex);

      // If we're replacing the current property with a composite value
      // separated by the same operator, we really just want to graft those
      // nodes at the current child's location.
      if (replacementNodes.size() == 1
          && replacementNodes.get(0) instanceof CssCompositeValueNode) {
        CssCompositeValueNode compositeValueNode =
            (CssCompositeValueNode) replacementNodes.get(0);
        if (compositeValueNode.getOperator() == node.getOperator()) {
          replacementNodes = compositeValueNode.getValues();
        }
      }

      children.addAll(currentIndex, replacementNodes);
      if (!visitTheReplacementNodes) {
        currentIndex += replacementNodes.size() - 1;
      } else {
        doNotIncreaseIndex = true;
      }
    }
  }

  @VisibleForTesting
  class VisitFunctionNodeState extends BaseVisitState<CssNode> {

    private final CssFunctionNode node;

    private boolean visitedChildren = false;

    VisitFunctionNodeState(CssFunctionNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterFunctionNode(node);
      } else {
        visitor.leaveFunctionNode(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        stateStack.push(
            new VisitFunctionArgumentsNodeState(node.getArguments()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }

    @Override
    public void removeCurrentNodeCalled() {
      // Remove the nearest declaration that contains the function.
      while (!(stateStack.getTop() instanceof VisitDeclarationState)) {
        stateStack.pop();
      }
      stateStack.pop();
      stateStack.getTop().removeCurrentChild();
    }
  }

  @VisibleForTesting
  class VisitFunctionArgumentsNodeState
      extends VisitReplaceChildrenState<CssValueNode> {

    VisitFunctionArgumentsNodeState(CssFunctionArgumentsNode node) {
      super(node);
    }

    @Override
    public void replaceCurrentBlockChildWith(
        List<CssValueNode> replacementNodes, boolean visitTheReplacementNodes) {
      // If we're replacing the current property with a composite value
      // separated by the space, we really just want to graft those nodes at
      // the current child's location.
      if (replacementNodes.size() == 1
          && replacementNodes.get(0) instanceof CssCompositeValueNode) {
        CssCompositeValueNode compositeValueNode =
            (CssCompositeValueNode) replacementNodes.get(0);
        if (compositeValueNode.getOperator() == Operator.SPACE) {
          replacementNodes = compositeValueNode.getValues();
        }
      }

      super.replaceCurrentBlockChildWith(
          replacementNodes, visitTheReplacementNodes);
    }

    @Override
    public VisitState<CssNode> createFallbackState(CssValueNode child) {
      return new VisitFunctionArgumentNodeState(child);
    }

  }

  @VisibleForTesting
  class VisitFunctionArgumentNodeState extends BaseVisitState<CssNode> {

    private final CssValueNode node;

    VisitFunctionArgumentNodeState(CssValueNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      visitor.enterArgumentNode(node);
      visitor.leaveArgumentNode(node);
    }

    @Override
    public void transitionToNextState() {
      stateStack.pop();
    }
  }

  @VisibleForTesting
  class VisitComponentState extends VisitChildrenOptionalState<CssNode> {

    private final CssComponentNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitComponentState(CssComponentNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterComponent(node);
      } else {
        visitor.leaveComponent(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        stateStack.push(new VisitComponentChildrenState(node.getBlock()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitComponentChildrenState extends VisitReplaceChildrenState<CssNode> {

    VisitComponentChildrenState(CssBlockNode block) {
      super(block);
    }
  }

  @VisibleForTesting
  class VisitUnknownAtRuleState extends VisitChildrenOptionalState<CssNode> {

    private final CssUnknownAtRuleNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitUnknownAtRuleState(CssUnknownAtRuleNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterUnknownAtRule(node);
      } else {
        visitor.leaveUnknownAtRule(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        if (node.getType().hasBlock()) {
          stateStack.push(new VisitUnknownAtRuleBlockState(node.getBlock()));
        }
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitUnknownAtRuleBlockState extends VisitChildrenOptionalState<CssNode> {

    private final CssAbstractBlockNode body;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitUnknownAtRuleBlockState(CssAbstractBlockNode body) {
      this.body = body;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        if (body instanceof CssBlockNode) {
          shouldVisitChildren = visitor.enterBlock((CssBlockNode) body);
        }
      } else {
        if (body instanceof CssBlockNode) {
          visitor.leaveBlock((CssBlockNode) body);
        }
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        if (body instanceof CssBlockNode) {
          stateStack.push(new VisitBlockChildrenState((CssBlockNode) body));
        } else if (body instanceof CssDeclarationBlockNode) {
          stateStack.push(
              new VisitDeclarationBlockState((CssDeclarationBlockNode) body));
        }
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitWebkitKeyframesState extends VisitChildrenOptionalState<CssNode> {

    private final CssWebkitKeyframesNode node;

    private boolean visitedChildren = false;

    private boolean shouldVisitChildren = true;

    VisitWebkitKeyframesState(CssWebkitKeyframesNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        shouldVisitChildren = visitor.enterWebkitKeyframesRule(node);
      } else {
        visitor.leaveWebkitKeyframesRule(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren && shouldVisitChildren) {
        if (node.getType().hasBlock()) {
          stateStack.push(new VisitUnknownAtRuleBlockState(node.getBlock()));
        }
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  @VisibleForTesting
  class VisitMixinDefinitionState extends BaseVisitState<CssNode> {

    private final CssMixinDefinitionNode node;

    private boolean visitedChildren = false;

    VisitMixinDefinitionState(CssMixinDefinitionNode node) {
      this.node = node;
    }

    @Override
    public void doVisit() {
      if (!visitedChildren) {
        visitor.enterMixinDefinition(node);
      } else {
        visitor.leaveMixinDefinition(node);
      }
    }

    @Override
    public void transitionToNextState() {
      if (!visitedChildren) {
        stateStack.push(new VisitDeclarationBlockState(node.getBlock()));
        visitedChildren = true;
      } else {
        stateStack.pop();
      }
    }
  }

  // TODO(oana): Maybe add a generic utility class for Stack than can be used in
  // CssTreeBuilder too.
  @VisibleForTesting
  static class StateStack {
    LinkedList<VisitState<? extends CssNode>> stack = Lists.newLinkedList();

    VisitState<? extends CssNode> getTop() {
      return stack.getFirst();
    }

    void push(VisitState<? extends CssNode> state) {
      stack.addFirst(state);
    }

    void pop() {
      stack.removeFirst();
    }

    void transitionTo(VisitState<? extends CssNode> state) {
      pop();
      push(state);
    }

    int size() {
      return stack.size();
    }

    boolean isEmpty() {
      return stack.isEmpty();
    }
  }

  public DefaultVisitController(CssTree tree, boolean allowMutating) {
    Preconditions.checkNotNull(tree);
    this.tree = tree;
    this.allowMutating = allowMutating;
  }

  public StateStack getStateStack() {
    return stateStack;
  }

  @Override
  public void removeCurrentNode() {
    Preconditions.checkState(allowMutating);
    stateStack.getTop().removeCurrentNodeCalled();
  }

  @Override
  public <T extends CssNode> void replaceCurrentBlockChildWith(
      List<T> replacementNodes,
      boolean visitTheReplacementNodes) {
    Preconditions.checkState(allowMutating);
    @SuppressWarnings("unchecked")
    VisitState<T> stackTop = (VisitState<T>) stateStack.getTop();
    stackTop.replaceCurrentBlockChildWithCalled(
        replacementNodes, visitTheReplacementNodes);
  }

  @Override
  public void startVisit(CssTreeVisitor treeVisitor) {
    Preconditions.checkNotNull(treeVisitor);
    this.visitor = treeVisitor;

    stateStack.push(new RootVisitBeforeChildrenState(tree.getRoot()));

    while (!stateStack.isEmpty()) {
      try {
        stateStack.getTop().doVisit();
        stateStack.getTop().transitionToNextState();
      } catch (StopVisitRequestedException e) {
        // We stop visiting.
        // assert stopVisitCalled;
        break;
      }
    }
  }

  @Override
  public void stopVisit() {
    stopVisitCalled = true;
    stateStack.getTop().stopVisitCalled();
    throw new StopVisitRequestedException();
  }

  /**
   * Factory method to create visit state for a child node. Class of the visit
   * state depends on the child node class.
   *
   * @param <T> child node type
   * @param child node for which the visit state is created
   * @param fallbackStateSource object which will create the fallback visit
   *     state if the factory cannot create the state for a given child node
   * @return created visit state or {@code null} if state cannot be created by
   *     the factory or the fallback factory
   */
  private <T extends CssNode> VisitState<? extends CssNode> createVisitState(
      T child, BaseVisitState<T> fallbackStateSource) {
    VisitState<? extends CssNode> state = createVisitStateInternal(child);
    return (state == null) ?
        fallbackStateSource.createFallbackState(child) : state;
  }

  private VisitState<? extends CssNode> createVisitStateInternal(CssNode child) {
    // VisitUnknownAtRuleBlockState
    if (child instanceof CssMediaRuleNode) {
      return new VisitMediaRuleState((CssMediaRuleNode) child);
    }

    // VisitUnknownAtRuleBlockState
    if (child instanceof CssPageRuleNode) {
      return new VisitPageRuleState((CssPageRuleNode) child);
    }

    // VisitUnknownAtRuleBlockState
    if (child instanceof CssPageSelectorNode) {
      return new VisitPageSelectorState((CssPageSelectorNode) child);
    }

    if (child instanceof CssComponentNode) {
      return new VisitComponentState((CssComponentNode) child);
    }

    // VisitRefinerListState
    if (child instanceof CssRefinerNode) {
      return new VisitRefinerNodeState((CssRefinerNode) child);
    }

    // VisitDeclarationBlockState
    if (child instanceof CssDeclarationNode) {
      return new VisitDeclarationState((CssDeclarationNode) child);
    }

    // VisitDeclarationBlockState
    if (child instanceof CssMixinNode) {
      return new VisitMixinState((CssMixinNode) child);
    }

    // VisitBlockChildrenState
    // VisitComponentChildrenState, VisitUnknownAtRuleChildrenState
    if (child instanceof CssUnknownAtRuleNode) {
      return new VisitUnknownAtRuleState((CssUnknownAtRuleNode) child);
    }

    // VisitUnknownAtRuleBlockState
    if (child instanceof CssWebkitKeyframesNode) {
      return new VisitWebkitKeyframesState((CssWebkitKeyframesNode) child);
    }

    // VisitKeyBlockState
    if (child instanceof CssKeyframeRulesetNode) {
      return new VisitKeyframeRulesetState((CssKeyframeRulesetNode) child);
    }

    // VisitComponentChildrenState, VisitUnknownAtRuleChildrenState
    // VisitBlockChildrenState, VisitConditionalRuleChildrenState
    if (child instanceof CssConditionalBlockNode) {
      return new VisitConditionalBlockState((CssConditionalBlockNode) child);
    }
    if (child instanceof CssRulesetNode) {
      return new VisitRulesetState((CssRulesetNode) child);
    }
    if (child instanceof CssDefinitionNode) {
      return new VisitDefinitionState((CssDefinitionNode) child);
    }

    // VisitDefinitionParametersState, VisitPropertyValueState, VisitFunctionArgumentsNodeState
    if (child instanceof CssFunctionNode) {
      return new VisitFunctionNodeState((CssFunctionNode) child);
    }

    if (child instanceof CssMixinDefinitionNode) {
      return new VisitMixinDefinitionState((CssMixinDefinitionNode) child);
    }

    if (child instanceof CssCompositeValueNode) {
      return new VisitCompositeValueState((CssCompositeValueNode) child);
    }

    return null;
  }
}
