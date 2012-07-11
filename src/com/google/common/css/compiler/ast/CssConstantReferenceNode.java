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

package com.google.common.css.compiler.ast;

import com.google.common.css.SourceCodeLocation;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * A node holding a reference to a constant or an argument of a mixin
 * definition.
 *
 * <p>Because references can be valid within one block, each reference has a
 * scope where it is valid. For example, references to arguments of mixin
 * definitions are only valid in the block of the mixin definition whereas
 * references to global definitions are valid everywhere.
 *
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
public class CssConstantReferenceNode extends CssValueNode {
  private static final Pattern PATTERN = Pattern.compile("[A-Z_][A-Z0-9_]*");

  /**
   * The scope is initialized lazily because it can be impossible to determine
   * the scope during construction as the node might have no parent at that
   * time.
   */
  private CssNode scope;

  public CssConstantReferenceNode(String value,
      @Nullable SourceCodeLocation sourceCodeLocation) {
    super(value, sourceCodeLocation);
  }

  public CssConstantReferenceNode(String value) {
    this(value, null);
  }

  /**
   * Creates a new constant reference node that is a deep copy of the
   * given node.
   */
  public CssConstantReferenceNode(CssConstantReferenceNode node) {
    super(node);
    this.scope = node.scope;
  }
  
  @Override
  public CssConstantReferenceNode deepCopy() {
    return new CssConstantReferenceNode(this);
  }

  /**
   * Returns the scope of the reference. If the reference belongs to a global
   * definition, the scope is the 'global scope'. If the reference belongs to
   * an argument of a mixin definition, the scope is the mixin definition.
   */
  public CssNode getScope() {
    return scope;
  }

  public void setScope(CssNode scope) {
    this.scope = scope;
  }

  /**
   * Returns if the given identifier matches the pattern of a constant.
   */
  public static boolean isDefinitionReference(String ident) {
    return PATTERN.matcher(ident).matches();
  }
}
