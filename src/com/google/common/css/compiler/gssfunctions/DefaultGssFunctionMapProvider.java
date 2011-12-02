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
import com.google.common.css.compiler.gssfunctions.GssFunctions.AddHsbToCssColor;
import com.google.common.css.compiler.gssfunctions.GssFunctions.AddToNumericValue;
import com.google.common.css.compiler.gssfunctions.GssFunctions.AdjustBrightness;
import com.google.common.css.compiler.gssfunctions.GssFunctions.BlendColors;
import com.google.common.css.compiler.gssfunctions.GssFunctions.BlendColorsRGB;
import com.google.common.css.compiler.gssfunctions.GssFunctions.MakeContrastingColor;
import com.google.common.css.compiler.gssfunctions.GssFunctions.MakeMutedColor;
import com.google.common.css.compiler.gssfunctions.GssFunctions.SelectFrom;
import com.google.common.css.compiler.gssfunctions.GssFunctions.SubtractFromNumericValue;

import java.util.Map;

/**
 * Provides the default set of functions that are bundled with Closure
 * Stylesheets.
 *
 */
public class DefaultGssFunctionMapProvider implements GssFunctionMapProvider {

  public Map<String, GssFunction> get() {
    return new ImmutableMap.Builder<String, GssFunction>()
        // Arithmetic functions.
        .put("add", new GssFunctions.AddToNumericValue())
        .put("sub", new GssFunctions.SubtractFromNumericValue())
        .put("mult", new GssFunctions.Mult())
        // Not named "div" so it will not be confused with the HTML element.
        .put("divide", new GssFunctions.Div())
        .put("min", new GssFunctions.MinValue())
        .put("max", new GssFunctions.MaxValue())

        // Color functions.
        .put("blendColors", new BlendColors())
        .put("blendColorsRgb", new BlendColorsRGB())
        .put("makeMutedColor", new MakeMutedColor())
        .put("addHsbToCssColor", new AddHsbToCssColor())
        .put("makeContrastingColor", new MakeContrastingColor())
        .put("addToNumericValue", new AddToNumericValue())
        .put("subtractFromNumericValue", new SubtractFromNumericValue())
        .put("adjustBrightness", new AdjustBrightness())

        // Logic functions.
        .put("selectFrom", new SelectFrom())

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
