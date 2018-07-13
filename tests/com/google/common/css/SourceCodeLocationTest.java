/*
 * Copyright 2008 Google Inc.
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

import static com.google.common.css.compiler.ast.testing.SourceCodeLocationSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link SourceCodeLocation} and {@link SourceCodeLocation.SourceCodePoint}.
 *
 */
@RunWith(JUnit4.class)
public class SourceCodeLocationTest {
  @Test
  public void testCreation() {
    String testSource = "abc\ndefg";
    SourceCodeLocation l = new SourceCodeLocation(
        new SourceCode("testfile", testSource),
        2, 1, 3, 7, 2, 4);
    assertThat(l.getSourceCode().getFileName()).isEqualTo("testfile");
    assertThat(l.getSourceCode().getFileContents()).isEqualTo(testSource);
    assertThat(l.getBeginCharacterIndex()).isEqualTo(2);
    assertThat(l.getBeginLineNumber()).isEqualTo(1);
    assertThat(l.getBeginIndexInLine()).isEqualTo(3);
    assertThat(l.getEndCharacterIndex()).isEqualTo(7);
    assertThat(l.getEndLineNumber()).isEqualTo(2);
    assertThat(l.getEndIndexInLine()).isEqualTo(4);
    assertThat(testSource.charAt(l.getBeginCharacterIndex())).isEqualTo('c');
    assertThat(testSource.charAt(l.getEndCharacterIndex())).isEqualTo('g');
    assertThat("abc".charAt(l.getBeginIndexInLine() - 1)).isEqualTo('c');
    assertThat("defg".charAt(l.getEndIndexInLine() - 1)).isEqualTo('g');
  }

  @Test
  public void testBadCreation1() {
    SourceCode sourceCode = new SourceCode("testfile", "abc\ndefg");
    try {
      new SourceCodeLocation(sourceCode, 7, 2, 4, 2, 1, 3);
      Assert.fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Beginning location must come before the end location.");
    }
  }

