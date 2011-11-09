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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.google.common.css.compiler.ast.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO(user): Consider adding an annotation of which classes
//     co-occur (or more generally, of which selectors with the same
//     specificity co-match) to prevent some breaking re-ordering.
//     For example, something like @conflict .FOO, .BAR should prevent
//     re-ordering conflicting declarations of the specified
//     selectors.
/**
 * Compiler pass that merges rulesets aggressively. The ordering of
 * the rulesets ensures that shorthand properties appear before their
 * detailed properties, and that the ordering of conflicting shorthand
 * properties is preserved. It assumes all other ordering is
 * irrelevant. When the {@link MarkRemovableRulesetNodes} pass is run
 * before this pass, the assumptions of this pass will be valid if no
 * DOM element matches two or more selectors of the same specificity
 * with conflicting declarations. For example, selectors {@code .FOO}
 * and {@code .BAR} might conflict if these classes co-occur on a DOM
 * element. As another example, {@code .FOO span} and {@code .BAR span}
 * might conflict say if a {@code span} DOM element has a parent with
 * a {@code FOO} class and a grandparent with a {@code BAR} class.
 *
 */
public class UnsafeMergeRulesetNodes implements CssCompilerPass {

  /**
   * Partitions for which a fixed pre-determined ordering of
   * properties won't do.
   */
  private static final Set<String> ORDER_DEPENDENT_PARTITIONS =
      ImmutableSet.of("border");

  private final CssTree tree;
  private final boolean byPartition;
  private final boolean skipping;

  /**
   * Constructor.
   *
   * <p>This pass runs in two modes, depending on the value of
   * {@code byPartition}. If {@code byPartition} is {@code true}, all
   * declarations on a selector are first grouped by their partition (as defined
   * by {@link CssPropertyNode#getPartition}). Then, selectors which have the
   * same declarations for a partition are grouped together into a ruleset. If
   * {@code byPartition} is {@code false} (recommended), partitioning is only
   * performed on order-dependent partitions (as defined by
   * {@link #ORDER_DEPENDENT_PARTITIONS}.
   *
   * @param tree the tree to run the pass on
   * @param byPartition whether to group <em>all</em> declarations by partition
   *     (and not just the ones such as border where order matters)
   * @param skipping whether to skip certain properties as defined by
   *      {@link SkippingTreeVisitor}
   */
  public UnsafeMergeRulesetNodes(
      CssTree tree, boolean byPartition, boolean skipping) {
    this.tree = tree;
    this.byPartition = byPartition;
    this.skipping = skipping;
  }

  @Override
  public void runPass() {
    replace(collect());
  }

  /**
   * Performs the collect phase by traversing the tree, collecting all the
   * rules and placing a placeholder ruleset.
   */
  private CollectPhaseTreeVisitor collect() {
    CollectPhaseTreeVisitor collectPhaseVisitor =
        new CollectPhaseTreeVisitor(tree.getMutatingVisitController(), byPartition, skipping);
    collectPhaseVisitor.runPass();
    return collectPhaseVisitor;
  }

  /**
   * Performs the replace phase, by using the structures from the collect phase
   * to compute a new list of rulesets and traversing the tree to replace the
   * placeholder with the new list.
   */
  private void replace(CollectPhaseTreeVisitor collectPhaseVisitor) {
    // If we don't have a placeholder, then nothing has been collected, and
    // nothing is to be done.
    if (collectPhaseVisitor.getPlaceholderRuleset() == null) {
      return;
    }

    List<CssNode> rulesets = Lists.newArrayList();

    for (Map.Entry<CssDeclarationNode, Collection<CssSelectorNode>> entry
        : collectPhaseVisitor.getDecToSels()) {
      rulesets.add(makeOneDeclarationRuleset(entry.getKey(), entry.getValue()));
    }

    for (Map.Entry<Collection<CssDeclarationNode>, Set<CssSelectorNode>> entry
        : collectPhaseVisitor.getPartitions()) {
      rulesets.add(makePartitionRuleset(entry.getKey(), entry.getValue()));
    }

    new ReplacePhaseTreeVisitor(
        tree.getMutatingVisitController(),
        rulesets,
        collectPhaseVisitor.getPlaceholderRuleset())
        .runPass();
  }

