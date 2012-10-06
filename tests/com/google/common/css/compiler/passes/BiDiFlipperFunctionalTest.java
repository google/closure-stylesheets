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

import com.google.common.css.compiler.passes.testing.AstPrinter;
import com.google.common.css.compiler.passes.testing.PassesTestBase;

/**
 * Functional tests for {@link BiDiFlipper}.
 *
 */
public class BiDiFlipperFunctionalTest extends PassesTestBase {
  /**
   * Tests that
   *   border-left : green
   * is flipped to
   *   border-right : green
   */
  public void test1() {
    testTreeConstruction(
        "foo { border-left: green; }",
        "[[foo]{[border-right:[[green]];]}]");
  }

 /**
   * Tests that
   *   padding : 0px 3px 1px 5px
   * is flipped to
   *   padding : 0px 5px 1px 3px
   */
  public void test2() {
    testTreeConstruction(
        "foo { padding: 0px 3px 1px 5px; }",
        "[[foo]{[padding:[[0px][5px][1px][3px]];]}]");
  }

 /**
   * Tests that
   *   padding : 0px 3px 1px
   * remains
   *   padding : 0px 3px 1px
   */
  public void test3() {
    testTreeConstruction(
        "foo { padding: 0px 3px 1px; }",
        "[[foo]{[padding:[[0px][3px][1px]];]}]");
  }

 /**
   * Tests that
   *   background : 10% 3px red
   * is flipped to
   *   background : 90% 3px red
   */
  public void test4() {
    testTreeConstruction(
        "foo { background: 10% 3px red; }",
        "[[foo]{[background:[[90%][3px][red]];]}]");
  }

 /**
   * Tests that
   *   background-position-x : 10%; padding-right: 2px
   * is flipped to
   *   background-position-x : 90%; padding-left: 2px
   */
  public void test5() {
    testTreeConstruction(
        "foo { background-position-x: 10%; padding-right: 2px; }",
        "[[foo]{[background-position-x:[[90%]];padding-left:[[2px]];]}]");
  }

 /**
   * Tests that
   *   background : url(/foo/rtl/background.png)
   * is flipped to
   *   background : url(/foo/ltr/background.png)
   */
  public void test6() {
    testTreeConstruction(
        "foo { background: url(/foo/rtl/background.png); }",
        "[[foo]{[background:[url(/foo/ltr/background.png)];]}]");
  }

 /**
   * Tests that
   *   background : url(/foo/ltr/background.png)
   * is flipped to
   *   background : url('/foo/rtl/background.png')
   */
  public void test7() {
    testTreeConstruction(
        "foo { background: url(/foo/ltr/background.png); }",
        "[[foo]{[background:[url(/foo/rtl/background.png)];]}]");
  }

  /**
   * Tests that
   *   background : url(/foo/background-left.png)
   * is flipped to
   *   background : url('/foo/background-right.png')
   */
  public void test8() {
    testTreeConstruction(
        "foo { background: url(/foo/background-left.png); }",
        "[[foo]{[background:[url(/foo/background-right.png)];]}]");
  }

  /**
   * Tests that
   *   background : url(/foo/background-right.png)
   * is flipped to
   *   background : url(/foo/background-left.png)
   */
  public void test9() {
    testTreeConstruction(
        "foo { background: url(/foo/background-right.png); }",
        "[[foo]{[background:[url(/foo/background-left.png)];]}]");
  }

  /**
   * Tests that
   *   background : url(/foo/background.png)
   * remains
   *   background : url(/foo/background.png)
   */
  public void test10() {
    testTreeConstruction(
        "foo { background: url('/foo/background.png'); }",
        "[[foo]{[background:[url('/foo/background.png')];]}]");
  }

