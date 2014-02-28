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

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.CssCommentNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssMixinNode;
import com.google.common.css.compiler.ast.CssRefinerNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A compiler pass that checks for missing {@code &#64;require} lines for def constant references
 * and mixins. This pass is used in conjunction with CollectProvideNamespaces, which provides
 * namespaces for constant definitions and mixins.
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

  private static final Pattern OVERRIDE_REGEX = Pattern.compile(
      "/\\*\\s+@overrideSelector\\s+\\{(.*)\\}\\s+\\*/");

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;

  // Key: filename; Value: provide namespace
  private final Map<String, String> filenameProvideMap;
  // Key: filename; Value: require namespace
  private final ListMultimap<String, String> filenameRequireMap;

  // Multiple namespaces can contain the same defs due to duplicate defs (or mods).
  // Key: def name; Value: provide namespace
  private final ListMultimap<String, String> defProvideMap;
  // Key: defmixin name; Value: provide namespace
  private final ListMultimap<String, String> defmixinProvideMap;

  public CheckMissingRequire(MutatingVisitController visitController,
      ErrorManager errorManager,
      Map<String, String> filenameProvideMap,
      ListMultimap<String, String> filenameRequireMap,
      ListMultimap<String, String> defProvideMap,
      ListMultimap<String, String> defmixinProvideMap) {
    this.visitController = visitController;
    this.errorManager = errorManager;
    this.filenameProvideMap = filenameProvideMap;
    this.filenameRequireMap = filenameRequireMap;
    this.defProvideMap = defProvideMap;
    this.defmixinProvideMap = defmixinProvideMap;
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    if (node instanceof CssConstantReferenceNode) {
      if (node.getSourceCodeLocation() == null) {
        // Cannot enforce provide / require for generated GSS components.
        return true;
      }
      CssConstantReferenceNode reference = (CssConstantReferenceNode) node;
      String filename = reference.getSourceCodeLocation().getSourceCode().getFileName();
      List<String> provides = defProvideMap.get(reference.getValue());
      // Remove this after switching to the new syntax.
      if (provides == null || provides.size() == 0) {  // ignore old format @provide
        return true;
      }
      if (hasMissingRequire(provides, filenameProvideMap.get(filename),
          filenameRequireMap.get(filename))) {
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
  public boolean enterMixin(CssMixinNode node) {
    if (node.getSourceCodeLocation() == null) {
      // Cannot enforce provide / require for generated GSS components.
      return true;
    }
    String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
    List<String> provides = defmixinProvideMap.get(node.getDefinitionName());
    // Remove this after switching to the new syntax.
    if (provides == null || provides.size() == 0) {  // ignore old format @provide
      return true;
    }
    if (hasMissingRequire(provides, filenameProvideMap.get(filename),
        filenameRequireMap.get(filename))) {
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

  private boolean hasMissingRequire(List<String> provides, String currentNamespace,
      List<String> requires) {
    // Either the namespace should be provided in this very file or it should be @require'd here.
    Set<String> defNamespaceSet = Sets.newHashSet(provides);
    Set<String> requireNamespaceSet = Sets.newHashSet(requires);
    requireNamespaceSet.retainAll(defNamespaceSet);
    if (requireNamespaceSet.size() > 0 || defNamespaceSet.contains(currentNamespace)) {
      return false;
    }
    return true;
  }

  /*
   * Check whether @overrideSelector namespaces are @require'd.
   */
  @Override
  public boolean enterSelector(CssSelectorNode node) {
    if (node.getSourceCodeLocation() == null) {
      // Cannot enforce provide / require for generated GSS components.
      return true;
    }
    String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
    for (CssRefinerNode refiner : node.getRefiners().getChildren()) {
      for (CssCommentNode comment : refiner.getComments()) {
        Matcher matcher = OVERRIDE_REGEX.matcher(comment.getValue());
        if (matcher.find()) {
          String overrideNamespace = matcher.group(1);
          List<String> requires = filenameRequireMap.get(filename);
          // Remove this after switching to the new syntax.
          if (requires == null || requires.size() == 0) {  // ignore old format @require
            continue;
          }
          Set<String> requireNamespaceSet = Sets.newHashSet(requires);
          if (!requireNamespaceSet.contains(overrideNamespace)) {
            String error = "Missing @require for @overrideSelector {" +
                overrideNamespace + "}. Please @require this namespace in file: " +
                filename + ".\n";
            errorManager.report(new GssError(error, node.getSourceCodeLocation()));
            return true;
          }
        }
      }
    }
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
