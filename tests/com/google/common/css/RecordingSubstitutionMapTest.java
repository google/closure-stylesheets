/*
 * Copyright 2016 Google Inc.
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

package com.google.common.css;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class RecordingSubstitutionMapTest {

  private static final ImmutableSet<String> OUTPUT_BLACKLIST = ImmutableSet.of("c", "e", "i");

  private static SubstitutionMap createDelegate() {
    // Test a whole mess of delegate classes.
    SubstitutionMap m = new MinimalSubstitutionMap(OUTPUT_BLACKLIST);
    m = new SplittingSubstitutionMap(m);
    m = new PrefixingSubstitutionMap(m, "x-");
    return m;
  }

  @Test
  public final void testReadAndWrite() throws IOException {
    for (OutputRenamingMapFormat format : OutputRenamingMapFormat.values()) {
      RecordingSubstitutionMap recording =
          new RecordingSubstitutionMap.Builder().withSubstitutionMap(createDelegate()).build();
      // Put some stuff into the map.
      // TIL: there are a lot of websites on the A-Z of fruits & vegetables.
      assertThat(recording.get("apple")).isEqualTo("x-a");
      assertThat(recording.get("banana")).isEqualTo("x-b");
      assertThat(recording.get("durian")).isEqualTo("x-d");
      assertThat(recording.get("figgy-goop")).isEqualTo("x-f-g");

      // Write it out.
      StringWriter out = new StringWriter();
      String formatted;
      try {
        format.writeRenamingMap(recording.getMappings(), out);
        formatted = out.toString();
      } finally {
        out.close();
      }

      // Reconstitute it.
      RecordingSubstitutionMap recordingFromString;
      StringReader in = new StringReader(formatted);
      try {
        recordingFromString =
            new RecordingSubstitutionMap.Builder()
                .withSubstitutionMap(createDelegate())
                .withMappings(format.readRenamingMap(in))
                .build();
        assertThat(in.read()).isEqualTo(-1);
      } catch (IOException | RuntimeException ex) {
        throw (AssertionFailedError)
            new AssertionFailedError(
                "Problem with input formatted according to "
                + format
                + "\n```\n"
                + formatted
                + "\n```")
            .initCause(ex);
      } finally {
        in.close();
      }

      // Vary the order to check that we get stable values from the
      // earlier uses, and unambiguous new values.
      assertThat(recordingFromString.get("banana")).isEqualTo("x-b");
      assertThat(recordingFromString.get("honeydew")).isEqualTo("x-h");
      assertThat(recordingFromString.get("apple")).isEqualTo("x-a");
      assertThat(recordingFromString.get("figgy-goop")).isEqualTo("x-f-g");
      assertThat(recordingFromString.get("durian")).isEqualTo("x-d");
      assertThat(recordingFromString.get("jalapeno")).isEqualTo("x-j");
    }
  }
}
