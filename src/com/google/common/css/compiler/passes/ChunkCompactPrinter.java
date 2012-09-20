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

import com.google.common.css.compiler.ast.CssAttributeSelectorNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCombinatorNode;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssIdSelectorNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssPseudoClassNode;
import com.google.common.css.compiler.ast.CssPseudoElementNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;

/**
 * Compact-printer for {@link CssTree}s that only outputs rulesets which
 * include a selector belonging to one chunk. This printer does not support
 * code moving between chunks and ouputs the same ruleset for as many chunks
 * as this ruleset belongs to.
 *
 * <p>This pass can only be used if {@link MapChunkAwareNodesToChunk} pass has been
 * run before. Otherwise this pass won't work.
 *
 * @param <T> type of chunk id objects set on {@link CssSelectorNode}s
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class ChunkCompactPrinter<T> extends CompactPrinter {

  /** Chunk to be printed by this printer. */
  private final T chunk;

  /**
   * Whether currently visited selector (including it's children) belongs to
   * printed chunk and should be printed.
   */
  private boolean printSelector;

  /**
   * Create a chunk printer for a given chunk.
   *
   * @param tree CSS AST to be printed (with regard to a selected chunk)
   * @param chunk the chunk selected for printing
   */
  public ChunkCompactPrinter(CssTree tree, T chunk) {
    super(tree);
    this.chunk = chunk;
  }

  @Override
  public boolean enterRuleset(CssRulesetNode ruleset) {
    for (CssSelectorNode selector : ruleset.getSelectors().childIterable()) {
      if (chunk.equals(selector.getChunk())) {
        return true;
      }
    }
    return false;
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
  public boolean enterMediaRule(CssMediaRuleNode media) {
    printSelector = chunk.equals(media.getChunk());
    if (!printSelector) {
      return false;
    }
    return super.enterMediaRule(media);
  }

  @Override
  public void leaveMediaRule(CssMediaRuleNode media) {
    if (printSelector) {
      super.leaveMediaRule(media);
    }
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
  public boolean enterFontFace(CssFontFaceNode cssFontFaceNode) {
    printSelector = chunk.equals(cssFontFaceNode.getChunk());
    if (!printSelector) {
      return false;
    }
    return super.enterFontFace(cssFontFaceNode);
  }

  @Override
  public void leaveFontFace(CssFontFaceNode cssFontFaceNode) {
    if (printSelector) {
      super.leaveFontFace(cssFontFaceNode);
    }
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
