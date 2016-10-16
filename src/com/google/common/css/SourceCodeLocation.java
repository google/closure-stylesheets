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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.primitives.Ints;
import java.util.Iterator;
import javax.annotation.Nullable;

/**
 * A location in source code. A location is a sequence of adjacent characters
 * that usually represent a token or a larger language construct.
 *
 * <p>Error messages represent the most common use of this class, as an error
 * message usually relates to a source code location. The location related to
 * some messages might not be known; this class has a special value that
 * represents an "unknown" location.
 *
 * <p>Character sequences this class points to can have 0 length. If that is the
 * case, the actual location is that "point" between two source code characters.
 * This usually means that something happens from that point onwards, or that an
 * error has been detected at that point but there is no information regarding
 * the actual token that caused the error.
 *
 * <p>Instances of this class are immutable.
 *
 */
public class SourceCodeLocation implements Comparable<SourceCodeLocation> {

  /**
   * This describes a point in a string. A point is the location between two
   * characters and is indicated by the character index of the immediately
   * following character. For example, in the string "abc", point 0 refers to
   * the location immediately before the 'a' and point 2 to the location before
   * the 'c'.
   *
   * <p>For convenience we also store the line number of the point (the line
   * that contains the character following the point) and the index in that
   * line. Both of these indices start at 1. The first line of a file is line 1
   * and the point before its first character has index 1 on line 1.
   *
   * <p>The exact definition of lines depends on the language conventions and is
   * best left for the parser to handle. If you want to display the text at that
   * location, either use the character index or use the line number and the
   * index in the line together with lines as they were split by the parser.
   *
   * <p>It might happen that the source code point for something is not known.
   * This is modeled by a point with the special value -1 for the character
   * index. The line and the index on the line must be 0 in this case.
   *
   * <p>Instances of this class are immutable.
   */
  @VisibleForTesting
  public static class SourceCodePoint implements Comparable<SourceCodePoint> {

    /**
     * The index of the character immediately after the source code point.
     * Indices start at 0; -1 means the location is not known.
     */
    private final int characterIndex;

    /**
     * The number of the line that contains the character at characterIndex.
     * Numbers start at 1; 0 means the location is not known.
     */
    private final int lineNumber;

    /**
     * The index in the line identified by lineNumber of the character at
     * characterIndex. Numbers start at 1; 0 means the location is not known.
     */
    private final int indexInLine;

    SourceCodePoint(int characterIndex, int lineNumber, int indexInLine) {
      this.lineNumber = lineNumber;
      this.indexInLine = indexInLine;
      this.characterIndex = characterIndex;
      if (!hasValidKnownCoordinates()
          && !hasValidUnknownCoordinates()) {
        throw new IllegalArgumentException(
          String.format(
              "The location passed "
              + "(lineNumber %d, indexInLine %d, characterIndex %d) "
              + "is not valid.",
              lineNumber, indexInLine, characterIndex));
      }
      if (!hasPlausibleCoordinates()) {
        throw new IllegalArgumentException(
          String.format(
              "The location passed "
              + "(lineNumber %d, indexInLine %d, characterIndex %d) "
              + "is not plausible.",
              lineNumber, indexInLine, characterIndex));
      }
    }

    SourceCodePoint(SourceCodePoint that) {
      this(that.characterIndex, that.lineNumber, that.indexInLine);
    }

    boolean hasValidKnownCoordinates() {
      return lineNumber >= 1 && indexInLine >= 1 && characterIndex >= 0;
    }

    boolean hasValidUnknownCoordinates() {
      return characterIndex == -1 && lineNumber == 0 && indexInLine == 0;
    }

    boolean hasPlausibleCoordinates() {
      return characterIndex >= lineNumber - 1 + indexInLine - 1;
    }

    int getCharacterIndex() {
      return characterIndex;
    }

    int getLineNumber() {
      return lineNumber;
    }

    int getIndexInLine() {
      return indexInLine;
    }

    boolean isUnknown() {
      return characterIndex == -1;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (o == null) {
        return false;
      }
      if (!(o instanceof SourceCodePoint)) {
        return false;
      }
      SourceCodePoint other = (SourceCodePoint) o;
      boolean areEqual = this.characterIndex == other.characterIndex;
      if (areEqual) {
        Preconditions.checkState((this.lineNumber == other.lineNumber)
            && (this.indexInLine == other.indexInLine),
            "Character indexes are equal but line numbers and indexes within " +
            "the line do not match.");
      } else {
        Preconditions.checkState((this.lineNumber != other.lineNumber)
            || (this.indexInLine != other.indexInLine),
            "Line numbers and indexes within the line match but character " +
            "indexes are not equal");
      }
      return areEqual;
    }

    @Override
    public int hashCode() {
      return characterIndex;
    }

    @Override
    public int compareTo(SourceCodePoint o) {
      Preconditions.checkNotNull(o);
      return Ints.compare(this.characterIndex, o.characterIndex);
    }
  }

  private static final SourceCode UNKNOWN_SOURCE_CODE =
      new SourceCode("unknown", "");

  /**
   * Returns an unknown location.
   */
  public static SourceCodeLocation getUnknownLocation() {
    SourceCodeLocation result = new SourceCodeLocation(
        UNKNOWN_SOURCE_CODE,
        -1 /* beginCharacterIndex */,
        0 /* beginLineNumber */,
        0 /* beginIndexInLine */,
        -1 /* endCharacterindex */,
        0 /* endLineNumber */,
        0 /* endIndexInLine */);
    Preconditions.checkState(result.isUnknown());
    Preconditions.checkState(result.begin.hasValidUnknownCoordinates());
    Preconditions.checkState(result.end.hasValidUnknownCoordinates());
    return result;
  }

