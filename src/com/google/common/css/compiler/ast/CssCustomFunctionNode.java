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

import com.google.common.collect.Lists;
import com.google.common.css.SourceCodeLocation;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A node representing a custom GSS function call.
 *
 * @author oana@google.com (Oana Florescu)
 * @author dgajda@google.com (Damian Gajda)
 */
public class CssCustomFunctionNode extends CssFunctionNode
    implements Proxiable<CssCustomFunctionNode> {

  private final String gssFunctionName;
  protected List<CssValueNode> resultNodes;

  /**
   * Constructor of the node.
   *
   * @param gssFunctionName name of the function "called" by this node
   * @param sourceCodeLocation location of this node
   */
  public CssCustomFunctionNode(
      String gssFunctionName,
      SourceCodeLocation sourceCodeLocation) {
    super(Function.CUSTOM, sourceCodeLocation);
    this.gssFunctionName = gssFunctionName;
  }

  /**
   * Copy constructor.
   *
   * @param function the copied custom function node
   */
  public CssCustomFunctionNode(CssCustomFunctionNode function) {
    super(function);
    this.gssFunctionName = function.getFunctionName();
  }

  /**
   * Constructor used by the proxy mechanism, which avoids creation of child
   * nodes.
   *
   * <p>NOTE(dgajda): The signature of this constructor only differs in argument
   * order from the main constructor of this class.
   *
   * @param gssFunctionName name of the function "called" by this node
   * @param sourceCodeLocation location of this node
   */
  private CssCustomFunctionNode(
      @Nullable SourceCodeLocation sourceCodeLocation,
      String gssFunctionName) {
    super(sourceCodeLocation, Function.CUSTOM);
    this.gssFunctionName = gssFunctionName;
  }

  @Override
  public CssCustomFunctionNode deepCopy() {
    return new CssCustomFunctionNode(this);
  }

  @Override
  public String getFunctionName() {
    return gssFunctionName;
  }

  @Override
  public String toString() {
    return gssFunctionName + super.toString();
  }

  /** {@inheritDoc} */
  @Override
  public void setArguments(CssFunctionArgumentsNode arguments) {
    resultNodes = null;
    super.setArguments(arguments);
  }

  /**
   * @return the copy-on-write proxy for this node
   */
  @Override
  public CssCustomFunctionNode createProxy() {
    return new CssCustomFunctionNodeProxy(this);
  }

  /**
   * @return cached result of function value computation
   */
  public List<CssValueNode> getResult() {
    return resultNodes;
  }

  /**
   * Saves the value of function computation for later use.
   *
   * @param functionResult the list of nodes returned by the call to the
   *     {@link GssFunction}
   */
  public void setResult(List<CssValueNode> functionResult) {
    resultNodes = functionResult;
  }

  /**
   * Copy-on-Write proxy for {@link CssCustomFunctionNode} used to avoid copying
   * of function call subtrees in
   * {@link com.google.common.css.compiler.passes.ReplaceConstantReferences}
   * pass. It behaves as a proxy of the original node until a write operation
   * happens. When the first write operation happens, proxy node copies the
   * original node and since then acts as a plain {@link CssCustomFunctionNode}.
   *
   * <p>NOTE(dgajda): Proxied custom function nodes are also replaced by proxies
   * of themselves to disallow changes, which would be visible to all proxy
   * nodes. Only one kind of changes is propagated to proxy nodes - this is
   * setting of function computation results.
   */
  public static class CssCustomFunctionNodeProxy extends CssCustomFunctionNode {

    private CssCustomFunctionNode proxiedNode;

    /**
     * Constructs the node proxy. It uses a special super class constructor
     * which avoids unnecessary initialization of arguments node.
     *
     * @param function the proxied node
     */
    public CssCustomFunctionNodeProxy(CssCustomFunctionNode function) {
      super(function.getSourceCodeLocation(),
          function.getFunctionName());
      this.proxiedNode = function;
      setChunk(function.getChunk());
    }

    /** {@inheritDoc} */
    @Override
    public CssFunctionArgumentsNode getArguments() {
      return proxiedNode != null ?
          proxiedNode.getArguments() : super.getArguments();
    }

    /** {@inheritDoc} */
    @Override
    public void setArguments(CssFunctionArgumentsNode arguments) {
      proxiedNode = null;
      super.setArguments(arguments);
    }

    /** {@inheritDoc} */
    @Override
    public CssCustomFunctionNode createProxy() {
      CssCustomFunctionNode newProxiedNode = proxiedNode != null ?
          proxiedNode : this;
      return new CssCustomFunctionNodeProxy(newProxiedNode);
    }

    /** {@inheritDoc} */
    @Override
    public List<CssValueNode> getResult() {
      if (proxiedNode == null) {
        return super.getResult();
      }

      List<CssValueNode> proxiedNodeResults = proxiedNode.getResult();
      if (proxiedNodeResults == null) {
        return null;
      }

      // Copy results because they are not owned by this node.
      List<CssValueNode> result =
          Lists.newArrayListWithCapacity(proxiedNodeResults.size());
      for (CssValueNode cssValueNode : proxiedNodeResults) {
        result.add(cssValueNode.deepCopy());
      }
      return result;
    }

    /**
     * Sets the results of function computation for this node, can set results
     * on the proxied node. This is harmless because proxied node is exactly
     * the same as proxy node, and function computation results are a
     * derivative of function arguments.
     */
    @Override
    public void setResult(List<CssValueNode> functionResult) {
      if (proxiedNode == null) {
        super.setResult(functionResult);
      } else {
        proxiedNode.setResult(functionResult);
      }
    }
  }
}
