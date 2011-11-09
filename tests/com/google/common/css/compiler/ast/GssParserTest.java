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

package com.google.common.css.compiler.ast;

import com.google.common.collect.ImmutableList;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.passes.testing.AstPrinter;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link GssParser}.
 *
 */

public class GssParserTest extends TestCase {

  private CssTree parse(String gss) throws GssParserException {
    GssParser parser = new GssParser(new SourceCode("test", gss));
    return parser.parse();
  }

  private CssTree testValid(String gss) throws GssParserException {
    CssTree tree = parse(gss);
    assertNotNull(tree);
    return tree;
  }

  private void testTree(String gss, String output) throws GssParserException {
    CssTree tree = parse(gss);
    assertNotNull(tree);
    CssRootNode root = tree.getRoot();
    assertNotNull(root);
    assertEquals(output, AstPrinter.print(tree));
  }

  public void testManySources() throws Exception {
    CssTree tree = new GssParser(ImmutableList.of(
        new SourceCode("test1", "a {}"),
        new SourceCode("test2", "@component c { x {y: z} }"),
        new SourceCode("test3", "b {}"))).parse();
    CssRootNode root = tree.getRoot();
    assertNotNull(root);
    assertEquals("[[a]{[]}@component c{[x]{[y:[z];]}}[b]{[]}]",
        AstPrinter.print(tree));
  }

  public void testAst1() throws Exception {
    testTree("a {}", "[[a]{[]}]");
  }

  public void testAst2() throws Exception {
    testTree("a.b c#d > e.f + g {}", "[[a.b c#d>e.f+g]{[]}]");
  }

  public void testAst3() throws Exception {
    testTree("a {x: y}", "[[a]{[x:[y];]}]");
  }

  public void testAst4() throws Exception {
    testTree("a {w: x; y: z}", "[[a]{[w:[x];y:[z];]}]");
  }

  public void testAst5() throws Exception {
    testTree("a {b: 1em}", "[[a]{[b:[1em];]}]");
  }

  public void testAst6() throws Exception {
    testTree("a {b: 1.5em}", "[[a]{[b:[1.5em];]}]");
  }

  public void testAst7() throws Exception {
    testTree("a {b: 'x'}", "[[a]{[b:['x'];]}]");
  }

  public void testAst8() throws Exception {
    testTree("a {b: url(#x)}", "[[a]{[b:[url(#x)];]}]");
  }

  public void testAst9() throws Exception {
    testTree("a {b: url('#x')}", "[[a]{[b:[url('#x')];]}]");
  }

  public void testAst10() throws Exception {
    testTree("a {b: x y z}", "[[a]{[b:[x y z];]}]");
  }

 public void testAst11() throws Exception {
    testTree("a {b: c,d,e/f g,h i j,k}",
             "[[a]{[b:[c,d,e/f g,h i j,k];]}]");
  }

  public void testAst12() throws Exception {
    testTree("a {b: rgb(0,0,0)}", "[[a]{[b:[rgb(0,0,0)];]}]");
  }

  public void testAst13() throws Exception {
    testTree("a {b: custom(0,0)}", "[[a]{[b:[custom(0,0)];]}]");
  }

  public void testAst14() throws Exception {
    testTree("@def a b;", "[@def a b;]");
  }

  public void testAst15() throws Exception {
    testTree("@component a { x {y: z} }",
             "[@component a{[x]{[y:[z];]}}]");
  }

  public void testAst16() throws Exception {
    testTree("a:foo {\n bla : d ; }",
             "[[a:foo]{[bla:[d];]}]");
  }

  public void testAst17() throws Exception {
    testTree("foo {f: rgb(o=0);}",
             "[[foo]{[f:[rgb(o=0)];]}]");
  }

  public void testAst18() throws Exception {
    testTree("a:lang(c) { d: e }",
             "[[a:lang(c)]{[d:[e];]}]");
  }

  public void testAst19() throws Exception {
    testTree("a~b { d: e }",
             "[[a~b]{[d:[e];]}]");
  }

  public void testAst20() throws Exception {
    testTree("a:b(-2n+3) { d: e }",
             "[[a:b(-2n+3)]{[d:[e];]}]");
  }

  public void testAst21() throws Exception {
    testTree("a:not(#id) { d: e }",
             "[[a:not(#id)]{[d:[e];]}]");
  }

  public void testAst22() throws Exception {
    testTree(".a { d:e,f }",
             "[[.a]{[d:[e,f];]}]");
  }

  public void testAst23() throws Exception {
    testTree(".a { d:e f,g h }",
             "[[.a]{[d:[e f,g h];]}]");
  }

