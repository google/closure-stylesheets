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

import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssSelectorNode.Specificity;

/**
 * A node representing a refiner of a selector in the AST.
 * Examples: <code>.class</code>, <code>#id</code>,
 * <code>:nth-child(2n+1)</code>, <code>:not(#id)</code>
 *
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
public abstract class CssRefinerNode extends CssNode {
  /** Type of refiner. */
  protected Refiner refinerType;
  /** Name of the refiner. */
  protected String refinerName;

  /**
   * Contains the list of all possible CSS refiners.
   */
  // TODO(fbenz): The handling of the pre- and suffixes should be moved to
  // the corresponding classes.
  protected enum Refiner {
    CLASS(".", ""),
    ID("#", ""),
    PSEUDO_CLASS(":", ""),
    PSEUDO_ELEMENT("::", ""),
    ATTRIBUTE("[", "]");

    private final String prefix;
    private final String suffix;

    private Refiner(String prefix, String suffix) {
      this.prefix = prefix;
      this.suffix = suffix;
    }

    @Override
    public String toString() {
      return prefix + suffix;
    }

    public String getPrefix() {
      return prefix;
    }

    public String getSuffix() {
      return suffix;
    }
  }

  protected CssRefinerNode(Refiner refinerType,
      String refinerName, SourceCodeLocation sourceCodeLocation) {
    super(sourceCodeLocation);
    this.refinerType = refinerType;
    this.refinerName = refinerName;
  }

  public String getRefinerName() {
    return refinerName;
  }

  public Refiner getRefinerType() {
    return refinerType;
  }

  public String getPrefix() {
    return refinerType.getPrefix();
  }

  public String getSuffix() {
    return refinerType.getSuffix();
  }

  public abstract Specificity getSpecificity();
}
