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

import com.google.common.collect.ImmutableSet;

/**
 * An unknown or generic at-rule node.
 *
 * <p>The {@link GssParser} only creates unknown at-rule nodes. Later passes
 * then converts them into more specific at-rule nodes, as needed. It's OK to
 * leave a known at-rule type as unknown if it doesn't require further
 * processing.
 *
 */
public class CssUnknownAtRuleNode extends CssAtRuleNode {

  /**
   * Names of at-rules that don't need special processing, and so can be left
   * as unknown for convenience.
   */
  private static final ImmutableSet<String> OK_WITHOUT_PROCESSING =
      ImmutableSet.of("media", "page");

  public CssUnknownAtRuleNode(CssLiteralNode name, boolean hasBlock) {
    super(hasBlock ? Type.UNKNOWN_BLOCK : Type.UNKNOWN, name,
        hasBlock ? new CssBlockNode() : null);
  }

  public CssUnknownAtRuleNode(CssUnknownAtRuleNode node) {
    super(node);
  }

  @Override
  public CssUnknownAtRuleNode deepCopy() {
    return new CssUnknownAtRuleNode(this);
  }

  public boolean isOkWithoutProcessing() {
   return OK_WITHOUT_PROCESSING.contains(getName().getValue());
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
    output += "@" + getName().toString() + getParameters().toString();

    if (getBlock() != null) {
      output += "{" + getBlock().toString() + "}";
    }

    return output;
  }

  /**
   * Returns an implementation of {@code CssAbstractBlockNode} or null.
   */
  @Override
  public CssAbstractBlockNode getBlock() {
    return super.getBlock();
  }
}
