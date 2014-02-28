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


import com.google.common.css.SourceCodeLocation;

import java.util.List;

/**
 * A node representing a GSS definition.
 * For example: <code>@def BASE_BG_COLOR white;</color>
 *
 * @author oana@google.com (Oana Florescu)
 *
 */
public class CssDefinitionNode extends CssAtRuleNode implements ChunkAware {

  private Object chunk;

  /**
   * Constructor of a definition.
   */
  public CssDefinitionNode(CssLiteralNode name, List<CssCommentNode> comments) {
    super(CssAtRuleNode.Type.DEF, name, comments);
  }

  /**
   * Constructor of a definition.
   */
  public CssDefinitionNode(CssLiteralNode name) {
    super(CssAtRuleNode.Type.DEF, name);
  }

  /**
   * Constructor of a definition.
   */
  public CssDefinitionNode(List<CssValueNode> parameters, CssLiteralNode name) {
    super(CssAtRuleNode.Type.DEF, name);
    setParameters(parameters);
  }

  /**
   * Constructor of a definition.
   */
  public CssDefinitionNode(List<CssValueNode> parameters, CssLiteralNode name,
                           SourceCodeLocation sourceCodeLocation) {
    super(CssAtRuleNode.Type.DEF, name);
    setParameters(parameters);
    setSourceCodeLocation(sourceCodeLocation);
  }

  /**
   * Constructor of a definition.
   */
  public CssDefinitionNode(List<CssValueNode> parameters, CssLiteralNode name,
                           List<CssCommentNode> comments) {
    super(CssAtRuleNode.Type.DEF, name, comments);
    setParameters(parameters);
  }

  /**
   * Constructor of a definition.
   */
  public CssDefinitionNode(List<CssValueNode> parameters,
                           CssLiteralNode name, List<CssCommentNode> comments,
                           SourceCodeLocation sourceCodeLocation) {
    this(parameters, name, comments);
    setSourceCodeLocation(sourceCodeLocation);
  }

  /**
   * Copy constructor.
   * 
   * @param node
   */
  public CssDefinitionNode(CssDefinitionNode node) {
    super(node);
    this.chunk = node.getChunk();
  }

  @Override
  public CssDefinitionNode deepCopy() {
    return new CssDefinitionNode(this);
  }
  
  /**
   * For debugging only.
   */
  @Override
  public String toString() {
    return getType().toString() + " " + getName().toString() + " "
        + getParameters().toString();
  }

  @Override
  public Object getChunk() {
    return chunk;
  }

  @Override
  public void setChunk(Object chunk) {
    this.chunk = chunk;
  }
}
