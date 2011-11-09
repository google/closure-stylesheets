/*
 * Copyright 2010 Google Inc.
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

package com.google.common.css.compiler.gssfunctions;

import com.google.common.collect.ImmutableMap;
import com.google.common.css.GssFunctionMapProvider;
import com.google.common.css.compiler.ast.GssFunction;

import java.util.Map;

/**
 * Provides a map of the arithmetic GSS functions for use in tests.
 *
 */
public class DefaultGssFunctionMapProvider implements GssFunctionMapProvider {

  public Map<String, GssFunction> get() {
    return new ImmutableMap.Builder<String, GssFunction>()
        .put("add", new ArithmeticGssFunctions.Plus())
        .put("sub", new ArithmeticGssFunctions.Minus())
        .put("mult", new ArithmeticGssFunctions.Mult())
        .put("div", new ArithmeticGssFunctions.Div())
        .put("min", new ArithmeticGssFunctions.Min())
        .put("max", new ArithmeticGssFunctions.Max())
        .build();
  }

  @Override
  public <F> Map<String, F> get(Class<F> gssFunctionClass) {
    if (GssFunction.class.equals(gssFunctionClass)) {
      @SuppressWarnings("unchecked")
      Map<String, F> map = (Map<String, F>) get();
      return map;
    }
    return null;
  }
}
