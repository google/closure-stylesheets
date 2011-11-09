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

import com.google.common.css.SourceCodeLocation;

/**
 * Node representing the priority of a declaration.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class CssPriorityNode extends CssValueNode {

  /**
   * Types of priority
   */
  public enum PriorityType {
    IMPORTANT("!important");

    private final String priority;

    private PriorityType(String priority) {
      this.priority = priority;
    }

    public String getPriority() {
      return priority;
    }
  }

  /**
   * Constructor of a priority node.
   *
   * @param priority
   */
  public CssPriorityNode(PriorityType priority) {
    super(priority.getPriority());
  }

  /**
   * Copy constructor.
   *
   * @param node
   */
  public CssPriorityNode(CssPriorityNode node) {
    super(node);
  }

  @Override
  public CssPriorityNode deepCopy() {
    return new CssPriorityNode(this);
  }

  /**
   * Constructor of a priority node.
   *
   * @param priority
   * @param sourceCodeLocation
   */
  CssPriorityNode(PriorityType priority,
                  SourceCodeLocation sourceCodeLocation) {
    super(priority.getPriority(), sourceCodeLocation);
  }
}
