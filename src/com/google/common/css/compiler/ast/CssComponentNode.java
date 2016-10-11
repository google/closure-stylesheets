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

import javax.annotation.Nullable;

public class CssComponentNode extends CssAtRuleNode {

  /** Indicates how we want to construct the prefix strings for this component. */
  public enum PrefixStyle {
    /** Means use the name as-is without any transformation. */
    LITERAL,

    /** Means case-convert the name before use. */
    CASE_CONVERT,
  }

  // Sentinel value used to indicate that the node name should be derived
  // from the @provide package name.
  public static final String IMPLICIT_NODE_NAME = "$package";

  private final CssLiteralNode parentName;
  private final PrefixStyle prefixStyle;

  /**
   * Constructor of a component.
   *
   * @param name Name of the component
   * @param parentName Name of the parent component (may be null)
   * @param isAbstract Whether this component is declared abstract
   * @param block The body of the component
   */
  public CssComponentNode(CssLiteralNode name, @Nullable CssLiteralNode parentName,
                          boolean isAbstract, PrefixStyle prefixStyle, CssBlockNode block) {
    super(isAbstract ? CssAtRuleNode.Type.ABSTRACT_COMPONENT : CssAtRuleNode.Type.COMPONENT,
          name,
          block);
    this.parentName = parentName;
    this.prefixStyle = prefixStyle;
  }

  /**
   * Copy constructor.
   *
   * @param node The node to copy
   */
  public CssComponentNode(CssComponentNode node) {
    super(node);
    this.parentName = node.parentName;
    this.prefixStyle = node.prefixStyle;
  }

  @Override
  public CssNode deepCopy() {
    return new CssComponentNode(this);
  }

  public CssLiteralNode getParentName() {
    return parentName;
  }

  public boolean isAbstract() {
    return getType() == CssAtRuleNode.Type.ABSTRACT_COMPONENT;
  }

  @SuppressWarnings("ReferenceEquality")
  public boolean isImplicitlyNamed() {
    // Test against the exact string instance.
    return getName().getValue() == IMPLICIT_NODE_NAME;
  }

  /** Return how the prefix strings should be handled. */
  public PrefixStyle getPrefixStyle() {
    return prefixStyle;
  }

  /**
   * For debugging only.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getType().toString()).append(' ');
    if (!isImplicitlyNamed()) {
      sb.append(getName().getValue()).append(' ');
    }
    if (parentName != null) {
      sb.append("extends ").append(parentName.getValue()).append(' ');
    }
    sb.append('{').append(getBlock().toString()).append('}');
    return sb.toString();
  }

  @Override
  public CssBlockNode getBlock() {
    // This type is ensured by the constructor.
    return (CssBlockNode) super.getBlock();
  }
}