  public void testParsingRules1() throws Exception {
    testValid("css_rule33 {\n" +
        "border: black ; /* comment */\n" +
        "height : 1em\n" +
        " }"
      );
  }

  // We don't test for comments between '!' and 'important'. See the comment on
  // the IMPORTANT_SYM in the grammar for the reason.
  public void testParsingRules2() throws Exception {
    testValid("ul.navbar {\n" +
        "    position: absolute;\n" +
        "    top: top;\n" +
        "    left: down;\n" +
        "    width: nice  }\n" +
        "\n" +
        ".foo {\n" +
        "   position: absolute ! important ;\n" +
        "}\n" +
        ".bar {\n" +
        "   position: absolute !  important;\n\n\n" +
       "}"
      );
  }

  public void testParsingRules3() throws Exception {
   testValid("css_rule33 test2 {\n" +
        "border: black ; /* comment */\n" +
        "height : 1em\n" +
        " }"
      );
  }

  public void testParsingRules4() throws Exception {
    testValid("p:before {content: counter(par-num, upper-roman) \". \"}");
  }

  public void testParsingSelector1() throws Exception {
    testValid("a b { x: y}");
  }

  public void testParsingSelector2() throws Exception {
    testValid("a > b { x: y}");
  }

  public void testParsingSelector3() throws Exception {
    testValid("a + b { x: y}");
  }

  public void testParsingSelector4() throws Exception {
    testValid("a + b > c d e.f + g { x: y}");
  }

  public void testParsingSelector5() throws Exception {
    testValid("a + b > c d e.f#d + g {}");
  }

  public void testParsingSelector6() throws Exception {
    testValid("a ~ b { x: y}");
  }

  public void testParsingExpr1() throws Exception {
    testValid("aab {x:s r t}");
  }

  public void testParsingExpr2() throws Exception {
    testValid("aab {x:s 1em t}");
  }

  public void testParsingExpr3() throws Exception {
    testValid("aab {x:-1px +1px -1px 1.7px}");
  }

  public void testParsingURL() throws Exception {
    testValid("a { x: url('http://test.com') }");
  }

  public void testParsingHexcolor() throws Exception {
    testValid("a { x: #fff }");
  }

  public void testParsingFunction1Arg() throws Exception {
    testValid("a { x: f(1) }");
  }

  public void testParsingFunctionManyArgs() throws Exception {
    testValid("a { x: f(1, 2, 3) }");
  }

  public void testParsingAt1() throws Exception {
    testValid("@import url('http://test.com/test.css');");
  }

  public void testParsingAt2() throws Exception {
    testValid("@import url(http://test.com/test.css);");
  }

  public void testParsingAt3() throws Exception {
    testValid("@component a extends b {\n" +
        "@def z 1;\n" +
        "x {y: z}\n" +
        "}");
  }

  public void testParsingDef1() throws Exception {
    testValid("@def RC_TOP_LEFT        tl;\n" +
        "@def RC_TOP_RIGHT       tr;\n" +
        "@def BASE_WARNING_LINK_COLOR   #c3d9ff; /* light blue */"
    );
  }

  public void testParsingDef2() throws Exception {
    testValid("@def NOTIFY_TOP_LEFT_CORNER_BG\n" +
        "roundedCornerImage(NOTIFY_BG_COLOR,\n" +
        "NOTIFY_TOP_BORDER_HEIGHT, RC_TOP_LEFT, IE6_FLAG);"
    );
  }

  public void testParsingDef3() throws Exception {
    testValid("@def A_B /* @default */ inherit;");
  }

  public void testParsingAttribute1() throws Exception {
    testValid("a[href=\"http://www.w3.org/\"]{\n" +
        "bla:d\n" +
        "}");
  }

  public void testParsingAttribute2() throws Exception {
    testValid("*[lang|=\"en\"] { color : red }");
  }

  public void testParsingPseudo1() throws Exception {
    testValid("a:foo {\n bla : d ; }");
  }

  public void testParsingPseudo2() throws Exception {
    testValid("a:lang(en) {\n bla : d ; }");
  }

  public void testParsingIf1() throws Exception {
    testValid("@if (RTL_LANG) {\n" +
        " @def RTL_FLAG 1; \n" +
        " @def LEFT right;\n" +
        "} @else {\n" +
        " @def IMGURL url('images/image.gif');\n" +
        "}");
  }

