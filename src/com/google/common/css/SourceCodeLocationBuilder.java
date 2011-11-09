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

import com.google.common.base.Preconditions;

/**
 * Aids in the creation of source code locations. A builder can be used for
 * creating a single {@link SourceCodeLocation}.
 *
 */
public class SourceCodeLocationBuilder {
  private SourceCode sourceCode;
  private int beginCharacterIndex;
  private int beginLineNumber;
  private int beginIndexInLine;
  private int endCharacterIndex;
  private int endLineNumber;
  private int endIndexInLine;

  /**
   * {@code null} until the location gets created. After that, none of the set
   * methods should be called, only {@link #getSourceCodeLocation()} should be
   * called.
   */
  private SourceCodeLocation sourceCodeLocation = null;

  public SourceCodeLocationBuilder() {
    // We initialize to a unknown location.
    sourceCode = null;
    beginCharacterIndex = -1;
    beginLineNumber = 0;
    beginIndexInLine = 0;
    endCharacterIndex = -1;
    endLineNumber = 0;
    endIndexInLine = 0;
  }

  private void checkLocationIsNotAlreadyCreated() {
    Preconditions.checkState(sourceCodeLocation == null, "You cannot set " +
        "source code location properties after the object was created.");
  }

  public SourceCodeLocationBuilder setSourceCode(SourceCode newSourceCode) {
    checkLocationIsNotAlreadyCreated();
    Preconditions.checkNotNull(newSourceCode);
    this.sourceCode = newSourceCode;
    return this;
  }

  public boolean hasSourceCode() {
    return this.sourceCode != null;
  }

  public SourceCodeLocationBuilder setBeginLocation(int characterIndex,
      int lineNumber, int indexInLine) {
    checkLocationIsNotAlreadyCreated();
    Preconditions.checkArgument(characterIndex >= 0,
        "The passed location is not valid.");
    Preconditions.checkArgument(lineNumber >= 1,
        "The passed location is not valid.");
    Preconditions.checkArgument(indexInLine >= 1,
        "The passed location is not valid.");
    this.beginCharacterIndex = characterIndex;
    this.beginLineNumber = lineNumber;
    this.beginIndexInLine = indexInLine;
    return this;
  }

  public boolean hasBeginLocation() {
    return this.beginCharacterIndex != -1;
  }

  public SourceCodeLocationBuilder setEndLocation(int characterIndex,
      int lineNumber, int indexInLine) {
    checkLocationIsNotAlreadyCreated();
    Preconditions.checkArgument(characterIndex >= 0,
        "The passed location is not valid.");
    Preconditions.checkArgument(lineNumber >= 1,
        "The passed location is not valid.");
    Preconditions.checkArgument(indexInLine >= 1,
        "The passed location is not valid.");
    this.endCharacterIndex = characterIndex;
    this.endLineNumber = lineNumber;
    this.endIndexInLine = indexInLine;
    return this;
  }

  public SourceCodeLocationBuilder setLocation(int characterIndex,
      int lineNumber, int indexInLine) {
    checkLocationIsNotAlreadyCreated();
    setBeginLocation(characterIndex, lineNumber, indexInLine);
    setEndLocation(characterIndex, lineNumber, indexInLine);
    return this;
  }

  public SourceCodeLocation getSourceCodeLocation() {
    if (sourceCodeLocation != null) {
      return sourceCodeLocation;
    }
    Preconditions.checkState(sourceCode != null,
        "You should specify a source code object");
    Preconditions.checkState(
        (beginCharacterIndex >= 0 && endCharacterIndex >= 0) ||
        (beginCharacterIndex == -1 && endCharacterIndex == -1),
        "Both the start and the end locations must be set.");
    Preconditions.checkState(hasSourceCode() || !hasBeginLocation(),
        "Must specify the source code if you specify a location.");
    sourceCodeLocation = new SourceCodeLocation(sourceCode,
        beginCharacterIndex, beginLineNumber, beginIndexInLine,
        endCharacterIndex, endLineNumber, endIndexInLine);
    return sourceCodeLocation;
  }
}
