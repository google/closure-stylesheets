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

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssCustomFunctionNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.ast.Proxiable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This compiler pass replaces {@link CssCustomFunctionNode}s with the
 * list of nodes returned by the GssFunction.
 *
 */
public class ResolveCustomFunctionNodes extends DefaultTreeVisitor
    implements CssCompilerPass {

  private final MutatingVisitController visitController;
  protected Map<String, GssFunction> functionMap;
  private final ErrorManager errorManager;
  private final boolean allowUnknownFunctions;
  private final Set<String> allowedNonStandardFunctions;

  /**
   * Constructs the pass.
   *
   * @param visitController The visit controller
   * @param errorManager The error manager
   * @param functionMap The map from function names to resolve to GSS functions
   */
  public ResolveCustomFunctionNodes(MutatingVisitController visitController,
                                    ErrorManager errorManager,
                                    Map<String, GssFunction> functionMap) {
    this(visitController, errorManager, functionMap,
        false /* allowUnknownFunctions */);
  }

  /**
   * Constructs the pass.
   *
   * @param visitController The visit controller
   * @param errorManager The error manager
   * @param functionMap The map from function names to resolve to GSS functions
   * @param allowUnknownFunctions Whether to allow unknown function calls,
   *     leaving them as is, instead of reporting an error
   */
  public ResolveCustomFunctionNodes(MutatingVisitController visitController,
                                    ErrorManager errorManager,
                                    Map<String, GssFunction> functionMap,
                                    boolean allowUnknownFunctions) {
    this(visitController, errorManager, functionMap, allowUnknownFunctions,
        ImmutableSet.<String>of() /* allowedNonStandardFunctions */);
  }

  /**
   * Constructs the pass.
   *
   * @param visitController The visit controller
   * @param errorManager The error manager
   * @param functionMap The map from function names to resolve to GSS functions
   * @param allowUnknownFunctions Whether to allow unknown function calls,
   *     leaving them as is, instead of reporting an error
   * @param allowedNonStandardFunctions functions that should not yield a
   *     warning if they appear in a stylesheet
   */
  public ResolveCustomFunctionNodes(MutatingVisitController visitController,
                                    ErrorManager errorManager,
                                    Map<String, GssFunction> functionMap,
                                    boolean allowUnknownFunctions,
                                    Set<String> allowedNonStandardFunctions) {
    Preconditions.checkNotNull(functionMap);
    this.visitController = visitController;
    this.errorManager = errorManager;
    this.functionMap = functionMap;
    this.allowUnknownFunctions = allowUnknownFunctions;
    this.allowedNonStandardFunctions = ImmutableSet.copyOf(
        allowedNonStandardFunctions);
  }

  @Override
  public void leaveFunctionNode(CssFunctionNode functionNode) {
    if (!(functionNode instanceof Proxiable<?>)) {
      return;
    }

    CssCustomFunctionNode node = (CssCustomFunctionNode) functionNode;

    List<CssValueNode> functionResult = node.getResult();
    if (functionResult == null) {
      // Look up the function's name in the map.
      String functionName = node.getFunctionName();
      GssFunction function = functionMap.get(functionName);
      if (function == null) {
        if (!allowUnknownFunctions && !allowedNonStandardFunctions.contains(
            functionName)) {
          errorManager.report(new GssError(
              String.format("Unknown function \"%s\"", functionName),
              node.getSourceCodeLocation()));
          visitController.removeCurrentNode();
        }
        return;
      }

      // Removes the commas from the arguments and groups arguments separated
      // by commas, e.g.:
      // verticalGradient(#fff, #f2f2f2, url(foo) left top)
      // has 7 initial arguments (2 commas, 5 words).
      // After the cleanup we end up with the 3 arguments separated by commas.
      // TODO(user): Clean this up once we get rid of the comma-as-argument hack.
      List<CssValueNode> arguments = Lists.newArrayList();
      CssValueNode lastArg = null;
      for (CssValueNode value : node.getArguments().childIterable()) {
        if (value.getValue() != null && value.getValue().equals(",")) {
          lastArg = null;
        } else {
          if (lastArg == null) {
            arguments.add(value);
            lastArg = value;
          } else {
            if (lastArg.getValue() == null) {
              arguments.remove(lastArg);
              lastArg = new CssLiteralNode(lastArg.toString() + " "
                  + value.toString());
              arguments.add(lastArg);
            } else {
              lastArg.setValue(lastArg.getValue() + " " + value.toString());
            }
          }
        }
      }

      Integer expArgNumber = function.getNumExpectedArguments();
      int argNumber = arguments.size();
      if (expArgNumber != null && expArgNumber.intValue() != argNumber) {
        errorManager.report(new GssError("Function expects " + expArgNumber
            + " arguments but has " + argNumber,
            node.getSourceCodeLocation()));
        visitController.removeCurrentNode();
        return;
      }

      try {
        functionResult = evaluateFunction(node, function, arguments, errorManager);
      } catch (GssFunctionException e) {
        visitController.removeCurrentNode();
        return;
      } catch (RuntimeException e) {
        errorManager.report(
            new GssError(
                Throwables.getStackTraceAsString(e),
                node.getSourceCodeLocation()));
        visitController.removeCurrentNode();
        return;
      }
    }
    visitController.replaceCurrentBlockChildWith(functionResult, false);
  }

  /**
   * Evaluates the given function node.
   *
   * <p>Subclasses may sublcass this method to change the evaluation
   * process in some circumstances.
   *
   * @param node the function node to evaluate
   * @param function the GSS function matching this node
   * @param arguments the arguments of this node
   * @param errorManager the error manager passed into the GSS function call
   * @return the result of the evaluation as a list of value nodes
   * @throws GssFunctionException if the function call is invalid
   * @throws RuntimeException if the function call fails to complete
   */
  protected List<CssValueNode> evaluateFunction(
      CssCustomFunctionNode node,
      GssFunction function,
      List<CssValueNode> arguments,
      ErrorManager errorManager) throws GssFunctionException {

    List<CssValueNode> functionResult =
        function.getCallResultNodes(arguments, errorManager);
    node.setResult(functionResult);
    return functionResult;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