  /**
   * Makes a ruleset with many selectors and one declaration.
   */
  private CssRulesetNode makeOneDeclarationRuleset(
      CssDeclarationNode dec, Iterable<CssSelectorNode> sels) {
    CssRulesetNode ruleset = new CssRulesetNode(new CssDeclarationBlockNode());
    ruleset.addDeclaration(dec);
    for (CssSelectorNode sel : sels) {
      ruleset.addSelector(sel.deepCopy());
    }
    return ruleset;
  }

  /**
   * Makes a ruleset with many selectors and many declarations, whose
   * properties are assumed to be from the same partition.
   */
  private CssRulesetNode makePartitionRuleset(
      Iterable<CssDeclarationNode> decs, Iterable<CssSelectorNode> sels) {
    CssRulesetNode ruleset = new CssRulesetNode(new CssDeclarationBlockNode());
    for (CssDeclarationNode dec : decs) {
      ruleset.addDeclaration(dec.deepCopy());
    }
    for (CssSelectorNode sel : sels) {
      ruleset.addSelector(sel.deepCopy());
    }
    return ruleset;
  }

  /**
   * The tree visitor for the collect phase, which collects all the rules
   * replacing them with one placeholder rule.
   */
  private static class CollectPhaseTreeVisitor extends SkippingTreeVisitor
      implements CssCompilerPass {
    private final MutatingVisitController visitController;
    private final boolean byPartition;
    private final Multimap<CssDeclarationNode, CssSelectorNode> decToSel =
        TreeMultimap.create(DECLARATION_COMPARATOR, TO_STRING_COMPARATOR);
    private final Partitioner partitioner = new Partitioner();
    private CssRulesetNode placeholderRuleset = null;

    public CollectPhaseTreeVisitor(
        MutatingVisitController visitController, boolean byPartition, boolean skipping) {
      super(skipping);
      this.visitController = visitController;
      this.byPartition = byPartition;
    }

    /**
     * Processes a ruleset when allowed by collecting it and then deleting it
     * from the tree. If this is the first ruleset to be deleted, adds a
     * placeholder ruleset to be replaced in the {#replacePhase}.
     */
    @Override
    public boolean enterRuleset(CssRulesetNode ruleset) {
      if (canModifyRuleset(ruleset)) {
        collectRuleset(ruleset);
        deleteRuleset();
      }
      return true;
    }

    @Override
    public void runPass() {
      visitController.startVisit(this);
    }

    /**
     * Returns entries of declaration/selectors grouped by declarations.
     */
    Iterable<Map.Entry<CssDeclarationNode, Collection<CssSelectorNode>>> getDecToSels() {
      return decToSel.asMap().entrySet();
    }

    /**
     * Returns entries of declarations/selectors grouped by partitions, then
     * selectors.
     */
    Iterable<Map.Entry<Collection<CssDeclarationNode>, Set<CssSelectorNode>>> getPartitions() {
      return partitioner.getMap().entrySet();
    }

    /**
     * Returns the placeholder ruleset, to be replaced.
     */
    CssRulesetNode getPlaceholderRuleset() {
      return placeholderRuleset;
    }

    private void collectRuleset(CssRulesetNode ruleset) {
      for (CssNode abstractDecl : ruleset.getDeclarations().childIterable()) {
        Preconditions.checkState(abstractDecl instanceof CssDeclarationNode);
        CssDeclarationNode dec = (CssDeclarationNode) abstractDecl;
        boolean inPartition = byPartition
            || ORDER_DEPENDENT_PARTITIONS.contains(dec.getPropertyName().getPartition());
        for (CssSelectorNode sel : ruleset.getSelectors().childIterable()) {
          if (inPartition) {
            partitioner.add(dec, sel);
          } else {
            decToSel.put(dec, sel);
          }
        }
      }
    }

    private void deleteRuleset() {
      if (placeholderRuleset == null) {
        // We don't have a place holder yet. Create one.
        placeholderRuleset = new CssRulesetNode(new CssDeclarationBlockNode());
        visitController.replaceCurrentBlockChildWith(
            ImmutableList.of((CssNode) placeholderRuleset), false);
      } else {
        visitController.removeCurrentNode();
      }
    }
  }

