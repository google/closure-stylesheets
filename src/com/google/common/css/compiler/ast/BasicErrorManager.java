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

package com.google.common.css.compiler.ast;

import com.google.common.collect.Sets;

import java.util.SortedSet;

/**
 * A basic error manager that sorts all errors reported to it and generate a
 * sorted report when the {@link #generateReport()} method is called.
 * (Sorting is based on {@link GssError#compareTo(GssError)}.)
 * <p>
 * This error manager delegates the method of output to subclasses via {@link #print(String)}.
 *
 */
public abstract class BasicErrorManager implements ErrorManager {
  protected final SortedSet<GssError> errors = Sets.newTreeSet();

  @Override
  public void report(GssError error) {
    errors.add(error);
  }

  @Override
  public void generateReport() {
    for (GssError error : errors) {
      print(error.format());
    }
    if (!errors.isEmpty()) {
      print(errors.size() + " error(s)\n");
    }
  }

  public int getErrorCount() {
    return errors.size();
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public abstract void print(String msg);

}
