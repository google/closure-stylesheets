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


/**
 * Tests the compact printer visitor.
 * TODO(oana): Remove definitions from these tests.
 *
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */

public class CompactPrinterTest extends AbstractCompactPrinterTest {

  public void testEmptyRuleset1() throws Exception {
    assertCompactPrintedResult("foo{}", "foo {}");
  }

  public void testEmptyRuleset2() throws Exception {
    assertCompactPrintedResult("bar{}", "bar {}");
  }

  public void testEmptyRuleset3() {
    assertCompactPrintedResult("foo,bar{}", "foo, bar {\n}\n");
  }

  public void testEmptyRuleset4() {
    assertCompactPrintedResult("foo,.bar{}", "foo, .bar {}");
    assertCompactPrintedResult("foo,.bar:hover{}", "foo, .bar:hover {}");
  }

  public void testEmptyRulesetFancyRefiner() {
    assertCompactPrintedResult("foo,.bar:hover{}", "foo, .bar:hover {}");
  }

  public void testRuleset1() {
    assertCompactPrintedResult(
        "foo,.bar{color:red}", "foo, .bar {color: red;}");
  }

  public void testRuleset2() {
    assertCompactPrintedResult(
        "foo,.bar{color:red;background-image:url('http://s')}",
        lines("foo, .bar {",
              "color: red;",
              "background-image: url('http://s');",
              "}"));
  }

  public void testRuleset3() {
    assertCompactPrintedResult(
        ".foo .bar{color:red;background-image:url('http://s')}",
        lines(".foo .bar {",
              "color: red;",
              "background-image: url('http://s');",
              "}"));
  }

  public void testRuleset4() {
    assertCompactPrintedResult(
        ".foo .bar .foobar{color:red;background-image:url('http://s')}",
        lines(".foo .bar .foobar {",
              "color: red;",
              "background-image: url('http://s');",
              "}",
              ""));
  }

  public void testRuleset5() {
    assertCompactPrintedResult(
        ".foo:hover .bar+.foobar{color:red;background-image:url('http://s')}",
        lines(".foo:hover .bar+.foobar {",
              "color: red;",
              "background-image: url('http://s');",
              "}"));
  }

  public  void testRuleset6() {
    assertCompactPrintedResult(
        ".CSS_RULE{background:generate(COLOR,2px);border:BORDER}",
        lines("@def COLOR red;",
              "@def BORDER border(COLOR, 3px);",
              ".CSS_RULE {",
              "  background: generate(COLOR, 2px);",
              "  border: BORDER;",
              "}"));
  }

  public void testConditionalBlock() {
    assertCompactPrintedResult(
        "",
        lines("@def COLOR red;",
              "@if COND_1 {",
              "  .CSS_RULE {",
              "    foo: bar;",
              "  }",
              "}"));
  }

  public void testDefinitions1() {
    assertCompactPrintedResult(
        lines(""),
        lines("@def COLOR red;",
              "@def BORDER 2px;"));
  }

  public void testDefinitions2() {
    assertCompactPrintedResult(
        "body,td,input,textarea,select{margin:0;" +
        "font-family:APP_FONT_FACE}" +
        "td{padding:0}" +
        "input,textarea,select{font-size:APP_CAPTION_FONT_SIZE}",
        lines("@def APP_FONT_FACE arial sans-serif;",
              "@def APP_CAPTION_FONT_SIZE 100%;",
              "body, td, input, textarea, select {",
              "  margin:0;",
              "  font-family: APP_FONT_FACE;",
              "}",
              "td {",
              "  padding: 0;",
              "}",
              "input, textarea, select {",
              "  font-size: APP_CAPTION_FONT_SIZE;",
              "}"));
  }

  public void testDefinitions3() {
    assertCompactPrintedResult(
        ".CSS_RULE{background:CORNER_BG;filter:alpha(opacity=70)}",
        lines(
            "@def A red;",
            "@def B 3px;",
            "@def C 1;",
            "@def CORNER_BG gssFunction(A, B, C);",
            ".CSS_RULE {",
            "  background: CORNER_BG;",
            "  filter: alpha(opacity=70);",
            "}"));
  }

  public void testImport() {
    assertNewCompactPrintedResult(
        "@import 'a.css';.CSS_RULE{foo:bar}",
        lines(
            "@import 'a.css';",
            ".CSS_RULE {",
            "  foo: bar;",
            "}",
            "@def COLOR blue;"));
  }

  public void testMedia() {
    assertCompactPrintedResult(
        "@media print{.CSS_RULE{foo:bar}}",
        lines(
            "@media print {",
            "  .CSS_RULE {",
            "    foo: bar;",
            "  }",
            "}",
            "@def COLOR blue;"));
  }

  public void testStarPropertyHack() {
    assertCompactPrintedResult(
        ".myClass{*height:14px}",
        lines(
            ".myClass {",
            "*height: 14px;",
            "}"));
  }

  public void testIeRect() {
    // Non-standard IE workaround.
    assertCompactPrintedResult(
        ".a{clip:rect(0 0 0 0)}",
        lines(
            ".a {",
            "clip: rect(0 0 0 0);",
            "}"));
  }

  public void testFont() {
    assertCompactPrintedResult(
        ".a{font:9px verdana,helvetica,arial,san-serif}",
        lines(
            ".a {",
            "  font: 9px verdana, helvetica, arial, san-serif;",
            "}"));
  }

