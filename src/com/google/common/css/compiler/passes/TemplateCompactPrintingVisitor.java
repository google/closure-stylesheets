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

import com.google.common.css.compiler.ast.CssAttributeSelectorNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCombinatorNode;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssIdSelectorNode;
import com.google.common.css.compiler.ast.CssImportRuleNode;
import com.google.common.css.compiler.ast.CssKeyframeRulesetNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssPageRuleNode;
import com.google.common.css.compiler.ast.CssPseudoClassNode;
import com.google.common.css.compiler.ast.CssPseudoElementNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.VisitController;
import javax.annotation.Nullable;

/**
 * Printer for templates, which outputs GSS with holes to be filled for references. In addition, the
 * declaration boundaries are explicitly noted, so that a declaration can be removed if it ends up
 * empty.
 *
 * <p>This pass can only be used if {@link MapChunkAwareNodesToChunk} pass has been run before.
 * Otherwise this pass won't work.
 *
 * @param <T> type of chunk id objects
 * @author dgajda@google.com (Damian Gajda)
 */
public class TemplateCompactPrintingVisitor<T> extends CompactPrintingVisitor {

  /** Chunk to be printed by this printer. */
  private final T chunk;

  /**
   * Whether currently visited selector (including its children) belongs to printed chunk and
   * should be printed.
   */
  private boolean printSelector;

  public TemplateCompactPrintingVisitor(
      VisitController visitController,
      T chunk,
      @Nullable CodeBuffer buffer) {
    super(visitController, buffer);
    this.chunk = chunk;
  }

  @Override
  protected void appendValueNode(CssValueNode node) {
    if (node instanceof CssConstantReferenceNode) {
      buffer.append(TemplateCompactPrinter.REFERENCE_START);
      super.appendValueNode(node);
      buffer.append(TemplateCompactPrinter.REFERENCE_END);
    } else {
      super.appendValueNode(node);
    }
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    buffer.append(TemplateCompactPrinter.DECLARATION_START);
    return super.enterDeclaration(declaration);
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode declaration) {
    super.leaveDeclaration(declaration);
    buffer.append(TemplateCompactPrinter.DECLARATION_END);
  }

  @Override
  public boolean enterRuleset(CssRulesetNode ruleset) {
    boolean printRuleset = false;
    for (CssSelectorNode selector : ruleset.getSelectors().childIterable()) {
      if (chunk.equals(selector.getChunk())) {
        printRuleset = true;
        break;
      }
    }
    if (printRuleset) {
      buffer.append(TemplateCompactPrinter.RULE_START);
    }
    return printRuleset;
  }

  @Override
  public void leaveRuleset(CssRulesetNode ruleset) {
    // only called if enterRuleset returns true
    buffer.append(TemplateCompactPrinter.RULE_END);
    super.leaveRuleset(ruleset);
  }

  @Override
  public boolean enterMediaRule(CssMediaRuleNode media) {
    buffer.append(TemplateCompactPrinter.RULE_START);
    printSelector = chunk.equals(media.getChunk());
    boolean printMediaRule = printSelector && super.enterMediaRule(media);
    if (!printMediaRule) {
      buffer.deleteLastCharIfCharIs(TemplateCompactPrinter.RULE_START);
    }
    return printMediaRule;
  }

  @Override
  public void leaveMediaRule(CssMediaRuleNode media) {
    // only called if enterMediaRule returns true
    if (printSelector) {
      super.leaveMediaRule(media);
    }
    buffer.append(TemplateCompactPrinter.RULE_END);
  }

  @Override
  public boolean enterSelector(CssSelectorNode selector) {
    printSelector = chunk.equals(selector.getChunk());
    if (printSelector) {
      return super.enterSelector(selector);
    }
    return true;
  }

  @Override
  public void leaveSelector(CssSelectorNode selector) {
    if (printSelector) {
      super.leaveSelector(selector);
    }
  }

  @Override
  public boolean enterFontFace(CssFontFaceNode cssFontFaceNode) {
    buffer.append(TemplateCompactPrinter.RULE_START);
    printSelector = chunk.equals(cssFontFaceNode.getChunk());
    boolean printFontFace = printSelector && super.enterFontFace(cssFontFaceNode);
    if (!printFontFace) {
      buffer.deleteLastCharIfCharIs(TemplateCompactPrinter.RULE_START);
    }
    return printFontFace;
  }

  @Override
  public void leaveFontFace(CssFontFaceNode cssFontFaceNode) {
    // only called if enterFontFace returns true
    if (printSelector) {
      super.leaveFontFace(cssFontFaceNode);
    }
    buffer.append(TemplateCompactPrinter.RULE_END);
  }

