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

import java.util.List;

import javax.annotation.Nullable;

/**
 * A node corresponding to an at-rule such as {@code @if} or {@code @media}.
 *
 */
public abstract class CssAtRuleNode extends CssNodesListNode<CssValueNode> {
  private final Type type;
  private CssLiteralNode name;
  private CssAbstractBlockNode block;

  /**
   * The Types of at-rules we (should/could) know about.
   */
  // TODO(user): Add support for all of these. Support generally means
  //     overriding this class and adding constraints, both during parsing and
  //     during tree construction. We can also add methods and types in order
  //     to have richer information on specific rules. Until a specific rule is
  //     supported, it can be represented using CssUnknownAtRuleNode.
  public enum Type {
    CHARSET("charset", false /* hasBlock */),
    IMPORT("import", false /* hasBlock */),
    NAMESPACE("namespace", false /* hasBlock */),

    MEDIA("media"),
    PAGE("page"),
    FONT_FACE("font-face"),

    // The recognized page margin box types in CSS 3.
    // See http://www.w3.org/TR/css3-page/#margin-boxes
    TOP_LEFT_CORNER("top-left-corner"),
    TOP_LEFT("top-left"),
    TOP_CENTER("top-center"),
    TOP_RIGHT("top-right"),
    TOP_RIGHT_CORNER("top-right-corner"),
    LEFT_TOP("left-top"),
    LEFT_MIDDLE("left-middle"),
    LEFT_BOTTOM("left-bottom"),
    RIGHT_TOP("right-top"),
    RIGHT_MIDDLE("right-middle"),
    RIGHT_BOTTOM("right-bottom"),
    BOTTOM_LEFT_CORNER("bottom-left-corner"),
    BOTTOM_LEFT("bottom-left"),
    BOTTOM_CENTER("bottom-center"),
    BOTTOM_RIGHT("bottom-right"),
    BOTTOM_RIGHT_CORNER("bottom-right-corner"),

    // Non-standard extension.
    // See http://disruptive-innovations.com/zoo/cssvariables/
    VARIABLES("variables"),

    // The Google extensions to CSS, referred to as GSS.
    DEF("def", false /* hasBlock */),
    IF("if"),
    ELSEIF("elseif"),
    ELSE("else"),

    // Loops extension.
    FOR("for"),

    // GSS components.
    ABSTRACT_COMPONENT("abstract_component"),
    COMPONENT("component"),

    // GSS mixins
    DEFMIXIN("defmixin"),
    MIXIN("mixin", false /* hasBlock */),

    // GSS dependency management
    PROVIDE("provide", false /* hasBlock */),
    REQUIRE("require", false /* hasBlock */),

    // An at rule of which we know nothing about. We can safely assume its
    // canonical name is its "name" literal converted to lower case and with no
    // escapes.
    UNKNOWN(null, false /* hasBlock */),
    // Same as above, but we allow it to have a block node.
    UNKNOWN_BLOCK(null, true /* hasBlock */);

    private final String canonicalName;
    private final boolean hasBlock;

    /**
     * Most at rules require a block.
     */
    private Type(@Nullable String canonicalName) {
      this(canonicalName, true /* hasBlock */);
    }

    private Type(@Nullable String canonicalName, boolean hasBlock) {
      this.canonicalName = canonicalName;
      this.hasBlock = hasBlock;
    }

    public String getCanonicalName() {
      return canonicalName;
    }

    public boolean hasBlock() {
      return hasBlock;
    }

    public boolean isConditional() {
      return this == IF || this == ELSEIF || this == ELSE;
    }

    /**
     * For debugging only.
     */
    @Override
    public String toString() {
      return "@" + getCanonicalName();
    }
  }

  /**
   * Constructor for an AtRule node.
   *
   * @param type Type of rule
   * @param name Name of rule
   * @param block The block child of this rule
   * @param comments The comments associated to this rule
   */
  CssAtRuleNode(Type type, CssLiteralNode name,
      @Nullable CssAbstractBlockNode block,
      @Nullable List<CssCommentNode> comments) {
    super(false, comments);
    Preconditions.checkNotNull(type);
    Preconditions.checkNotNull(name);
    Preconditions.checkArgument(type.hasBlock() || block == null);
    this.type = type;
    this.name = name;
    this.block = block;
    // TODO(user): We could check that the canonicalized name matches the
    //     type that was passed. The same for setName().
    becomeParentForNode(this.name);
    becomeParentForNode(this.block);
  }

  /**
   * Constructor for an AtRule node.
   *
   * @param type Type of rule
   * @param name Name of rule
   * @param block The block child of this rule
   */
  CssAtRuleNode(Type type, CssLiteralNode name,
      @Nullable CssAbstractBlockNode block) {
    this(type, name, block, null);
  }

  /**
   * Constructor for an AtRule node.
   *
   * @param type Type of rule
   * @param name Name of rule
   */
  CssAtRuleNode(Type type, CssLiteralNode name) {
    this(type, name, type.hasBlock() ? new CssBlockNode() : null);
  }

  /**
   * Constructor for an AtRule node.
   *
   * @param type Type of rule
   * @param name Name of rule
   * @param comments The comments associated to this rule
   */
  CssAtRuleNode(Type type, CssLiteralNode name, List<CssCommentNode> comments) {
    this(type, name, type.hasBlock() ? new CssBlockNode() : null, comments);
  }

  CssAtRuleNode(CssAtRuleNode node) {
    this(node.getType(), node.getName().deepCopy(),
        node.getBlock() != null ? node.getBlock().deepCopy() : null,
        copyNodes(node.getComments()));
    setParameters(copyNodes(node.getParameters()));
    setSourceCodeLocation(node.getSourceCodeLocation());
  }

  public static <N extends CssNode> List<N> copyNodes(List<N> nodes) {
    List<N> list = Lists.newArrayList();
    for (N node : nodes) {
      @SuppressWarnings("unchecked")
      N copy = (N) node.deepCopy();
      list.add(copy);
    }
    return list;
  }

  public Type getType() {
    return type;
  }

  public CssLiteralNode getName() {
    return name;
  }

  void setName(CssLiteralNode name) {
    Preconditions.checkNotNull(name);
    removeAsParentOfNode(this.name);
    this.name = name;
    becomeParentForNode(this.name);
  }

  public List<CssValueNode> getParameters() {
    return this.children;
  }

  public int getParametersCount() {
    return numChildren();
  }

  public void setParameters(List<CssValueNode> parameters) {
    setChildren(parameters);
  }

  /**
   * Subclasses should override {@code getBlock} to return a more specific
   * subclass of {@link CssAbstractBlockNode}.
   */
  protected CssAbstractBlockNode getBlock() {
    return block;
  }

  void setBlock(@Nullable CssAbstractBlockNode block) {
    Preconditions.checkArgument(block.isEnclosedWithBraces(),
        "Only blocks that are enclosed with braces are valid for @-rules.");
    removeAsParentOfNode(this.block);
    this.block = block;
    becomeParentForNode(this.block);
  }

  /**
   * For debugging only.
   */
  @Override
  public String toString() {
    String output = "";
    if (!getComments().isEmpty()) {
      output = getComments().toString();
    }
    output += getType().toString() + getParameters().toString();

    if (getBlock() != null) {
      output += "{" + getBlock().toString() + "}";
    }

    return output;
  }
}
