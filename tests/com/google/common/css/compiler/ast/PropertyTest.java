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

import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.Property.Vendor;

import junit.framework.TestCase;

/**
 * Unit test for {@link Property}. For legacy reasons, much of the behavior of
 * {@link Property} is verified by {@link CssPropertyNodeTest} rather than this
 * test.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class PropertyTest extends TestCase {

  public void testBorderRadius() {
    Property borderRadius = Property.byName("border-radius");
    assertTrue(borderRadius.isRecognizedProperty());
    assertEquals(null, borderRadius.getVendor());
    assertEquals("border-radius", borderRadius.getPartition());
    assertEquals(ImmutableSet.of("border-radius"),
        borderRadius.getShorthands());

    Property borderTopLeftRadius = Property.byName("border-top-left-radius");
    assertTrue(borderTopLeftRadius.isRecognizedProperty());
    assertEquals(null, borderRadius.getVendor());
    assertEquals("border-radius", borderRadius.getPartition());
    assertEquals(ImmutableSet.of("border-radius"),
        borderRadius.getShorthands());
  }

  public void testWebkitBorderRadius() {
    Property webkitBorderRadius = Property.byName("-webkit-border-radius");
    assertTrue(webkitBorderRadius.isRecognizedProperty());
    assertEquals(Vendor.WEBKIT, webkitBorderRadius.getVendor());
    assertEquals("-webkit-border-radius", webkitBorderRadius.getPartition());
    assertEquals(ImmutableSet.of("-webkit-border-radius"),
        webkitBorderRadius.getShorthands());

    Property webkitBorderBottomRightRadius = Property.byName(
        "-webkit-border-bottom-right-radius");
    assertTrue(webkitBorderBottomRightRadius.isRecognizedProperty());
    assertEquals(Vendor.WEBKIT, webkitBorderBottomRightRadius.getVendor());
    assertEquals("-webkit-border-radius", webkitBorderBottomRightRadius
        .getPartition());
    assertEquals(ImmutableSet.of("-webkit-border-radius"),
        webkitBorderBottomRightRadius.getShorthands());
  }

  public void testMozBorderRadius() {
    Property mozBorderRadius = Property.byName("-moz-border-radius");
    assertTrue(mozBorderRadius.isRecognizedProperty());
    assertEquals(Vendor.MOZILLA, mozBorderRadius.getVendor());
    assertEquals("-moz-border-radius", mozBorderRadius.getPartition());
    assertEquals(ImmutableSet.of("-moz-border-radius"),
        mozBorderRadius.getShorthands());

    Property mozBorderRadiusTopLeft = Property.byName(
        "-moz-border-radius-topright");
    assertTrue(mozBorderRadiusTopLeft.isRecognizedProperty());
    assertEquals(Vendor.MOZILLA, mozBorderRadiusTopLeft.getVendor());
    assertEquals("-moz-border-radius", mozBorderRadiusTopLeft.getPartition());
    assertEquals(ImmutableSet.of("-moz-border-radius"),
        mozBorderRadiusTopLeft.getShorthands());
  }
}
