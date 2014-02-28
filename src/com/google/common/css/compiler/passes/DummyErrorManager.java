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

package com.google.common.css.compiler.passes;

import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;

/**
 * {@link DummyErrorManager} is a stateless error manager suitable for use
 * in the Null Object pattern.
 * {@link ErrorManager}.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public class DummyErrorManager implements ErrorManager {
  private static final DummyErrorManager INSTANCE = new DummyErrorManager();

  public static DummyErrorManager getInstance() {
    return INSTANCE;
  }

  @Override
  public void report(GssError error) {}

  @Override
  public void reportWarning(GssError warning) {}

  @Override
  public void generateReport() {}

  @Override
  public boolean hasErrors() {
    return false;
  }
}
