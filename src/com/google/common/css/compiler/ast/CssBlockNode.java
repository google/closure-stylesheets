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

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A {@link CssAbstractBlockNode} that contains only rulesets and @-rules.
 *
 */
public class CssBlockNode extends CssAbstractBlockNode {

  private static final ImmutableList<Class<? extends CssNode>>
      VALID_NODE_CLASSES = ImmutableList.of(
          CssRulesetNode.class, CssAtRuleNode.class,
          CssConditionalBlockNode.class, CssKeyframeRulesetNode.class);

  public CssBlockNode(boolean isEnclosedWithBraces, List<CssNode> children) {
    super(isEnclosedWithBraces, children, null, VALID_NODE_CLASSES);
  }

  public CssBlockNode(boolean isEnclosedWithBraces) {
    super(isEnclosedWithBraces, VALID_NODE_CLASSES);
  }

  public CssBlockNode() {
    this(true /* isEnclosedWithBraces */);
  }

  public CssBlockNode(CssBlockNode node) {
    super(node);
  }

  @Override
  public CssBlockNode deepCopy() {
    return new CssBlockNode(this);
  }
}
