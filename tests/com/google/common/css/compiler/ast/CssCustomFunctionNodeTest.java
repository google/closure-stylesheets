/*
 * Copyright 2017 Google Inc.
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

import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.CssStringNode.Type;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for {@link CssCustomFunctionNode}. */
@RunWith(JUnit4.class)
public class CssCustomFunctionNodeTest {

  private static final CssValueNode SPACE = new CssLiteralNode(" ");
  private static final CssValueNode COMMA = new CssLiteralNode(",");

  @Test
  public void fixupFunctionArgumentsDoesNotModifyLiteralNodes_b35587881() throws Exception {
    CssValueNode x = new CssLiteralNode("x");
    CssValueNode y = new CssLiteralNode("y");
    ImmutableList<CssValueNode> parameters = ImmutableList.of(x, SPACE, y);
    List<CssValueNode> fixedParameters = CssCustomFunctionNode.fixupFunctionArguments(parameters);

    assertThat(x.getValue()).isEqualTo("x");
    assertThat(y.getValue()).isEqualTo("y");
    assertThat(fixedParameters).hasSize(1);
    assertThat(fixedParameters.get(0).toString()).isEqualTo("x y");
  }

  @Test
  public void stringsAsFunctionArgumentsAreNotHideouslyBroken() throws Exception {
    CssValueNode x = new CssLiteralNode("x");
    CssValueNode y = new CssStringNode(Type.DOUBLE_QUOTED_STRING, "double quotes!");
    CssValueNode z = new CssStringNode(Type.SINGLE_QUOTED_STRING, "single quotes!");
    ImmutableList<CssValueNode> parameters = ImmutableList.of(x, SPACE, y, COMMA, z);
    List<CssValueNode> fixedParameters = CssCustomFunctionNode.fixupFunctionArguments(parameters);

    assertThat(fixedParameters).hasSize(2);
    assertThat(fixedParameters.get(0).toString()).isEqualTo("x \"double quotes!\"");
    assertThat(fixedParameters.get(1).toString()).isEqualTo("'single quotes!'");
  }
}
