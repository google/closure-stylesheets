package com.google.common.css.compiler.ast;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.css.compiler.ast.testing.SourceCodeLocationSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Iterables;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.testing.TestParser;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests that {@link com.google.common.css.SourceCodeLocation}s are created properly. */
@RunWith(JUnit4.class)
public class SourceCodeLocationTest {

  private TestParser testParser = new TestParser();

  @Test
  @Ignore // The parser does not handle @charset.
  public void testCharset() throws Exception {
    CssTree tree = parse("@charset 'UTF-8';");
    CssAtRuleNode charsetRule = tree.getRoot().getCharsetRule();
    SourceCodeLocation location = charsetRule.getSourceCodeLocation();

    assertThat(location.getBeginLineNumber()).isEqualTo(1);
    assertThat(location.getBeginIndexInLine()).isEqualTo(1);
  }

  @Test
  @Ignore // The parser does not handle @import.
  public void testImport() throws Exception {
    CssTree tree = parse("@import ull('funky.css');");
    CssImportBlockNode importRules = tree.getRoot().getImportRules();
    CssImportRuleNode importRule = getOnlyElement(importRules.getChildren());
    SourceCodeLocation location = importRule.getSourceCodeLocation();

    assertThat(location.getBeginLineNumber()).isEqualTo(1);
    assertThat(location.getBeginIndexInLine()).isEqualTo(1);
  }

  @Test
  public void testSimpleRule() throws Exception {
    CssTree tree = parse(".foo {}");
    List<CssNode> rules = tree.getRoot().getBody().getChildren();
    CssRulesetNode rule = (CssRulesetNode) getOnlyElement(rules);
    assertThat(rule.getSourceCodeLocation()).hasSpan(1, 1, 1, 8);
    assertThat(rule.getSourceCodeLocation()).matches(".foo {}");

    CssSelectorListNode selectors = rule.getSelectors();
    assertThat(selectors.getSourceCodeLocation()).hasSpan(1, 1, 1, 5);
    assertThat(selectors.getSourceCodeLocation()).matches(".foo");

    CssSelectorNode selector = getOnlyElement(selectors.getChildren());
    assertThat(selector.getSourceCodeLocation()).hasSpan(1, 1, 1, 5);
    assertThat(selector.getSourceCodeLocation()).matches(".foo");

    assertThat(selector.getCombinator()).isNull();

    CssRefinerListNode refiners = selector.getRefiners();
    assertThat(refiners.getSourceCodeLocation()).hasSpan(1, 2, 1, 5);
    assertThat(refiners.getSourceCodeLocation()).matches("foo");

    CssRefinerNode refiner = getOnlyElement(refiners.getChildren());
    assertThat(refiner.getSourceCodeLocation()).hasSpan(1, 2, 1, 5);
    assertThat(refiner.getSourceCodeLocation()).matches("foo");

    CssDeclarationBlockNode declarations = rule.getDeclarations();
    assertThat(declarations.getSourceCodeLocation())
        .isUnknown(); // This is because the braces aren't part of the node.
  }

  @Test
  public void testTwoSimpleRules() throws Exception {
    CssTree tree = parse(".foo {}\n.bar {}");
    List<CssNode> rules = tree.getRoot().getBody().getChildren();
    CssRulesetNode rule = (CssRulesetNode) rules.get(0);
    assertThat(rule.getSourceCodeLocation()).hasSpan(1, 1, 1, 8);
    assertThat(rule.getSourceCodeLocation()).matches(".foo {}");

    CssSelectorListNode selectors = rule.getSelectors();
    assertThat(selectors.getSourceCodeLocation()).hasSpan(1, 1, 1, 5);
    assertThat(selectors.getSourceCodeLocation()).matches(".foo");

    rule = (CssRulesetNode) rules.get(1);
    assertThat(rule.getSourceCodeLocation()).hasSpan(2, 1, 2, 8);
    assertThat(rule.getSourceCodeLocation()).matches(".bar {}");

    selectors = rule.getSelectors();
    assertThat(selectors.getSourceCodeLocation()).hasSpan(2, 1, 2, 5);
    assertThat(selectors.getSourceCodeLocation()).matches(".bar");
  }

  @Test
  public void testRuleWithDeclaration() throws Exception {
    CssTree tree = parse(".foo {\n  background-color: blue\n}");
    List<CssNode> rules = tree.getRoot().getBody().getChildren();
    CssRulesetNode rule = (CssRulesetNode) getOnlyElement(rules);
    assertThat(rule.getSourceCodeLocation()).hasSpan(1, 1, 3, 2); // whole thing

    CssSelectorListNode selectors = rule.getSelectors();
    assertThat(selectors.getSourceCodeLocation()).hasSpan(1, 1, 1, 5);
    assertThat(selectors.getSourceCodeLocation()).matches(".foo");

    CssDeclarationBlockNode declarations = rule.getDeclarations();
    assertThat(declarations.getSourceCodeLocation()).hasSpan(2, 3, 2, 25);
    assertThat(declarations.getSourceCodeLocation()).matches("background-color: blue");

    CssDeclarationNode declaration =
        (CssDeclarationNode) Iterables.getOnlyElement(declarations.getChildren());
    assertThat(declaration.getSourceCodeLocation()).hasSpan(2, 3, 2, 25);

    CssPropertyNode propertyName = declaration.getPropertyName();
    assertThat(propertyName.getSourceCodeLocation()).hasSpan(2, 3, 2, 19);
    assertThat(propertyName.getSourceCodeLocation()).matches("background-color");

    CssPropertyValueNode propertyValueNode = declaration.getPropertyValue();
    assertThat(propertyValueNode.getSourceCodeLocation()).hasSpan(2, 21, 2, 25);
    assertThat(propertyValueNode.getSourceCodeLocation()).matches("blue");
  }

