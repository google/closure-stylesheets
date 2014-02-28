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
import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssMixinNode;
import com.google.common.css.compiler.ast.CssProvideNode;
import com.google.common.css.compiler.ast.CssRequireNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A compiler pass that checks for missing {@code &#64;require} lines for def constant references
 * and mixins.
 * Example for def references:
 * file foo/gss/button.gss provides namespace {@code &#64;provide 'foo.gss.button';} and has
 *  the def: {@code &#64;def FONT_SIZE 10px;}.
 * File foo/gss/item.gss references the above def as follows:
 * {@code &#64;def ITEM_FONT_SIZE FONT_SIZE;}
 * This pass enforces that file foo/gss/item.gss contains {@code &#64;require 'foo.gss.button';}
 *
 */
public final class CheckMissingRequire extends DefaultTreeVisitor implements CssCompilerPass {
  private static final Logger logger = Logger.getLogger(CheckMissingRequire.class.getName());

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;

  // Key: filename; Value: provide namespace
  private final Map<String, String> filenameProvideMap = Maps.newHashMap();
  // Key: filename; Value: require namespace
  private final ListMultimap<String, String> filenameRequireMap = LinkedListMultimap.create();

  // Multiple namespaces can contain the same defs due to duplicate defs (or mods).
  // Key: def name; Value: provide namespace
  private final ListMultimap<String, String> defProvideMap = LinkedListMultimap.create();
  // Key: defmixin name; Value: provide namespace
  private final ListMultimap<String, String> defmixinProvideMap = LinkedListMultimap.create();

  public CheckMissingRequire(MutatingVisitController visitController, ErrorManager errorManager) {
    this.visitController = visitController;
    this.errorManager = errorManager;
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
    Preconditions.checkState(node.getSourceCodeLocation() != null);
    String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
    String provideNamespace = filenameProvideMap.get(filename);
    Preconditions.checkArgument(provideNamespace != null);
    defProvideMap.put(node.getName().getValue(), provideNamespace);
    return true;
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    if (node instanceof CssConstantReferenceNode) {
      CssConstantReferenceNode reference = (CssConstantReferenceNode) node;
      String filename = reference.getSourceCodeLocation().getSourceCode().getFileName();
      if (hasMissingRequire(defProvideMap.get(reference.getValue()),
          filenameProvideMap.get(filename), filenameRequireMap.get(filename))) {
        StringBuilder error = new StringBuilder("Missing @require for constant " +
            reference.getValue() + ". Please @require namespace from:\n");
        for (String namespace : defProvideMap.get(reference.getValue())) {
          error.append("\t");
          error.append(namespace);
          error.append("\n");
        }
        errorManager.report(new GssError(error.toString(), reference.getSourceCodeLocation()));
      }
    }
    return true;
  }

  @Override
  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    Preconditions.checkState(node.getSourceCodeLocation() != null);
    String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
    String provideNamespace = filenameProvideMap.get(filename);
    Preconditions.checkArgument(provideNamespace != null);
    defmixinProvideMap.put(node.getDefinitionName(), provideNamespace);
    return true;
  }

  @Override
  public boolean enterMixin(CssMixinNode node) {
    Preconditions.checkState(node.getSourceCodeLocation() != null);
    String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
    if (hasMissingRequire(defmixinProvideMap.get(node.getDefinitionName()),
        filenameProvideMap.get(filename), filenameRequireMap.get(filename))) {
      StringBuilder error = new StringBuilder("Missing @require for mixin " +
          node.getDefinitionName() + ". Please @require namespace from:\n");
      for (String namespace : defmixinProvideMap.get(node.getDefinitionName())) {
        error.append("\t");
        error.append(namespace);
        error.append("\n");
      }
      errorManager.report(new GssError(error.toString(), node.getSourceCodeLocation()));
    }
    return true;
  }

  private boolean hasMissingRequire(List<String> defProvides,
      String currentNamespace, List<String> requires) {
    // Either the namespace should be provided in this very file or it should be @require'd here.
    Set<String> defNamespaceSet = Sets.newHashSet(defProvides);
    Set<String> requireNamespaceSet = Sets.newHashSet(requires);
    requireNamespaceSet.retainAll(defNamespaceSet);
    if (requireNamespaceSet.size() > 0 || defNamespaceSet.contains(currentNamespace)) {
      return false;
    }
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