  /**
   * Tests that
   *   background : 10%
   * is flipped to
   *   background : 90%
   * inside a conditional block.
   */
  public void test11() {
    testTreeConstruction(
        "@if COND1 {"
        + "  foo {"
        + "    background: 10%;"
        + "  }"
        + "}",
        "[@if [COND1]{[foo]{[background:[[90%]];]}}]");
  }

  /**
   * Tests that
   *   padding : 0px 3px 1px 5px
   * is flipped to
   *   padding : 0px 5px 1px 3px
   * and
   *   background : 40%
   * is flipped to
   *   background : 60%
   * and
   *   background : url(/foo/background-right.png)
   * is flipped to
   *   background : url(/foo/background-left.png)
   * inside a slightly complex conditional block.
   */
  public void test12() {
    testTreeConstruction(
       "@if COND1 {"
        + "  foo {"
        + "    background: 40%;"
        + "  }"
        + "} @elseif COND2 {"
        + "  foo {"
        + "    padding: 0px 3px 1px 5px;"
        + "  }"
        + "} @else {"
        + "  bar {"
        + "    background: url('/foo/background-right.png');"
        + "  }"
        + "}",
        "[@if [COND1]{[foo]{[background:[[60%]];]}}"
        + "@elseif [COND2]{[foo]{[padding:[[0px][5px][1px][3px]];]}}"
        + "@else{[bar]{[background:[url('/foo/background-left.png')];]}}]");
  }

  /**
   * Tests that
   *   background-position-x : 10%
   * is flipped to
   *   background-position-x : 90%
   * and
   *   padding-left : 2px
   * is flipped to
   *   padding-right : 2px
   * inside a media rule.
   */
  public void test13() {
    testTreeConstruction(
        "@media print {"
        + "  @if COND1 {"
        + "    @if COND2 {"
        + "      foo { background-position-x: 10% }"
        + "    } "
        + "  } @else {"
        + "    foo { padding-left: 2px }"
        + "  }"
        + "}",
        "[@media [print]{@if [COND1]{@if [COND2]"
        + "{[foo]{[background-position-x:[[90%]];]}}}"
        + "@else{[foo]{[padding-right:[[2px]];]}}}]");
  }

  /**
   * Test to make sure that percentage value in 'font' node is not flipped.
   */
  public void test14() {
    testTreeConstruction(
        "foo { font: 100%/1.3em Verdana, Sans-serif; }",
        "[[foo]{[font:[[[100%]/[1.3em]][[Verdana],[Sans-serif]]];]}]");
  }

  /**
   * Test to make sure that the star hack property does not disappear in the
   * process of flipping.
   */
  public void test15() {
    testTreeConstruction(
        "foo { *height: 13px }",
        "[[foo]{[*height:[[13px]];]}]");
  }

  /**
   * Tests that
   *     float : left
   * is flipped to
   *     float : right
   */
  public void test16() {
    testTreeConstruction(
        "foo { float: left }",
        "[[foo]{[float:[[right]];]}]");
  }

  /**
   * Tests that
   *     left : 5px
   * is flipped to
   *     right : 5px
   */
  public void test17() {
    testTreeConstruction(
        "foo { left: 5px }",
        "[[foo]{[right:[[5px]];]}]");
  }

  /**
   * Tests that
   *     cursor : e-resize
   * is flipped to
   *     cursor : w-resize
   */
  public void test18() {
    testTreeConstruction(
        "foo { cursor: e-resize }",
        "[[foo]{[cursor:[[w-resize]];]}]");
  }

  /**
   * Tests that
   *     border-top-left-radius : 3px
   * is flipped to
   *     border-top-right-radius : 3px
   */
  public void test19() {
    testTreeConstruction(
        "foo { border-top-left-radius: 3px }",
        "[[foo]{[border-top-right-radius:[[3px]];]}]");
  }

  /**
   * Tests that
   *     -moz-border-radius-topleft : 3px
   * is flipped to
   *     -moz-border-radius-topright : 3px
   */
  public void test20() {
    testTreeConstruction(
        "foo { -moz-border-radius-topleft: 3px }",
        "[[foo]{[-moz-border-radius-topright:[[3px]];]}]");
  }