  /**
   * The tree visitor for the replace phase, which replaces the placeholder with
   * the new computed rulesets.
   */
  private static class ReplacePhaseTreeVisitor extends DefaultTreeVisitor
      implements CssCompilerPass {
    private final MutatingVisitController visitController;
    private final List<CssNode> rulesets;
    private final CssRulesetNode placeholderRuleset;

    public ReplacePhaseTreeVisitor(
        MutatingVisitController visitController,
        List<CssNode> rulesets,
        CssRulesetNode placeholderRuleset) {
      this.visitController = visitController;
      this.rulesets = rulesets;
      this.placeholderRuleset = placeholderRuleset;
    }

    @Override
    public boolean enterRuleset(CssRulesetNode ruleset) {
      if (ruleset == placeholderRuleset) {
        visitController.replaceCurrentBlockChildWith(rulesets, false);
      }
      return true;
    }

    @Override
    public void runPass() {
      visitController.startVisit(this);
    }
  }

  /**
   * A partition organizes pairs of declaration/selector.
   */
  private static abstract class Partition {
    @SuppressWarnings("unused")
    protected final String partition;

    protected Partition(String partition) {
      this.partition = partition;
    }

    /**
     * Creates a new partition.
     */
    public static Partition newPartition(String partition) {
      return ORDER_DEPENDENT_PARTITIONS.contains(partition)
          ? new OrderDependentPartition(partition)
          : new OrderIndependentPartition(partition);
    }

    /**
     * Adds the given pair of declaration/selector to the partition. Assumes
     * that the selector belongs to this partition.
     */
    public abstract void add(CssDeclarationNode declaration, CssSelectorNode selector);

    /**
     * Adds the pairs of this partition to the given map.
     */
    public abstract void addTo(Map<Collection<CssDeclarationNode>, Set<CssSelectorNode>> outMap);

    /**
     * Returns the selectors keyed by the given declarations in the given map.
     */
    protected Set<CssSelectorNode> getSelectorsByDeclarations(
        Collection<CssDeclarationNode> declarations,
        Map<Collection<CssDeclarationNode>, Set<CssSelectorNode>> map) {
      Set<CssSelectorNode> selectors = map.get(declarations);
      if (selectors == null) {
        selectors = Sets.newTreeSet(TO_STRING_COMPARATOR);
        map.put(declarations, selectors);
      }
      return selectors;
    }
  }

  /**
   * A partition whose declarations are order-independent.
   */
  private static class OrderIndependentPartition extends Partition {
    private final Multimap<CssSelectorNode, CssDeclarationNode> inMap;

    private OrderIndependentPartition(String partition) {
      super(partition);
      this.inMap = TreeMultimap.create(TO_STRING_COMPARATOR, DECLARATION_COMPARATOR);
    }

    @Override
    public void add(CssDeclarationNode declaration, CssSelectorNode selector) {
      inMap.put(selector, declaration);
    }

    @Override
    public void addTo(Map<Collection<CssDeclarationNode>, Set<CssSelectorNode>> outMap) {
      for (Map.Entry<CssSelectorNode, Collection<CssDeclarationNode>> entry
          : inMap.asMap().entrySet()) {
        getSelectorsByDeclarations(entry.getValue(), outMap).add(entry.getKey());
      }
    }
  }

  /**
   * A partition whose declarations are order-dependent.
   */
  private static class OrderDependentPartition extends Partition {
    private final Map<CssSelectorNode, List<CssDeclarationNode>> inMap;

    private OrderDependentPartition(String partition) {
      super(partition);
      this.inMap = Maps.newTreeMap(TO_STRING_COMPARATOR);
    }

    @Override
    public void add(CssDeclarationNode declaration, CssSelectorNode selector) {
      getDeclarations(selector).add(declaration);
    }

