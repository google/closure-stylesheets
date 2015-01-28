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
 * <p>A rule could be matched on property name alone or both property name and value.
 * If the value is a function, then the function names must match.
 *
 * <p>Each rule, if matched, provides a set of placeholder expansion nodes - which will
 * be cloned and swapped into the tree, as part of AutoExpandBrowserPrefix pass.
 */
class BrowserPrefixRule {
  private String matchPropertyName;
  private Optional<String> matchPropertyValue;
  private boolean isFunction;
  private List<CssDeclarationNode> expansionNodes = Lists.newArrayList();

  private BrowserPrefixRule(Builder builder) {
    checkState(!builder.matchPropertyName.isEmpty());
    this.matchPropertyName = builder.matchPropertyName;
    if (builder.matchPropertyValue != null) {
      this.matchPropertyValue = Optional.of(builder.matchPropertyValue);
    } else {
      this.matchPropertyValue = Optional.absent();
    }
    this.isFunction = builder.isFunction;

    // Pre-compute placeholder expansion nodes for this rule.
    // Either expandPropertyValueList or expandPropertyNameList will be non-empty.
    if (builder.expandPropertyValueList.isEmpty()) {
      checkState(!builder.expandPropertyNameList.isEmpty());
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
        CssPropertyNode propertyNode = new CssPropertyNode(matchPropertyName);
        CssPropertyValueNode valueNode = new CssPropertyValueNode();
        if (isFunction) {
          checkState(!builder.expandPropertyValueList.isEmpty());
          CssFunctionNode functionNode = new CssFunctionNode(
              CssFunctionNode.Function.byName(propertyValue), null);
          // Function args will be set, when matched.
          valueNode.addChildToBack((CssValueNode) functionNode);
        } else {
          checkState(matchPropertyValue != null);  // Has both name and value
          valueNode.addChildToBack(new CssLiteralNode(propertyValue));
        }
        CssDeclarationNode node = new CssDeclarationNode(propertyNode, valueNode);
        node.appendComment(new CssCommentNode("/* @alternate */", null));
        expansionNodes.add(node);
      }
    }
  }

  String getMatchPropertyName() {
    return matchPropertyName;
  }

  @Nullable String getMatchPropertyValue() {
    return matchPropertyValue.orNull();
  }

  boolean isFunction() {
    return isFunction;
  }

  List<CssDeclarationNode> getExpansionNodes() {
    return expansionNodes;
  }

  static class Builder {
    private String matchPropertyName;
    @Nullable private String matchPropertyValue = null;
    List<String> expandPropertyNameList = Lists.newArrayList();
    List<String> expandPropertyValueList = Lists.newArrayList();
    private boolean isFunction = false;

    Builder matchPropertyName(String propertyName) {
      this.matchPropertyName = propertyName;
      return this;
    }

    Builder matchPropertyValue(String propertyValue) {
      this.matchPropertyValue = propertyValue;
      return this;
    }

    Builder isFunction(boolean isFunction) {
      this.isFunction = isFunction;
      return this;
    }

    Builder addExpandPropertyName(String propertyName) {
      expandPropertyNameList.add(propertyName);
      return this;
    }

    Builder addExpandPropertyValue(String propertyValue) {
      expandPropertyValueList.add(propertyValue);
      return this;
    }

    BrowserPrefixRule build() {
      return new BrowserPrefixRule(this);
    }
  }
}
