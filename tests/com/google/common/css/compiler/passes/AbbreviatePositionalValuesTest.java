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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Iterables;
import com.google.common.css.compiler.ast.BackDoorNodeMutation;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssHexColorNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPropertyNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.MutatingVisitController;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

/** @author Andrew Gove (agove@google.com) */
@RunWith(MockitoJUnitRunner.class)
public class AbbreviatePositionalValuesTest {

  @Captor ArgumentCaptor<List<CssNode>> cssNodesCaptor;

  @Test
  public void testEnterDeclaration() {
    MutatingVisitController mockVisitController = mock(MutatingVisitController.class);

    AbbreviatePositionalValues pass = new AbbreviatePositionalValues(mockVisitController);

    CssDeclarationNode declaration = new CssDeclarationNode(
        new CssPropertyNode("padding"),
        new CssPropertyValueNode());
    BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
        new CssLiteralNode("A"));
    BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
        new CssLiteralNode("A"));
    BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
        new CssLiteralNode("A"));
    BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
        new CssLiteralNode("A"));

    pass.enterDeclaration(declaration);

    verify(mockVisitController)
        .replaceCurrentBlockChildWith(cssNodesCaptor.capture(), Matchers.anyBoolean());
    CssNode cssNode = Iterables.getOnlyElement(cssNodesCaptor.getValue());
    assertThat(cssNode).isInstanceOf(CssDeclarationNode.class);
    CssDeclarationNode replacement = (CssDeclarationNode) cssNode;
    assertThat(replacement.getPropertyValue().numChildren()).isEqualTo(1);
    assertThat(replacement.getPropertyValue().getChildAt(0).getValue()).isEqualTo("A");
  }

  @Test
  public void testEqualLiterals() {
    CssLiteralNode v1 = new CssLiteralNode("auto");
    CssLiteralNode v2 = new CssLiteralNode("auto");
    CssLiteralNode v3 = new CssLiteralNode("blah");

    assertThat(AbbreviatePositionalValues.equalValues(v1, v2)).isTrue();
    assertThat(AbbreviatePositionalValues.equalValues(v1, v3)).isFalse();
  }

  @Test
  public void testEqualNumerics() {
    CssNumericNode v1 = new CssNumericNode("5", "%");
    CssNumericNode v2 = new CssNumericNode("5", "%");
    CssNumericNode v3 = new CssNumericNode("5", "");

    assertThat(AbbreviatePositionalValues.equalValues(v1, v2)).isTrue();
    assertThat(AbbreviatePositionalValues.equalValues(v1, v3)).isFalse();
  }

  @Test
  public void testEqualColors() {
    CssHexColorNode v1 = new CssHexColorNode("#ccc");
    CssHexColorNode v2 = new CssHexColorNode("#ccc");
    CssHexColorNode v3 = new CssHexColorNode("#fff");

    assertThat(AbbreviatePositionalValues.equalValues(v1, v2)).isTrue();
    assertThat(AbbreviatePositionalValues.equalValues(v1, v3)).isFalse();
  }
}
