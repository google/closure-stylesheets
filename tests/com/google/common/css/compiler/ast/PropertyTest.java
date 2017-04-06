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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.css.Vendor;
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
    assertThat(borderRadius.isRecognizedProperty()).isTrue();
    assertThat(borderRadius.getVendor()).isNull();
    assertThat(borderRadius.getPartition()).isEqualTo("border-radius");
    assertThat(borderRadius.getShorthands()).containsExactly("border-radius");

    Property borderTopLeftRadius = Property.byName("border-top-left-radius");
    assertThat(borderTopLeftRadius.isRecognizedProperty()).isTrue();
    assertThat(borderRadius.getVendor()).isNull();
    assertThat(borderRadius.getPartition()).isEqualTo("border-radius");
    assertThat(borderRadius.getShorthands()).containsExactly("border-radius");
  }

  public void testWebkitBorderRadius() {
    Property webkitBorderRadius = Property.byName("-webkit-border-radius");
    assertThat(webkitBorderRadius.isRecognizedProperty()).isTrue();
    assertThat(webkitBorderRadius.getVendor()).isEqualTo(Vendor.WEBKIT);
    assertThat(webkitBorderRadius.getPartition()).isEqualTo("-webkit-border-radius");
    assertThat(webkitBorderRadius.getShorthands()).containsExactly("-webkit-border-radius");

    Property webkitBorderBottomRightRadius = Property.byName(
        "-webkit-border-bottom-right-radius");
    assertThat(webkitBorderBottomRightRadius.isRecognizedProperty()).isTrue();
    assertThat(webkitBorderBottomRightRadius.getVendor()).isEqualTo(Vendor.WEBKIT);
    assertThat(webkitBorderBottomRightRadius.getPartition()).isEqualTo("-webkit-border-radius");
    assertThat(webkitBorderBottomRightRadius.getShorthands())
        .containsExactly("-webkit-border-radius");
  }

  public void testMozBorderRadius() {
    Property mozBorderRadius = Property.byName("-moz-border-radius");
    assertThat(mozBorderRadius.isRecognizedProperty()).isTrue();
    assertThat(mozBorderRadius.getVendor()).isEqualTo(Vendor.MOZILLA);
    assertThat(mozBorderRadius.getPartition()).isEqualTo("-moz-border-radius");
    assertThat(mozBorderRadius.getShorthands()).containsExactly("-moz-border-radius");

    Property mozBorderRadiusTopLeft = Property.byName(
        "-moz-border-radius-topright");
    assertThat(mozBorderRadiusTopLeft.isRecognizedProperty()).isTrue();
    assertThat(mozBorderRadiusTopLeft.getVendor()).isEqualTo(Vendor.MOZILLA);
    assertThat(mozBorderRadiusTopLeft.getPartition()).isEqualTo("-moz-border-radius");
    assertThat(mozBorderRadiusTopLeft.getShorthands()).containsExactly("-moz-border-radius");
  }

  public void testCustomBorderProperty() {
    Property borderHeight = Property.byName("border-height");
    assertThat(borderHeight.isRecognizedProperty()).isFalse();
    assertThat(borderHeight.getShorthands()).isEmpty();

    Property borderLeftHeight = Property.byName("border-left-height");
    assertThat(borderLeftHeight.isRecognizedProperty()).isFalse();
    assertThat(borderHeight.getShorthands()).isEmpty();

    Property borderRightHeight = Property.byName("border-right-height");
    assertThat(borderRightHeight.isRecognizedProperty()).isFalse();
    assertThat(borderHeight.getShorthands()).isEmpty();

    Property borderTopHeight = Property.byName("border-top-height");
    assertThat(borderTopHeight.isRecognizedProperty()).isFalse();
    assertThat(borderHeight.getShorthands()).isEmpty();

    Property borderBottomHeight = Property.byName("border-bottom-height");
    assertThat(borderBottomHeight.isRecognizedProperty()).isFalse();
    assertThat(borderHeight.getShorthands()).isEmpty();
  }
}
