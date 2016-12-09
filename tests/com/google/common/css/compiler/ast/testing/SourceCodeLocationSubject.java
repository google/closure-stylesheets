/*
 * Copyright 2016 Google Inc.
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

package com.google.common.css.compiler.ast.testing;

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.css.SourceCodeLocation;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import javax.annotation.CheckReturnValue;

/**
 * Truth subject for {@link SourceCodeLocation}.
 */
public class SourceCodeLocationSubject
    extends Subject<SourceCodeLocationSubject, SourceCodeLocation> {

  static final SubjectFactory<SourceCodeLocationSubject, SourceCodeLocation> LOCATION =
      new SubjectFactory<SourceCodeLocationSubject, SourceCodeLocation>() {
        @Override
        public SourceCodeLocationSubject getSubject(FailureStrategy fs, SourceCodeLocation that) {
          return new SourceCodeLocationSubject(fs, that);
        }
      };

  @CheckReturnValue
  public static SourceCodeLocationSubject assertThat(SourceCodeLocation target) {
    return assertAbout(LOCATION).that(target);
  }

  public SourceCodeLocationSubject(FailureStrategy failureStrategy, SourceCodeLocation subject) {
    super(failureStrategy, subject);
  }

  public void hasSpan(int beginLine, int beginIndex, int endLine, int endIndex) {
    check().that(getSubject()).isNotNull();
    if (!(beginLine == getSubject().getBeginLineNumber()
        && beginIndex == getSubject().getBeginIndexInLine()
        && endLine == getSubject().getEndLineNumber()
        && endIndex == getSubject().getEndIndexInLine())) {
      failWithRawMessage(
          "Location did not match <%s,%s -> %s,%s>, was <%s,%s -> %s,%s>",
          String.valueOf(beginLine),
          String.valueOf(beginIndex),
          String.valueOf(endLine),
          String.valueOf(endIndex),
          String.valueOf(getSubject().getBeginLineNumber()),
          String.valueOf(getSubject().getBeginIndexInLine()),
          String.valueOf(getSubject().getEndLineNumber()),
          String.valueOf(getSubject().getEndIndexInLine()));
    }
  }

  public void matches(String text) {
    check().that(getSubject()).isNotNull();
    String source =
        getSubject()
            .getSourceCode()
            .getFileContents()
            .substring(
                getSubject().getBeginCharacterIndex(), getSubject().getEndCharacterIndex());
    check().that(source).isEqualTo(text);
  }

  public void isUnknown() {
    check().that(getSubject()).isNotNull();
    check().that(getSubject().isUnknown()).named("isUnknown").isTrue();
  }
}
