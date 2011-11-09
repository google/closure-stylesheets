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

package com.google.common.css.compiler.passes;

import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.MutatingVisitController;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * Unit tests for {@link EliminateUnitsFromZeroNumericValues}.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class EliminateUnitsFromZeroNumericValuesTest extends TestCase {

  public void testRunPass() {
    IMocksControl controller = EasyMock.createStrictControl();
    MutatingVisitController visitController = controller.createMock(
        MutatingVisitController.class);
    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);
    visitController.startVisit(pass);
    controller.replay();

    pass.runPass();
    controller.verify();
  }

  public void testEnterValueNode1() {
    MutatingVisitController visitController = EasyMock.createMock(
        MutatingVisitController.class);

    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);

    CssNumericNode node = new CssNumericNode("3", "px");
    pass.enterValueNode(node);
    assertEquals("3", node.getNumericPart());
    assertEquals("px", node.getUnit());
  }

  public void testEnterValueNode2() {
    MutatingVisitController visitController = EasyMock.createMock(
        MutatingVisitController.class);

    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);

    CssNumericNode node = new CssNumericNode("0", "px");
    pass.enterValueNode(node);
    assertEquals("0", node.getNumericPart());
    assertEquals("", node.getUnit());
  }

  public void testEnterValueNode3() {
    MutatingVisitController visitController = EasyMock.createMock(
        MutatingVisitController.class);

    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);

    CssNumericNode node = new CssNumericNode("0.000", "px");
    pass.enterValueNode(node);
    assertEquals("0", node.getNumericPart());
    assertEquals("", node.getUnit());
  }

  public void testEnterValueNode4() {
    MutatingVisitController visitController = EasyMock.createMock(
        MutatingVisitController.class);

    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);

    CssNumericNode node = new CssNumericNode("3.0", "px");
    pass.enterValueNode(node);
    assertEquals("3", node.getNumericPart());
    assertEquals("px", node.getUnit());
  }

  public void testEnterValueNode5() {
    MutatingVisitController visitController = EasyMock.createMock(
        MutatingVisitController.class);

    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);

    CssNumericNode node = new CssNumericNode("003.0", "px");
    pass.enterValueNode(node);
    assertEquals("3", node.getNumericPart());
    assertEquals("px", node.getUnit());
  }

  public void testEnterValueNode6() {
    MutatingVisitController visitController = EasyMock.createMock(
        MutatingVisitController.class);

    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);

    CssNumericNode node = new CssNumericNode("0.3", "px");
    pass.enterValueNode(node);
    assertEquals(".3", node.getNumericPart());
    assertEquals("px", node.getUnit());
  }

  public void testEnterValueNode7() {
    MutatingVisitController visitController = EasyMock.createMock(
        MutatingVisitController.class);

    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);

    CssNumericNode node = new CssNumericNode("0.3000", "px");
    pass.enterValueNode(node);
    assertEquals(".3", node.getNumericPart());
    assertEquals("px", node.getUnit());
  }

  public void testEnterValueNode8() {
    MutatingVisitController visitController = EasyMock.createMock(
        MutatingVisitController.class);

    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);

    CssNumericNode node = new CssNumericNode("002.3000", "px");
    pass.enterValueNode(node);
    assertEquals("2.3", node.getNumericPart());
    assertEquals("px", node.getUnit());
  }

  public void testEnterValueNode9() {
    MutatingVisitController visitController = EasyMock.createMock(
        MutatingVisitController.class);

    EliminateUnitsFromZeroNumericValues pass
        = new EliminateUnitsFromZeroNumericValues(visitController);

    CssNumericNode node = new CssNumericNode("woo34", "px");
    pass.enterValueNode(node);
    assertEquals("woo34", node.getNumericPart());
    assertEquals("px", node.getUnit());
  }
}
