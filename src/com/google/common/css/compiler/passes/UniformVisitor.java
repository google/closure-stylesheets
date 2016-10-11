/*
 * Copyright 2012 Google Inc.
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

import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.reflect.Reflection;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/** A visitor with a pair of operations (enter and leave) that apply to every node type. */
public interface UniformVisitor {

  void enter(CssNode node);

  void leave(CssNode node);

  /** Adapter methods that transform {@code UniformVisitor}s into {@code CssTreeVisitor}s. */
  final class Adapters {

    private Adapters() {}

    /** Transforms the given {@code UniformVisitor} into a {@code CssTreeVisitor}. */
    public static CssTreeVisitor asVisitor(final UniformVisitor visitor) {
      return Reflection.newProxy(
          CssTreeVisitor.class,
          new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
              // Allow methods from Object, like toString().
              if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(visitor, args);
              }

              CssNode node = (CssNode) args[0];
              if (method.getName().startsWith("enter")) {
                visitor.enter(node);
                return true; // Always visit children
              } else if (method.getName().startsWith("leave")) {
                visitor.leave(node);
                return null; // All leave* methods are void
              }
              throw new IllegalStateException("Unexpected method '" + method + "' called");
            }
          });
    }

    /**
     * Transforms the given visitor into a {@code CssTreeVisitor} that calls the {@code
     * UniformVisitor}'s {@code enter} method before each {@code enter*} method and its {@code
     * leave} method after each {@code leave*} method.
     */
    public static <T extends UniformVisitor & CssTreeVisitor> CssTreeVisitor asCombinedVisitor(
        final T visitor) {
      return Reflection.newProxy(
          CssTreeVisitor.class,
          new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
              // Allow methods from Object, like toString().
              if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(visitor, args);
              }

              CssNode node = (CssNode) args[0];
              if (method.getName().startsWith("enter")) {
                visitor.enter(node);
                return method.invoke(visitor, args);
              } else if (method.getName().startsWith("leave")) {
                Object result = method.invoke(visitor, args);
                visitor.leave(node);
                return result;
              }
              throw new IllegalStateException("Unexpected method '" + method + "' called");
            }
          });
    }
  }
}
