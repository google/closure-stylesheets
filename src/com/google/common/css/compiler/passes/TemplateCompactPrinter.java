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

import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssAttributeSelectorNode;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCombinatorNode;
import com.google.common.css.compiler.ast.CssCommentNode;
import com.google.common.css.compiler.ast.CssComponentNode;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssForLoopRuleNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssIdSelectorNode;
import com.google.common.css.compiler.ast.CssImportBlockNode;
import com.google.common.css.compiler.ast.CssImportRuleNode;
import com.google.common.css.compiler.ast.CssKeyListNode;
import com.google.common.css.compiler.ast.CssKeyNode;
import com.google.common.css.compiler.ast.CssKeyframeRulesetNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.CssMixinNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssPageRuleNode;
import com.google.common.css.compiler.ast.CssPageSelectorNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssProvideNode;
import com.google.common.css.compiler.ast.CssPseudoClassNode;
import com.google.common.css.compiler.ast.CssPseudoElementNode;
import com.google.common.css.compiler.ast.CssRequireNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorListNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;

import java.util.regex.Pattern;

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

  private boolean preserveMarkedComments;

  private static final Pattern LICENSE_ANNOTATION_PATTERN =
      Pattern.compile(".*@license\\b.*", Pattern.DOTALL);

  private static final Pattern PRESERVE_ANNOTATION_PATTERN =
      Pattern.compile(".*@preserve\\b.*", Pattern.DOTALL);

  // CodeBuffer with specific behavior for the printer
  private static final class CodeBufferForTemplate extends CodeBuffer {
    @Override
    public void deleteLastCharIfCharIs(char ch) {
      if (ch == ';' && getLastChar() == DECLARATION_END) {
        deleteLastChars(2);
        append(DECLARATION_END);
      } else {
        super.deleteLastCharIfCharIs(ch);
      }
    }
  }

  /**
   * Create a template printer for a given chunk.
   *
   * @param tree CSS AST to be printed (with regard to a selected chunk)
   * @param chunk the chunk selected for printing
   */
  public TemplateCompactPrinter(CssTree tree, T chunk) {
    super(tree, chunk, new CodeBufferForTemplate());
  }

  /**
   * Whether special comments in the CSS nodes are preserved in the printed
   * output. Currently supported special comments are annotated with one of the following:
   * <ul><li>@preserve</li>
   * <li>@license</li></ul>
   * Comments marked with @license will cause a special "END OF LICENSED CSS FILE"
   * comment to be inserted when the parser moves on to a new source file.
   * <p>Note: Comments layout is not guaranteed, since detailed position
   * information in the input files is not preserved by the parser.</p>
   */
  public TemplateCompactPrinter setPreserveMarkedComments(boolean preserve) {
    this.preserveMarkedComments = preserve;
    return this;
  }

  @Override
  protected void appendValueNode(CssValueNode node) {
    if (node instanceof CssConstantReferenceNode) {
      buffer.append(REFERENCE_START);
      super.appendValueNode(node);
      buffer.append(REFERENCE_END);
    } else {
      super.appendValueNode(node);
    }
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    maybeAppendComments(declaration);
    buffer.append(DECLARATION_START);
    return super.enterDeclaration(declaration);
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode declaration) {
    super.leaveDeclaration(declaration);
    buffer.append(DECLARATION_END);
  }

  @Override
  public boolean enterRuleset(CssRulesetNode ruleset) {
    maybeAppendComments(ruleset);
    boolean printRuleset = super.enterRuleset(ruleset);
    if (printRuleset) {
      buffer.append(RULE_START);
    }
    return printRuleset;
  }

  @Override
  public void leaveRuleset(CssRulesetNode ruleset) {
    // only called if enterRuleset returns true
    buffer.append(RULE_END);
    super.leaveRuleset(ruleset);
  }

  @Override
  public boolean enterMediaRule(CssMediaRuleNode media) {
    maybeAppendComments(media);
    buffer.append(RULE_START);
    boolean printMediaRule = super.enterMediaRule(media);
    if (!printMediaRule) {
      buffer.deleteLastCharIfCharIs(RULE_START);
    }
    return printMediaRule;
  }

  @Override
  public void leaveMediaRule(CssMediaRuleNode media) {
    // only called if enterMediaRule returns true
    super.leaveMediaRule(media);
    buffer.append(RULE_END);
  }

  @Override
  public boolean enterFontFace(CssFontFaceNode cssFontFaceNode) {
    maybeAppendComments(cssFontFaceNode);
    buffer.append(RULE_START);
    boolean printFontFace = super.enterFontFace(cssFontFaceNode);
    if (!printFontFace) {
      buffer.deleteLastCharIfCharIs(RULE_START);
    }
    return printFontFace;
  }

  @Override
  public void leaveFontFace(CssFontFaceNode cssFontFaceNode) {
    // only called if enterFontFace returns true
    super.leaveFontFace(cssFontFaceNode);
    buffer.append(RULE_END);
  }

  @Override
  public boolean enterKeyframeRuleset(CssKeyframeRulesetNode ruleset) {
    maybeAppendComments(ruleset);
    buffer.append(RULE_START);
    boolean printKeyframeRuleset = super.enterKeyframeRuleset(ruleset);
    if (!printKeyframeRuleset) {
      buffer.deleteLastCharIfCharIs(RULE_START);
    }
    return printKeyframeRuleset;
  }

  @Override
  public void leaveKeyframeRuleset(CssKeyframeRulesetNode ruleset) {
    // only called if enterKeyframeRuleset returns true
    super.leaveKeyframeRuleset(ruleset);
    buffer.append(RULE_END);
  }

  @Override
  public boolean enterPageRule(CssPageRuleNode node) {
    maybeAppendComments(node);
    buffer.append(RULE_START);
    boolean printPageRule = super.enterPageRule(node);
    if (!printPageRule) {
      buffer.deleteLastCharIfCharIs(RULE_START);
    }
    return printPageRule;
  }

  @Override
  public void leavePageRule(CssPageRuleNode node) {
    // only called if enterPageRule returns true
    super.leavePageRule(node);
    buffer.append(RULE_END);
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    maybeAppendComments(node);
    buffer.append(RULE_START);
    boolean printUnknownAtRule = super.enterUnknownAtRule(node);
    if (!printUnknownAtRule) {
      buffer.deleteLastCharIfCharIs(RULE_START);
    }
    return printUnknownAtRule;
  }

  @Override
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    // only called if enterUnknownAtRule returns true
    super.leaveUnknownAtRule(node);
    buffer.append(RULE_END);
  }

  @Override
  public boolean enterImportRule(CssImportRuleNode node) {
    maybeAppendComments(node);
    buffer.append(RULE_START);
    boolean printImportRule = super.enterImportRule(node);
    if (!printImportRule) {
      buffer.deleteLastCharIfCharIs(RULE_START);
    }
    return printImportRule;
  }

  @Override
  public void leaveImportRule(CssImportRuleNode node) {
    super.leaveImportRule(node);
    buffer.append(RULE_END);
  }

  @Override
  public boolean enterConditionalRule(CssConditionalRuleNode node) {
    maybeAppendComments(node);
    return super.enterConditionalRule(node);
  }

  /** Called before visiting a {@code CssPageSelectorNode}'s sub trees */
  @Override
  public boolean enterPageSelector(CssPageSelectorNode node) {
    maybeAppendComments(node);
    return super.enterPageSelector(node);
  }

  /**
   * @return {@code true} if the contents of the rule should be visited,
   *     false otherwise. {@link #leaveDefinition(CssDefinitionNode)}
   *     will still be called.
   */
  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    maybeAppendComments(node);
    return super.enterDefinition(node);
  }

  /** Called before visiting a {@code CssComponentNode}'s sub trees */
  @Override
  public boolean enterComponent(CssComponentNode node) {
    maybeAppendComments(node);
    return super.enterComponent(node);
  }

  /** Called before visiting a {@code CssKeyframesNode}'s sub trees */
  @Override
  public boolean enterKeyframesRule(CssKeyframesNode node) {
    maybeAppendComments(node);
    return super.enterKeyframesRule(node);
  }

  /** Called before visiting a {@code CssMixinDefinitionNode}'s sub trees */
  @Override
  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    maybeAppendComments(node);
    return super.enterMixinDefinition(node);
  }

  /** Called before visiting a {@code CssMixinNode}'s sub trees */
  @Override
  public boolean enterMixin(CssMixinNode node) {
    maybeAppendComments(node);
    return super.enterMixin(node);
  }

  /** Called before visiting a {@code CssRootNode}'s sub trees */
  @Override
  public boolean enterTree(CssRootNode root) {
    maybeAppendComments(root);
    return super.enterTree(root);
  }

  /** Called before visiting a {@code CssImportBlockNode}'s sub trees */
  @Override
  public boolean enterImportBlock(CssImportBlockNode block) {
    maybeAppendComments(block);
    return super.enterImportBlock(block);
  }

  /** Called before visiting a {@code CssBlockNode}'s sub trees */
  @Override
  public boolean enterBlock(CssBlockNode block) {
    maybeAppendComments(block);
    return super.enterBlock(block);
  }

  /** Called before visiting a {@code CssConditionalBlockNode}'s sub trees */
  @Override
  public boolean enterConditionalBlock(CssConditionalBlockNode block) {
    maybeAppendComments(block);
    return super.enterConditionalBlock(block);
  }

  /** Called before visiting a {@code CssSelectorListNode}'s sub trees */
  @Override
  public boolean enterSelectorBlock(CssSelectorListNode block) {
    maybeAppendComments(block);
    return super.enterSelectorBlock(block);
  }

  /** Called before visiting a {@code CssSelectorNode}'s sub trees */
  @Override
  public boolean enterSelector(CssSelectorNode selector) {
    maybeAppendComments(selector);
    return super.enterSelector(selector);
  }

  /** Called before visiting a {@code CssClassSelectorNode}'s sub trees */
  @Override
  public boolean enterClassSelector(CssClassSelectorNode classSelector) {
    maybeAppendComments(classSelector);
    return super.enterClassSelector(classSelector);
  }

  /** Called before visiting a {@code CssIdSelectorNode}'s sub trees */
  @Override
  public boolean enterIdSelector(CssIdSelectorNode idSelector) {
    maybeAppendComments(idSelector);
    return super.enterIdSelector(idSelector);
  }

  /** Called before visiting a {@code CssPseudoClassNode}'s sub trees */
  @Override
  public boolean enterPseudoClass(CssPseudoClassNode pseudoClass) {
    maybeAppendComments(pseudoClass);
    return super.enterPseudoClass(pseudoClass);
  }

  /** Called before visiting a {@code CssPseudoElementNode}'s sub trees */
  @Override
  public boolean enterPseudoElement(CssPseudoElementNode pseudoElement) {
    maybeAppendComments(pseudoElement);
    return super.enterPseudoElement(pseudoElement);
  }

  /** Called before visiting a {@code CssAttributeSelectorNode}'s sub trees */
  @Override
  public boolean enterAttributeSelector(CssAttributeSelectorNode attributeSelector) {
    maybeAppendComments(attributeSelector);
    return super.enterAttributeSelector(attributeSelector);
  }

  /** Called before visiting a {@code CssPropertyValueNode}'s sub trees */
  @Override
  public boolean enterPropertyValue(CssPropertyValueNode propertyValue) {
    maybeAppendComments(propertyValue);
    return super.enterPropertyValue(propertyValue);
  }

  /** Called before visiting a {@code CssValueNode} that is a
      {@code CssCompositeValueNode} */
  @Override
  public boolean enterCompositeValueNode(CssCompositeValueNode value) {
    maybeAppendComments(value);
    return super.enterCompositeValueNode(value);
  }

  /** Called before visiting a {@code CssFunctionNode}'s sub trees */
  @Override
  public boolean enterFunctionNode(CssFunctionNode value) {
    maybeAppendComments(value);
    return super.enterFunctionNode(value);
  }

  /** Called before visiting a {@code CssFunctionNode}'s sub trees */
  @Override
  public boolean enterArgumentNode(CssValueNode value) {
    maybeAppendComments(value);
    return super.enterArgumentNode(value);
  }

  /** Called before visiting a {@code CssCombinatorNode}'s sub trees */
  @Override
  public boolean enterCombinator(CssCombinatorNode combinator) {
    maybeAppendComments(combinator);
    return super.enterCombinator(combinator);
  }

  /** Called before visiting a {@code CssKeyNode}'s sub trees */
  @Override
  public boolean enterKey(CssKeyNode key) {
    maybeAppendComments(key);
    return super.enterKey(key);
  }

  /** Called before visiting a {@code CssKeyListNode}'s sub trees */
  @Override
  public boolean enterKeyBlock(CssKeyListNode block) {
    maybeAppendComments(block);
    return super.enterKeyBlock(block);
  }

  @Override
  public boolean enterValueNode(CssValueNode n) {
    maybeAppendComments(n);
    return super.enterValueNode(n);
  }

  @Override
  public boolean enterForLoop(CssForLoopRuleNode node) {
    maybeAppendComments(node);
    return super.enterForLoop(node);
  }

  /** Called before visiting a {@code CssProvideNode} */
  @Override
  public boolean enterProvideNode(CssProvideNode node) {
    maybeAppendComments(node);
    return super.enterProvideNode(node);
  }

  /** Called before visiting a {@code CssRequireNode} */
  @Override
  public boolean enterRequireNode(CssRequireNode node) {
    maybeAppendComments(node);
    return super.enterRequireNode(node);
  }

  private String lastLicensedCommentSource = null;

  private void maybeAppendComments(CssNode node) {
    String newSourceLocation = fileNameForSourceCodeLocation(
            node.getSourceCodeLocation());
    // If we previously printed out a comment with a license and we're transitioning
    // source files, print a END OF LICENSED CSS FILE comment.
    if (lastLicensedCommentSource != null
        && newSourceLocation != null
        && !lastLicensedCommentSource.equals(newSourceLocation)) {
      buffer.append("/* END OF LICENSED CSS FILE */\n");
      lastLicensedCommentSource = null;
    }
    if (preserveMarkedComments && !node.getComments().isEmpty()) {
      StringBuilder commentBuffer = new StringBuilder();
      for (CssCommentNode c : node.getComments()) {
        commentBuffer.append(c.getValue());
      }
      String comment = commentBuffer.toString();
      boolean hasLicense = LICENSE_ANNOTATION_PATTERN.matcher(comment).matches();
      if (hasLicense || PRESERVE_ANNOTATION_PATTERN.matcher(comment).matches()) {
        buffer.startNewLine();
        buffer.append(comment);
        buffer.startNewLine();

        if (hasLicense) {
          lastLicensedCommentSource = newSourceLocation;
        }
      }
    }
  }

  private String fileNameForSourceCodeLocation(SourceCodeLocation location) {
    if (location == null || location.getSourceCode() == null) {
      return null;
    }
    return location.getSourceCode().getFileName();
  }
}
