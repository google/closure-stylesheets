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

package com.google.common.css.compiler.passes;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.css.compiler.ast.CssAtRuleNode.Type;
import com.google.common.css.compiler.ast.CssAttributeSelectorNode;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCombinatorNode;
import com.google.common.css.compiler.ast.CssCommentNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssComponentNode;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssIdSelectorNode;
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
import com.google.common.css.compiler.ast.CssPseudoClassNode.FunctionType;
import com.google.common.css.compiler.ast.CssPseudoElementNode;
import com.google.common.css.compiler.ast.CssRefinerNode;
import com.google.common.css.compiler.ast.CssRequireNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorListNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssStringNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;

/**
 * A pretty-printer for {@link CssTree} instances. This is work in progress.
 * Look at PrettyPrinterTest to see what's supported.
 *
 * @author mkretzschmar@google.com (Martin Kretzschmar)
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
public class PrettyPrinter extends DefaultTreeVisitor
    implements CssCompilerPass {

  private StringBuilder sb = null;
  private String prettyPrintedString = null;
  private String indent = "";
  private final VisitController visitController;
  private boolean stripQuotes = false;
  private boolean preserveComments = false;

  public PrettyPrinter(VisitController visitController) {
    this.visitController = visitController;
  }

  /**
   * Whether to strip quotes from certain values. This facilitates
   * tests that want to compare trees.
   */
  public void setStripQuotes(boolean stripQuotes) {
    this.stripQuotes = stripQuotes;
  }

  /**
   * Whether comments in the CSS nodes are preserved in the pretty printed
   * output.
   * <p>Note: Comments layout is not guaranteed, since detailed position
   * information in the input files is not preserved by the parser. Line breaks
   * are added after every comment with current identation as best effort.</p>
   */
  public PrettyPrinter setPreserveComments(boolean preserve) {
    this.preserveComments = preserve;
    return this;
  }

  @Override
  public boolean enterImportRule(CssImportRuleNode node) {
    maybeAppendComments(node);
    sb.append(node.getType().toString());
    for (CssValueNode param : node.getParameters()) {
      sb.append(" ");
      // TODO(user): teach visit controllers to explore this subtree
      // rather than leaving it to each pass to figure things out.
      if (param instanceof CssStringNode) {
        CssStringNode n = (CssStringNode) param;
        sb.append(n.toString(CssStringNode.SHORT_ESCAPER));
      } else {
        sb.append(param.getValue());
      }
    }
    return true;
  }

  @Override
  public void leaveImportRule(CssImportRuleNode node) {
    sb.append(";\n");
  }

  @Override
  public boolean enterMediaRule(CssMediaRuleNode node) {
    maybeAppendComments(node);
    sb.append(node.getType().toString());
    if (node.getParameters().size() > 0
        || (node.getType().hasBlock() && node.getBlock() != null)) {
      sb.append(" ");
    }
    return true;
  }

  @Override
  public void leaveMediaRule(CssMediaRuleNode node) {}

  @Override
  public boolean enterPageRule(CssPageRuleNode node) {
    maybeAppendComments(node);
    sb.append(node.getType().toString());
    sb.append(' ');
    for (CssValueNode param : node.getParameters()) {
      sb.append(param.getValue());
    }
    if (node.getParametersCount() > 0) {
      sb.append(' ');
    }
    return true;
  }

  @Override
  public boolean enterPageSelector(CssPageSelectorNode node) {
    maybeAppendComments(node);
    sb.append(node.getType().toString());
    for (CssValueNode param : node.getParameters()) {
      sb.append(" ");
      sb.append(param.getValue());
    }
    return true;
  }

  @Override
  public boolean enterFontFace(CssFontFaceNode node) {
    maybeAppendComments(node);
    sb.append(node.getType().toString());
    return true;
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    maybeAppendComments(node);
    sb.append(indent);
    sb.append(node.getType());
    sb.append(" ");
    sb.append(node.getName());
    // Add a space to separate it from next value.
    sb.append(" ");
    return true;
  }

  @Override
  public void leaveDefinition(CssDefinitionNode node) {
    // Remove trailing space after last value.
    deleteEndingIfEndingIs(" ");
    sb.append(";\n");
  }

  @Override
  public boolean enterRuleset(CssRulesetNode ruleset) {
    maybeAppendComments(ruleset);
    sb.append(indent);
    return true;
  }

  @Override
  public boolean enterKeyframeRuleset(CssKeyframeRulesetNode ruleset) {
    maybeAppendComments(ruleset);
    sb.append(indent);
    return true;
  }

  // TODO(mkretzschmar): make DeclarationBlock subclass of Block and eliminate
  //     this.
  @Override
  public boolean enterDeclarationBlock(CssDeclarationBlockNode block) {
    maybeAppendComments(block);
    deleteEndingIfEndingIs(" ");
    sb.append(" {\n");
    indent += "  ";
    return true;
  }

  @Override
  public void leaveDeclarationBlock(CssDeclarationBlockNode block) {
    indent = indent.substring(0, indent.length() - 2);
    sb.append(indent);
    sb.append("}\n");
  }

  @Override
  public boolean enterBlock(CssBlockNode block) {
    maybeAppendComments(block);
    if (block.getParent() instanceof CssUnknownAtRuleNode
        || block.getParent() instanceof CssMediaRuleNode) {
      sb.append("{\n");
      indent += "  ";
    }
    return true;
  }

  @Override
  public void leaveBlock(CssBlockNode block) {
    if (block.getParent() instanceof CssMediaRuleNode) {
      sb.append("}\n");
      indent = indent.substring(0, indent.length() - 2);
    }
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    maybeAppendComments(declaration);
    sb.append(indent);
    if (declaration.hasStarHack()) {
      sb.append('*');
    }
    sb.append(declaration.getPropertyName().getValue());
    sb.append(": ");
    return true;
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode declaration) {
    deleteEndingIfEndingIs(" ");
    sb.append(";\n");
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    maybeAppendComments(node);
    checkArgument(!(node instanceof CssCompositeValueNode));

    String v = node.toString();
    if (stripQuotes && node.getParent() instanceof CssDefinitionNode) {
      v = maybeStripQuotes(v);
    }
    sb.append(v);

    // NOTE(flan): When visiting function arguments, we don't want to add extra
    // spaces because they are already in the arguments list if they are
    // required. Yes, this sucks.
    if (!node.inFunArgs()) {
      sb.append(" ");
    }
    return true;
  }

  @Override
  public boolean enterCompositeValueNodeOperator(CssCompositeValueNode parent) {
    maybeAppendComments(parent);
    sb.append(parent.getOperator().getOperatorName());
    if (!parent.inFunArgs()) {
      sb.append(" ");
    }
    return true;
  }

  @Override
  public boolean enterFunctionNode(CssFunctionNode node) {
    maybeAppendComments(node);
    sb.append(node.getFunctionName());
    sb.append("(");
    return true;
  }

  @Override
  public void leaveFunctionNode(CssFunctionNode node) {
    deleteEndingIfEndingIs(" ");
    sb.append(") ");
  }

  @Override
  public boolean enterArgumentNode(CssValueNode node) {
    maybeAppendComments(node);
    String v = node.toString();
    if (stripQuotes
        && node.getParent().getParent() instanceof CssFunctionNode
        && ((CssFunctionNode) node.getParent().getParent()).getFunctionName()
            .equals("url")) {
      v = maybeStripQuotes(v);
    }
    sb.append(v);
    return !(node instanceof CssCompositeValueNode);
  }

  @Override
  public boolean enterSelector(CssSelectorNode selector) {
    maybeAppendComments(selector);
    String name = selector.getSelectorName();
    if (name != null) {
      sb.append(name);
    }
    return true;
  }

  @Override
  public void leaveSelector(CssSelectorNode selector) {
    sb.append(", ");
  }

  @Override
  public boolean enterClassSelector(CssClassSelectorNode node) {
    maybeAppendComments(node);
    appendRefiner(node);
    return true;
  }

  @Override
  public boolean enterIdSelector(CssIdSelectorNode node) {
    maybeAppendComments(node);
    appendRefiner(node);
    return true;
  }

  @Override
  public boolean enterPseudoClass(CssPseudoClassNode node) {
    maybeAppendComments(node);
    sb.append(node.getPrefix());
    sb.append(node.getRefinerName());
    switch (node.getFunctionType()) {
      case NTH:
        sb.append(node.getArgument().replace(" ", ""));
        sb.append(")");
        break;
      case LANG:
        sb.append(node.getArgument());
        sb.append(")");
        break;
    }
    return true;
  }

  @Override
  public void leavePseudoClass(CssPseudoClassNode node) {
    if (node.getFunctionType() == FunctionType.NOT) {
      deleteEndingIfEndingIs(", ");
      sb.append(")");
    }
  }

  @Override
  public boolean enterPseudoElement(CssPseudoElementNode node) {
    maybeAppendComments(node);
    appendRefiner(node);
    return true;
  }

  @Override
  public boolean enterAttributeSelector(CssAttributeSelectorNode node) {
    maybeAppendComments(node);
    sb.append(node.getPrefix());
    sb.append(node.getAttributeName());
    sb.append(node.getMatchSymbol());
    sb.append(node.getValue());
    sb.append(node.getSuffix());
    return true;
  }

  /**
   * Appends the representation of a class selector, an id selector,
   * or a pseudo-element.
   */
  private void appendRefiner(CssRefinerNode node) {
    sb.append(node.getPrefix());
    sb.append(node.getRefinerName());
  }

  @Override
  public boolean enterCombinator(CssCombinatorNode combinator) {
    if (combinator != null) {
      maybeAppendComments(combinator);
      sb.append(combinator.getCombinatorType().getCanonicalName());
    }
    return true;
  }

  @Override
  public void leaveCombinator(CssCombinatorNode combinator) {
    deleteEndingIfEndingIs(", ");
  }

  @Override
  public void leaveSelectorBlock(CssSelectorListNode node) {
    deleteEndingIfEndingIs(", ");
  }

  @Override
  public void leaveConditionalBlock(CssConditionalBlockNode block) {
    sb.append("\n");
  }

  @Override
  public boolean enterConditionalRule(CssConditionalRuleNode node) {
    maybeAppendComments(node);
    if (node.getType() != Type.IF) {
      sb.append(" ");
    } else {
      sb.append(indent);
    }
    sb.append(node.getType());
    if (node.getParametersCount() > 0) {
      sb.append(" ");
      boolean firstParameter = true;
      for (CssValueNode value : node.getParameters()) {
        if (!firstParameter) {
          sb.append(" ");
        }
        firstParameter = false;
        sb.append(value.toString());
      }
    }
    sb.append(" {\n");
    indent += "  ";
    return true;
  }

  @Override
  public void leaveConditionalRule(CssConditionalRuleNode node) {
    indent = indent.substring(0, indent.length() - 2);
    sb.append(indent);
    sb.append("}");
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    maybeAppendComments(node);
    sb.append(indent);
    sb.append('@').append(node.getName().toString());
    if (node.getParameters().size() > 0
        || (node.getType().hasBlock() && node.getBlock() != null)) {
      sb.append(" ");
    }
    return true;
  }

  @Override
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    if (node.getType().hasBlock()) {
      if (!(node.getBlock() instanceof CssDeclarationBlockNode)) {
        indent = indent.substring(0, indent.length() - 2);
        sb.append(indent);
        sb.append("}\n");
      }
    } else {
      deleteEndingIfEndingIs(" ");
      sb.append(";\n");
    }
  }

  @Override
  public boolean enterKeyframesRule(CssKeyframesNode node) {
    maybeAppendComments(node);
    sb.append(indent);
    sb.append('@').append(node.getName().toString());
    for (CssValueNode param : node.getParameters()) {
      sb.append(" ");
      sb.append(param.getValue());
    }
    if (node.getType().hasBlock()) {
      sb.append(" {\n");
      indent += "  ";
    }
    return true;
  }

  @Override
  public void leaveKeyframesRule(CssKeyframesNode node) {
    if (node.getType().hasBlock()) {
      indent = indent.substring(0, indent.length() - 2);
      sb.append(indent);
      sb.append("}\n");
    } else {
      sb.append(";\n");
    }
  }

  @Override
  public boolean enterKey(CssKeyNode key) {
    maybeAppendComments(key);
    String value = key.getKeyValue();
    if (value != null) {
      sb.append(value);
    }
    return true;
  }

  @Override
  public void leaveKey(CssKeyNode key) {
    sb.append(", ");
  }

  @Override
  public void leaveKeyBlock(CssKeyListNode node) {
    deleteEndingIfEndingIs(", ");
  }

  @Override
  public boolean enterProvideNode(CssProvideNode node) {
    maybeAppendComments(node);
    return true;
  }

  @Override
  public boolean enterRequireNode(CssRequireNode node) {
    maybeAppendComments(node);
    return true;
  }

  @Override
  public boolean enterComponent(CssComponentNode node) {
    maybeAppendComments(node);
    return true;
  }

  @Override
  public boolean enterMixin(CssMixinNode node) {
    maybeAppendComments(node);
    return true;
  }

  @Override
  public boolean enterConditionalBlock(CssConditionalBlockNode block) {
    maybeAppendComments(block);
    return true;
  }

  @Override
  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    maybeAppendComments(node);
    return true;
  }

  @Override
  public boolean enterCompositeValueNode(CssCompositeValueNode value) {
    maybeAppendComments(value);
    return true;
  }

  @Override
  public boolean enterPropertyValue(CssPropertyValueNode propertyValue) {
    maybeAppendComments(propertyValue);
    return true;
  }

  @Override
  public boolean enterTree(CssRootNode root) {
    maybeAppendComments(root);
    return true;
  }

  private void maybeAppendComments(CssNode node) {
    if (preserveComments && !node.getComments().isEmpty()) {
      for (CssCommentNode c : node.getComments()) {
        sb.append(indent);
        sb.append(c.getValue());
        sb.append("\n");
      }
    }
  }

  private void deleteEndingIfEndingIs(String s) {
    if (sb.subSequence(sb.length() - s.length(), sb.length()).equals(s)) {
      sb.delete(sb.length() - s.length(), sb.length());
    }
  }

  public String getPrettyPrintedString() {
    return prettyPrintedString;
  }

  @Override
  public void runPass() {
    sb = new StringBuilder();
    visitController.startVisit(this);
    prettyPrintedString = sb.toString();
  }

  private String maybeStripQuotes(String v) {
    if (v.startsWith("'") || v.startsWith("\"")) {
      assert(v.endsWith(v.substring(0, 1)));
      v = v.substring(1, v.length() - 1);
    }
    return v;
  }
}