  /**
   * Tests that @defs get expanded and flipped correctly.
   */
  public void test21() {
    parseAndBuildTree(
        "@def RTL rtl;"
        + "@def WHOLE_PADDING 1px 2px 3px 4px;"
        + "@def TOP_RIGHT_PADDING 1px 2px;"
        + "@def BOTTOM_LEFT_PADDING 3px 4px;"
        + ".b {"
        + "  direction: RTL;"
        + "  padding: WHOLE_PADDING;"
        + "  padding: TOP_RIGHT_PADDING BOTTOM_LEFT_PADDING;"
        + "}");

    new CreateDefinitionNodes(tree.getMutatingVisitController(),
        errorManager).runPass();
    new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
    CollectConstantDefinitions collectConstantDefinitionsPass =
        new CollectConstantDefinitions(tree);
    collectConstantDefinitionsPass.runPass();
    new ReplaceConstantReferences(tree,
        collectConstantDefinitionsPass.getConstantDefinitions(),
        true /* removeDefs */, errorManager,
        true /* allowUndefinedConstants */).runPass();
    new BiDiFlipper(tree.getMutatingVisitController(), true, true).runPass();
    String expectedOutput = "[[.b]{["
        + "direction:[[ltr]];"
        + "padding:[[1px][4px][3px][2px]];"
        + "padding:[[1px][4px][3px][2px]];"
        + "]}]";
    assertEquals(expectedOutput, AstPrinter.print(tree));
  }

  /**
   * Tests that
   *   border-color: #fff #aaa #ccc transparent
   * is flipped to
   *   border-color: #fff transparent #ccc #aaa
   */
  public void test22() {
    testTreeConstruction(
        "foo { border-color: #fff #aaa #ccc transparent; }",
        "[[foo]{[border-color:[[#fff][transparent][#ccc][#aaa]];]}]");
  }

 /**
   * Tests that
   *   -ms-background-position-x : 10%
   * is flipped to
   *   -ms-background-position-x : 90%
   */
  public void test23() {
    testTreeConstruction(
         "foo { -ms-background-position-x: 10%; }",
        "[[foo]{[-ms-background-position-x:[[90%]];]}]");
 }

 /**
   * Tests that
   *   background-position-y : 10%
   * is not flipped.
   */
  public void test24() {
    testTreeConstruction(
         "foo { background-position-y: 10%; }",
        "[[foo]{[background-position-y:[[10%]];]}]");
 }

 /**
   * Tests that
   *   background-size : 10% 20%
   * is not flipped.
   */
  public void test25() {
    testTreeConstruction(
         "foo { background-size: 10% 20%; }",
        "[[foo]{[background-size:[[10%][20%]];]}]");
 }

 /**
   * Tests that
   *   background-position: 10% 20%
   * is flipped to
   *   background-position: 90% 20%
   */
  public void test26() {
    testTreeConstruction(
        "foo { background-position: 10% 20%; }",
        "[[foo]{[background-position:[[90%][20%]];]}]");
  }

 /**
   * Tests that
   *   background-position: 0 0
   * is not flipped.
   */
  public void test27() {
    testTreeConstruction(
        "foo { background-position: 0 0; }",
        "[[foo]{[background-position:[[0][0]];]}]");
  }

 /**
   * Tests that
   *   background: rgb(1%, 2%, 3%) 4% 5%
   * is flipped to
   *   background: rgb(1%, 2%, 3%) 96% 5%
   */
  public void test28() {
    testTreeConstruction(
        "foo { background: rgb(1%, 2%, 3%) 4% 5%; }",
        "[[foo]{[background:[rgb(1%,2%,3%) [96%][5%]];]}]");
  }

