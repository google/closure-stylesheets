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

import com.google.common.css.SourceCodeLocation;

/**
 * A node representing a mixin definition.
 *
 * <p>A mixin definition defines parameterized declarations that can be
 * inserted with a mixin. For example:
 * <pre>
 * {@code @defmixin} gradient(POS, HSL1, HSL2, HSL3, COLOR) {
 *   background-color: COLOR;
 *   background-image:
 *       -webkit-linear-gradient(POS, hsl(HSL1, HSL2, HSL3), COLOR);
 * }
 * </pre>
 *
 */
public class CssMixinDefinitionNode extends CssAtRuleNode {
  private final String name;
  private final CssDeclarationBlockNode declarations;
  private final CssFunctionArgumentsNode arguments;

  public CssMixinDefinitionNode(String name, CssFunctionArgumentsNode arguments,
      CssDeclarationBlockNode declarations, SourceCodeLocation location) {
    super(Type.DEFMIXIN, new CssLiteralNode("defmixin"), declarations);
    this.setSourceCodeLocation(location);
    this.name = name;
    this.declarations = declarations;
    becomeParentForNode(declarations);
    this.arguments = arguments;
  }

  /**
   * Creates a new mixin definition node that is a deep copy of the given node.
   */
  public CssMixinDefinitionNode(CssMixinDefinitionNode node) {
    super(node);
    this.name = node.name;
    this.declarations = node.declarations.deepCopy();
    this.arguments = node.arguments.deepCopy();
  }

  @Override
  public CssNode deepCopy() {
    return new CssMixinDefinitionNode(this);
  }

  public String getDefinitionName() {
    return name;
  }

  public CssFunctionArgumentsNode getArguments() {
    return arguments;
  }

  @Override
  public CssDeclarationBlockNode getBlock() {
    // The type is ensured by the constructor.
    return (CssDeclarationBlockNode) super.getBlock();
  }
}
