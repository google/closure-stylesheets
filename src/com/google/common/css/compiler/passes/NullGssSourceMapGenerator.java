/*
 * Copyright 2015 Google Inc.
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

import com.google.common.css.compiler.ast.CssNode;

import java.io.IOException;

/**
 * Null class implements {@link GssSourceMapGenerator}.
 * 
 * @author steveyang@google.com (Chenyun Yang)
 */
public final class NullGssSourceMapGenerator implements GssSourceMapGenerator {

  @Override
  public void appendOutputTo(Appendable out, String name) throws IOException {
    return;
  }

  @Override
  public void startSourceMapping(CssNode node, int startLine, int startCharIndex) {
    return;
  }

  @Override
  public void endSourceMapping(CssNode node, int endLine, int endCharIndex) {
    return;
  }

  @Override
  public void setSourceRoot(String path) {
    return;
  }
}
