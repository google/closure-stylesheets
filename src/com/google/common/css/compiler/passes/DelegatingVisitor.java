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
import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.reflect.Reflection;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Dispatches to multiple {@code CssTreeVisitor}s. All {@code enter*} methods are called in the
 * order provided to the constructor and all {@code leave*} methods are called in the opposite
 * order.
 *
 * <p>Because {@code enter*} methods' return value changes the visitor behavior, the value returned
 * by the <em>last</em> delegate is the one returned by the method.
 */
public class DelegatingVisitor {
  private DelegatingVisitor() {}

  /**
   * Creates a {@code DelegatingVisitor} from the given list of visitors. The list must have at
   * least one element.
   */
  public static CssTreeVisitor from(List<CssTreeVisitor> originalVisitors) {
    Preconditions.checkArgument(originalVisitors.size() >= 1);
    if (originalVisitors.size() == 1) {
      return originalVisitors.get(0);
    }

    final ImmutableList<CssTreeVisitor> visitors = ImmutableList.copyOf(originalVisitors);
    final ImmutableList<CssTreeVisitor> reverseVisitors = visitors.reverse();
    return Reflection.newProxy(
        CssTreeVisitor.class,
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
              Object returnValue = null;
              Iterable<CssTreeVisitor> visitorsInOrderForMethod;
              if (method.getName().startsWith("enter")) {
                visitorsInOrderForMethod = visitors;
              } else { // assume it's a leave* method
                visitorsInOrderForMethod = reverseVisitors;
              }
              for (CssTreeVisitor visitor : visitorsInOrderForMethod) {
                returnValue = method.invoke(visitor, args);
              }
              return returnValue;
            } catch (InvocationTargetException e) {
              throw e.getTargetException();
            }
          }
        });
  }

  /**
   * Creates a {@code DelegatingVisitor} from the given array of visitors. Changes to the array will
   * not be reflected after construction.
   */
  public static CssTreeVisitor from(CssTreeVisitor... visitors) {
    return from(ImmutableList.copyOf(visitors));
  }
}
