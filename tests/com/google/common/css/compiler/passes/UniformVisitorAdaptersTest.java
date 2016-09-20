/*
 * Copyright 2012 Google Inc.
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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.passes.UniformVisitor.Adapters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;

/** Tests {@link com.google.common.css.compiler.passes.UniformVisitor.Adapters}. */
@RunWith(JUnit4.class)
public class UniformVisitorAdaptersTest {

  @Test
  public void asVisitor() throws Exception {
    UniformVisitor uniformVisitor = mock(UniformVisitor.class);
    CssTreeVisitor visitor = Adapters.asVisitor(uniformVisitor);

    visitor.enterSelector(null /* selector */);
    visitor.leaveSelector(null /* selector */);

    InOrder verifier = inOrder(uniformVisitor);
    verifier.verify(uniformVisitor).enter(null);
    verifier.verify(uniformVisitor).leave(null);
    verifier.verifyNoMoreInteractions();
  }

  @Test
  public void asCombinedVisitor() throws Exception {
    CombinedVisitor combinedVisitor = mock(CombinedVisitor.class);
    CssTreeVisitor visitor = Adapters.asCombinedVisitor(combinedVisitor);

    visitor.enterSelector(null /* selector */);
    visitor.leaveSelector(null /* selector */);

    InOrder verifier = inOrder(combinedVisitor);
    verifier.verify(combinedVisitor).enter(null);
    verifier.verify(combinedVisitor).enterSelector(null);
    verifier.verify(combinedVisitor).leaveSelector(null);
    verifier.verify(combinedVisitor).leave(null);
  }

  @Test
  public void testCombinedVisitorObjectMethods() throws Exception {
    CombinedVisitor combinedVisitor = mock(CombinedVisitor.class);
    CssTreeVisitor visitor = Adapters.asCombinedVisitor(combinedVisitor);

    assertThat(visitor.toString()).isEqualTo(combinedVisitor.toString());
  }

  @Test
  public void testUniformVisitorObjectMethods() throws Exception {
    UniformVisitor uniformVisitor = mock(UniformVisitor.class);
    CssTreeVisitor visitor = Adapters.asVisitor(uniformVisitor);

    assertThat(visitor.toString()).isEqualTo(uniformVisitor.toString());
  }

  private interface CombinedVisitor extends CssTreeVisitor, UniformVisitor {};
}
