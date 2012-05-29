/*
 * Copyright 2011 Google Inc.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssMixinNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Compiler pass that replaces mixins with the corresponding mixin definitions
 * and replaces the variables in the definitions with the values given by the
 * mixin.
 *
 * <p>In addition, this pass ensures that each mixin has a matching definition
 * and that the argument count of the use and the definition is equal.
 *
 * <p>{@link CollectMixinDefinitions} has to run before.
 * {@link ReplaceConstantReferences} has to run afterwards.
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class ReplaceMixins extends DefaultTreeVisitor
    implements CssCompilerPass {
  @VisibleForTesting
  static final String NO_MATCHING_MIXIN_DEFINITION_ERROR_MESSAGE =
      "The name of the mixin matches no mixin definition name";
  @VisibleForTesting
  static final String ARGUMENT_MISMATCH_ERROR_MESSAGE =
    "The number of arguments of the mixin and the corresponding definition "
    + "are different";
  @VisibleForTesting
  static final String CYCLE_ERROR_MESSAGE =
      "A nested mixin call produces a cycle";
  @VisibleForTesting
  static final String NO_MATCHING_MIXIN_FOR_REF_ERROR_MESSAGE =
      "Internal compiler error: The current definition reference belongs to "
      + "a mixin but does not match the mixin on top of the stack";

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;
  private final Map<String, CssMixinDefinitionNode> definitions;

  /**
   * the current call stack of the mixins
   */
  private Stack<StackFrame> currentMixinStack;

  public ReplaceMixins(MutatingVisitController visitController,
      ErrorManager errorManager,
      Map<String, CssMixinDefinitionNode> definitions) {
    this.visitController = visitController;
    this.errorManager = errorManager;
    this.definitions = definitions;
    this.currentMixinStack = new Stack<StackFrame>();
  }

  @Override
  public void leaveMixin(CssMixinNode node) {
    if (!currentMixinStack.empty()) {
      currentMixinStack.peek().decreaseDeclarationCount();
    }
    // This pushes the mixin on the stack if the corresponding definition
    // contains at least one declaration.
    replaceMixin(node);
    // Goes up the stack if this is the last declaration inserted by a mixin.
    // This is done for the case where no mixin is added to the stack.
    while (!currentMixinStack.empty()
        && currentMixinStack.peek().isDeclarationCountZero()) {
      currentMixinStack.pop();
    }
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode node) {
    // Updates the stacks if the last declaration that was added by the last
    // mixin call is reached.
    if (currentMixinStack.empty()) {
      return;
    }
    // get the number of declarations left that were added by the last
    // mixin call
    currentMixinStack.peek().decreaseDeclarationCount();
    // go up the stack if this is the last declaration inserted by a mixin
    while (!currentMixinStack.empty()
        && currentMixinStack.peek().isDeclarationCountZero()) {
      currentMixinStack.pop();
    }
  }

  @Override
  public void leaveDeclarationBlock(CssDeclarationBlockNode node) {
    currentMixinStack.clear();
  }

  /**
   * Replaces a variable inside the copy of the mixin definition with the
   * value given by the mixin.
   */
  @Override
  public boolean enterValueNode(CssValueNode node) {
    return replaceReference(node, false /* isArgument */);
  }

  @Override
  public boolean enterArgumentNode(CssValueNode node) {
    return replaceReference(node, true /* isArgument */);
  }

  private boolean replaceReference(CssValueNode node, boolean isArgument) {
    if (!(node instanceof CssConstantReferenceNode)) {
      return true;
    }
    List<CssValueNode> values = getValuesForReference(
        (CssConstantReferenceNode) node, isArgument);
    if (values == null) {
      return true;
    }
    visitController.replaceCurrentBlockChildWith(values, false);
    return true;
  }

  /**
   * Replaces a mixin with the declarations of the corresponding definition.
   */
  private void replaceMixin(CssMixinNode mixin) {
    if (containsCycle(mixin)) {
      return;
    }
    CssMixinDefinitionNode currentMixinDefinition
        = definitions.get(mixin.getDefinitionName());
    if (currentMixinDefinition == null) {
      errorManager.report(new GssError(
          NO_MATCHING_MIXIN_DEFINITION_ERROR_MESSAGE,
          mixin.getSourceCodeLocation()));
      return;
    }
    // Adds deep copies of the declarations in the definition to the current
    // declaration block. The variables are visited and replaced afterwards.
    List<CssNode> mixinDecls =
        currentMixinDefinition.getBlock().deepCopy().getChildren();
    visitController.replaceCurrentBlockChildWith(mixinDecls,
        /* visitTheReplacementNodes */ true);
    // Create a mapping so that references can easily be replaced by their
    // value.
    Map<String, List<CssValueNode>> refMap = createReferenceMapping(mixin,
        currentMixinDefinition);
    if (refMap == null) {
      visitController.stopVisit();
      return;
    }
    if (mixinDecls.size() == 0) {
      return;
    }
    // Add the mixin and the number of declarations to the stack
    currentMixinStack.push(new StackFrame(mixin, mixinDecls.size(), refMap));
  }

  /**
   * Returns the value of the given reference. The value is defined by the
   * mixin referring to the mixin definition the reference is in.
   */
  private List<CssValueNode> getValuesForReference(
      CssConstantReferenceNode ref, boolean isArgument) {
    if (!(ref.getScope() instanceof CssMixinDefinitionNode)) {
      return null;
    }

    String defName = ref.getValue();
    CssMixinDefinitionNode currentMixinDefinition =
        (CssMixinDefinitionNode) ref.getScope();
    CssMixinNode currentMixin = currentMixinStack.peek().getMixin();
    if (!currentMixin.getDefinitionName().equals(
        currentMixinDefinition.getDefinitionName())) {
      errorManager.report(new GssError(
          NO_MATCHING_MIXIN_FOR_REF_ERROR_MESSAGE,
          ref.getSourceCodeLocation()));
      return null;
    }

    List<CssValueNode> values = currentMixinStack.peek().getValuesForReference(
        ref.getValue());
    Preconditions.checkNotNull(values);

    // Create deep copies because the values can be inserted in several places.
    ImmutableList.Builder<CssValueNode> builder = ImmutableList.builder();
    for (CssValueNode val : values) {
      if (isArgument || !" ".equals(val.getValue())) {
        // Values only containing a whitespace are only added if they inside
        // the argument of a function as they are inserted by the parser to
        // separate values that are together seen as one argument.
        builder.add(val.deepCopy());
      }
    }
    return builder.build();
  }

  /**
   * Ensures that no cyclic mixin calls occur.
   */
  private boolean containsCycle(CssMixinNode mixin) {
    for (StackFrame frame : currentMixinStack) {
      if (mixin.getDefinitionName().equals(
          frame.getMixin().getDefinitionName())) {
        errorManager.report(new GssError(
            CYCLE_ERROR_MESSAGE,
            frame.getMixin().getSourceCodeLocation()));
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a mapping between argument names of the mixin definition and the
   * values that are provided by the mixin.
   */
  private Map<String, List<CssValueNode>> createReferenceMapping(
      CssMixinNode mixin, CssMixinDefinitionNode def) {
    Map<String, List<CssValueNode>> refMap = Maps.newHashMap();
    List<CssValueNode> currentValues = Lists.newArrayList();
    Iterator<CssValueNode> definitionArgumentIterator =
        def.getArguments().getChildIterator();
    // Collects all values up to a comma and then adds these values to the map.
    for (CssValueNode arg : mixin.getArguments().getChildren()) {
      if (",".equals(arg.getValue())) {
        if (!addValuesToMap(refMap, definitionArgumentIterator, currentValues,
            arg.getSourceCodeLocation())) {
          return null;
        }
        currentValues.clear();
      } else {
        currentValues.add(arg);
      }
    }
    if (!currentValues.isEmpty()) {
      // Add values for last argument.
      if (!addValuesToMap(refMap, definitionArgumentIterator, currentValues,
          mixin.getSourceCodeLocation())) {
        return null;
      }
    }
    if (definitionArgumentIterator.hasNext()) {
      // The definition takes more arguments than the mixin provides.
      errorManager.report(new GssError(
          ARGUMENT_MISMATCH_ERROR_MESSAGE,
          mixin.getSourceCodeLocation()));
      return null;
    }
    return refMap;
  }

  /**
   * Adds the given values to the map that maps argument names to their values.
   */
  private boolean addValuesToMap(Map<String, List<CssValueNode>> refMap,
      Iterator<CssValueNode> definitionArgumentIterator,
      List<CssValueNode> values, SourceCodeLocation location) {
    if (values.isEmpty() || !definitionArgumentIterator.hasNext()) {
      // There is no value between two commas or the mixin provides more
      // arguments than the definition takes.
      errorManager.report(new GssError(
          ARGUMENT_MISMATCH_ERROR_MESSAGE,
          location));
      return false;
    }
    CssValueNode argument = definitionArgumentIterator.next();
    // Commas are skipped.
    if (",".equals(argument.getValue())) {
      if (values.isEmpty() || !definitionArgumentIterator.hasNext()) {
        errorManager.report(new GssError(
            ARGUMENT_MISMATCH_ERROR_MESSAGE,
            location));
        return false;
      }
      argument = definitionArgumentIterator.next();
    }
    refMap.put(argument.getValue(), ImmutableList.copyOf(values));
    return true;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

  /**
   * Helper class that is used to keep track of the called mixins inside of
   * other mixins.
   */
  private static class StackFrame {
    private CssMixinNode mixin;
    private int declarationCount;
    private final Map<String, List<CssValueNode>> valueMap;

    StackFrame(CssMixinNode mixin, int declarationCount,
        Map<String, List<CssValueNode>> valueMap) {
      Preconditions.checkNotNull(mixin);
      Preconditions.checkArgument(declarationCount > 0);
      Preconditions.checkNotNull(valueMap);
      this.mixin = mixin;
      this.declarationCount = declarationCount;
      this.valueMap = valueMap;
    }

    CssMixinNode getMixin() {
      return mixin;
    }

    void decreaseDeclarationCount() {
      if (declarationCount > 0) {
        declarationCount--;
      }
    }

    boolean isDeclarationCountZero() {
      return this.declarationCount == 0;
    }

    List<CssValueNode> getValuesForReference(String refName) {
      return valueMap.get(refName);
    }
  }
}