  @Override
  public boolean enterKeyframeRuleset(CssKeyframeRulesetNode ruleset) {
    buffer.append(TemplateCompactPrinter.RULE_START);
    boolean printKeyframeRuleset = super.enterKeyframeRuleset(ruleset);
    if (!printKeyframeRuleset) {
      buffer.deleteLastCharIfCharIs(TemplateCompactPrinter.RULE_START);
    }
    return printKeyframeRuleset;
  }

  @Override
  public void leaveKeyframeRuleset(CssKeyframeRulesetNode ruleset) {
    // only called if enterKeyframeRuleset returns true
    super.leaveKeyframeRuleset(ruleset);
    buffer.append(TemplateCompactPrinter.RULE_END);
  }

  @Override
  public boolean enterKeyframesRule(CssKeyframesNode keyframes) {
    printSelector = chunk.equals(keyframes.getChunk());
    if (!printSelector) {
      return false;
    }
    return super.enterKeyframesRule(keyframes);
  }

  @Override
  public void leaveKeyframesRule(CssKeyframesNode keyframes) {
    if (printSelector) {
      super.leaveKeyframesRule(keyframes);
    }
  }

  @Override
  public boolean enterPageRule(CssPageRuleNode node) {
    buffer.append(TemplateCompactPrinter.RULE_START);
    boolean printPageRule = super.enterPageRule(node);
    if (!printPageRule) {
      buffer.deleteLastCharIfCharIs(TemplateCompactPrinter.RULE_START);
    }
    return printPageRule;
  }

  @Override
  public void leavePageRule(CssPageRuleNode node) {
    // only called if enterPageRule returns true
    super.leavePageRule(node);
    buffer.append(TemplateCompactPrinter.RULE_END);
  }

  @Override
  public boolean enterClassSelector(CssClassSelectorNode node) {
    if (printSelector) {
      return super.enterClassSelector(node);
    }
    return true;
  }

  @Override
  public void leaveClassSelector(CssClassSelectorNode node) {
    if (printSelector) {
      super.leaveClassSelector(node);
    }
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    buffer.append(TemplateCompactPrinter.RULE_START);
    boolean printUnknownAtRule = super.enterUnknownAtRule(node);
    if (!printUnknownAtRule) {
      buffer.deleteLastCharIfCharIs(TemplateCompactPrinter.RULE_START);
    }
    return printUnknownAtRule;
  }

  @Override
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    // only called if enterUnknownAtRule returns true
    super.leaveUnknownAtRule(node);
    buffer.append(TemplateCompactPrinter.RULE_END);
  }

  @Override
  public boolean enterImportRule(CssImportRuleNode node) {
    buffer.append(TemplateCompactPrinter.RULE_START);
    boolean printImportRule = super.enterImportRule(node);
    if (!printImportRule) {
      buffer.deleteLastCharIfCharIs(TemplateCompactPrinter.RULE_START);
    }
    return printImportRule;
  }

  @Override
  public void leaveImportRule(CssImportRuleNode node) {
    super.leaveImportRule(node);
    buffer.append(TemplateCompactPrinter.RULE_END);
  }

  @Override
  public boolean enterIdSelector(CssIdSelectorNode node) {
    if (printSelector) {
      return super.enterIdSelector(node);
    }
    return true;
  }

  @Override
  public void leaveIdSelector(CssIdSelectorNode node) {
    if (printSelector) {
      super.leaveIdSelector(node);
    }
  }

  @Override
  public boolean enterPseudoClass(CssPseudoClassNode node) {
    if (printSelector) {
      return super.enterPseudoClass(node);
    }
    return true;
  }

  @Override
  public void leavePseudoClass(CssPseudoClassNode node) {
    if (printSelector) {
      super.leavePseudoClass(node);
    }
  }

  @Override
  public boolean enterPseudoElement(CssPseudoElementNode node) {
    if (printSelector) {
      return super.enterPseudoElement(node);
    }
    return true;
  }

  @Override
  public void leavePseudoElement(CssPseudoElementNode node) {
    if (printSelector) {
      super.leavePseudoElement(node);
    }
  }

  @Override
  public boolean enterAttributeSelector(CssAttributeSelectorNode node) {
    if (printSelector) {
      return super.enterAttributeSelector(node);
    }
    return true;
  }

  @Override
  public void leaveAttributeSelector(CssAttributeSelectorNode node) {
    if (printSelector) {
      super.leaveAttributeSelector(node);
    }
  }

  @Override
  public boolean enterCombinator(CssCombinatorNode combinator) {
    if (printSelector) {
      return super.enterCombinator(combinator);
    }
    return true;
  }

  @Override
  public void leaveCombinator(CssCombinatorNode combinator) {
    if (printSelector) {
      super.leaveCombinator(combinator);
    }
  }
}
