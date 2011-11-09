/*
 * Copyright 2008 Google Inc.
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

package com.google.common.css.compiler.ast;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.css.SourceCodeLocation;

import javax.annotation.Nullable;

/**
 * A node representing a selector in the AST.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssSelectorNode extends CssNode implements ChunkAware {
  /** Reference to a list of refiners. */
  private CssRefinerListNode refiners;
  /** Reference to a combinator of selectors. */
  private CssCombinatorNode combinator = null;
  /** Name of the selector held by this node. */
  private String selectorName;
  /** The chunk this selector belongs to. */
  private Object chunk;

  /**
   * Constructor of a selector node.
   *
   * @param selectorName
   * @param sourceCodeLocation
   */
  public CssSelectorNode(@Nullable String selectorName,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(sourceCodeLocation);
    this.selectorName = selectorName;
    this.refiners = new CssRefinerListNode();
    becomeParentForNode(this.refiners);
  }

  /**
   * Constructor of a selector node.
   *
   * @param selectorName
   */
  public CssSelectorNode(String selectorName) {
    this(selectorName, null);
  }

  /**
   * Copy-constructor of a selector node.
   *
   * @param node
   */
  public CssSelectorNode(CssSelectorNode node) {
    this(node.getSelectorName(), node.getSourceCodeLocation());
    this.chunk = node.getChunk();
    this.refiners = node.getRefiners().deepCopy();
    becomeParentForNode(this.refiners);
    if (node.getCombinator() != null) {
      this.combinator = node.getCombinator().deepCopy();
      becomeParentForNode(this.combinator);
    }
  }

  @Override
  public CssSelectorNode deepCopy() {
    return new CssSelectorNode(this);
  }

  public CssRefinerListNode getRefiners() {
    return refiners;
  }

  public void setRefiners(CssRefinerListNode refiners) {
    removeAsParentOfNode(this.refiners);
    this.refiners = refiners;
    becomeParentForNode(this.refiners);
  }

  public CssCombinatorNode getCombinator() {
    return combinator;
  }

  public void setCombinator(CssCombinatorNode combinator) {
    if (this.combinator != null) {
      removeAsParentOfNode(this.combinator);
    }
    this.combinator = combinator;
    becomeParentForNode(this.combinator);
  }

  public void setSelectorName(String selectorName) {
    this.selectorName = selectorName;
  }

  public String getSelectorName() {
    return selectorName;
  }

  public Specificity getSpecificity() {
    return Specificity.of(this);
  }

  @Override
  public void setChunk(Object chunk) {
    this.chunk = chunk;
  }

  @Override
  public Object getChunk() {
    return chunk;
  }

  /**
   * For debugging only.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (selectorName != null) {
      sb.append(selectorName);
    }
    if (!refiners.isEmpty()) {
      for (CssRefinerNode node : refiners.childIterable()) {
        sb.append(node.toString());
      }
    }
    if (combinator != null) {
      sb.append(combinator.toString());
    }
    return sb.toString();
  }

  /**
   * The specifity of a selector is used to select among rules with the same
   * importance and origin. It is calculated as specified at
   * http://www.w3.org/TR/CSS2/cascade.html#specificity.
   */
  public static class Specificity implements Comparable<Specificity> {
    /**
     * Counts 1 if the declaration is from is a 'style' attribute rather than
     * a rule with a selector, 0 otherwise
     */
     // a omitted as always 0

    /**
     * Counts the number of ID attributes in the selector.
     */
    private final int b;

    /**
     * Counts the number of other attributes and pseudo-classes in the selector.
     */
    private final int c;

    /**
     * Counts the number of element names and pseudo-elements in the selector.
     */
    private final int d;

    Specificity(int b, int c, int d) {
      this.b = b;
      this.c = c;
      this.d = d;
    }

    private static Specificity of(CssSelectorNode s) {
      int b = 0;
      int c = 0;
      int d = 0;
      if (s.selectorName != null
          && !s.selectorName.isEmpty()
          && !s.selectorName.equals("*")) {
        d++;
      }

      for (CssRefinerNode refiner : s.refiners.childIterable()) {
        Specificity refinerSecificity = refiner.getSpecificity();
        b += refinerSecificity.b;
        c += refinerSecificity.c;
        d += refinerSecificity.d;
      }

      if (s.combinator != null) {
        Specificity o = s.combinator.getSelector().getSpecificity();
        b += o.b;
        c += o.c;
        d += o.d;
      }

      return new Specificity(b, c, d);
    }

    @Override
    public int compareTo(Specificity other) {
      return ComparisonChain.start()
          .compare(b, other.b)
          .compare(c, other.c)
          .compare(d, other.d)
          .result();
    }

    @Override
    public boolean equals(@Nullable Object object) {
      if (object instanceof Specificity) {
        Specificity that = (Specificity) object;
        return this.b == that.b && this.c == that.c && this.d == that.d;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(b, c, d);
    }

    @Override
    public String toString() {
      return "0," + b + "," + c + "," + d;
    }
  }
}
