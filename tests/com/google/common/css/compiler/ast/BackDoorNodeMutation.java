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

/**
 * A back door class to nodes mutation that should be used only for testing.
 * This class is work in progress.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class BackDoorNodeMutation {

  public static void addDeclarationToRuleset(
      CssRulesetNode ruleset,
      CssDeclarationNode declaration) {
    ruleset.getDeclarations().addChildToBack(declaration);
  }

  public static void addPropertyValueToDeclaration(
      CssDeclarationNode declaration,
      CssValueNode node) {
    declaration.getPropertyValue().addChildToBack(node);
  }

  public static void addRuleToConditionalBlock(
      CssConditionalBlockNode node,
      CssConditionalRuleNode rule) {
    node.addChildToBack(rule);
  }

  public static void setParent(CssNode child, CssNode parent) {
    parent.becomeParentForNode(child);
  }

  public static <T extends CssNode> void addChildToBack(
      CssNodesListNode<T> parent,
      T child) {
    parent.addChildToBack(child);
  }
}