 /**
   * Tests that
   *   background: red 4% 5%
   * is flipped to
   *   background: red 96% 5%
   */
  public void test29() {
    testTreeConstruction(
        "foo { background: red 4% 5%; }",
        "[[foo]{[background:[[red][96%][5%]];]}]");
  }

 /**
   * Tests that
   *   background: center 5%
   * is not flipped.
   */
  public void test30() {
    testTreeConstruction(
        "foo { background: center 5%; }",
        "[[foo]{[background:[[center][5%]];]}]");
  }

 /**
   * Tests that
   *   box-shadow: 1px 2px 3px 4px
   * is not flipped.
   */
  public void test31() {
    testTreeConstruction(
        "foo { box-shadow: 1px 2px 3px 4px; }",
        "[[foo]{[box-shadow:[[1px][2px][3px][4px]];]}]");
  }

 /**
   * Tests that
   *   text-shadow: 1px 2px 3px 4px
   * is not flipped.
   */
  public void test32() {
    testTreeConstruction(
        "foo { text-shadow: 1px 2px 3px 4px; }",
        "[[foo]{[text-shadow:[[1px][2px][3px][4px]];]}]");
  }

 /**
   * Tests that
   *   border-radius: 1px
   * is not flipped.
   */
  public void test33() {
    testTreeConstruction(
        "foo { border-radius: 1px; }",
        "[[foo]{[border-radius:[[1px]];]}]");
  }

 /**
   * Tests that
   *   border-radius: 1px / 5px
   * is not flipped.
   */
  public void test34() {
    testTreeConstruction(
        "foo { border-radius: 1px / 5px; }",
        "[[foo]{[border-radius:[[[1px]/[5px]]];]}]");
  }

 /**
   * Tests that
   *   border-radius: 1px 2px
   * is flipped to
   *   border-radius: 2px 1px
   */
  public void test35() {
    testTreeConstruction(
        "foo { border-radius: 1px 2px; }",
        "[[foo]{[border-radius:[[2px][1px]];]}]");
  }

 /**
   * Tests that
   *   border-radius: 1px 2px 3px
   * is flipped to
   *   border-radius: 2px 1px 2px 3px
   */
  public void test36() {
    testTreeConstruction(
        "foo { border-radius: 1px 2px 3px; }",
        "[[foo]{[border-radius:[[2px][1px][2px][3px]];]}]");
  }

 /**
   * Tests that
   *   border-radius: 1px 2px 3px 4px
   * is flipped to
   *   border-radius: 2px 1px 4px 3px
   */
  public void test37() {
    testTreeConstruction(
        "foo { border-radius: 1px 2px 3px 4px; }",
        "[[foo]{[border-radius:[[2px][1px][4px][3px]];]}]");
  }

 /**
   * Tests that
   *   border-radius: 1px 2px 3px 4px / 5px 6px 7px 8px
   * is flipped to
   *   border-radius: 2px 1px 4px 3px / 6px 5px 8px 7px
   */
  public void test38() {
    testTreeConstruction(
        "foo { border-radius: 1px 2px 3px 4px / 5px 6px 7px 8px; }",
        "[[foo]{[border-radius:" +
        "[[2px][1px][4px][[3px]/[6px]][5px][8px][7px]];]}]");
  }

 /**
   * Tests that
   *   border-radius: 1px / 5px 6px 7px 8px
   * is flipped to
   *   border-radius: 1px / 6px 5px 8px 7px
   */
  public void test39() {
    testTreeConstruction(
        "foo { border-radius: 1px / 5px 6px 7px 8px; }",
        "[[foo]{[border-radius:[[[1px]/[6px]][5px][8px][7px]];]}]");
  }

 /**
   * Tests that
   *   border-radius: 1px 2px 3px / 5px 6px
   * is flipped to
   *   border-radius: 2px 1px 2px 3px / 6px 5px
   */
  public void test40() {
    testTreeConstruction(
        "foo { border-radius: 1px 2px 3px / 5px 6px; }",
        "[[foo]{[border-radius:[[2px][1px][2px][[3px]/[6px]][5px]];]}]");
  }

