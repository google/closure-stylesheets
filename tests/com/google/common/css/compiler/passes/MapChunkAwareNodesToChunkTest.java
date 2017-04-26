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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssValueNode;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link MapChunkAwareNodesToChunk}.
 *
 * <p>This test case can be extended, so that the tests are reused. The check* methods need to be
 * overridden if the subclass expects a different result.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
@RunWith(JUnit4.class)
public class MapChunkAwareNodesToChunkTest {

  protected static final String F2 = "b";
  protected static final String F1 = "a";
  protected static final String F3 = "c";
  protected static final String F4 = "d";
  protected static final String F5 = "e";

  protected static final String CS = "D";
  protected static final String CA = "A";
  protected static final String CB = "B";
  protected static final String CC = "C";

  protected static final ImmutableMap<String, String> FILE_TO_CHUNK =
      ImmutableMap.<String, String>builder()
          .put(F1, CA)
          .put(F2, CA)
          .put(F3, CB)
          .put(F4, CC)
          .put(F5, CS)
          .build();

  protected MapChunkAwareNodesToChunk<String> pass;
  protected CssSelectorNode sel1a;
  protected CssSelectorNode sel1b;
  protected CssSelectorNode sel2a;
  protected CssSelectorNode sel3a;
  protected CssSelectorNode sel3b;
  protected CssSelectorNode sel3c;
  protected CssSelectorNode sel4c;
  protected CssSelectorNode sel5a;

  protected CssDefinitionNode def1a;
  protected CssDefinitionNode def2a;

  protected CssFunctionNode fun1b;

  protected CssMediaRuleNode media3a;

  protected CssKeyframesNode keyframes3b;

  @Before
  public void setUp() throws Exception {
    pass = getPass(FILE_TO_CHUNK);

    List<CssValueNode> parameters = ImmutableList.of();

    SourceCode sourceCode1 = new SourceCode(F1, null);

    SourceCodeLocation location1a =
        new SourceCodeLocation(sourceCode1, 1, 1, 1, 2, 1, 1);
    sel1a = new CssSelectorNode("a", location1a);
    def1a = new CssDefinitionNode(
        parameters, new CssLiteralNode("DEF1"), null, location1a);

    SourceCodeLocation location1b =
      new SourceCodeLocation(sourceCode1, 10, 2, 2, 11, 2, 2);
    sel1b = new CssSelectorNode("b", location1b);
    fun1b =
        new CssFunctionNode(CssFunctionNode.Function.byName("url"), location1b);

    SourceCode sourceCode2 = new SourceCode(F2, null);

    SourceCodeLocation location2a =
        new SourceCodeLocation(sourceCode2, 1, 1, 1, 2, 1, 1);
    sel2a = new CssSelectorNode("a", location2a);
    def2a = new CssDefinitionNode(
        parameters, new CssLiteralNode("DEF2"), null, location2a);

    SourceCode sourceCode3 = new SourceCode(F3, null);

    SourceCodeLocation location3a =
        new SourceCodeLocation(sourceCode3, 1, 1, 1, 2, 1, 1);
    sel3a = new CssSelectorNode("a", location3a);
    media3a = new CssMediaRuleNode();
    media3a.setSourceCodeLocation(location3a);

    SourceCodeLocation location3b =
        new SourceCodeLocation(sourceCode3, 1, 1, 1, 2, 1, 1);
    sel3b = new CssSelectorNode("b", location3b);
    keyframes3b = new CssKeyframesNode(new CssLiteralNode("keyframes"));
    keyframes3b.setSourceCodeLocation(location3b);

    SourceCodeLocation location3c =
      new SourceCodeLocation(sourceCode3, 10, 2, 2, 11, 2, 2);
    sel3c = new CssSelectorNode("c", location3c);

    SourceCode sourceCode4 = new SourceCode(F4, null);

    SourceCodeLocation location4c =
      new SourceCodeLocation(sourceCode4, 10, 2, 2, 11, 2, 2);
    sel4c = new CssSelectorNode("c", location4c);

    SourceCode sourceCode5 = new SourceCode(F5, null);

    SourceCodeLocation location5a =
        new SourceCodeLocation(sourceCode5, 1, 1, 1, 2, 1, 1);
    sel5a = new CssSelectorNode("a", location5a);
  }

