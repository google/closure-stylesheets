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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssCommentNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPriorityNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Compiler pass that replaces font and font-family declaration subtrees
 * so that the tree structure resembles the (rather idiosyncratic) grammar
 * of the corresponding CSS properties.
 *
 */
public class FixupFontDeclarations extends DefaultTreeVisitor
    implements CssCompilerPass {

  /**
   * Specifies how the input tree should be interpreted.
   */
  public enum InputMode {
    /**
     * Assume the input follows the grammar of CSS.
     */
    CSS,
    /**
     * Perform a best-effort parse that allows unsubstituted definition uses.
     */
    GSS;
  }

  /**
   * Properties whose values may be specified in a @{code Font} declaration.
   */
  public enum FontProperty {
    STYLE, VARIANT, WEIGHT, SIZE, LINE_HEIGHT, FAMILY
  };

  /**
   * A simple predicate on {@code CssCompositeValueNode}.
   */
  private static class WithOperator
      implements Predicate<CssCompositeValueNode> {
    private final CssCompositeValueNode.Operator op;

    public WithOperator(CssCompositeValueNode.Operator op) {
      this.op = op;
    }

    @Override public boolean apply(CssCompositeValueNode n) {
      if (n.getOperator() != op) {
        return false;
      }
      return true;
    }
  }

  private static WithOperator withOperator(CssCompositeValueNode.Operator op) {
    return new WithOperator(op);
  }

  private static final String FONT = "font";
  private static final String FONT_FAMILY = "font-family";
  private static final String NORMAL = "normal";
  private static final String INHERIT = "inherit";
  private static final Set<String> SYSTEM_FONTS = ImmutableSet.of(
      "caption", "icon", "menu", "message-box", "small-caption", "status-bar");
  private static final Set<String> FONT_ABSOLUTE_SIZES = ImmutableSet.of(
      "xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large");
  private static final Set<String> FONT_RELATIVE_SIZES = ImmutableSet.of(
      "larger", "smaller");
  private static final Set<String> DEFINITELY_STYLE = ImmutableSet.of(
      "italic", "oblique");
  private static final Set<String> DEFINITELY_VARIANT = ImmutableSet.of(
      "small-caps");
  private static final Set<String> DEFINITELY_WEIGHT = ImmutableSet.of(
      "bold", "bolder", "lighter");
  private static final Set<String> NUMERIC_WEIGHTS = ImmutableSet.of(
      "100", "200", "300", "400", "500", "600", "700", "800", "900");

  /**
   * No point looking through lots of nodes for clues that are only allowed
   * in a prefix. This limit is conservative, assuming we create as many
   * top-level nodes as tokens. As we'll see shortly, we actually expect
   * the parser to structure things slightly more elaborately, resulting
   * in fewer top-level nodes.
   */
  private static final int PRE_FAMILY_LIMIT = 6;

  /**
   * Recognizes a simple size value represented by a node in the
   * original AST of a font declaration value.
   */
  private static final Predicate<CssValueNode> IS_PLAIN_SIZE =
      new Predicate<CssValueNode>() {
    @Override public boolean apply(CssValueNode n) {
      return (n instanceof CssNumericNode
              && (!CssNumericNode.NO_UNITS.equals(
                  ((CssNumericNode) n).getUnit())))
      || FONT_ABSOLUTE_SIZES.contains(n.getValue())
      || FONT_RELATIVE_SIZES.contains(n.getValue());
    }
  };

  /**
   * Picks out the size from the original AST subtree representing
   * size/line-height in a font declaration value.
   */
  private static final
      Function<CssCompositeValueNode, CssValueNode> EXTRACT_SIZE =
      new Function<CssCompositeValueNode, CssValueNode>() {
    @Override public CssValueNode apply(CssCompositeValueNode n) {
      return n.getValues().get(0);
    }
  };

  @VisibleForTesting
  static final String SIZE_AND_FAMILY_REQUIRED =
      "Size and family are required in the absence of a system font or a "
      + "simple inherit";

  @VisibleForTesting
  static final Map<FontProperty, String> TOO_MANY =
      ImmutableMap.<FontProperty, String>builder()
      .put(FontProperty.LINE_HEIGHT,
           "The '/' can occur at most once in a font shorthand value")
      .put(FontProperty.SIZE,
           "Font size can occur at most once in a font shorthand value")
      .put(FontProperty.STYLE,
           "Font style can occur at most once in a font shorthand value")
      .put(FontProperty.VARIANT,
           "Font variant can occur at most once in a font shorthand value")
      .put(FontProperty.WEIGHT,
           "Font weight can occur at most once in a font shorthand value")
      .build();

  private static final ImmutableSortedSet<FontProperty> SLOTTABLE_PROPERTIES =
      ImmutableSortedSet.of(
          FontProperty.STYLE, FontProperty.VARIANT, FontProperty.WEIGHT);

  @VisibleForTesting
  static final String TOO_MANY_NORMALS =
      "The keyword normal can occur at most thrice in a font shorthand value";

  @VisibleForTesting
  static final String NORMAL_TOO_LATE =
      "The keyword normal is only allowed in the first three tokens of a "
      + "font shorthand";

  @VisibleForTesting
  static final String SIZE_AFTER_HEIGHT =
      "Font size must be specified before line-height";

  @VisibleForTesting
  static final String TOO_MANY_PRE_SIZE =
      "Too many font shorthand tokens before size";

  @VisibleForTesting
  static final String PRE_SIZE_INTERLOPER_SIZE =
      "Unrecognized tokens immediately preceding size";

  /**
   * Should the input be parsed strictly or do we assume gss variables
   * might be substituted later?
   */
  private final InputMode mode;

  private final ErrorManager errorManager;

  private final MutatingVisitController visitController;

  private CssTree tree;

  public FixupFontDeclarations(
      InputMode mode, ErrorManager errorManager, CssTree tree) {
    this.mode = mode;
    this.errorManager = errorManager;
    this.tree = tree;
    visitController = tree.getMutatingVisitController();
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode decl) {
    String propertyName = decl.getPropertyName().getProperty().getName();
    if (!(FONT.equals(propertyName) || FONT_FAMILY.equals(propertyName))) {
      return false;
    }

    CssDeclarationNode d = decl.deepCopy();
    List<CssDeclarationNode> replacement = ImmutableList.of(d);
    if (FONT.equals(propertyName)) {
      d.setPropertyValue(reparseFont(decl.getPropertyValue()));
    } else if (FONT_FAMILY.equals(propertyName)) {
      d.setPropertyValue(reparseFontFamily(decl.getPropertyValue()));
    }
    visitController.replaceCurrentBlockChildWith(replacement, false);
    return false;
  }

  private CssPropertyValueNode reparseFont(CssPropertyValueNode n) {
    // Preliminary easy cases
    if (n.numChildren() == 0) {
      return n.deepCopy();
    } else if (n.numChildren() == 1
               && SYSTEM_FONTS.contains(n.getChildAt(0).getValue())
               || INHERIT.equals(n.getChildAt(0).getValue())) {
      return n.deepCopy();
    } else if (n.numChildren() < 2) {
      if (mode == InputMode.CSS) {
        errorManager.report(
            new GssError(SIZE_AND_FAMILY_REQUIRED, getSourceCodeLocation(n)));
      }
      return n.deepCopy();
    }

    // Some clients want to be able to whitelist typefaces or otherwise
    // understand the font shorthand in detail. Unbound GSS variables make
    // this pretty hopeless:
    //   font: CLUELESS;
    // but we can try:
    //   font: italic bold SIZE/LEADING FAMILIES;
    // and a best-effort parse tree for that might be useful.
    //
    // The best case for us is CSS, where we can rely on clues such as
    // the keywords that can disambiguate some cases of style vs. variant
    // and the slash token that tells us that the surrounding tokens are
    // size and line-height and not e.g., size and a family name.
    //
    // Our strategy is to segment the property value and deal with each
    // segment independently. Segmentation isn't straightforward because
    // tokens can't always be understood independently:
    //   font: normal italic 100 medium medium roman regular;
    //         ^the next two tokens tell us this normal specifies a font-variant
    //                       ^this is easily understood to be a font-weight
    //                           ^obviously a font-size
    //                                  ^because we're past size, this must
    //                                   be a font-family name component

    // Try to recognize things individually, then check for conflicts
    // among our constraints, and finally rebuild our AST.

    Iterable<CssValueNode> preFamilyCandidates =
        Iterables.limit(n.childIterable(), PRE_FAMILY_LIMIT);

    Iterable<CssCompositeValueNode> sizeLineHeights =
        Iterables.filter(
            extractByType(CssCompositeValueNode.class, preFamilyCandidates),
            withOperator(CssCompositeValueNode.Operator.SLASH));
    Iterable<CssValueNode> plainSizes =
        Iterables.filter(
            preFamilyCandidates,
            IS_PLAIN_SIZE);
    Iterable<CssValueNode> lhSizes =
        Iterables.transform(
            sizeLineHeights,
            EXTRACT_SIZE);
    final HashMap<CssNode, Integer> lexicalOrder =
        EnumeratingVisitor.enumerate(tree);
    Iterable<CssValueNode> sizes = Iterables.concat(plainSizes, lhSizes);
    if (!validateSplitPoint(
            getSourceCodeLocation(n), lexicalOrder, sizeLineHeights, sizes)) {
      return n.deepCopy();
    }

    // Now we can split on the one and only size node.
    final CssValueNode splitPoint =
        Iterables.getOnlyElement(Iterables.concat(sizeLineHeights, plainSizes));
    Iterable<CssValueNode> prefix =
        takeWhile(n.childIterable(), new Predicate<CssValueNode>() {
            @Override public boolean apply(CssValueNode n) {
              return lexicalOrder.get(n).compareTo(
                  lexicalOrder.get(splitPoint)) < 0;
            }
          });
    final CssPriorityNode priority = getPriority(n);
    Iterable<CssValueNode> families =
        dropWhile(
            takeUntil(n.childIterable(), priority),
            new Predicate<CssValueNode>() {
              @Override public boolean apply(CssValueNode n) {
                return lexicalOrder.get(splitPoint).compareTo(
                    lexicalOrder.get(n)) < 0;
              }
            });
    final Map<CssValueNode, FontProperty> properties =
        classifyNodes(prefix, sizes, sizeLineHeights);

    // Validate analysis
    validatePrefix(prefix);
    validateProperties(prefix, properties);

    // Build output
    return rebuildFont(prefix, splitPoint, families, priority, properties, n);
  }

  private CssPriorityNode getPriority(CssPropertyValueNode n) {
    if (n.numChildren() < 1) return null;

    CssNode last = n.getChildAt(n.numChildren() - 1);
    if (last instanceof CssPriorityNode) {
      return (CssPriorityNode) last;
    } else {
      return null;
    }
  }

  private <T> Iterable<T> takeUntil(Iterable<T> xs, final T excludedEndpoint) {
    return takeWhile(
        xs,
        new Predicate<T>() {
          @Override public boolean apply(T i) {
            return excludedEndpoint != i;
          }
        });
  }

  private Map<CssValueNode, FontProperty> classifyNodes(
      Iterable<CssValueNode> prefix,
      Iterable<CssValueNode> sizes,
      Iterable<CssCompositeValueNode> sizeLineHeights) {
    final Map<CssValueNode, FontProperty> properties = Maps.newHashMap();
    for (CssValueNode i : prefix) {
      if (DEFINITELY_STYLE.contains(i.getValue())) {
        properties.put(i, FontProperty.STYLE);
      } else if (DEFINITELY_VARIANT.contains(i.getValue())) {
        properties.put(i, FontProperty.VARIANT);
      } else if (isWeight(i)) {
        properties.put(i, FontProperty.WEIGHT);
      }
    }
    properties.put(Iterables.getOnlyElement(sizes), FontProperty.SIZE);
    if (!Iterables.isEmpty(sizeLineHeights)) {
      properties.put(
          Iterables.getOnlyElement(sizeLineHeights).getValues().get(1),
          FontProperty.LINE_HEIGHT);
    }
    return properties;
  }

  private boolean isWeight(CssValueNode n) {
    if (DEFINITELY_WEIGHT.contains(n.getValue())) {
      return true;
    }
    if (!(n instanceof CssNumericNode)) {
      return false;
    }
    CssNumericNode numeric = (CssNumericNode) n;
    if (!CssNumericNode.NO_UNITS.equals(numeric.getUnit())) {
      return false;
    }
    return (NUMERIC_WEIGHTS.contains(numeric.getNumericPart()));
  }

  private void validateSizeLineHeight(CssCompositeValueNode composite) {
    if (composite.getValues().size() != 2) {
      reportError(TOO_MANY.get(FontProperty.LINE_HEIGHT),
                  getSourceCodeLocation(composite));
    }
  }

  private void reportError(String message, SourceCodeLocation location) {
    errorManager.report(new GssError(message, location));
  }

  private boolean validateSplitPoint(
      SourceCodeLocation loc,
      HashMap<CssNode, Integer> lexicalOrder,
      Iterable<CssCompositeValueNode> sizeLineHeights,
      Iterable<CssValueNode> sizes) {
    CssCompositeValueNode secondSLH = Iterables.get(sizeLineHeights, 1, null);
    if (secondSLH != null) {
      reportError(TOO_MANY.get(FontProperty.LINE_HEIGHT),
                  getSourceCodeLocation(secondSLH));
      return false;
    }
    CssCompositeValueNode slashy = Iterables.find(
        sizeLineHeights,
        new Predicate<CssCompositeValueNode>() {
          @Override public boolean apply(CssCompositeValueNode n) {
            return n.getValues().size() != 2;
          }
        },
        null);
    if (slashy != null) {
      reportError(TOO_MANY.get(FontProperty.LINE_HEIGHT),
                  getSourceCodeLocation(slashy));
    }
    if (Iterables.isEmpty(sizes)) {
      if (mode == InputMode.CSS) {
        reportError(SIZE_AND_FAMILY_REQUIRED, loc);
      }
      return false;
    }
    if (Iterables.get(sizes, 1, null) != null) {
      reportError(TOO_MANY.get(FontProperty.SIZE),
                  getSourceCodeLocation(
                      max(sizes, Functions.forMap(lexicalOrder))));
      return false;
    }
    return true;
  }

  private void validatePrefix(
      Iterable<CssValueNode> prefix) {
    CssValueNode tooMuch = Iterables.get(prefix, 3, null);
    if (tooMuch != null) {
      reportError(TOO_MANY_PRE_SIZE, getSourceCodeLocation(tooMuch));
    }
  }

  private void validateProperties(
      Iterable<CssValueNode> prefix,
      final Map<CssValueNode, FontProperty> classified) {
    final List<CssValueNode> normals = Lists.newLinkedList();
    for (CssValueNode i : prefix) {
      if (!classified.containsKey(i) && NORMAL.equals(i.getValue())) {
        normals.add(i);
      }
    }
    if (normals.size() > 3) {
      errorManager.report(
          new GssError(
              TOO_MANY_NORMALS,
              getSourceCodeLocation(normals.get(normals.size() - 1))));
    }
    HashSet<FontProperty> properties = Sets.newHashSet();
    for (Map.Entry<CssValueNode, FontProperty> p : classified.entrySet()) {
      if (!properties.add(p.getValue())) {
        reportError(TOO_MANY.get(p.getValue()),
                    getSourceCodeLocation(p.getKey()));
      }
    }
    if (mode == InputMode.CSS) {
      CssValueNode interloper =
          Iterables.find(
              prefix,
              new Predicate<CssValueNode>() {
                @Override public boolean apply(CssValueNode n) {
                  return !classified.containsKey(n) && !normals.contains(n);
                }
              },
              null);
      if (interloper != null) {
        reportError(PRE_SIZE_INTERLOPER_SIZE,
                    getSourceCodeLocation(interloper));
      }
    }
  }

  private CssPropertyValueNode rebuildFont(
      Iterable<CssValueNode> prefix,
      CssValueNode splitPoint,
      Iterable<CssValueNode> families,
      CssPriorityNode priority,
      final Map<CssValueNode, FontProperty> properties,
      CssPropertyValueNode n) {
    TreeMap<FontProperty, CssValueNode> parts =
        Maps.newTreeMap();
    for (Map.Entry<CssValueNode, FontProperty> p : properties.entrySet()) {
      parts.put(p.getValue(), p.getKey());
    }
    List<CssValueNode> preFamily = Lists.newArrayList();
    Iterables.addAll(preFamily, prefix);
    if (parts.containsKey(FontProperty.SIZE)) {
      preFamily.add(parts.get(FontProperty.SIZE));
    }
    if (parts.containsKey(FontProperty.LINE_HEIGHT)) {
      CssValueNode lineHeight = parts.get(FontProperty.LINE_HEIGHT);
      preFamily.add(new CssLiteralNode("/", getSourceCodeLocation(lineHeight)));
      preFamily.add(lineHeight);
    }
    List<CssValueNode> tail =
        Iterables.isEmpty(families)
        ? ImmutableList.<CssValueNode>of()
        : ImmutableList.<CssValueNode>of(reparseFamilies(
            families,
            getSourceCodeLocation(Iterables.get(families, 0))));
    ImmutableList.Builder<CssValueNode> resultNodes = ImmutableList.builder();
    resultNodes.addAll(Iterables.concat(preFamily, tail));
    if (priority != null) {
      resultNodes.add(priority);
    }
    CssPropertyValueNode result = new CssPropertyValueNode(resultNodes.build());
    return result.deepCopy();
  }

  private CssCompositeValueNode reparseFamilies(
      Iterable<CssValueNode> families, SourceCodeLocation loc) {
    // they will be a comma-delimited sequence of (strings | id-sequences)
    List<CssValueNode> alternatives = Lists.newArrayList();
    List<CssCommentNode> commentsOnAlternatives = Lists.newArrayList();
    for (CssValueNode i : families) {
      if (i instanceof CssCompositeValueNode) {
        CssCompositeValueNode segment = (CssCompositeValueNode) i;
        if (segment.getValues().size() == 0) {
          continue;
        }
        CssValueNode first = Iterables.getFirst(segment.getValues(), null);
        collect(alternatives, first);
        Iterable<CssValueNode> rest = Iterables.skip(segment.getValues(), 1);
        Iterables.addAll(alternatives, rest);
        for (CssNode j : rest) {
          for (CssCommentNode c : j.getComments()) {
            commentsOnAlternatives.add(c);
          }
        }
      } else {
        collect(alternatives, i);
      }
    }
    CssCompositeValueNode result = new CssCompositeValueNode(
        alternatives, CssCompositeValueNode.Operator.COMMA, loc);
    for (CssCommentNode c : commentsOnAlternatives) {
      result.appendComment(c);
    }
    return result;
  }

  private CssPropertyValueNode reparseFontFamily(CssPropertyValueNode n) {
    if (n.numChildren() == 0) {
      return n.deepCopy();
    }
    if (n.numChildren() == 1
        && INHERIT.equals(n.getChildAt(0).getValue())) {
      return n.deepCopy();
    }
    CssPriorityNode priority = getPriority(n);
    // deal with alternatives
    CssCompositeValueNode altNode = reparseFamilies(
            takeUntil(n.childIterable(), priority),
            getSourceCodeLocation(n));
    ImmutableList.Builder<CssValueNode> result = ImmutableList.builder();
    result.add(altNode);
    if (priority != null) {
      result.add(priority);
    }
    return new CssPropertyValueNode(result.build());
  }

  /**
   * Agglomerate each id sequence into a space-separated literal value,
   * leaving strings as they were and fixing up comments and
   * SourceCodeLocations as best we can.
   */
  private void collect(List<CssValueNode> alternatives, CssValueNode item) {
    if (isString(item)) {
      alternatives.add(item);
    } else {
      CssValueNode stump;
      if (alternatives.size() > 0
          && !isString(alternatives.get(alternatives.size() - 1))) {
        // concatenate onto previous node
        stump = alternatives.get(alternatives.size() - 1);
        stump.setValue(stump.getValue() + " ");
      } else {
        // start a new node
        stump = new CssLiteralNode("", getSourceCodeLocation(item));
        alternatives.add(stump);
      }
      stump.setValue(stump.getValue() + item.getValue());
      for (CssCommentNode c : item.getComments()) {
        stump.appendComment(c);
      }
    }
  }

  private boolean isString(CssValueNode n) {
    // TODO(user): parse strings fully in javacc rather than making this
    // pass and all others that care about reading or writing strings don't
    // have to reimplement parsing the sublanguage of CSS strings.
    return n instanceof CssLiteralNode
        && n.getValue() != null
        && (n.getValue().startsWith("'") || n.getValue().startsWith("\""));
  }

  private static SourceCodeLocation getSourceCodeLocation(CssNode n) {
    n = Iterables.find(n.ancestors(), new Predicate<CssNode>() {
        @Override public boolean apply(CssNode n) {
          return n.getSourceCodeLocation() != null;
        }
      }, null);
    return n != null
        ? n.getSourceCodeLocation()
        : new SourceCodeLocation(
            new SourceCode(null, "x"),
            1, 1, 1, 1, 1, 1);
  }

  /**
   * Computes the prefix of {@code xs} for which {@code p} holds.
   */
  private <T> Iterable<T> takeWhile(
      final Iterable<T> xs, final Predicate<? super T> p) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new UnmodifiableIterator<T>() {
          Iterator<T> xsi = xs.iterator();
          boolean validT = false;
          T t;
          {
            next();
          }
          @Override
          public boolean hasNext() {
            return validT;
          }
          @Override
          public T next() {
            T result = t;
            if (xsi.hasNext()) {
              t = xsi.next();
              validT = p.apply(t);
            } else {
              validT = false;
            }
            return result;
          }
        };
      }
    };
  }

  /**
   * Computes the suffix of {@code xs} starting at the first node for which
   * {@code p} fails.
   *
   * {@code Iterables.concat(takeWhile(xs, p), dropWhile(xs, p)) = xs}
   */
  private <T> Iterable<T> dropWhile(
      final Iterable<T> xs, final Predicate<? super T> p) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        PeekingIterator<T> xsi = Iterators.peekingIterator(xs.iterator());
        while (xsi.hasNext()) {
          if (p.apply(xsi.peek())) {
            break;
          }
          xsi.next();
        }
        return xsi;
      }
    };
  }

  /**
   * Returns an element of {@code xs} whose image under {@code f} is
   * maximal.
   */
  private <T, U extends Comparable<U>> T max(
      Iterable<T> xs, Function<? super T, U> f) {
    T result = Iterables.getFirst(xs, null);
    U extreme = f.apply(result);
    for (T x : xs) {
      U u = f.apply(x);
      if (extreme.compareTo(u) < 0) {
        result = x;
        extreme = u;
      }
    }
    return result;
  }

  /**
   * Filter to elements of type {@code T} designated by {@code ct}
   * and cast the result.
   */
  private <T> Iterable<T> extractByType(
      final Class<T> ct, Iterable<? super T> xs) {
    return Iterables.transform(
        Iterables.filter(xs, Predicates.instanceOf(ct)),
        new Function<Object, T>() {
          @Override public T apply(Object x) {
            return ct.cast(x);
          }
        });
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }
}
