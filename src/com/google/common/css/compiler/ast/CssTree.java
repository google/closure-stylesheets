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

import com.google.common.collect.Sets;
import com.google.common.css.SourceCode;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * A mutable abstract syntax tree that corresponds to a CSS input file.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssTree {
  /** The root of the tree. */
  private final CssRootNode root;
  /** The source code corresponding to the tree. */
  private final SourceCode sourceCode;
  /** A reference to the collection of ruleset nodes to remove. */
  private RulesetNodesToRemove rulesetNodesToRemove;

  // TODO(oana): Maybe make this part of some generic information we want to
  // store for the tree.
  public static class RulesetNodesToRemove {
    private final LinkedHashSet<CssRulesetNode> rulesetNodes =
        Sets.newLinkedHashSet();

    public void addRulesetNode(CssRulesetNode node) {
      rulesetNodes.add(node);
    }

    public Collection<CssRulesetNode> getRulesetNodes() {
      return rulesetNodes;
    }
  }

  /**
   * Constructor of a tree.
   *
   * @param sourceCode
   * @param root
   */
  public CssTree(SourceCode sourceCode, CssRootNode root) {
    this.root = root;
    this.sourceCode = sourceCode;
    this.rulesetNodesToRemove = new RulesetNodesToRemove();
  }

  /**
   * Constructor of a tree.
   *
   * @param sourceCode
   */
  public CssTree(SourceCode sourceCode) {
    this(sourceCode, new CssRootNode());
  }

  public CssRootNode getRoot() {
    return root;
  }

  public SourceCode getSourceCode() {
    return sourceCode;
  }

  public RulesetNodesToRemove getRulesetNodesToRemove() {
    return rulesetNodesToRemove;
  }

  public void resetRulesetNodesToRemove() {
    this.rulesetNodesToRemove = new RulesetNodesToRemove();
  }

  public MutatingVisitController getMutatingVisitController() {
    return new DefaultVisitController(this, true /* allowMutating */);
  }

  public VisitController getVisitController() {
    return new DefaultVisitController(this, false /* allowMutating */);
  }

  // TODO(user): Add a method that merges two trees and produces a new one as
  //     a result. This method might belong to a compiler pass.
}
