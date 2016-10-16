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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.google.common.css.SourceCodeLocation;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Represents a list of nodes. This is meant to represent a succession of
 * statements that logically group together as one node in the tree. Examples
 * include:
 * <ul>
 * <li>all the statements in a stylesheet, except for the ones that can only
 * appear on top ({@code charset}, {@code import}, etc.)
 * <li>all the declarations inside a ruleset, a font face or a page rule
 * <li>all the nodes that form a chain of if-elseif-else rules
 * <li>the block that belongs to some of the at rules, such as {@code media}
 * </ul>
 *
 * @param <T> the list is restricted to nodes of this type
 */
public abstract class CssNodesListNode<T extends CssNode> extends CssNode {
  protected List<T> children = Lists.newArrayList();
  private final boolean isEnclosedWithBraces;

  /**
   * Constructor of a list of nodes alike.
   *
   * @param isEnclosedWithBraces
   */
  public CssNodesListNode(boolean isEnclosedWithBraces) {
    this(isEnclosedWithBraces, null);
  }

  /**
   * Constructor of a list of nodes alike.
   *
   * @param isEnclosedWithBraces
   * @param comments
   */
  public CssNodesListNode(boolean isEnclosedWithBraces,
                   @Nullable List<CssCommentNode> comments) {
    super(null, comments, null);
    this.isEnclosedWithBraces = isEnclosedWithBraces;
  }

  /**
   * Constructor of a list of nodes alike.
   *
   * @param isEnclosedWithBraces
   * @param comments
   * @param childrenList list of children
   */
  public CssNodesListNode(boolean isEnclosedWithBraces,
                          List<T> childrenList,
                          @Nullable List<CssCommentNode> comments) {
    super(null, comments, null);
    for (T child : childrenList) {
      @SuppressWarnings("unchecked")
      T childCopy = (T) child.deepCopy();
      addChildToBack(childCopy);
    }

    this.isEnclosedWithBraces = isEnclosedWithBraces;
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssNodesListNode(CssNodesListNode<? extends CssNode> node) {
    super(
        node.getParent(),
        node.getComments(),
        node.getSourceCodeLocation());
    this.isEnclosedWithBraces = node.isEnclosedWithBraces;

    for (CssNode child : node.childIterable()) {
      @SuppressWarnings("unchecked")
      T childCopy = (T) child.deepCopy();
      addChildToBack(childCopy);
    }
  }

  public List<T> getChildren() {
    return Collections.unmodifiableList(children);
  }

  public Iterator<T> getChildIterator() {
    return children.iterator();
  }

  public Iterable<T> childIterable() {
    return Iterables.unmodifiableIterable(children);
  }

  void setChildren(List<T> children) {
    Preconditions.checkArgument(!children.contains(null));
    removeAsParentOfNodes(this.children);
    this.children = copyToList(children);
    becomeParentForNodes(this.children);
  }

  T removeChildAt(int index) {
    Preconditions.checkState(index >= 0 && index < children.size());
    T child = children.get(index);
    removeAsParentOfNode(child);
    children.remove(index);
    return child;
  }

  // TODO(dgajda): Make it package private once we can walk the tree backwards
  //     and ReplaceConstantReferences won't need to use this method directly.
  public void replaceChildAt(int index, List<? extends T> newChildren) {
    Preconditions.checkState(index >= 0 && index < children.size());
    Preconditions.checkArgument(!newChildren.contains(null));
    removeChildAt(index);
    children.addAll(index, newChildren);
    becomeParentForNodes(newChildren);
  }

  public T getChildAt(int index) {
    Preconditions.checkState(index >= 0 && index < children.size());
    return children.get(index);
  }

  public int numChildren() {
    return children.size();
  }

  T removeLastChild() {
    return removeChildAt(children.size() - 1);
  }

  public T getLastChild() {
    return children.get(children.size() - 1);
  }

  public void addChildToBack(T child) {
    Preconditions.checkNotNull(child);
    this.children.add(child);
    becomeParentForNode(child);
  }

  public boolean isEmpty() {
    return children.isEmpty();
  }

  public boolean isEnclosedWithBraces() {
    return isEnclosedWithBraces;
  }

  /**
   * For debugging only.
   */
  @Override
  public String toString() {
    StringBuffer output = new StringBuffer();
    if (!getComments().isEmpty()) {
      output.append("[");
      output.append(getComments().toString());
      output.append(children.toString());
      output.append("]");
    } else {
      output.append(children.toString());
    }

    return output.toString();
  }

  @Override
  public SourceCodeLocation getSourceCodeLocation() {
    SourceCodeLocation location = super.getSourceCodeLocation();
    if (location == null) {
      location = SourceCodeLocation.merge(children);
    }
    return location;
  }
}
