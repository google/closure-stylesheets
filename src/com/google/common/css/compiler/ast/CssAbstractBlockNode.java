/*
 * Copyright 2011 Google Inc.
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
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

/**
 * An abstract representation of a list of nodes that are enclosed inside
 * braces.
 *
 * <p>Subclasses have to define a list of classes that are valid children.
 * The list of valid classes can contain the classes itself or superclasses
 * because all instances of the provided classes are accepted as valid.
 * If invalid children are added, an {@link IllegalStateException} is thrown.
 *
 */
public abstract class CssAbstractBlockNode
    extends CssNodesListNode<CssNode> {
  private final ImmutableList<Class<? extends CssNode>> validNodeClasses;

  public CssAbstractBlockNode(boolean isEnclosedWithBraces,
      List<Class<? extends CssNode>> validNodeClasses) {
    super(isEnclosedWithBraces, null /* comments */);
    this.validNodeClasses = ImmutableList.copyOf(validNodeClasses);
  }

  public CssAbstractBlockNode(boolean isEnclosedWithBraces,
      List<CssNode> childrenList, @Nullable List<CssCommentNode> comments,
      ImmutableList<Class<? extends CssNode>> validSuperclasses) {
    super(isEnclosedWithBraces, comments);
    // The valid superclasses have to be set before children are added.
    this.validNodeClasses = validSuperclasses;
    setChildren(childrenList);
  }

  /**
   * Copy constructor.
   */
  public CssAbstractBlockNode(CssAbstractBlockNode node) {
    super(node.isEnclosedWithBraces(), node.getComments());
    this.setParent(node.getParent());
    this.setSourceCodeLocation(node.getSourceCodeLocation());
    // The valid superclasses have to be set before children are added.
    this.validNodeClasses = node.validNodeClasses;
    for (CssNode child : node.childIterable()) {
      CssNode childCopy = child.deepCopy();
      addChildToBack(childCopy);
    }
  }

  @Override
  public abstract CssAbstractBlockNode deepCopy();

  @Override
  public void addChildToBack(CssNode child) {
    checkChild(child);
    super.addChildToBack(child);
  }

  @Override
  void setChildren(List<CssNode> children) {
    checkChildren(children);
    super.setChildren(children);
  }

  @Override
  public void replaceChildAt(int index, List<? extends CssNode> newChildren) {
    checkChildren(newChildren);
    super.replaceChildAt(index, newChildren);
  }

  private void checkChildren(List<? extends CssNode> children) {
    for (CssNode child : children) {
      checkChild(child);
    }
  }

  /**
   * Ensures that only valid children are added. This method is called for
   * every child that is added.
   */
  private void checkChild(CssNode node) {
    for (Class<? extends CssNode> allowedClass : validNodeClasses) {
      if (allowedClass.isInstance(node)) {
        return;
      }
    }
    Preconditions.checkState(false,
        "Trying to add an instance of the class %s (\"%s\"), which is not a "
        + "valid child for a block of class %s.",
        node.getClass().getName(), node.toString(), this.getClass().getName());
  }
}
