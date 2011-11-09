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

package com.google.common.css.compiler.passes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.css.compiler.ast.CssDefinitionNode;

import java.util.Map;

/**
 * A container for GSS constant definitions.  This is used to calculate what
 * the correct constant value should be based on the last definition.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class ConstantDefinitions {
  private final Map<String, CssDefinitionNode> constants = Maps.newHashMap();

  @VisibleForTesting
  Map<String, CssDefinitionNode> getConstants() {
    return constants;
  }

  /**
   * Returns the last definition of a constant. Callers should not attempt to
   * modify the returned value.
   * 
   * @return definition node or {@code null} if the constant is not defined
   */
  public CssDefinitionNode getConstantDefinition(String constant) {
    return constants.get(constant);
  }

  /**
   * Adds a constant definition to this css tree.
   *
   * Note that a constant may be defined multiple times in the tree. For the
   * compact representation of the tree, all references to a constant will be
   * replaced with the same value: the last one specified in the stylesheet
   * (to ignore the definitions in inactive condition blocks the
   * {@code EliminateConditionalNodes} compiler pass needs to be run first).
   */
  void addConstantDefinition(CssDefinitionNode definition) {
    constants.put(definition.getName().getValue(), definition);
  }

  /**
   * @return the iterable of names of all defined constants
   */
  public Iterable<String> getConstantsNames() {
    return constants.keySet();
  }
}
