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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssValueNode;

import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link MapChunkAwareNodesToChunk}.
 *
 * <p>This test case can be extended, so that the tests are
 * reused. The check* methods need to be overridden if the subclass
 * expects a different result.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
public class MapChunkAwareNodesToChunkTest extends TestCase {

  protected static final String F2 = "b";
  protected static final String F1 = "a";
  protected static final String F3 = "c";
  protected static final String F4 = "d";
  protected static final String F5 = "e";

  protected static final String CS = "D";
  protected static final String CA = "A";
  protected static final String CB = "B";
  protected static final String CC = "C";

  protected static final Map<String, String> FILE_TO_CHUNK =
      ImmutableMap.<String ,String>builder()
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

  @Override
  protected void setUp() throws Exception {
    super.setUp();
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

    SourceCodeLocation location3b =
        new SourceCodeLocation(sourceCode3, 1, 1, 1, 2, 1, 1);
    sel3b = new CssSelectorNode("b", location3b);

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

  public void testMapToChunk() {
    setupEnterSelector();
    setupEnterDefinition();

    checkEnterSelector();
    checkEnterDefinition();
  }

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
      fail("Node 2a does not have a file to chunk mapping");
    } catch (NullPointerException expected) {
      // OK
    }
  }

  protected MapChunkAwareNodesToChunk<String> getPass(Map<String, String> fileToChunk) {
    return new MapChunkAwareNodesToChunk<String>(null, fileToChunk);
  }

  protected void checkEnterSelector() {
    assertEquals(CA, sel1a.getChunk());
    assertEquals(CA, sel1b.getChunk());
    assertEquals(CA, sel2a.getChunk());
    assertEquals(CB, sel3a.getChunk());
    assertEquals(CB, sel3b.getChunk());
    assertEquals(CB, sel3c.getChunk());
    assertEquals(CC, sel4c.getChunk());
    assertEquals(CS, sel5a.getChunk());
  }

  protected void checkEnterDefinition() {
    assertEquals(CA, def1a.getChunk());
    assertEquals(CA, def2a.getChunk());
  }

  private void setupEnterSelector() {
    assertNull(sel1a.getChunk());
    assertNull(sel1b.getChunk());
    assertNull(sel2a.getChunk());
    assertNull(sel3a.getChunk());
    assertNull(sel3b.getChunk());
    assertNull(sel3c.getChunk());
    assertNull(sel4c.getChunk());
    assertNull(sel5a.getChunk());

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
    assertNull(def1a.getChunk());
    assertNull(def2a.getChunk());

    pass.enterDefinition(def1a);
    pass.enterDefinition(def2a);
  }
}
