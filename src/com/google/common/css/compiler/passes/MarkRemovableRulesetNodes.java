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
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssPriorityNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssRefinerNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.ast.SkippingTreeVisitor;

import java.util.Set;

/**
 * Compiler pass that marks the ruleset nodes that should be removed from the
 * tree.
 *
 * <p>This pass assumes that each ruleset node contains exactly one declaration
 * and one selector. This pass should be preceded by SplitRulesetNodes
 * pass. This pass skips over nodes in the body that are not ruleset nodes and
 * ignores all their children including ruleset nodes. So, in particular, all
 * ruleset nodes contained within {@code @media} rules will be ignored.
 *
 * @author oana@google.com (Oana Florescu)
 */
// TODO(oana): Split this pass into two, but only after running some performance
//     tests to figure out the time it takes for a pass that does not change the
//     tree to run.
public class MarkRemovableRulesetNodes extends SkippingTreeVisitor
    implements CssCompilerPass {

  private final CssTree tree;
  private final MutatingVisitController visitController;

  /**
   * Property names not be to checked while traversing the tree to find
   * overridden declarations.
   */
  static final Set<String> PROPERTIES_NOT_TO_BE_CHECKED =
      ImmutableSet.of("display", "cursor");

  /** The set of rules known to be referenced. */
  private Set<String> referencedRules = null;

  /** The prefix of the class names. TODO(oana): This should be a namespace. */
  private String prefixOfReferencedRules = "";

  /**
   * Creates a new pass over the specified tree.
   */
  public MarkRemovableRulesetNodes(CssTree tree) {
    this(tree, false);
  }

  /**
   * Creates a new pass over the specified tree.
   *
   * @param skipping whether to skip over rulesets containing properties that
   *     might make them unsafe to modify (see {@link SkippingTreeVisitor})
   */
  public MarkRemovableRulesetNodes(CssTree tree, boolean skipping) {
    super(skipping);
    this.tree = tree;
    this.visitController = tree.getMutatingVisitController();
  }

  /**
   * Sets the reference rules.
   *
   * @param referencedRules the set of referenced rules, which, since it is
   *     aliased, is expected not to change while this pass is in progress
   * @param prefixOfReferencedRules the prefix to match when looking for
   *     unreferenced rules
   */
  public void setReferencedRules(
      Set<String> referencedRules,
      String prefixOfReferencedRules) {
    this.referencedRules = referencedRules;
    this.prefixOfReferencedRules = prefixOfReferencedRules;
  }

  @Override
  public boolean enterBlock(CssBlockNode block) {
    // All the children of the block, which are ruleset nodes, are looked at
    // in reverse order, from the last one to the first. We mark as removable
    // those nodes that we are found as overridden already.
    // Collect the already-seen pairs of selectors and property names in this
    // table, save the CssRulesetNode also.
    Table<String, String, CssRulesetNode> rules = HashBasedTable.create();

    for (int i = block.numChildren() - 1; i >= 0; i--) {
      if (block.getChildAt(i) instanceof CssRulesetNode) {
        CssRulesetNode ruleset = (CssRulesetNode) block.getChildAt(i);

        // Filter out unreferenced rules. We do this regardless of whether the
        // rule in question has a "display" property, is in the chunk currently
        // being processed or any of the other criteria that canModifyRuleset
        // checks for because there's no reason not to remove unreferenced
        // rules.
        if (referencedRules != null && !referencedRules.isEmpty()
            && isSelectorUnreferenced(ruleset.getSelectors().getChildAt(0))) {
          tree.getRulesetNodesToRemove().addRulesetNode(ruleset);
          continue;
        }

        // If skipping is on and the rule contains a property from a
        // pre-defined set then we skip processing this ruleset
        // (in that case "canModifyRuleset()" will return false).
        if (canModifyRuleset(ruleset)) {
          // Make sure the node has only one selector.
          Preconditions.checkArgument(isSkipping() || (ruleset.getSelectors().numChildren() == 1));

          processRuleset(rules, ruleset);
        }
      }
    }

    return false;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

  /**
   * Processes the given ruleset, deciding whether it should be kept
   * or removed by looking at the given previous rules.
   */
  private void processRuleset(
      Table<String, String, CssRulesetNode> rules, CssRulesetNode ruleset) {
    if ((referencedRules != null) && !referencedRules.isEmpty()) {
      // If this rule is not referenced to in the code we remove it.
      if (isSelectorUnreferenced(ruleset.getSelectors().getChildAt(0))) {
        // TODO(henrywong, dgajda): Storing the set of things to clean up
        // in the tree is pretty brittle - better would be to have the pass
        // return the rules it finds and then manually pass them in to the
        // EliminateUselessRulesets pass.
        tree.getRulesetNodesToRemove().addRulesetNode(ruleset);
        return;
      }
    }

    // Make sure the node has only one declaration.
    Preconditions.checkArgument(ruleset.getDeclarations().numChildren() == 1);
    Preconditions.checkArgument(
        ruleset.getDeclarations().getChildAt(0) instanceof CssDeclarationNode);
    CssDeclarationNode declaration =
      (CssDeclarationNode) ruleset.getDeclarations().getChildAt(0);
    CssPropertyNode propertyNode = declaration.getPropertyName();
    String propertyName = propertyNode.getPropertyName();
    if (PROPERTIES_NOT_TO_BE_CHECKED.contains(propertyName)) {
      return;
    }
    // If the declaration is star-hacked then we make the star be part of
    // the property name to ensure that we do not consider hacked
    // declarations as overridden by the non-hacked ones.
    if (declaration.hasStarHack()) {
      propertyName = "*" + propertyName;
    }

    String selector = PassUtil.printSelector(
        ruleset.getSelectors().getChildAt(0));

    CssRulesetNode previousRuleset = rules.get(selector, propertyName);
    if (previousRuleset != null) {
      // If the new rule is important and the saved was not, then remove the saved one.
      if (isImportantRule(ruleset) && !isImportantRule(previousRuleset)) {
        tree.getRulesetNodesToRemove().addRulesetNode(previousRuleset);
        // Replace the non-important ruleset in the map, keep the important one.
        rules.put(selector, propertyName, ruleset);
      } else {
        tree.getRulesetNodesToRemove().addRulesetNode(ruleset);
      }
    } else if (hasOverridingShorthand(propertyNode, selector, rules, ruleset)) {
      tree.getRulesetNodesToRemove().addRulesetNode(ruleset);
    } else if (PassUtil.hasAlternateAnnotation(declaration)) {
      // The declaration has @alternate, so do not let it mask other
      // declarations that precede it.  However, @alternate rules may be masked
      // by succeeding non-@alternate rules.
    } else {
      rules.put(selector, propertyName, ruleset);
    }
  }

  private boolean isSelectorUnreferenced(CssSelectorNode selector) {
    return okToRemoveSelector(selector)
        && (atLeastOneUnreferencedRefiner(selector) || unreferencedSelectorCombinator(selector));
  }

  /**
   * Returns whether it's OK to remove the specified selector, i.e. it is not a
   * tag, like: body, html, etc, as tags should not be removed unless they are
   * in combination with other CSS classes.
   */
  private boolean okToRemoveSelector(CssSelectorNode selector) {
    return (selector.getSelectorName() == null)
        || !selector.getRefiners().isEmpty()
        || (selector.getCombinator() != null);
  }

  /**
   * Returns whether at least one of the refiners of the specified selector is
   * not in the list of referenced rules.
   */
  private boolean atLeastOneUnreferencedRefiner(CssSelectorNode selector) {
    for (CssRefinerNode ref : selector.getRefiners().childIterable()) {
      if (ref instanceof CssClassSelectorNode) {
        String refiner = ref.getRefinerName();
        if (refiner.startsWith(prefixOfReferencedRules)
            && isRefinerUnreferenced(refiner)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns whether the combinator of the specified selector is unreferenced.
   */
  private boolean unreferencedSelectorCombinator(CssSelectorNode selector) {
    return (selector.getCombinator() != null
        && isSelectorUnreferenced(selector.getCombinator().getSelector()));
  }

  private boolean isRefinerUnreferenced(String refiner) {
    String[] splits = refiner.split("-");
    for (String s : splits) {
      if (!referencedRules.contains(s)) {
        return true;
      }
    }
    return false;
  }

  private boolean isImportantRule(CssRulesetNode ruleset) {
    CssDeclarationNode decl =
        (CssDeclarationNode) ruleset.getDeclarations().getChildAt(0);
    Iterable<CssValueNode> propertyValues =
        decl.getPropertyValue().childIterable();
    return Iterables.any(propertyValues,
        Predicates.instanceOf(CssPriorityNode.class));
  }

  /**
   * Computes whether the given rule is overridden by another rule with some
   * related shorthand property of equal or higher importance.
   *
   * @param propertyNode the property node of the rule to check
   * @param selector the printed representation of the selector of the rule
   * @param rules rulesets occurring after the ruleset to check (represented as
   *     a map from selector/property pairs to rulesets for easy searching)
   * @param ruleset the ruleset to check (assumed to contain one rule)
   * @return whether the given ruleset has an overriding ruleset which uses a
   *     related shorthand property
   */
  private boolean hasOverridingShorthand(
      CssPropertyNode propertyNode,
      String selector,
      Table<String, String, CssRulesetNode> rules,
      final CssRulesetNode ruleset) {

    Supplier<Boolean> rulesetIsImportant = Suppliers.memoize(
        new Supplier<Boolean>() {
          @Override
          public Boolean get() {
            return isImportantRule(ruleset);
          }
        });

    for (String shorthand : propertyNode.getProperty().getShorthands()) {
      CssRulesetNode shorthandRuleset = rules.get(selector, shorthand);
      if ((shorthandRuleset != null)
          && (!rulesetIsImportant.get() || isImportantRule(shorthandRuleset))) {
        return true;
      }
    }

    return false;
  }
}
