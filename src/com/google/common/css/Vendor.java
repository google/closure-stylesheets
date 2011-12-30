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

package com.google.common.css;

import javax.annotation.Nullable;

/**
 * A browser vendor that provides non-standard CSS properties that can be
 * identified by a special prefix.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public enum Vendor {
  WEBKIT("-webkit-"),
  MOZILLA("-moz-"),
  MICROSOFT("-ms-"),
  OPERA("-o-"),
  ;

  private final String prefix;

  private Vendor(String prefix) {
    this.prefix = prefix;
  }

  public static @Nullable Vendor parseProperty(String propertyName) {
    for (Vendor vendor : values()) {
      if (propertyName.startsWith(vendor.prefix)) {
        return vendor;
      }
    }
    return null;
  }
}
