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

import com.google.common.css.compiler.ast.CssFunctionNode.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link CssFunctionNode}.
 *
 * @author oana@google.com (Oana Florescu)
 */
@RunWith(JUnit4.class)
public class CssFunctionNodeTest {

  private static final CssFunctionNode.Function RGB =
      CssFunctionNode.Function.byName("rgb");

  @Test
  public void testConstructor() {
    CssFunctionNode function = new CssFunctionNode((Function) null, null);
    assertThat(function.getFunction()).isNull();
    assertThat(function.getSourceCodeLocation()).isNull();
    assertThat(function.getArguments()).isNotNull();
    assertThat(function.getArguments().getChildren()).isEmpty();
  }
  
  @Test
  public void testFunctionName() {
    CssFunctionNode function = new CssFunctionNode(RGB, null);
    assertThat(function.getFunction()).isNotNull();
    assertThat(function.getFunction()).isEqualTo(RGB);
    assertThat(function.getFunctionName()).isEqualTo("rgb");
    assertThat(function.getSourceCodeLocation()).isNull();
    assertThat(function.getArguments()).isNotNull();
    assertThat(function.getArguments().getChildren()).isEmpty();
  }
  
  @Test
  public void testFunctionArguments() {
    CssFunctionNode function = new CssFunctionNode(RGB, null);
    CssFunctionArgumentsNode args = new CssFunctionArgumentsNode();
    args.addChildToBack(new CssLiteralNode("test"));
    function.setArguments(args);
    assertThat(function.getFunction()).isNotNull();
    assertThat(function.getFunction()).isEqualTo(RGB);
    assertThat(function.getFunctionName()).isEqualTo("rgb");
    assertThat(function.getSourceCodeLocation()).isNull();
    assertThat(function.getArguments()).isNotNull();
    assertThat(function.getArguments().getChildren()).isNotEmpty();
    assertThat(function.toString()).isEqualTo("rgb(test)");
    assertThat(function.getValue()).isEqualTo("rgb(test)");
  }
}
