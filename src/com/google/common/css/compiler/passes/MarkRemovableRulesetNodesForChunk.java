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

import com.google.common.base.Preconditions;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorListNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree;

/**
 * Compiler pass that marks ruleset nodes that should be removed from
 * the tree, within the confines of a single chunk.
 *
 * <p>This pass has the same assumptions as {@link MarkRemovableRulesetNodes}.
 *
 * @param <T> type of chunk id objects
 *
 */
public class MarkRemovableRulesetNodesForChunk<T>
    extends MarkRemovableRulesetNodes {

  private final T chunk;

  public MarkRemovableRulesetNodesForChunk(
      T chunk,
      CssTree tree,
      boolean skipping) {
    super(tree, skipping);
    Preconditions.checkNotNull(chunk);
    this.chunk = chunk;
  }

  @Override
  public boolean canModifyRuleset(CssRulesetNode ruleset) {
    if (!super.canModifyRuleset(ruleset)) {
      return false;
    }

    CssSelectorListNode selectors = ruleset.getSelectors();
    Preconditions.checkArgument(selectors.numChildren() == 1);
    CssSelectorNode selector = selectors.getChildAt(0);

    return chunk.equals(selector.getChunk());
  }
}
