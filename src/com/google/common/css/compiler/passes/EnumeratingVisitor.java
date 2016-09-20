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

import com.google.common.collect.Maps;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTree;
import java.util.HashMap;

/**
 * A visitor that maps nodes to their visit ordering.
 *
 */
public class EnumeratingVisitor implements UniformVisitor {

  private int counter;
  private HashMap<CssNode, Integer> enumeration = Maps.newHashMap();

  @Override
  public void enter(CssNode node) {
    enumeration.put(node, counter);
    ++counter;
  }

  @Override
  public void leave(CssNode node) {}

  public static HashMap<CssNode, Integer> enumerate(CssTree t) {
    EnumeratingVisitor v = new EnumeratingVisitor();
    t.getVisitController().startVisit(UniformVisitor.Adapters.asVisitor(v));
    return v.enumeration;
  }
}
