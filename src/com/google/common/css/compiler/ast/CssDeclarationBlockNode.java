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

/**
 * A {@link CssAbstractBlockNode} that contains only declarations and @-rules.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssDeclarationBlockNode extends CssAbstractBlockNode {

  private static final ImmutableList<Class<? extends CssNode>>
      VALID_NODE_CLASSES = ImmutableList.of(
          CssDeclarationNode.class, CssAtRuleNode.class);

  public CssDeclarationBlockNode() {
    super(true, VALID_NODE_CLASSES);
  }

  public CssDeclarationBlockNode(CssDeclarationBlockNode node) {
    super(node);
  }

  @Override
  public CssDeclarationBlockNode deepCopy() {
    return new CssDeclarationBlockNode(this);
  }
}
