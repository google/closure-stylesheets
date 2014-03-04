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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssCustomFunctionNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This compiler pass evaluates {@link CssCustomFunctionNode} instances only when they
 * have no references as arguments. Otherwise, it creates a new definition for
 * the function call even if it's already a definition. At the end, the main
 * tree is free of function calls; all the new definitions are collected in a
 * map of {link ConstantDefinitions} per chunk; there are no nested calls.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class ResolveCustomFunctionNodesForChunks<T> extends ResolveCustomFunctionNodes {

  /** The prefix for definitions of calls with references */
  public static final String DEF_PREFIX = "__F";

  private final Function<T, String> nextUniqueSuffix;
  private final Map<T, ConstantDefinitions> constantDefinitions =
      Maps.newHashMap();

  /**
   * Constructs the pass.
   *
   * @param visitController the visit controller
   * @param errorManager the error manager
   * @param functionMap the map from function names to resolve to GSS functions
   * @param allowUnknownFunctions whether to allow unknown function calls,
   *     leaving them as is, instead of reporting an error
   * @param nextUniqueSuffix a function from a chunk to a globally unique suffix
   */
  public ResolveCustomFunctionNodesForChunks(
      MutatingVisitController visitController,
      ErrorManager errorManager,
      Map<String, GssFunction> functionMap,
      boolean allowUnknownFunctions,
      Set<String> allowedNonStandardFunctions,
      Function<T, String> nextUniqueSuffix) {
    super(visitController, errorManager, functionMap, allowUnknownFunctions,
        allowedNonStandardFunctions);
    this.nextUniqueSuffix = nextUniqueSuffix;
  }

  @Override
  protected List<CssValueNode> evaluateFunction(
      CssCustomFunctionNode node,
      GssFunction function,
      List<CssValueNode> arguments,
      ErrorManager errorManager) throws GssFunctionException {

    List<CssValueNode> functionResult;

    if (Iterables.any(arguments, Predicates.instanceOf(CssConstantReferenceNode.class))) {
      functionResult = replaceCallWithReference(node);
    } else {
      functionResult = super.evaluateFunction(
          node, function, arguments, errorManager);
    }
    return functionResult;
  }

  /**
   * Gets the constant definitions for the replaced calls with references.
   * Chunks which have no such calls are not in the map.
   *
   * @return A map of constant definitions per chunk
   */
  public Map<T, ConstantDefinitions> getConstantDefinitions() {
    return constantDefinitions;
  }

  private List<CssValueNode> replaceCallWithReference(CssCustomFunctionNode node) {
    @SuppressWarnings("unchecked")
    T chunk = (T) node.getChunk();

    String defName = DEF_PREFIX + nextUniqueSuffix.apply(chunk);

    CssLiteralNode defLiteral =
        new CssLiteralNode(defName, node.getSourceCodeLocation());
    CssDefinitionNode def =
        new CssDefinitionNode(ImmutableList.<CssValueNode>of(node.deepCopy()), defLiteral);
    CssConstantReferenceNode defRef =
        new CssConstantReferenceNode(defName, node.getSourceCodeLocation());

    addNewDefinition(chunk, def);

    return ImmutableList.<CssValueNode>of(defRef);
  }

  private void addNewDefinition(T chunk, CssDefinitionNode def) {
    Preconditions.checkNotNull(chunk);
    def.setChunk(chunk);

    ConstantDefinitions chunkDefinitions = constantDefinitions.get(chunk);
    if (chunkDefinitions == null) {
      chunkDefinitions = new ConstantDefinitions();
      constantDefinitions.put(chunk, chunkDefinitions);
    }
    chunkDefinitions.addConstantDefinition(def);
  }
}