  public void testParsingIf2() throws Exception {
    testValid("@if BROWSER_IE6 {\n" +
        "  @def FUNBOX_MARGIN                    0;\n" +
        "} @elseif BROWSER_IE {\n" +
        "  @def FUNBOX_MARGIN                    1 0 -1px 0;\n" +
        "} @elseif BROWSER_FF3_OR_HIGHER {\n" +
        "  @def FUNBOX_MARGIN                    -2px 0 0 0;\n" +
        "} @else {\n" +
        "    @if(A) { @def BB 23; }\n" +
        "  @def FUNBOX_MARGIN                    -2px 0 -1px 0;\n" +
        "}");
  }

  public void testParsingIf3() throws Exception {
    testValid("@if (RTL_LANG) {\n" +
        " CSS_RULE2.CLASS#id{ d:34em; }\n" +
        "} @else {\n" +
        "}");
  }

  public void testParsingParenthesizedTerm() throws Exception {
    testValid("@if (FOO) { x { y: z } }");
  }

  public void testParsingBooleanTerm1() throws Exception {
    testValid("@if ( A && (!B || C )) { @def RTL_FLAG 1;}");
  }

  public void testParsingBooleanTerm2() throws Exception {
    testValid("@if (!A &&   !B || C || !(F && G ) ) { @def RTL_FLAG 1;}");
  }

  public void testParsingComplexDef1() throws Exception {
    testValid("@def A a, b, c;");
  }

  public void testParsingComplexDef2() throws Exception {
    testValid("@def FONT a, b, c 14px/2em #fff;");
  }

  public void testParsingEqualsOperator() throws Exception {
    testValid(".CSS_ {\n" +
        " filter: alpha(opacity = 85) ;\n" +
        "}");
  }

  public void testParsingColonFunctionName() throws Exception {
    testValid("x {y: a.b:c(d)}");
  }

  public void testParsingColonFunctionName2() throws Exception {
    testValid(".CSS_ {\n" +
        "-ms-filter: \"progid:DXImageTr.Microsoft.Alpha(Opacity=80)\" ;\n" +
        "filter: progid:DXImageTr.Microsoft.AlphaImageLoader" +
        "(src='images/muc_bubble_left.png', sizingMethod='scale' );\n" +
        "}");
  }

  public void testParsingEmptyPseudo() throws Exception {
    testValid("::a, :a[b]::c { x: y}");
  }

  public void testParsingArbitraryDim() throws Exception {
    testValid("a {x: 2emelet 3x 5t}");
  }

  public void testSelectorWithSpace() throws Exception {
    testValid("a /* x */ , b {x: y}");
  }

  public void testIeRect() throws Exception {
    // Non-standard IE workaround.
    testValid(".a { clip: rect(0 0 0 0);}");
  }

  public void testEqualAttribute() throws Exception {
    testValid("h1[foo=\"bar\"] {x : y}");
  }

  public void testCaretEqualAttribute() throws Exception {
    testValid("h1[foo^=\"bar\"] {x : y}");
  }

  public void testDollarEqualAttribute() throws Exception {
    testValid("h1[foo$=\"bar\"] {x : y}");
  }

  public void testAsteriskEqualAttribute() throws Exception {
    testValid("h1[foo*=\"bar\"] {x : y}");
  }

  public void testPipeEqualAttribute() throws Exception {
    testValid("h1[foo|=\"bar\"] {x : y}");
  }

  public void testWebkitGradient() throws Exception {
    CssTree tree = testValid(".CSS { background: " +
        "-webkit-gradient(linear, 0 0, 0 100%, from(#fff), to(#ddd)) }");

    CssRootNode root = tree.getRoot();
    assertNotNull(root);
    assertEquals("[[.CSS]{[background:[" +
        "-webkit-gradient(linear,0 0,0 100%,from(#fff),to(#ddd))];]}]",
        AstPrinter.print(tree));

    CssRulesetNode ruleset =
        (CssRulesetNode) tree.getRoot().getBody().getChildAt(0);
    CssDeclarationNode decl =
        (CssDeclarationNode) ruleset.getDeclarations().getChildAt(0);
    CssFunctionNode function =
        (CssFunctionNode) decl.getPropertyValue().getChildAt(0);
    CssFunctionArgumentsNode args = function.getArguments();
    assertEquals("The argument list should be flattened, and contain " +
        "7 arguments + 6 separators (4 commas and 2 meaningful spaces).",
        13, args.numChildren());
  }

  public void testGradients() throws Exception {
    testValid("div {"
        + "a:radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
        + "b:radial-gradient(30% 30%, closest-corner, white, black);"
        + "c:radial-gradient(center, 5em 40px, white, black);"
        + "d:linear-gradient(bottom left, red 20px, yellow, green,"
        + "blue 90%);"
        + "e:repeating-linear-gradient(left, red 10%, blue 30%);"
        + "f:repeating-radial-gradient(top left, circle, red, blue 10%,"
        + "red 20%);"
        + "}");
  }

