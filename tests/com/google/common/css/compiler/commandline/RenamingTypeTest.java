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

package com.google.common.css.compiler.commandline;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.css.IdentitySubstitutionMap;
import com.google.common.css.RecordingSubstitutionMap;
import com.google.common.css.SubstitutionMap;
import com.google.common.css.SubstitutionMapProvider;

import junit.framework.TestCase;

import java.util.Set;

/**
 * {@link RenamingTypeTest} is a unit test for {@link RenamingType}.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class RenamingTypeTest extends TestCase {

  public void testNone() {
    SubstitutionMapProvider provider = RenamingType.NONE
        .getCssSubstitutionMapProvider();
    assertNotNull(provider);

    SubstitutionMap map = provider.get();
    assertNotNull(map);
    // This is sufficient to guarantee the contract of the renaming.
    assertTrue(map instanceof IdentitySubstitutionMap);
  }

  public void testDebug() {
    SubstitutionMapProvider provider = RenamingType.DEBUG
        .getCssSubstitutionMapProvider();
    assertNotNull(provider);

    SubstitutionMap map = provider.get();
    assertNotNull(map);

    assertEquals("dialog_", map.get("dialog"));
    assertEquals("dialog_-button_", map.get("dialog-button"));
    assertEquals("button__", map.get("button_"));

    testRenamingTypeThatWrapsASplittingSubstitutionMap(RenamingType.DEBUG);
  }


  public void testClosure() {
    SubstitutionMapProvider provider = RenamingType.CLOSURE
        .getCssSubstitutionMapProvider();
    assertNotNull(provider);

    SubstitutionMap map = provider.get();
    assertNotNull(map);

    assertEquals("a", map.get("dialog"));
    assertEquals("b", map.get("settings"));
    assertEquals("a-c", map.get("dialog-button"));
    assertEquals("c", map.get("button"));
    assertEquals(
        "A CSS class may include a part with the same name multiple times.",
        "d-e-c-c-f", map.get("goog-imageless-button-button-pos"));

    testRenamingTypeThatWrapsASplittingSubstitutionMap(RenamingType.CLOSURE);
  }

  private void testRenamingTypeThatWrapsASplittingSubstitutionMap(RenamingType
      renamingType) {
    SubstitutionMapProvider provider = renamingType
        .getCssSubstitutionMapProvider();
    RecordingSubstitutionMap map = new RecordingSubstitutionMap(provider.get(),
        Predicates.alwaysTrue());

    map.get("dialog-content");
    map.get("dialog-title");

    Set<String> expectedMappings = ImmutableSet.of(
        "content",
        "dialog",
        "title"
        );
    Set<String> observedMappings = map.getMappings().keySet();
    assertEquals("There should be entries for both 'dialog' and 'content' in"
        + "case someone does: "
        + "goog.getCssName(goog.getCssName('dialog'), 'content')",
        expectedMappings, observedMappings);
  }
}