  public void testMediaQuery() {
    assertNewCompactPrintedResult(
        "@media screen,print{}",
        "@media screen, print {}");
    assertNewCompactPrintedResult(
        "@media screen and (-webkit-min-device-pixel-ratio:0){}",
        "@media screen and (-webkit-min-device-pixel-ratio:0){}");
    assertNewCompactPrintedResult(
        "@media all and (max-height:300px){}",
        "@media all and ( max-height: 300px ){}");
    assertNewCompactPrintedResult(
        "@media screen and (device-width:800px){}",
        "@media screen and (device-width:800px){}");
    assertNewCompactPrintedResult(
        "@media screen and (device-width:800px),print{}",
        "@media screen and (device-width:800px), print {}");
    assertNewCompactPrintedResult(
        "@media screen and (device-width:800px),tv and (scan:progressive),"
        + "handheld and (grid) and (max-width:15em){}",
        "@media screen and (device-width: 800px), "
        + "tv and (scan: progressive), "
        + "handheld and (grid) and (max-width: 15em) {}");
  }

  public void testMediaQueryPage() {
    assertNewCompactPrintedResult(
        "@media print{.CLASS{width:50%}@page{size:8.5in 11in}}",
        lines(
            "@media print {",
            "  .CLASS {",
            "     width: 50%;",
            "  }",
            "  @page {",
            "    size: 8.5in 11in;",
            "  }",
            "}"));
  }

  public void testWebkitKeyframes1() {
    assertNewCompactPrintedResult(
        "@-webkit-keyframes bounce{from{left:0px}to{left:200px}}",
        lines("@-webkit-keyframes bounce {",
            "from { left: 0px; }",
            "to { left: 200px; } }"));
  }

  public void testWebkitKeyframes2() {
    assertNewCompactPrintedResult(
        "@-webkit-keyframes pulse{0%{}33.33%{}100%{}}",
        lines("@-webkit-keyframes pulse {",
            "0% {}",
            "33.33% {}",
            "100% {}",
            "}"));
  }

  public void testRefiners() {
    assertNewCompactPrintedResult("foo:bar{}", "foo:bar {}");
    assertNewCompactPrintedResult("foo::bar{}", "foo::bar {}");
    assertNewCompactPrintedResult("foo:bar(ident){}", "foo:bar( ident ) {}");
    assertNewCompactPrintedResult("foo:bar(2n+1){}", "foo:bar( 2n+1 ) {}");
    assertNewCompactPrintedResult("button:not([DISABLED]){}",
        "button:not( [DISABLED] ) {}");
    assertNewCompactPrintedResult("*:not(:link):not(:visited){}",
         "*:not( :link ):not( :visited ) {}");
  }

  public void testPageRule() {
    assertNewCompactPrintedResult(
        "@page{a:b}",
        "@page{a:b}");
  }

  public void testPagePseudoClass1() {
    assertNewCompactPrintedResult(
        "@page :left{a:b}",
        "@page :left { a:b }");
  }

  public void testPagePseudoClass2() {
    assertNewCompactPrintedResult(
        "@page CompanyLetterHead:first{a:b}",
        "@page CompanyLetterHead:first { a:b }");
  }

  public void testPageInMedia() {
    assertNewCompactPrintedResult(
        "@media print{@page{a:b}}",
        "@media print { @page{ a:b } }");
  }

  public void testPageSelector() {
    assertNewCompactPrintedResult(
        "@media print{@page{@bottom-right-corner{a:b}}}",
        "@media print { @page { @bottom-right-corner { a:b } } }");
  }

  public void testFontFace1() {
    // Example taken from http://www.w3.org/TR/css3-fonts/#src-desc.
    assertNewCompactPrintedResult(
        "@font-face{font-family:MyGentium;"
        + "src:local(Gentium),url(Gentium.ttf)}",
        lines(
            "@font-face {",
            "  font-family: MyGentium;",
            "  src: local(Gentium), url(Gentium.ttf);",
            "}"));
  }

  public void testFontFace2() {
    // Example taken from http://www.w3.org/TR/css3-fonts/#src-desc.
    assertNewCompactPrintedResult(
        "@font-face{font-family:Headline;"
        + "src:local(Futura-Medium),"
        + "url(fonts.svg#MyGeometricModern) format(\"svg\")}",
        lines(
            "@font-face {",
            "  font-family: Headline;",
            "  src: local(Futura-Medium),",
            "       url(fonts.svg#MyGeometricModern) format(\"svg\");",
            "}"));
  }

  public void testAlphaImageLoader() throws Exception {
    assertNewCompactPrintedResult(
        ".CSS_CW_MUC_BUBBLE_BOTTOM_LEFT_ANCHOR{filter:"
        + "progid:DXImageTransform.Microsoft.AlphaImageLoader("
        + "src='images/muc_left_anchor_bubble_bot.png',sizingMethod='crop')}",
        lines(
            ".CSS_CW_MUC_BUBBLE_BOTTOM_LEFT_ANCHOR {",
            "  filter: progid:DXImageTransform.Microsoft.AlphaImageLoader("
            + "src = 'images/muc_left_anchor_bubble_bot.png',"
            + " sizingMethod = 'crop');",
            "}",
            ""));
  }

  public void testYouTube() throws Exception {
    assertNewCompactPrintedResult(
        ".test{"
        + "background:-webkit-linear-gradient(left,rgba(0,0,0,.12),"
        + "rgba(0,0,0,.08) 1px,rgba(0,0,0,.08) 1px,rgba(0,0,0,0) 30px,"
        + "transparent 100%)"
        + "}",
        lines(
            ".test {",
            "  background: -webkit-linear-gradient(left, rgba(0,0,0,.12),"
            + " rgba(0,0,0,.08) 1px, rgba(0,0,0,.08) 1px,rgba(0,0,0,0) 30px,"
            + "transparent 100%);",
            "}"));
  }
}