    @Override
    public void addTo(Map<Collection<CssDeclarationNode>, Set<CssSelectorNode>> outMap) {
      for (Map.Entry<CssSelectorNode, List<CssDeclarationNode>> entry
          : inMap.entrySet()) {
        getSelectorsByDeclarations(entry.getValue(), outMap).add(entry.getKey());
      }
    }

    private List<CssDeclarationNode> getDeclarations(CssSelectorNode selector) {
      List<CssDeclarationNode> declarations = inMap.get(selector);
      if (declarations == null) {
        declarations = Lists.newLinkedList();
        inMap.put(selector, declarations);
      }
      return declarations;
    }
  }

  /**
   * A partitioner adds up pairs of declaration/selector to build a map from
   * groups of declarations in the same partition to selectors.
   */
  private static class Partitioner {
    /**
     * This map stores all the declaration/selector pairs added to the
     * partitioner, grouped by partitions.
     */
    private final Map<String, Partition> partitions = Maps.newHashMap();

    /**
     * Adds a declaration/selector pair to the partitioner.
     *
     * @param declaration the declaration of the pair to add
     * @param selector the selector of the pair to add
     */
    public void add(CssDeclarationNode declaration, CssSelectorNode selector) {
      String partitionName = declaration.getPropertyName().getPartition();
      getPartition(partitionName).add(declaration, selector);
    }

    /**
     * Computes a conveninent map from groups of declarations to sets of
     * selectors from all the additions to the partitioner.
     *
     * @return a map conveniently summarizing all the additions to the partitioner
     */
    public Map<Collection<CssDeclarationNode>, Set<CssSelectorNode>> getMap() {
      Map<Collection<CssDeclarationNode>, Set<CssSelectorNode>> map =
          Maps.newTreeMap(TO_STRING_ITERABLE_COMPARATOR);
      for (Partition partition : partitions.values()) {
        partition.addTo(map);
      }
      return map;
    }

    private Partition getPartition(String partitionName) {
      Partition partition = partitions.get(partitionName);
      if (partition == null) {
        partition = Partition.newPartition(partitionName);
        partitions.put(partitionName, partition);
      }
      return partition;
    }
  }

  /** Compares objects by their string representation. */
  @VisibleForTesting
  static final Comparator<Object> TO_STRING_COMPARATOR = Ordering.usingToString();

  /**
   * Create a comparator for iterables that compares element pairwise using the
   * given element comparator.
   */
  private static <T> Comparator<Iterable<? extends T>> createIterableComparator(
      final Comparator<T> elementComparator) {
    return new Comparator<Iterable<? extends T>>() {
      @Override
      public int compare(Iterable<? extends T> o1, Iterable<? extends T> o2) {
        Iterator<? extends T> i1 = o1.iterator();
        Iterator<? extends T> i2 = o2.iterator();
        while (true) {
          if (i1.hasNext() && !i2.hasNext()) {
            return 1;
          } else if (!i1.hasNext() && i2.hasNext()) {
            return -1;
          } else if (!i1.hasNext() && !i2.hasNext()) {
            return 0;
          } else {
            int c = elementComparator.compare(i1.next(), i2.next());
            if (c != 0) {
              return c;
            }
          }
        }
      }
    };
  }

  /** Compares iterables by their extended string representation. */
  @VisibleForTesting
  static final Comparator<Iterable<?>> TO_STRING_ITERABLE_COMPARATOR =
      createIterableComparator(TO_STRING_COMPARATOR);

  /**
   * Compare declarations, ensuring that shorthand properties appear before
   * their related properties and using their string representation otherwise.
   */
  @VisibleForTesting
  static final Comparator<CssDeclarationNode> DECLARATION_COMPARATOR =
      new Comparator<CssDeclarationNode>() {
    @Override
    public int compare(CssDeclarationNode o1, CssDeclarationNode o2) {
      CssPropertyNode property1 = o1.getPropertyName();
      CssPropertyNode property2 = o2.getPropertyName();
      if (property1.getShorthands().contains(property2.getProperty())) {
        return 1;
      } else if (property2.getShorthands().contains(property1.getProperty())) {
        return -1;
      } else {
        return TO_STRING_COMPARATOR.compare(o1, o2);
      }
    }
  };
}
