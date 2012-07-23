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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.css.SourceCodeLocation;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A CSS node that holds a value of some sort. This is the base class for all
 * the nodes in the abstract syntax tree that have a value.
 *
 * TODO(oana): Maybe de-emphasize the value aspect, allow value to be null, and
 * rename this as CssTermNode.
 *
 */
public abstract class CssValueNode extends CssNode {

  /** The value contained by the node. */
  private String value;

  /** Annotation to show whether this is a default value or not. */
  private boolean isDefault;

  /**
   * Constructor of a node that contains a value.
   *
   * @param value
   * @param sourceCodeLocation
   */
  public CssValueNode(@Nullable String value,
                      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(sourceCodeLocation);
    this.value = value;
    this.isDefault = false;
  }

  /**
   * Constructor of a node that contains a value.
   *
   * @param value
   */
  public CssValueNode(String value) {
    this(value, null);
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssValueNode(CssValueNode node) {
    this(node.getValue(), node.getSourceCodeLocation());
    this.isDefault = node.getIsDefault();
  }

  @Override
  public abstract CssValueNode deepCopy();

  public String getValue() {
    return this.value;
  }

  /**
   * Subclasses should perform additional consistency checks. For example, a
   * boolean expression node will not allow setting this as boolean expression
   * trees are immutable.
   */
  public void setValue(String value) {
    Preconditions.checkNotNull(value);
    this.value = value;
  }

  public void setIsDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public boolean getIsDefault() {
    return isDefault;
  }

  /**
   * Visit just the leaves in the subtrees from the given roots.
   */
  public static Iterable<CssValueNode> leaves(
      final Collection<CssValueNode> roots) {
    return new Iterable<CssValueNode>() {
      public Iterator<CssValueNode> iterator() {
        return new UnmodifiableIterator<CssValueNode>() {
          LinkedList<CssValueNode> q;
          {
            q = Lists.newLinkedList();
            q.addAll(roots);
          }
          List<CssValueNode> expand(CssValueNode n) {
            if (n instanceof CssBooleanExpressionNode) {
              CssBooleanExpressionNode b = (CssBooleanExpressionNode) n;
              return ImmutableList.<CssValueNode>of(b.getLeft(), b.getRight());
            } else if (n instanceof CssCompositeValueNode) {
              CssCompositeValueNode c = (CssCompositeValueNode) n;
              return c.getValues();
            } else {
              return ImmutableList.<CssValueNode>of(n);
            }
          }
          void advance() {
            CssValueNode unexpanded;
            List<CssValueNode> expanded;
            do {
              unexpanded = q.remove();
              expanded = expand(unexpanded);
              q.addAll(0, expanded);
            } while (expanded.size() != 1
                     || !expanded.get(0).equals(unexpanded));
          }
          @Override
          public boolean hasNext() {
            return !q.isEmpty();
          }
          @Override
          public CssValueNode next() {
            advance();
            return q.remove();
          }
        };
      }
    };
  }

  /**
   * Use for debugging only.
   */
  @Override
  public String toString() {
    return getValue();
  }
}
