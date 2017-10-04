/*
 * Copyright 2015 Google Inc.
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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssCommentNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssValueNode;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A utility for the AutoExpandBrowserPrefix pass, which provides a list of rules
 * that govern automatic addition of browser specific property declarations.
 *
 * <p>A rule could be matched on property name or value alone, or both property name and value.
 * If the value is a function, then the function names must match.
 *
 * <p>Each rule, if matched, provides a set of placeholder expansion nodes - which will
 * be cloned and swapped into the tree, as part of AutoExpandBrowserPrefix pass.
 */
public final class BrowserPrefixRule {
  private final Optional<String> matchPropertyName;
  private final Optional<String> matchPropertyValue;
  private final boolean isFunction;
  private final List<CssDeclarationNode> expansionNodes = Lists.newArrayList();
  private final List<CssPropertyValueNode> valueOnlyExpansionNodes = Lists.newArrayList();

  private BrowserPrefixRule(Builder builder) {
    checkState(builder.matchPropertyName != null || builder.matchPropertyValue != null);
    this.matchPropertyName = Optional.fromNullable(builder.matchPropertyName);
    this.matchPropertyValue = Optional.fromNullable(builder.matchPropertyValue);
    this.isFunction = builder.isFunction;

    // Pre-compute placeholder expansion nodes for this rule.
    // Either expandPropertyValueList or expandPropertyNameList will be non-empty.
    if (builder.expandPropertyValueList.isEmpty()) {
      checkState(!builder.expandPropertyNameList.isEmpty());
      // Case #1: The value expansion list is empty and there is only a property name to expand.
      for (String propertyName : builder.expandPropertyNameList) {
        CssPropertyNode propertyNode = new CssPropertyNode(propertyName);
        // The property value will be set, when matched.
        CssPropertyValueNode valueNode = new CssPropertyValueNode();
        CssDeclarationNode node = new CssDeclarationNode(propertyNode, valueNode);
        node.appendComment(new CssCommentNode("/* @alternate */", null));
        expansionNodes.add(node);
      }
    }
    if (builder.expandPropertyNameList.isEmpty()) {
      checkState(!builder.expandPropertyValueList.isEmpty());
      for (String propertyValue : builder.expandPropertyValueList) {
        CssPropertyValueNode valueNode = new CssPropertyValueNode();
        if (isFunction) {
          // Case #3: Property value is a function
          checkState(!builder.expandPropertyValueList.isEmpty());
          CssFunctionNode functionNode = new CssFunctionNode(
              CssFunctionNode.Function.byName(propertyValue), null);
          // Function args will be set, when matched.
          valueNode.addChildToBack((CssValueNode) functionNode);
        } else {
          // Case #2: Property value is not a function
          checkState(matchPropertyValue != null);  // Has both name and value
          valueNode.addChildToBack(new CssLiteralNode(propertyValue));
        }
        if (matchPropertyName.isPresent()) {
          // If the property name is present a full declaration expansion can be created.
          CssPropertyNode propertyNode = new CssPropertyNode(matchPropertyName.get());
          CssDeclarationNode node = new CssDeclarationNode(propertyNode, valueNode);
          node.appendComment(new CssCommentNode("/* @alternate */", null));
          expansionNodes.add(node);
        } else {
          // Since the property name is not present there is a value-only expansion added.
          // The property name will be taken from the original declaration.
          valueOnlyExpansionNodes.add(valueNode);
        }
      }
    }
  }

  @Nullable
  public String getMatchPropertyName() {
    return matchPropertyName.orNull();
  }

  @Nullable
  public String getMatchPropertyValue() {
    return matchPropertyValue.orNull();
  }

  public boolean isFunction() {
    return isFunction;
  }

  /**
   * @return A list of expansion nodes that contain a property names, and may contain a value. These
   *     nodes should be used when replacing declarations by matching property name.
   */
  public List<CssDeclarationNode> getExpansionNodes() {
    return expansionNodes;
  }

  /**
   * @return A list of value-only expansion nodes, meaning they do not contain a property name.
   * These nodes should be used when replacing declarations by matching the value only.
   */
  List<CssPropertyValueNode> getValueOnlyExpansionNodes() {
    return valueOnlyExpansionNodes;
  }

  static class Builder {
    @Nullable private String matchPropertyName = null;
    @Nullable private String matchPropertyValue = null;
    List<String> expandPropertyNameList = Lists.newArrayList();
    List<String> expandPropertyValueList = Lists.newArrayList();
    private boolean isFunction = false;

    /**
     * Property name to match.
     */
    Builder matchPropertyName(String propertyName) {
      this.matchPropertyName = propertyName;
      return this;
    }

    /**
     * Property value to match.
     */
    Builder matchPropertyValue(String propertyValue) {
      this.matchPropertyValue = propertyValue;
      return this;
    }

    /**
     * Whether the property value to be *matched* is a function.
     * For example the following is a function:
     * background-image: linear-gradient(GRADIENT);
     */
    Builder isFunction(boolean isFunction) {
      this.isFunction = isFunction;
      return this;
    }

    /**
     * Property name to add as expansion.
     */
    Builder addExpandPropertyName(String propertyName) {
      expandPropertyNameList.add(propertyName);
      return this;
    }

    /**
     * Property value to add as expansion.
     */
    Builder addExpandPropertyValue(String propertyValue) {
      expandPropertyValueList.add(propertyValue);
      return this;
    }

    BrowserPrefixRule build() {
      return new BrowserPrefixRule(this);
    }
  }
}
