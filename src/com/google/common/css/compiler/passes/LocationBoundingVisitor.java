/*
 * Copyright 2013 Google Inc.
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

import com.google.common.collect.Ordering;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.SourceCodeLocation.SourceCodePoint;
import com.google.common.css.SourceCodeLocationBuilder;
import com.google.common.css.compiler.ast.CssNode;

/**
 * A pass for finding an approximation to a minimum-size SourceCodeLocation
 * interval that contains a given node.
 *
 * <p>In typical CssTrees c. April 2013, most nodes do not have
 * SourceCodeLocations, but suppose we have such a node A with
 * descendent nodes D_0, ... D_n that do have locations:
 *     A
 *    / \
 *   -----
 *    |   \
 *   D_0  D_1
 *
 * Then we know that the subtree rooted at A includes markup beginning
 * by min_0^n(beginLocation(D_i)) and ending by max_0^n(endLocation(D_i)).
 * Concretely, let
 *   node  getBeginCharacterIndex  getEndCharacterIndex
 *   ---   ---                     ---
 *   A     ?                       ?
 *   D_0   5                       15
 *   D_1   17                      19
 * Then we can estimate for A:
 *   A     5                       19
 */
public class LocationBoundingVisitor extends UniformVisitor {
  private SourceCodeLocation result = null;

  @Override
  public void enter(CssNode n) {
    SourceCodeLocation loc = n.getSourceCodeLocation();
    if (loc == null || loc.isUnknown()) {
      return;
    }
    if (result == null || result.isUnknown()) {
      result = loc;
    } else {
      Ordering<SourceCodePoint> o = Ordering.natural();
      SourceCodePoint lowerBound = o.min(result.getBegin(), loc.getBegin());
      SourceCodePoint upperBound = o.max(result.getEnd(), loc.getEnd());
      result = new SourceCodeLocationBuilder()
          .setSourceCode(result.getSourceCode())
          .setBeginLocation(lowerBound)
          .setEndLocation(upperBound)
          .getSourceCodeLocation();
    }
  }

  public static SourceCodeLocation bound(CssNode n) {
    SourceCodeLocation location = n.getSourceCodeLocation();
    if (location != null && !location.isUnknown()) {
      return location;
    }
    LocationBoundingVisitor v = new LocationBoundingVisitor();
    n.getVisitController().startVisit(v);
    if (v.result == null) {
      return SourceCodeLocation.getUnknownLocation();
    }
    return v.result;
  }
}
