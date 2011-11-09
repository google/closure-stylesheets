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

/**
 * The idea behind the {@link Proxiable} interface is to improve performance of
 * compiler passes by eliminating unnecessary copying of AST subtrees. Passes
 * which perform a lot of copying should use this copy-on-write proxy creation
 * mechanism instead.
 *
 * <p>{@link CssNode} classes which implement the {@link Proxiable} interface
 * create copy-on-write proxy objects. Created proxies reflect the state of the
 * proxied object until a mutation method is called on the proxy. When this
 * happens the proxy copies the original object's data, breaks the link to the
 * original and updates itself with the mutation.
 *
 * <p>Proxy nodes will usually reflect any changes performed on the original
 * node. If this behavior is not desired -- say proxies should act as copies of
 * the original node -- there is a simple solution. Just replace the original
 * node with a proxy and use the original node as the backing node for all
 * proxies. This way the original node is hidden and all modifications go
 * through proxies. The example implementation:
 * {@link CssCustomFunctionNode.CssCustomFunctionNodeProxy#createProxy()}.
 *q
 * @author dgajda@google.com (Damian Gajda)
 *
 * @param <T> the type of the proxy object, it should be the same as of proxied
 *     object
 */
public interface Proxiable<T> {

  /**
   * Creates the proxy object. If the original object is immutable and does not
   * have any children, it can return itself.
   *
   * @return the proxy object
   */
  public T createProxy();

}
