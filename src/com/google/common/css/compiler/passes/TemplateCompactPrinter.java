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
import com.google.common.css.compiler.ast.CssKeyframeRulesetNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssTree;
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
 * @author nadaa@google.com (Nada Amin)
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

  // TODO(reinerp): Add delimiters RULE_GROUP_START, RULE_GROUP_END
  // for @keyframes and @media rule groups, so that the TemplateStylesheetLinker
  // can drop empty @keyframes{} clauses.

  /**
   * Whether currently visited rule has any selectors that belong to the
   * printed chunk and should be printed.
   */
  private boolean printRule = false;

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
      sb.append(REFERENCE_START_OLD);
      super.appendValueNode(node);
      sb.append(REFERENCE_END_OLD);
    } else {
      super.appendValueNode(node);
    }
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    sb.append(DECLARATION_START);
    return super.enterDeclaration(declaration);
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode declaration) {
    super.leaveDeclaration(declaration);
    sb.append(DECLARATION_END);
  }

  @Override
  public boolean enterRuleset(CssRulesetNode ruleset) {
    printRule = super.enterRuleset(ruleset);
    if (printRule) {
      sb.append(RULE_START);
    }
    return printRule;
  }

  @Override
  public void leaveRuleset(CssRulesetNode ruleset) {
    if (printRule) {
      sb.append(RULE_END);
    }
    super.leaveRuleset(ruleset);
  }

  @Override
  public boolean enterFontFace(CssFontFaceNode cssFontFaceNode) {
    printRule = super.enterFontFace(cssFontFaceNode);
    if (printRule) {
      sb.append(RULE_START);
    }
    return printRule;
  }

  @Override
  public void leaveFontFace(CssFontFaceNode cssFontFaceNode) {
    if (printRule) {
      sb.append(RULE_END);
    }
    super.leaveFontFace(cssFontFaceNode);
  }

  @Override
  public boolean enterKeyframeRuleset(CssKeyframeRulesetNode ruleset) {
    printRule = super.enterKeyframeRuleset(ruleset);
    if (printRule) {
      sb.append(RULE_START);
    }
    return printRule;
  }

  @Override
  public void leaveKeyframeRuleset(CssKeyframeRulesetNode ruleset) {
    if (printRule) {
      sb.append(RULE_END);
    }
    super.leaveKeyframeRuleset(ruleset);
  }

  @Override
  protected void deleteLastCharIfCharIs(char ch) {
    if (ch == ';' && sb.charAt(sb.length() - 1) == DECLARATION_END) {
      sb.deleteCharAt(sb.length() - 2);
    } else {
      super.deleteLastCharIfCharIs(ch);
    }
  }
}