  /* http://www.webkit.org/blog/1424/css3-gradients/ */
  public void testWebkitGradients() throws Exception {
    testValid("div {"
        + "a:-webkit-radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
        + "b:-webkit-radial-gradient(30% 30%, closest-corner, white, black);"
        + "c:-webkit-radial-gradient(center, 5em 40px, white, black);"
        + "d:-webkit-linear-gradient(bottom left, red 20px, yellow, green,"
        + "blue 90%);"
        + "e:-webkit-repeating-linear-gradient(left, red 10%, blue 30%);"
        + "f:-webkit-repeating-radial-gradient(top left, circle, red, blue 10%,"
        + "red 20%);"
        + "}");
  }

  public void testMozillaGradients() throws Exception {
    testValid("div {"
        + "a:-moz-radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
        + "b:-moz-radial-gradient(30% 30%, closest-corner, white, black);"
        + "c:-moz-radial-gradient(center, 5em 40px, white, black);"
        + "d:-moz-linear-gradient(bottom left, red 20px, yellow, green,"
        + "blue 90%);"
        + "e:-moz-repeating-linear-gradient(left, red 10%, blue 30%);"
        + "f:-moz-repeating-radial-gradient(top left, circle, red, blue 10%,"
        + "red 20%);"
        + "}");
  }

  public void testOperaGradients() throws Exception {
    testValid("div {"
        + "a:-o-radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
        + "b:-o-radial-gradient(30% 30%, closest-corner, white, black);"
        + "c:-o-radial-gradient(center, 5em 40px, white, black);"
        + "d:-o-linear-gradient(bottom left, red 20px, yellow, green,"
        + "blue 90%);"
        + "e:-o-repeating-linear-gradient(left, red 10%, blue 30%);"
        + "f:-o-repeating-radial-gradient(top left, circle, red, blue 10%,"
        + "red 20%);"
        + "}");
  }

  public void testInternetExplorerGradients() throws Exception {
    testValid("div {"
        + "a:-ms-radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
        + "b:-ms-radial-gradient(30% 30%, closest-corner, white, black);"
        + "c:-ms-radial-gradient(center, 5em 40px, white, black);"
        + "d:-ms-linear-gradient(bottom left, red 20px, yellow, green,"
        + "blue 90%);"
        + "e:-ms-repeating-linear-gradient(left, red 10%, blue 30%);"
        + "f:-ms-repeating-radial-gradient(top left, circle, red, blue 10%,"
        + "red 20%);"
        + "}");
  }

  public void testWebkitMinDevicePixelRatio() throws Exception {
    testValid("@media screen and (-webkit-min-device-pixel-ratio:0) {}");
  }

  public void testMediaQuery() throws Exception {
    testValid("@media screen and (max-height: 300px) and (min-width: 20px) {}");
  }

  public void testMozLinearGradient() throws Exception {
    testValid(".CSS { background-image: " +
        "-moz-linear-gradient(bottom, #c0c0c0 0%, #dddddd 90%) }");
  }

  public void testParsingWebkitKeyframes1() throws Exception {
    testValid("@-webkit-keyframes bounce {\n" +
        "  from {\n" +
        "    left: 0px;\n" +
        "  }\n" +
        "  to {\n" +
        "    left: 200px;\n" +
        "  }\n" +
        "}\n");
  }

  public void testParsingMozKeyframes1() throws Exception {
    testValid("@-moz-keyframes bounce {\n" +
        "  from {\n" +
        "    left: 0px;\n" +
        "  }\n" +
        "  to {\n" +
        "    left: 200px;\n" +
        "  }\n" +
        "}\n");
  }

  public void testParsingWebkitKeyframes2() throws Exception {
    testValid("@-webkit-keyframes pulse {\n" +
        "  0% {\n" +
        "    background-color: red;\n" +
        "    opacity: 1.0;\n" +
        "    -webkit-transform: scale(1.0) rotate(0deg);\n" +
        "  }\n" +
        "  33.33% {\n" +
        "    background-color: blue;\n" +
        "    opacity: 0.75;\n" +
        "    -webkit-transform: scale(1.1) rotate(-5deg);\n" +
        "  }\n" +
        "  66.66% {\n" +
        "    background-color: green;\n" +
        "    opacity: 0.5;\n" +
        "    -webkit-transform: scale(1.1) rotate(5deg);\n" +
        "  }\n" +
        "  100% {\n" +
        "    background-color: red;\n" +
        "    opacity: 1.0;\n" +
        "    -webkit-transform: scale(1.0) rotate(0deg);\n" +
        "  }\n" +
        "}");
  }

