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

import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssCommentNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * An abstract code-printer for {@link CssTree} instances that provides read/write access to the
 * output buffer and performs common tasks during code generation, like creating sourcemaps.
 *
 * @author steveyang@google.com (Chenyun Yang)
 */
public abstract class CodePrinter implements CssCompilerPass {

  /** The visit controller for the (sub)tree being printed. */
  private final VisitController visitController;

  /** Holds the output of the printing visitor. */
  private final CodeBuffer buffer;

  /** The source map generator used by CodePrinter and subclasses. */
  private final GssSourceMapGenerator generator;

  /** Whether or not to preserve special comments in the output. */
  private boolean preserveMarkedComments;

  /**
   * Initializes this instance from the given {@link VisitController}, could optionally accept
   * {@link CodeBuffer} and {@link GssSourceMapGenerator} to use.
   */
  protected CodePrinter(
      VisitController visitController,
      @Nullable CodeBuffer buffer,
      @Nullable GssSourceMapGenerator generator) {
    this.visitController = visitController;
    this.buffer = buffer != null ? buffer : new CodeBuffer();
    this.generator = generator;
  }

  /**
   * Constructs the visitor required by the subclass. This visitor's {@code enter*} methods will be
   * called after the source map generator's {@code startSourceMapping} method and before its {@code
   * endSourceMapping} method.
   */
  protected abstract CssTreeVisitor createVisitor(
      VisitController visitController, CodeBuffer codeBuffer);

  protected final void visit() {
    List<CssTreeVisitor> visitors = new LinkedList<>();
    /*
     * NOTE(flan): This order is important. We need the SourceMapVisitor to be called first because
     * it keeps track of the extent of the node. Next, we need to print any comments that need
     * preserving. Finally, we actually visit the node itself.
     */
    if (generator != null) {
      visitors.add(UniformVisitor.Adapters.asVisitor(new SourceMapVisitor()));
    }
    if (preserveMarkedComments) {
      visitors.add(UniformVisitor.Adapters.asVisitor(new CommentPrintingVisitor()));
    }
    visitors.add(createVisitor(visitController, buffer));
    visitController.startVisit(DelegatingVisitor.from(visitors));
  }

  // Proxy method for external usage.
  protected final void resetBuffer() {
    buffer.reset();
  }

  protected final String getOutputBuffer() {
    return buffer.getOutput();
  }

  /**
   * Whether special comments in the CSS nodes are preserved in the printed
   * output. Currently supported special comments are annotated with one of the following:
   * <ul>
   *   <li>@preserve</li>
   *   <li>@license</li>
   *   <li>/*! (start of comment)</li>
   * </ul>
   * Comments marked with @license will cause a special "END OF LICENSED CSS FILE"
   * comment to be inserted when the parser moves on to a new source file. Note that
   * some optimizations may move pieces around, so there's no guarantees the licensed
   * file will remain entirely intact.
   *
   * <p>Note: Comments layout is not guaranteed, since detailed position
   * information in the input files is not preserved by the parser.
   */
  public void setPreserveMarkedComments(boolean preserveMarkedComments) {
    this.preserveMarkedComments = preserveMarkedComments;
  }

  private class SourceMapVisitor implements UniformVisitor {

    @Override
    public void enter(CssNode node) {
      generator.startSourceMapping(node, buffer.getNextLineIndex(), buffer.getNextCharIndex());
    }

    @Override
    public void leave(CssNode node) {
      generator.endSourceMapping(node, buffer.getLastLineIndex(), buffer.getLastCharIndex());
    }
  }


  private static final Pattern LICENSE_ANNOTATION_PATTERN =
      Pattern.compile(".*@license\\b.*", Pattern.DOTALL);
  private static final Pattern PRESERVE_ANNOTATION_PATTERN =
      Pattern.compile(".*@preserve\\b.*", Pattern.DOTALL);
  private static final Pattern IMPORTANT_ANNOTATION_PATTERN =
      Pattern.compile("/\\*!.*", Pattern.DOTALL);
  private static final String END_OF_LICENSED_CSS_FILE = "/* END OF LICENSED CSS FILE */\n";

  private static class LastLicenseData {
    private String source;
    private String content;
  }

  private class CommentPrintingVisitor implements UniformVisitor {

    private LastLicenseData lastLicenseData = new LastLicenseData();

    @Override
    public void enter(CssNode node) {
      String newSourceLocation = fileNameForSourceCodeLocation(node.getSourceCodeLocation());
      // If we previously printed out a comment with a license and we're transitioning
      // source files, print a END OF LICENSED CSS FILE comment.
      if (lastLicenseData.source != null
          && newSourceLocation != null
          && !lastLicenseData.source.equals(newSourceLocation)) {
        buffer.append("/* END OF LICENSED CSS FILE */\n");
        lastLicenseData.source = null;
      }
      if (!node.getComments().isEmpty()) {
        boolean hasLicense = false;
        StringBuilder commentBuffer = new StringBuilder();
        for (CssCommentNode c : node.getComments()) {
          boolean currentHasLicense = LICENSE_ANNOTATION_PATTERN.matcher(c.getValue()).matches();
          hasLicense |= currentHasLicense;
          if (currentHasLicense
              || PRESERVE_ANNOTATION_PATTERN.matcher(c.getValue()).matches()
              || IMPORTANT_ANNOTATION_PATTERN.matcher(c.getValue()).matches()) {
            commentBuffer.append(c.getValue());
          }
        }
        String comment = commentBuffer.toString();

        if (!comment.isEmpty()) {
          if (hasLicense && comment.equals(lastLicenseData.content)) {
            // If the last license matches the current, lump the two files together to safe space.
            buffer.deleteEndingIfEndingIs(END_OF_LICENSED_CSS_FILE);
          } else {
            buffer.startNewLine();
            buffer.append(comment);
            buffer.startNewLine();
            lastLicenseData.content = null;
          }

          if (hasLicense) {
            lastLicenseData.source = newSourceLocation;
            lastLicenseData.content = comment;
          }
        }
      }
    }

    @Override
    public void leave(CssNode node) {

    }

    private String fileNameForSourceCodeLocation(SourceCodeLocation location) {
      if (location == null || location.getSourceCode() == null) {
        return null;
      }
      return location.getSourceCode().getFileName();
    }
  }
}
