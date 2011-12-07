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

import javax.annotation.Nullable;
import com.google.common.base.Preconditions;

/**
 * This is a wrapper for a file that will be compiled. It conveniently stores
 * both the file name and the file contents, so that the parser does not have
 * to deal with IO.
 *
 * <p>Instances of this class are immutable.
 *
 */
public final class SourceCode {
  private final String fileName;
  private final String fileContents;

  /**
   * Constructs a {@code SourceCode}. At least one of fileName and fileContents
   * must be non-{@code null}.
   *
   * @param fileName the name of the source code file or {@code null} if the
   *     input does not come from a file
   * @param fileContents the contents of the source code file or {@code null} if
   *     the file contents is not yet known (the file has not yet been read)
   */
  public SourceCode(@Nullable String fileName, @Nullable String fileContents) {
    Preconditions.checkArgument(fileName != null || fileContents != null);
    this.fileName = fileName;
    this.fileContents = fileContents;
  }

  public String getFileName() {
    return fileName;
  }

  public String getFileContents() {
    return fileContents;
  }

  int getFileContentsLength() {
    return fileContents.length();
  }
}