  @Test
  @SuppressWarnings("SelfComparison")
  public void testComparisonOfEqualLocations1() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 =
      new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 =
      new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    assertThat(loc1.equals(loc1)).isTrue();
    assertThat(loc1.equals(loc2)).isTrue();
    assertThat(loc2.equals(loc1)).isTrue();
    assertThat(loc2.equals(loc2)).isTrue();
    assertThat(loc1.compareTo(loc2)).isEqualTo(0);
    assertThat(loc1.compareTo(loc1)).isEqualTo(0);
    assertThat(loc2.compareTo(loc1)).isEqualTo(0);
    assertThat(loc2.compareTo(loc2)).isEqualTo(0);
    assertThat(loc2.hashCode()).isEqualTo(loc1.hashCode());
  }

  @Test
  @SuppressWarnings("SelfComparison")
  public void testComparisonOfEqualLocations2() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 =
      new SourceCodeLocation(sourceCode, -1, 0, 0, -1, 0, 0);
    SourceCodeLocation loc2 =
      new SourceCodeLocation(sourceCode, -1, 0, 0, -1, 0, 0);
    assertThat(loc1.equals(loc1)).isTrue();
    assertThat(loc1.equals(loc2)).isTrue();
    assertThat(loc2.equals(loc1)).isTrue();
    assertThat(loc2.equals(loc2)).isTrue();
    assertThat(loc1.compareTo(loc2)).isEqualTo(0);
    assertThat(loc1.compareTo(loc1)).isEqualTo(0);
    assertThat(loc2.compareTo(loc1)).isEqualTo(0);
    assertThat(loc2.compareTo(loc2)).isEqualTo(0);
    assertThat(loc2.hashCode()).isEqualTo(loc1.hashCode());
  }

  @Test
  public void testComparisonOfEqualLocations3() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 =
      new SourceCodeLocation(sourceCode, -1, 0, 0, -1, 0, 0);
    SourceCodeLocation loc2 =
      new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    assertThat(loc1.equals(loc2)).isFalse();
    assertThat(loc2.equals(loc1)).isFalse();
    assertThat(loc1.compareTo(loc2)).isEqualTo(-1);
    assertThat(loc2.compareTo(loc1)).isEqualTo(1);
  }

  @Test
  public void testComparisonOfEqualLocations4() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 =
      new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 =
      new SourceCodeLocation(sourceCode, 0, 1, 1, 3, 1, 4);
    assertThat(loc1.equals(loc2)).isFalse();
    assertThat(loc2.equals(loc1)).isFalse();
    assertThat(loc1.compareTo(loc2)).isEqualTo(-1);
    assertThat(loc2.compareTo(loc1)).isEqualTo(1);
  }

  @Test
  public void testComparisonOfEqualLocations5() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 =
      new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 =
      new SourceCodeLocation(sourceCode, 1, 1, 2, 2, 1, 3);
    assertThat(loc1.equals(loc2)).isFalse();
    assertThat(loc2.equals(loc1)).isFalse();
    assertThat(loc1.compareTo(loc2)).isEqualTo(-1);
    assertThat(loc2.compareTo(loc1)).isEqualTo(1);
  }

  @Test
  public void testComparisonOfEqualLocations6() {
    SourceCode sourceCode1 = new SourceCode("testfile1", "abcdef");
    SourceCodeLocation loc1 =
      new SourceCodeLocation(sourceCode1, 0, 1, 1, 2, 1, 3);
    SourceCode sourceCode2 = new SourceCode("testfile2", "abcdef");
    SourceCodeLocation loc2 =
      new SourceCodeLocation(sourceCode2, 0, 1, 1, 2, 1, 3);
    assertThat(loc1.equals(loc2)).isFalse();
    assertThat(loc2.equals(loc1)).isFalse();
  }

  @Test
  public void testMergeTwo_sameFileInOrder() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 = new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 = new SourceCodeLocation(sourceCode, 1, 1, 2, 2, 1, 3);

    SourceCodeLocation loc3 = SourceCodeLocation.merge(loc1, loc2);
    assertThat(loc3).hasSpan(1, 1, 1, 3);
    assertThat(loc3).matches("ab");
  }

  @Test
  public void testMergeTwo_sameFileInWrongOrder() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 = new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 = new SourceCodeLocation(sourceCode, 1, 1, 2, 2, 1, 3);

    try {
      SourceCodeLocation loc3 = SourceCodeLocation.merge(loc2, loc1);
      Assert.fail("merge should have thrown an exception for out of order locations");
    } catch (Exception expected) {}
  }

  @Test
  public void testMergeTwo_differentFiles() {
    SourceCode sourceCode1 = new SourceCode("testfile1", "abcdef");
    SourceCode sourceCode2 = new SourceCode("testfile2", "abcdef");
    SourceCodeLocation loc1 = new SourceCodeLocation(sourceCode1, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 = new SourceCodeLocation(sourceCode2, 1, 1, 2, 2, 1, 3);

    try {
      SourceCodeLocation loc3 = SourceCodeLocation.merge(loc1, loc2);
      Assert.fail("merge should have thrown an exception for locations in different files");
    } catch (Exception expected) {}
  }

  @Test
  public void testMergeIterable_sameFileInOrderTwo() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 = new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 = new SourceCodeLocation(sourceCode, 1, 1, 2, 2, 1, 3);

    SourceCodeLocation loc3 = SourceCodeLocation.mergeAll(ImmutableList.of(loc1, loc2));
    assertThat(loc3).hasSpan(1, 1, 1, 3);
    assertThat(loc3).matches("ab");
  }

  @Test
  public void testMergeIterable_sameFileInOrderThree() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 = new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 = new SourceCodeLocation(sourceCode, 1, 1, 2, 2, 1, 3);
    SourceCodeLocation loc3 = new SourceCodeLocation(sourceCode, 4, 1, 5, 5, 1, 6);
    assertThat(loc1).matches("ab");
    assertThat(loc2).matches("b");
    assertThat(loc3).matches("e");

    SourceCodeLocation loc4 = SourceCodeLocation.mergeAll(ImmutableList.of(loc1, loc2, loc3));
    assertThat(loc4).hasSpan(1, 1, 1, 6);
    assertThat(loc4).matches("abcde");
  }

  @Test
  public void testMergeIterable_sameFileOutOfOrderThree() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 = new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 = new SourceCodeLocation(sourceCode, 1, 1, 2, 2, 1, 3);
    SourceCodeLocation loc3 = new SourceCodeLocation(sourceCode, 4, 1, 5, 5, 1, 6);
    assertThat(loc1).matches("ab");
    assertThat(loc2).matches("b");
    assertThat(loc3).matches("e");

    SourceCodeLocation loc4 = SourceCodeLocation.mergeAll(ImmutableList.of(loc1, loc3, loc2));
    assertThat(loc4).hasSpan(1, 1, 1, 6);
    assertThat(loc4).matches("abcde");
  }

  @Test
  public void testMergeIterable_sameFileReverseOrderThree() {
    SourceCode sourceCode = new SourceCode("testfile", "abcdef");
    SourceCodeLocation loc1 = new SourceCodeLocation(sourceCode, 0, 1, 1, 2, 1, 3);
    SourceCodeLocation loc2 = new SourceCodeLocation(sourceCode, 1, 1, 2, 2, 1, 3);
    SourceCodeLocation loc3 = new SourceCodeLocation(sourceCode, 4, 1, 5, 5, 1, 6);
    assertThat(loc1).matches("ab");
    assertThat(loc2).matches("b");
    assertThat(loc3).matches("e");

    SourceCodeLocation loc4 = SourceCodeLocation.mergeAll(ImmutableList.of(loc3, loc2, loc1));
    assertThat(loc4).hasSpan(1, 1, 1, 6);
    assertThat(loc4).matches("abcde");
  }
}
