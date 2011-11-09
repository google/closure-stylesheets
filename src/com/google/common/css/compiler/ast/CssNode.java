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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.css.SourceCodeLocation;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A node of the abstract syntax tree.
 *
 * TODO(oana): Declare this class as:
 * public abstract class CssNode<T extends CssNode> {...}
 *
 */
public abstract class CssNode {
  /** The parent of this node. */
  private CssNode parent;
  /** The source code corresponding to this node. */
  private SourceCodeLocation sourceCodeLocation;
  /** List of comments/annotations. */
  private List<CssCommentNode> comments;
  /** Annotation of a node to show whether it should be flipped. */
  private boolean shouldBeFlipped = true;

  /**
   * Constructor of a node.
   */
  public CssNode() {
    this(null, null);
  }

  /**
   * Constructor of a node.
   *
   * @param sourceCodeLocation
   */
  public CssNode(@Nullable SourceCodeLocation sourceCodeLocation) {
    this(null, sourceCodeLocation);
  }

  /**
   * Constructor of a node.
   *
   * @param parent
   */
  public CssNode(@Nullable CssNode parent) {
    this(parent, null);
  }

  /**
   * Constructor of a node.
   *
   * @param parent
   * @param sourceCodeLocation
   */
  public CssNode(@Nullable CssNode parent,
          @Nullable SourceCodeLocation sourceCodeLocation) {
    this(parent, null, sourceCodeLocation);
  }

  /**
   * Constructor of a node.
   *
   * @param parent
   * @param comments
   * @param sourceCodeLocation
   */
  public CssNode(@Nullable CssNode parent,
          @Nullable List<CssCommentNode> comments,
          @Nullable SourceCodeLocation sourceCodeLocation) {
    this.parent = parent;
    this.sourceCodeLocation = sourceCodeLocation;
    if (comments == null) {
      this.comments = Lists.newArrayListWithExpectedSize(0);
    } else {
      this.comments = Lists.newArrayList(comments);
    }
    becomeParentForNodes(this.comments);
  }

  // TODO(oana): Declare this method as public abstract T deepCopy() to ensure
  // that the return type is always correct.
  public abstract CssNode deepCopy();

  public CssNode getParent() {
    return parent;
  }

  void setParent(CssNode parent) {
    this.parent = parent;
  }

  public SourceCodeLocation getSourceCodeLocation() {
    return sourceCodeLocation;
  }

  public void setSourceCodeLocation(
      @Nullable SourceCodeLocation sourceCodeLocation) {
    this.sourceCodeLocation = sourceCodeLocation;
  }

  /**
   * Removes the relation between this node and its parent.
   */
  void removeParent() {
    this.parent = null;
  }

  /**
   * Removes the parent-child relation between this node and a child.
   *
   * @param child
   */
  void removeAsParentOfNode(CssNode child) {
    if (child == null) {
      return;
    }
    Preconditions.checkArgument(child.getParent() == this);
    child.removeParent();
  }

  /**
   * Removes the parent-children relations between this node and a list of
   * children.
   *
   * @param children
   */
  void removeAsParentOfNodes(List<? extends CssNode> children) {
    if (children == null) {
      return;
    }
    for (CssNode child : children) {
      removeAsParentOfNode(child);
    }
  }

  /**
   * Makes this node the parent of a child.
   *
   * @param child
   */
  final void becomeParentForNode(@Nullable CssNode child) {
    if (child == null) {
      return;
    }
    child.setParent(this);
  }

  /**
   * Makes this node the parent of a list of children.
   *
   * @param children
   */
  final void becomeParentForNodes(List<? extends CssNode> children) {
    Preconditions.checkNotNull(children);
    for (CssNode child : children) {
      becomeParentForNode(child);
    }
  }

  public void appendComment(CssCommentNode comment) {
    comments.add(comment);
    becomeParentForNode(comment);
  }

  public void setComments(List<CssCommentNode> comments) {
    Preconditions.checkNotNull(this.comments);
    removeAsParentOfNodes(this.comments);
    this.comments = Lists.newArrayList(comments);
    becomeParentForNodes(this.comments);
  }

  public List<CssCommentNode> getComments() {
    return comments;
  }

  /**
   * Returns whether one of the comments attached to this node exactly matches
   * the given string.
   */
  public boolean hasComment(String comment) {
    for (CssCommentNode c : comments) {
      if (c.getValue().equals(comment)) {
        return true;
      }
    }
    return false;
  }

  public boolean getShouldBeFlipped() {
    return shouldBeFlipped;
  }

  /**
   * Marks a node whether it should be flipped or not.
   *
   * @param shouldBeFlipped Whether the node should be flipped or not.
   */
  public void setShouldBeFlipped(boolean shouldBeFlipped) {
    this.shouldBeFlipped = shouldBeFlipped;
  }

  <T extends CssNode> List<T> newListCopy(List<T> list) {
    return Lists.newArrayList(list);
  }

  <T extends CssNode> List<T> copyToList(List<T> list) {
    return Lists.newArrayList(list);
  }

  /**
   * This is the default implementation of {@code equals()}. The method is
   * marked {@code final} so that nobody changes the default behavior.
   *
   * <p>The rationale for not allowing redefinition of equals is that different
   * compiler passes and different optimizations have their own notion of
   * equality. In order to avoid confusion and errors everybody that needs a
   * custom definition of equality and custom hash codes should use customized
   * collections with custom comparators or hash code providers.
   *
   * <p>In addition, this class should not implement {@link Comparable}. Use
   * {@link java.util.Comparator} instead.
   *
   * @see Object#equals(Object)
   */
  @Override
  public final boolean equals(@Nullable Object obj) {
    return super.equals(obj);
  }

  /**
   * This is the default implementation of {@code hashCode()}. The method is
   * marked {@code final} so that nobody changes the default behavior.
   *
   * @see #equals(Object)
   * @see Object#hashCode()
   */
  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  /**
   * This is the default implementation of {@code toString()}.
   *
   * <p>Overriding this method should only be done for debugging or logging
   * purposes, not for the actual functionality of the compiler. If a string
   * representation of a tree is needed, define a Visitor that builds the
   * desired representation.
   *
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return super.toString();
  }
}
