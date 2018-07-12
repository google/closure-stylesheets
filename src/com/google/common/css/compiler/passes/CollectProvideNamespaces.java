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

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssProvideNode;
import com.google.common.css.compiler.ast.CssRequireNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;

import java.util.Map;

/**
 * A compiler pass to help find missing {@code @require} lines for def constant references
 * and mixins.
 * This pass simply collects namespaces that correpond to constant definitions and mixins.
 * Also see the CheckMissingRequire pass that is used in conjunction with this one.
 *
 * NOTE: The maps in this class can only be used within the same set of pass runs.
 *
 */
public final class CollectProvideNamespaces extends DefaultTreeVisitor implements CssCompilerPass {
  private final VisitController visitController;

  // Key: filename; Value: provide namespace
  private final Map<String, String> filenameProvideMap = Maps.newHashMap();
  // Key: filename; Value: require namespace
  private final ListMultimap<String, String> filenameRequireMap = LinkedListMultimap.create();

  // Multiple namespaces can contain the same defs due to duplicate defs (or mods).
  // Key: def name; Value: provide namespace
  private final ListMultimap<String, String> defProvideMap = LinkedListMultimap.create();
  // Key: defmixin name; Value: provide namespace
  private final ListMultimap<String, String> defmixinProvideMap = LinkedListMultimap.create();

  public Map<String, String> getFilenameProvideMap() {
    return filenameProvideMap;
  }

  public ListMultimap<String, String> getFilenameRequireMap() {
    return filenameRequireMap;
  }

  public ListMultimap<String, String> getDefProvideMap() {
    return defProvideMap;
  }

  public ListMultimap<String, String> getDefmixinProvideMap() {
    return defmixinProvideMap;
  }

  public CollectProvideNamespaces(VisitController visitController) {
    this.visitController = visitController;
  }

  @Override
  public boolean enterProvideNode(CssProvideNode node) {
    Preconditions.checkState(node.getSourceCodeLocation() != null);
    String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
    filenameProvideMap.put(filename, node.getProvide());
    return true;
  }

  @Override
  public boolean enterRequireNode(CssRequireNode node) {
    Preconditions.checkState(node.getSourceCodeLocation() != null);
    String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
    filenameRequireMap.put(filename, node.getRequire());
    return true;
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    if (node.getSourceCodeLocation() == null) {
      // Cannot enforce provide / require for generated GSS components.
      return true;
    }
    String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
    String provideNamespace = filenameProvideMap.get(filename);
    defProvideMap.put(node.getName().getValue(), provideNamespace);
    return true;
  }

  @Override
  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    if (node.getSourceCodeLocation() == null) {
      // Cannot enforce provide / require for generated GSS components.
      return true;
    }
    String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
    String provideNamespace = filenameProvideMap.get(filename);
    Preconditions.checkArgument(provideNamespace != null);
    defmixinProvideMap.put(node.getDefinitionName(), provideNamespace);
    return true;
  }

  @Override
  public void runPass() {
    filenameProvideMap.clear();
    filenameRequireMap.clear();
    defProvideMap.clear();
    defmixinProvideMap.clear();
    visitController.startVisit(this);
  }
}