  @Test
  public void testMapToChunk() {
    setupEnterSelector();
    setupEnterDefinition();
    setupEnterFunctionNode();
    setupEnterMediaRule();
    setupEnterKeyframesRule();

    checkEnterSelector();
    checkEnterDefinition();

    // Only one assert per node type, so these aren't put into their own
    // functions.
    assertThat(fun1b.getChunk()).isEqualTo(CA);
    assertThat(media3a.getChunk()).isEqualTo(CB);
    assertThat(keyframes3b.getChunk()).isEqualTo(CB);
  }

  @Test
  public void testMissingFileToChunkMapping() {
    Map<String, String> badFileToChunk =
      ImmutableMap.<String ,String>builder()
          .put(F1, CA)
          .put(F3, CB)
          .put(F4, CC)
          .put(F5, CS)
          .build();
    pass = getPass(badFileToChunk);
    try {
      pass.enterSelector(sel2a);
      Assert.fail("Node 2a does not have a file to chunk mapping");
    } catch (NullPointerException expected) {
      // OK
    }
  }

  protected MapChunkAwareNodesToChunk<String> getPass(Map<String, String> fileToChunk) {
    return new MapChunkAwareNodesToChunk<String>(null, fileToChunk);
  }

  protected void checkEnterSelector() {
    assertThat(sel1a.getChunk()).isEqualTo(CA);
    assertThat(sel1b.getChunk()).isEqualTo(CA);
    assertThat(sel2a.getChunk()).isEqualTo(CA);
    assertThat(sel3a.getChunk()).isEqualTo(CB);
    assertThat(sel3b.getChunk()).isEqualTo(CB);
    assertThat(sel3c.getChunk()).isEqualTo(CB);
    assertThat(sel4c.getChunk()).isEqualTo(CC);
    assertThat(sel5a.getChunk()).isEqualTo(CS);
  }

  protected void checkEnterDefinition() {
    assertThat(def1a.getChunk()).isEqualTo(CA);
    assertThat(def2a.getChunk()).isEqualTo(CA);
  }

  private void setupEnterSelector() {
    assertThat(sel1a.getChunk()).isNull();
    assertThat(sel1b.getChunk()).isNull();
    assertThat(sel2a.getChunk()).isNull();
    assertThat(sel3a.getChunk()).isNull();
    assertThat(sel3b.getChunk()).isNull();
    assertThat(sel3c.getChunk()).isNull();
    assertThat(sel4c.getChunk()).isNull();
    assertThat(sel5a.getChunk()).isNull();

    pass.enterSelector(sel1a);
    pass.enterSelector(sel1b);
    pass.enterSelector(sel2a);
    pass.enterSelector(sel3a);
    pass.enterSelector(sel3b);
    pass.enterSelector(sel3c);
    pass.enterSelector(sel4c);
    pass.enterSelector(sel5a);
  }

  private void setupEnterDefinition() {
    assertThat(def1a.getChunk()).isNull();
    assertThat(def2a.getChunk()).isNull();

    pass.enterDefinition(def1a);
    pass.enterDefinition(def2a);
  }

  private void setupEnterFunctionNode() {
    assertThat(fun1b.getChunk()).isNull();
    pass.enterFunctionNode(fun1b);
  }

  private void setupEnterMediaRule() {
    assertThat(media3a.getChunk()).isNull();
    pass.enterMediaRule(media3a);
  }

  private void setupEnterKeyframesRule() {
    assertThat(keyframes3b.getChunk()).isNull();
    pass.enterKeyframesRule(keyframes3b);
  }
}
