/*
 * Copyright 2011 Google Inc.
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

import com.google.common.css.compiler.ast.CssPseudoClassNode.FunctionType;
import junit.framework.TestCase;

/**
 * Unit tests for {@link CssPseudoClassNode}
 *
 * @author fbenz@google.com (Florian Benz)
 */
public class CssPseudoClassNodeTest extends TestCase {

  public void testNonFunction() {
    String name = "foo";
    CssPseudoClassNode node = new CssPseudoClassNode(name, null);
    assertThat(node.getFunctionType()).isEqualTo(FunctionType.NONE);
    assertThat(node.getPrefix()).isEqualTo(":");
    assertThat(node.getSuffix()).isEmpty();
    assertThat(node.getRefinerName()).isEqualTo(name);
    assertThat(node.getArgument()).isNull();
    assertThat(node.getNotSelector()).isNull();
  }

  public void testLangFunction() {
    String name = "foo";
    String arg = "en";
    CssPseudoClassNode node = new CssPseudoClassNode(FunctionType.LANG, name,
        arg, null);
    assertThat(node.getFunctionType()).isEqualTo(FunctionType.LANG);
    assertThat(node.getRefinerName()).isEqualTo(name);
    assertThat(node.getArgument()).isEqualTo(arg);
    assertThat(node.getNotSelector()).isNull();
  }

  public void testNthFunction() {
    String name = "foo";
    String arg = "2n+1";
    CssPseudoClassNode node = new CssPseudoClassNode(FunctionType.NTH, name,
        arg, null);
    assertThat(node.getFunctionType()).isEqualTo(FunctionType.NTH);
    assertThat(node.getRefinerName()).isEqualTo(name);
    assertThat(node.getArgument()).isEqualTo(arg);
    assertThat(node.getNotSelector()).isNull();
  }

  public void testNotFunction() {
    String name = "not";
    CssSelectorNode selector = new CssSelectorNode("foo");
    CssPseudoClassNode node = new CssPseudoClassNode(name, selector, null);
    assertThat(node.getFunctionType()).isEqualTo(FunctionType.NOT);
    assertThat(node.getRefinerName()).isEqualTo(name);
    assertThat(node.getNotSelector()).isEqualTo(selector);
    assertThat(node.getArgument()).isNull();
  }
}
