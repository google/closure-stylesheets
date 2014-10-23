/*
 * Copyright 2005 Google Inc.
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

package com.google.testing.util;

import static java.util.Arrays.asList;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;

import junit.framework.Assert;

import java.util.Arrays;

/**
 * Contains additional assertion methods not found in JUnit.
 *
 */
public final class MoreAsserts {

  private MoreAsserts() {}

  /**
   * Asserts that {@code actual} contains equal elements to those in
   * {@code expected}, and in the same order.
   */
  public static void assertContentsInOrder(
      String message, Iterable<?> actual, Object... expected) {
    assertEqualsImpl(message,
        Arrays.asList(expected), Lists.newArrayList(actual));
  }

  /**
   * Variant of {@link #assertContentsInOrder(String,Iterable,Object...)}
   * using a generic message.
   */
  public static void assertContentsInOrder(
      Iterable<?> actual, Object... expected) {
    assertContentsInOrder((String) null, actual, expected);
  }

  /**
   * Asserts that {@code actual} contains precisely the elements
   * {@code expected}, in any order.  Both collections may contain
   * duplicates, and this method will only pass if the quantities are
   * exactly the same.
   */
  public static void assertContentsAnyOrder(
      Iterable<?> actual, Object... expected) {
    assertEqualsImpl((String) null,
        HashMultiset.create(asList(expected)), HashMultiset.create(actual));
  }

  /**
   * Replacement of {@link Assert#assertEquals} which provides the same error
   * message in GWT and java.
   */
  private static void assertEqualsImpl(
      String message, Object expected, Object actual) {
    if (!Objects.equal(expected, actual)) {
      failWithMessage(
          message, "expected:<" + expected + "> but was:<" + actual + ">");
    }
  }

  private static void failWithMessage(String userMessage, String ourMessage) {
    Assert.fail((userMessage == null)
        ? ourMessage
        : userMessage + ' ' + ourMessage);
  }
}
