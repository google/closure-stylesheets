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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.css.compiler.ast.CssDefinitionNode;

import java.util.List;
import java.util.Map;

/**
 * A container for GSS constant definitions, since a constant could be
 * defined multiple times in the tree, this class has two set of interfaces
 * that can be used to get either all definitions of a constant or only last
 * definition of a constant.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class ConstantDefinitions {
  private final Map<String, CssDefinitionNode> constants = Maps.newHashMap();

  private final ListMultimap<String, CssDefinitionNode> constantsMultimap =
      LinkedListMultimap.create();

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
   * Returns all definitions of a constant.
   *
   * @return collection of definition node or empty collection if the
   * constant is not defined
   */
  public List<CssDefinitionNode> getConstantDefinitions(String constant) {
    return constantsMultimap.get(constant);
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
  public void addConstantDefinition(CssDefinitionNode definition) {
    constants.put(definition.getName().getValue(), definition);
    constantsMultimap.put(definition.getName().getValue(), definition);
  }

  /**
   * @return the iterable of names of all defined constants
   */
  public Iterable<String> getConstantsNames() {
    return constants.keySet();
  }
}