  @Test
  public void testRuleWithTwoDeclarations() throws Exception {
    CssTree tree = parse(".foo {\n  background-color: blue;\n  text-color: red\n}");
    List<CssNode> rules = tree.getRoot().getBody().getChildren();
    CssRulesetNode rule = (CssRulesetNode) getOnlyElement(rules);
    assertThat(rule.getSourceCodeLocation()).hasSpan(1, 1, 4, 2); // whole thing

    CssSelectorListNode selectors = rule.getSelectors();
    assertThat(selectors.getSourceCodeLocation()).hasSpan(1, 1, 1, 5);
    assertThat(selectors.getSourceCodeLocation()).matches(".foo");

    CssDeclarationBlockNode declarations = rule.getDeclarations();
    assertThat(declarations.getSourceCodeLocation()).hasSpan(2, 3, 3, 18);
    assertThat(declarations.getSourceCodeLocation())
        .matches("background-color: blue;\n  text-color: red");

    CssDeclarationNode declaration = (CssDeclarationNode) declarations.getChildren().get(0);
    assertThat(declaration.getSourceCodeLocation()).hasSpan(2, 3, 2, 25);
    assertThat(declaration.getSourceCodeLocation()).matches("background-color: blue");

    CssPropertyNode propertyName = declaration.getPropertyName();
    assertThat(propertyName.getSourceCodeLocation()).hasSpan(2, 3, 2, 19);
    assertThat(propertyName.getSourceCodeLocation()).matches("background-color");

    CssPropertyValueNode propertyValueNode = declaration.getPropertyValue();
    assertThat(propertyValueNode.getSourceCodeLocation()).hasSpan(2, 21, 2, 25);
    assertThat(propertyValueNode.getSourceCodeLocation()).matches("blue");

    declaration = (CssDeclarationNode) declarations.getChildren().get(1);
    assertThat(declaration.getSourceCodeLocation()).hasSpan(3, 3, 3, 18);
    assertThat(declaration.getSourceCodeLocation()).matches("text-color: red");

    propertyName = declaration.getPropertyName();
    assertThat(propertyName.getSourceCodeLocation()).hasSpan(3, 3, 3, 13);
    assertThat(propertyName.getSourceCodeLocation()).matches("text-color");

    propertyValueNode = declaration.getPropertyValue();
    assertThat(propertyValueNode.getSourceCodeLocation()).hasSpan(3, 15, 3, 18);
    assertThat(propertyValueNode.getSourceCodeLocation()).matches("red");
  }

  @Test
  public void testRuleWithFontDeclaration() throws Exception {
    CssTree tree = parse(".foo {\n  font: 12pt Times New Roman, serif;\n}");
    List<CssNode> rules = tree.getRoot().getBody().getChildren();
    CssRulesetNode rule = (CssRulesetNode) getOnlyElement(rules);
    CssDeclarationBlockNode declarations = rule.getDeclarations();
    CssDeclarationNode declaration =
        (CssDeclarationNode) Iterables.getOnlyElement(declarations.getChildren());
    assertThat(declaration.getSourceCodeLocation()).hasSpan(2, 3, 2, 36);
    assertThat(declaration.getSourceCodeLocation()).matches("font: 12pt Times New Roman, serif");

    CssPropertyNode propertyName = declaration.getPropertyName();
    assertThat(propertyName.getSourceCodeLocation()).hasSpan(2, 3, 2, 7);
    assertThat(propertyName.getSourceCodeLocation()).matches("font");

    CssPropertyValueNode propertyValueNode = declaration.getPropertyValue();
    assertThat(propertyValueNode.getSourceCodeLocation()).hasSpan(2, 9, 2, 36);
    assertThat(propertyValueNode.getSourceCodeLocation()).matches("12pt Times New Roman, serif");

    List<CssValueNode> valueNodes = propertyValueNode.getChildren();
    assertThat(valueNodes).hasSize(2);

    CssNumericNode cssNumericNode = (CssNumericNode) valueNodes.get(0);
    assertThat(cssNumericNode.getSourceCodeLocation()).hasSpan(2, 9, 2, 13);
    assertThat(cssNumericNode.getSourceCodeLocation()).matches("12pt");

    CssCompositeValueNode cssCompositeValueNode = (CssCompositeValueNode) valueNodes.get(1);
    assertThat(cssCompositeValueNode.getSourceCodeLocation()).hasSpan(2, 14, 2, 36);
    assertThat(cssCompositeValueNode.getSourceCodeLocation()).matches("Times New Roman, serif");

    assertThat(cssCompositeValueNode.getValues()).hasSize(2);
    assertThat(cssCompositeValueNode.getValues().get(0).getSourceCodeLocation())
        .hasSpan(2, 14, 2, 29);
    assertThat(cssCompositeValueNode.getValues().get(0).getSourceCodeLocation())
        .matches("Times New Roman");
    assertThat(cssCompositeValueNode.getValues().get(1).getSourceCodeLocation())
        .hasSpan(2, 31, 2, 36);
    assertThat(cssCompositeValueNode.getValues().get(1).getSourceCodeLocation()).matches("serif");
  }

  private CssTree parse(String cssText) throws GssParserException {
    testParser.addSource("test.css", cssText);
    return testParser.parse();
  }

}
