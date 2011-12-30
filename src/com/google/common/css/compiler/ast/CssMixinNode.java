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
 * A node representing a mixin.
 *
 * <p>Mixins can be used in places where declarations are expected. They are
 * replaced by the corresponding mixin definition.
 * For example: {@code @mixin gradient(top, 0%, 80%, 70%, #BADA55)}
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssMixinNode extends CssAtRuleNode {
  private final String definitionName;
  private final CssFunctionArgumentsNode args;

  public CssMixinNode(String definitionName, CssFunctionArgumentsNode args,
      SourceCodeLocation location) {
    super(Type.MIXIN, new CssLiteralNode("mixin"));
    this.definitionName = definitionName;
    this.args = args;
    super.setSourceCodeLocation(location);
    becomeParentForNode(args);
  }

  /**
   * Creates a new mixin node that is a deep copy of the given node.
   */
  public CssMixinNode(CssMixinNode node) {
    super(node);
    this.definitionName = new String(node.definitionName);
    this.args = node.args.deepCopy();
  }

  @Override
  public CssMixinNode deepCopy() {
    CssMixinNode copy = new CssMixinNode(this);
    copy.setSourceCodeLocation(this.getSourceCodeLocation());
    return copy;
  }

  public String getDefinitionName() {
    return definitionName;
  }

  /**
   * Returns the arguments belonging to this mixin as a node.
   */
  public CssFunctionArgumentsNode getArguments() {
    return args;
  }
}