  /**
   * Returns a new SourceCodeLocation which covers everything between the
   * beginning of the first location and the end of the second location.
   */
  public static SourceCodeLocation merge(SourceCodeLocation beginLocation,
      SourceCodeLocation endLocation) {
    Preconditions.checkArgument(beginLocation.sourceCode.equals(endLocation.sourceCode));
    return new SourceCodeLocation(beginLocation.sourceCode,
        beginLocation.getBeginCharacterIndex(),
        beginLocation.getBeginLineNumber(),
        beginLocation.getBeginIndexInLine(),
        endLocation.getEndCharacterIndex(),
        endLocation.getEndLineNumber(),
        endLocation.getEndIndexInLine());
  }

  public static SourceCodeLocation merge(
      Iterable<? extends CssNode> locations) {
    Iterator<? extends CssNode> i = locations.iterator();
    if (!i.hasNext()) {
      return getUnknownLocation();
    }
    SourceCodeLocation loc = i.next().getSourceCodeLocation();
    while (i.hasNext()) {
      SourceCodeLocation iLoc = i.next().getSourceCodeLocation();
      if (iLoc == null || iLoc.isUnknown()) continue;
      loc = merge(loc, iLoc);
    }
    return loc;
  }

  private final SourceCode sourceCode;

  /**
   * The sequence starts at the character immediately following the begin point.
   */
  private final SourceCodePoint begin;

  /**
   * The sequence ends at the character immediately before the end point. The
   * character immediately after the end point (the one indicated by the end's
   * {@link SourceCodePoint#characterIndex}) is not part of the sequence. The
   * empty sequence's begin point and end point are the same:
   * {@code begin.equals(end)}.
   */
  private final SourceCodePoint end;

  @VisibleForTesting
  public SourceCodeLocation(SourceCode sourceCode, int beginCharacterIndex,
      int beginLineNumber, int beginIndexInLine, int endCharacterIndex,
      int endLineNumber, int endIndexInLine) {
    Preconditions.checkNotNull(sourceCode);
    this.sourceCode = sourceCode;
    this.begin = new SourceCodePoint(beginCharacterIndex, beginLineNumber,
        beginIndexInLine);
    this.end = new SourceCodePoint(endCharacterIndex, endLineNumber,
        endIndexInLine);
    Preconditions.checkArgument(begin.compareTo(end) <= 0,
        "Beginning location must come before the end location.");
  }

  public SourceCode getSourceCode() {
    return sourceCode;
  }

  public boolean isUnknown() {
    Preconditions.checkState(begin.isUnknown() == end.isUnknown());
    return begin.isUnknown();
  }

  public int getBeginCharacterIndex() {
    return begin.getCharacterIndex();
  }

  /**
   * The index of the line that contains the first character of the node. Indexes start at 1; 0
   * means the location is not known.
   */
  public int getBeginLineNumber() {
    return begin.getLineNumber();
  }

  /**
   * The index of the column that contains the first character of the node. Indexes start at 1; 0
   * means the location is not known.
   */
  public int getBeginIndexInLine() {
    return begin.getIndexInLine();
  }

  public int getEndCharacterIndex() {
    return end.getCharacterIndex();
  }

  /**
   * The index of the line that contains the last character of the node. Indexes start at 1; 0
   * means the location is not known.
   */
  public int getEndLineNumber() {
    return end.getLineNumber();
  }

  /**
   * The index of the column that comes after the last character of the node. Indexes start at 1; 0
   * means the location is not known.
   */
  public int getEndIndexInLine() {
    return end.getIndexInLine();
  }

  public int getCharacterIndex() {
    return getBeginCharacterIndex();
  }

  public int getLineNumber() {
    return getBeginLineNumber();
  }

  public int getIndexInLine() {
    return getBeginIndexInLine();
  }

  public SourceCodePoint getBegin() {
    return begin;
  }

  public SourceCodePoint getEnd() {
    return end;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof SourceCodeLocation)) {
      return false;
    }
    SourceCodeLocation other = (SourceCodeLocation) o;
    return sourceCode == other.sourceCode && begin.equals(other.begin)
        && end.equals(other.end);
  }

  @Override
  public int hashCode() {
    return sourceCode.hashCode() ^ begin.hashCode() ^ (end.hashCode() << 16);
  }

  /**
   * Comparison and ordering of locations for source code in different
   * input files is supported because we don't always preserve
   * locations during AST mutations and yet we still want to be able
   * to sort error reports, doing the best job we can for the errors
   * that have known locations. For the semantics of this method, see
   * {@link Comparable#compareTo(Object)}.
   */
  @Override
  public int compareTo(SourceCodeLocation o) {
    Preconditions.checkNotNull(o);
    if (sourceCode != o.sourceCode) {
      if (sourceCode == null) {
        return -1;
      } else if (o.sourceCode == null) {
        return 1;
      } else {
        return sourceCode.hashCode() - o.sourceCode.hashCode();
      }
    }
    int startPointsComparison = begin.compareTo(o.begin);
    if (startPointsComparison != 0) {
      return startPointsComparison;
    }
    return end.compareTo(o.end);
  }

  @Override
  public String toString() {
    return String.format(
        "%s: line %d, col %d -> line %d, col %d",
        sourceCode.getFileName(),
        begin.getLineNumber(),
        begin.getIndexInLine(),
        end.getLineNumber(),
        end.getIndexInLine());
  }
}
