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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import junit.framework.TestCase;

/**
 * Unit tests for {@link CssDefinitionNode}.
 * 
 * @author oana@google.com (Oana Florescu)
 */
public class CssDefinitionNodeTest extends TestCase {

  public void testDefinitionNodeCreation() {
    CssDefinitionNode definition = new CssDefinitionNode(
        new CssLiteralNode("COLOR"));

    assertThat(definition.getParent()).isNull();
    assertThat(definition.getSourceCodeLocation()).isNull();
    assertThat(definition.getType().toString()).isEqualTo("@def");
    assertThat(definition.toString()).isEqualTo("@def COLOR []");
  }

  public void testDefinitionNodeCopy() {
    CssDefinitionNode definition1 = new CssDefinitionNode(
        new CssLiteralNode("COLOR"), 
        Lists.newArrayList(new CssCommentNode("/* foo */", null)));
    CssDefinitionNode definition2 = new CssDefinitionNode(definition1);
    
    assertThat(definition1.getParent()).isNull();
    assertThat(definition2.getParent()).isNull();

    assertThat(definition1.getSourceCodeLocation()).isNull();
    assertThat(definition2.getSourceCodeLocation()).isNull();

    assertThat(definition1.getType().toString()).isEqualTo("@def");
    assertThat(definition2.getType().toString()).isEqualTo("@def");

    assertThat(definition1.toString()).isEqualTo("@def COLOR []");
    assertThat(definition2.toString()).isEqualTo("@def COLOR []");

    assertThat(definition1.hasComment("/* foo */")).isTrue();
    assertThat(definition2.hasComment("/* foo */")).isTrue();
  }
}