 /**
   * Tests that
   *   -webkit-border-radius: 1px 2px 3px / 5px 6px
   * is flipped to
   *   -webkit-border-radius: 2px 1px 2px 3px / 6px 5px
   */
  public void test41() {
    testTreeConstruction(
        "foo { -webkit-border-radius: 1px 2px 3px / 5px 6px; }",
        "[[foo]{[-webkit-border-radius:[[2px][1px][2px][[3px]/[6px]][5px]];]}]");
  }

  /**
   * Tests that
   *   border-color: red yellow green blue
   * is flipped to
   *   border-color: red blue green yellow
   */
  public void test42() {
    testTreeConstruction(
        "foo { border-color: red yellow green blue; }",
        "[[foo]{[border-color:[[red][blue][green][yellow]];]}]");
  }

  /**
   * Tests that
   *   border-width: thin medium thick 2px
   * is flipped to
   *   border-width: thin 2px thick medium
   */
  public void test43() {
    testTreeConstruction(
        "foo { border-width: thin medium thick 2px; }",
        "[[foo]{[border-width:[[thin][2px][thick][medium]];]}]");
  }

  /**
   * Tests that
   *   border-image-slice: 5% 10% 15% 20%
   * is not flipped.
   */
  public void test44() {
    testTreeConstruction(
        "foo { border-image-slice: 5% 10% 15% 20%; }",
        "[[foo]{[border-image-slice:[[5%][10%][15%][20%]];]}]");
  }

  /**
   * Tests that
   *   border-image-slice: 5% 10% 15% 20% fill
   * is not flipped.
   */
  public void test45() {
    testTreeConstruction(
        "foo { border-image-slice: 5% 10% 15% 20% fill; }",
        "[[foo]{[border-image-slice:[[5%][10%][15%][20%][fill]];]}]");
  }

  /**
   * Tests that
   *   border-image-slice: 5% 10% 15% fill
   * is not flipped.
   */
  public void test46() {
    testTreeConstruction(
        "foo { border-image-slice: 5% 10% 15% fill; }",
        "[[foo]{[border-image-slice:[[5%][10%][15%][fill]];]}]");
  }

  /**
   * Tests that
   *   border-image-slice: 5% 10% fill
   * is not flipped.
   */
  public void test47() {
    testTreeConstruction(
        "foo { border-image-slice: 5% 10% fill; }",
        "[[foo]{[border-image-slice:[[5%][10%][fill]];]}]");
  }

  /**
   * Tests that
   *   border-image-slice: 5% fill
   * is not flipped.
   */
  public void test48() {
    testTreeConstruction(
        "foo { border-image-slice: 5% fill; }",
        "[[foo]{[border-image-slice:[[5%][fill]];]}]");
  }

  /**
   * Tests that
   *   border-image-outset: 1 2 3 4
   * is not flipped.
   */
  public void test49() {
    testTreeConstruction(
        "foo { border-image-outset: 1 2 3 4; }",
        "[[foo]{[border-image-outset:[[1][2][3][4]];]}]");
  }

  /**
   * Tests that
   *   border-image-width: 1px auto 0 3%
   * is not flipped.
   */
  public void test50() {
    testTreeConstruction(
        "foo { border-image-width: 1px auto 0 3%; }",
        "[[foo]{[border-image-width:[[1px][auto][0][3%]];]}]");
  }

  /**
   * Tests that
   *   background: url()
   * is handled fine.
   */
  public void test51() {
    testTreeConstruction(
        "foo { background: url(); }",
        "[[foo]{[background:[url()];]}]");
  }

  @Override
  protected void runPass() {
    BiDiFlipper pass
        = new BiDiFlipper(tree.getMutatingVisitController(), true, true);
    pass.runPass();
  }
}