  public void testParsingWebkitKeyframes3() throws Exception {
    testValid("@-webkit-keyframes bounce {\n" +
        "  0%, 51.2% {\n" +
        "    left: 0px;\n" +
        "    background: red;\n" +
        "  }\n" +
        "  25%, 90.5% {\n" +
        "    left: 200px;\n" +
        "    background: green;\n" +
        "  }\n" +
        "  25% {\n" +
        "    background: blue;\n" +
        "  }\n" +
        "}");
  }

  public void testParsingWebkitKeyframes4() throws Exception {
    testValid("@-webkit-keyframes from {}");
    testValid("@-webkit-keyframes to {}");
    testValid("from {}");
    testValid("to {}");
  }

  public void testEscapingInDoubleQuoteString() throws Exception {
    testValid("body {content: \"\\0af9bcHH\"}");
    testValid("body {content: \"\\0HH\"}");
    testValid("body {content: \"\\aHH\"}");
    testValid("body {content: \"\\gHH\"}");
    testValid("body {content: \"\\\"'HH\"}");
  }

  public void testEscapingInSingleQuoteString() throws Exception {
    testValid("body {content: '\\0af9bcHH'}");
    testValid("body {content: '\\0HH'}");
    testValid("body {content: '\\aHH'}");
    testValid("body {content: '\\gHH'}");
    testValid("body {content: '\"\\'HH'}");
  }

  public void testPseudoFunction() throws Exception {
    testValid("div :lang(en) { color: #FFF; }");
    testValid(":lang(fr) { color: #FFF; }");
  }

  public void testPseudoNth() throws Exception {
    testValid("div :nth-child(1n+1) { color: #FFF; }");
    testValid("div :nth-child(n+1) { color: #FFF; }");
    testValid("div :nth-child(+n+2) { color: #FFF; }");
    testValid("div :nth-child(n-1) { color: #FFF; }");
    testValid("div :nth-child(-n-1) { color: #FFF; }");
    testValid("div :nth-child(+2n+3) { color: #FFF; }");
    testValid("div :nth-child(-5n+1) { color: #FFF; }");
    // just 'n' is not supported by WebKit yet
    testValid("div :nth-child(n) { color: #FFF; }");
    testValid("div :nth-child(-n) { color: #FFF; }");
    testValid("div :nth-child(+n) { color: #FFF; }");
    testValid("div :nth-child(n-0) { color: #FFF; }");
    testValid("div :nth-child(0n+0) { color: #FFF; }");
    testValid("div :nth-child(1) { color: #FFF; }");
    testValid("div :nth-child(+7) { color: #FFF; }");
    testValid("div :nth-child(-9) { color: #FFF; }");
    testValid("div :nth-child(odd) { color: #FFF; }");
    testValid("div :nth-child(even) { color: #FFF; }");
  }

  public void testPseudoNot() throws Exception {
    testValid("p :not(.classy) { color: #123; }");
    testValid("p :not(div) { color: #123; }");
    testValid("p:not(div) { color: #123; }");
    testValid("p :not(  div  ) { color: #123; }");
    testValid("p :not(#id) { color: #123; }");
    testValid("*:not(:link):not(:visited) {}");
  }

  public void testPseudoElements() throws Exception {
    testValid("p::first-line { text-transform: uppercase }");
    testValid("p::first-letter { color: green; font-size: 200% }");
    testValid("div::after { color: #123; }");
    testValid("div::before { color: #123; }");
  }

  public void testOldPseudoElements() throws Exception {
    testValid("p:first-line { text-transform: uppercase }");
    testValid("p:first-letter { color: green; font-size: 200% }");
    testValid("div:after { color: #123; }");
    testValid("div:before { color: #123; }");
  }

  public void testMixinDefinitions() throws Exception {
    testValid("@defmixin name(PAR1, PAR2) { prop1: PAR1; prop2: PAR2 }");
    testValid("@defmixin name(  PAR1  , PAR2 )"
        + "{ prop1: PAR1; prop2: PAR2 }");
    testValid("@defmixin name(PAR1, PAR2) { prop1: PAR1; prop2: CONST; }");
  }

  public void testMixins() throws Exception {
    testValid("div { @mixin name(); }");
    testValid("div { @mixin name( ) ; }");
    testValid("div { prop1: val; @mixin defname(2px, #fff, 23%); }");
    testValid("div { prop1: val; @mixin defname(); p:v;}");
    testValid("div { @mixin foo(1px/1em); }");
    testValid("div { @mixin foo(1px 1px); }");
  }
}
