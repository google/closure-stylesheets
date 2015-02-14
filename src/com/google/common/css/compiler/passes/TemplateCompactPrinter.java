/*
 * Copyright 2010 Google Inc.
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

import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssImportRuleNode;
import com.google.common.css.compiler.ast.CssKeyframeRulesetNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssPageRuleNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;

/**
 * Printer for templates, which outputs GSS with holes to be filled
 * for references. In addition, the declaration boundaries are
 * explicitly noted, so that a declaration can be removed if it ends
 * up empty.
 *
 * @param <T> type of chunk id objects
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class TemplateCompactPrinter<T> extends ChunkCompactPrinter<T> {

  public static final char REFERENCE_START = '\u0123';
  public static final char REFERENCE_END = '\u0122';

  public static final char REFERENCE_START_OLD = '$';
  public static final char REFERENCE_END_OLD = '^';

  public static final char DECLARATION_START = '\u0105';
  public static final char DECLARATION_END = '\u0104';

  public static final char RULE_START = '\u0118';
  public static final char RULE_END = '\u0119';

  /**
   * Create a template printer for a given chunk.
   *
   * @param tree CSS AST to be printed (with regard to a selected chunk)
   * @param chunk the chunk selected for printing
   */
  public TemplateCompactPrinter(CssTree tree, T chunk) {
    super(tree, chunk);
  }

  @Override
  protected void appendValueNode(CssValueNode node) {
    if (node instanceof CssConstantReferenceNode) {
      append(REFERENCE_START);
      super.appendValueNode(node);
      append(REFERENCE_END);
    } else {
      super.appendValueNode(node);
    }
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    append(DECLARATION_START);
    return super.enterDeclaration(declaration);
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode declaration) {
    super.leaveDeclaration(declaration);
    append(DECLARATION_END);
  }

  @Override
  public boolean enterRuleset(CssRulesetNode ruleset) {
    boolean printRuleset = super.enterRuleset(ruleset);
    if (printRuleset) {
      append(RULE_START);
    }
    return printRuleset;
  }

  @Override
  public void leaveRuleset(CssRulesetNode ruleset) {
    // only called if enterRuleset returns true
    append(RULE_END);
    super.leaveRuleset(ruleset);
  }

  @Override
  public boolean enterMediaRule(CssMediaRuleNode media) {
    append(RULE_START);
    boolean printMediaRule = super.enterMediaRule(media);
    if (!printMediaRule) {
      deleteLastCharIfCharIs(RULE_START);
    }
    return printMediaRule;
  }

  @Override
  public void leaveMediaRule(CssMediaRuleNode media) {
    // only called if enterMediaRule returns true
    super.leaveMediaRule(media);
    append(RULE_END);
  }

  @Override
  public boolean enterFontFace(CssFontFaceNode cssFontFaceNode) {
    append(RULE_START);
    boolean printFontFace = super.enterFontFace(cssFontFaceNode);
    if (!printFontFace) {
      deleteLastCharIfCharIs(RULE_START);
    }
    return printFontFace;
  }

  @Override
  public void leaveFontFace(CssFontFaceNode cssFontFaceNode) {
    // only called if enterFontFace returns true
    super.leaveFontFace(cssFontFaceNode);
    append(RULE_END);
  }

  @Override
  public boolean enterKeyframeRuleset(CssKeyframeRulesetNode ruleset) {
    append(RULE_START);
    boolean printKeyframeRuleset = super.enterKeyframeRuleset(ruleset);
    if (!printKeyframeRuleset) {
      deleteLastCharIfCharIs(RULE_START);
    }
    return printKeyframeRuleset;
  }

  @Override
  public void leaveKeyframeRuleset(CssKeyframeRulesetNode ruleset) {
    // only called if enterKeyframeRuleset returns true
    super.leaveKeyframeRuleset(ruleset);
    append(RULE_END);
  }

  @Override
  public boolean enterPageRule(CssPageRuleNode node) {
    append(RULE_START);
    boolean printPageRule = super.enterPageRule(node);
    if (!printPageRule) {
      deleteLastCharIfCharIs(RULE_START);
    }
    return printPageRule;
  }

  @Override
  public void leavePageRule(CssPageRuleNode node) {
    // only called if enterPageRule returns true
    super.leavePageRule(node);
    append(RULE_END);
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    append(RULE_START);
    boolean printUnknownAtRule = super.enterUnknownAtRule(node);
    if (!printUnknownAtRule) {
      deleteLastCharIfCharIs(RULE_START);
    }
    return printUnknownAtRule;
  }

  @Override
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    // only called if enterUnknownAtRule returns true
    super.leaveUnknownAtRule(node);
    append(RULE_END);
  }

  @Override
  public boolean enterImportRule(CssImportRuleNode node) {
    append(RULE_START);
    boolean printImportRule = super.enterImportRule(node);
    if (!printImportRule) {
      deleteLastCharIfCharIs(RULE_START);
    }
    return printImportRule;
  }

  @Override
  public void leaveImportRule(CssImportRuleNode node) {
    super.leaveImportRule(node);
    append(RULE_END);
  }

  @Override
  protected void deleteLastCharIfCharIs(char ch) {
    if (ch == ';' && getLastCharInBuffer() == DECLARATION_END) {
      deleteBufferCharAt(getCurrentBufferLength() - 2);
    } else {
      super.deleteLastCharIfCharIs(ch);
    }
  }
}
