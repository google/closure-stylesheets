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

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.css.JobDescription;
import com.google.common.css.JobDescriptionBuilder;
import com.google.common.css.MinimalSubstitutionMap;
import com.google.common.css.RecordingSubstitutionMap;
import com.google.common.css.SimpleSubstitutionMap;
import com.google.common.css.SourceCode;
import com.google.common.css.SubstitutionMap;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.testing.UtilityTestCase;

import java.util.Map;

/**
 * Test for RecordingSubstitutionMap.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class RecordingSubstitutionMapTest extends UtilityTestCase {

  private String styleSheet;
  private Predicate<String> predicate;
  private Map<String,String> mappings;

  @Override
  protected void setUp() {
    styleSheet = linesToString(
        ".CSS_SPRITE { background-image: url(\"foo.png\"); }",
        ".sprite { background-image: url(\"bar.png\"); }"
        );
    predicate = new Predicate<String>() {
      @Override
      public boolean apply(String key) {
        return key.startsWith("CSS_");
      }
    };
  }

  private void parse(String styleSheet, RecordingSubstitutionMap map) {
    SourceCode input = new SourceCode("test-input", styleSheet);
    GssParser parser = new GssParser(input);
    CssTree cssTree;
    try {
      cssTree = parser.parse();
    } catch (GssParserException e) {
      throw new RuntimeException(e);
    }
    JobDescription job = new JobDescriptionBuilder().getJobDescription();
    ErrorManager errorManager = new DummyErrorManager();
    PassRunner passRunner = new PassRunner(job, errorManager, map);
    passRunner.runPasses(cssTree);
  }

  public void testGet() {
    SubstitutionMap substitutionMap = new SubstitutionMap() {
      @Override
      public String get(String key) {
        if ("CSS_FOO".equals(key)) {
          return "a";
        } else if ("CSS_BAR".equals(key)) {
          return "b";
        } else {
          return key;
        }
      }
    };
    RecordingSubstitutionMap recordingMap = new RecordingSubstitutionMap(
        substitutionMap, predicate);
    assertEquals("a", recordingMap.get("CSS_FOO"));
    assertEquals("b", recordingMap.get("CSS_BAR"));
    assertEquals("CSS_BAZ", recordingMap.get("CSS_BAZ"));
    assertEquals("BIZ", recordingMap.get("BIZ"));

    mappings = recordingMap.getMappings();
    assertFalse("Predicate for RecordingSubstitutionMap was not honored",
        mappings.containsKey("BIZ"));
    Map<String,String> expectedMap = ImmutableMap.of("CSS_FOO", "a", "CSS_BAR",
        "b", "CSS_BAZ", "CSS_BAZ");
    assertEquals(expectedMap, mappings);
  }

  public void testOrderIsPreserved() {
    styleSheet = linesToString(
        ".zero { color: red; }",
        ".one { color: red; }",
        ".two { color: red; }",
        ".three { color: red; }",
        ".four { color: red; }",
        ".five { color: red; }",
        ".six { color: red; }",
        ".seven { color: red; }",
        ".eight { color: red; }",
        ".nine { color: red; }"
        );
    RecordingSubstitutionMap map = new RecordingSubstitutionMap(
        new SimpleSubstitutionMap(), alwaysTrue());
    parse(styleSheet, map);

    mappings = map.getMappings();
    assertEquals(10, mappings.size());

    assertThat(mappings.keySet())
        .containsExactly(
            "zero",
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine")
        .inOrder();
  }

  private RecordingSubstitutionMap setupWithMap(RecordingSubstitutionMap map) {
    parse(styleSheet, map);
    mappings = map.getMappings();
    return map;
  }

  public void testMapWithTypeIdentity() {
    RecordingSubstitutionMap map = new RecordingSubstitutionMap(
        new SimpleSubstitutionMap(), predicate);
    setupWithMap(map);
    assertEquals(1, mappings.size());
    assertEquals("CSS_SPRITE_", mappings.get("CSS_SPRITE"));
  }

  public void testMapWithTypeMinimal() {
    RecordingSubstitutionMap map = new RecordingSubstitutionMap(
        new MinimalSubstitutionMap(), predicate);
    setupWithMap(map);
    assertEquals(1, mappings.size());
    assertEquals(1, mappings.get("CSS_SPRITE").length());
  }
}
