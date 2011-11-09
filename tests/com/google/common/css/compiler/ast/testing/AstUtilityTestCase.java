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

package com.google.common.css.compiler.ast.testing;

import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.testing.UtilityTestCase;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Iterator;

/**
 * Utility class for comparison of css nodes.
 *
 * @author oana@google.com (Oana Florescu)
 */
public class AstUtilityTestCase extends UtilityTestCase {

  /**
   * Utility method for deep equals comparison between two css nodes.
   */
  public void deepEquals(CssNode node1, CssNode node2)
      throws IllegalArgumentException, IllegalAccessException {

    Class<? extends CssNode> class1 = node1.getClass();
    Class<? extends CssNode> class2 = node2.getClass();

    assertEquals(class1, class2);

    Class<?> currentClass = class1;
    assertFieldsEqual(node1, node2, currentClass);
    while (!CssNode.class.equals(currentClass)) {
      currentClass = currentClass.getSuperclass();
      assertFieldsEqual(node1, node2, currentClass);
    }
  }

  /**
   * Utility method to assert that the fields of two nodes are equal. The
   * comparison looks only at the current node and its descendants. Nodes
   * containing collections of CssNode objects are recursively compared.
   *
   * @param node1 Node1 for the comparison
   * @param node2 Node2 for the comparison
   * @param currentClass The class for which the fields are taken
   * @throws IllegalAccessException
   */
  @SuppressWarnings("unchecked")
  private void assertFieldsEqual(CssNode node1,
                                 CssNode node2,
                                 Class<?> currentClass)
      throws IllegalAccessException {
    Field fields[] = currentClass.getDeclaredFields();
    for (Field field : fields) {
      if ("parent".equals(field.getName())) {
        continue;
      }
      field.setAccessible(true);
      final Object value1 = field.get(node1);
      final Object value2 = field.get(node2);
      if (value1 != value2) {

        // Recursively compare fields of CssNode
        if (CssNode.class.isAssignableFrom(field.getType())) {
          deepEquals((CssNode) value1, (CssNode) value2);
          continue;
        }

        // Recursively compare fields that are collections of CssNodes
        if (isFieldCollectionOfCssNode(field)) {
          assertCollectionEqual(
              (Collection<? extends CssNode>) value1,
              (Collection<? extends CssNode>) value2);
          continue;
        }

        assertEquals("Field " + field, value1, value2);
      }
    }
  }

  private boolean isFieldCollectionOfCssNode(Field field) {
    // There are two ways in which this field can be a collection of CssNodes:
    // it can be something like Collection<SomethingThatExtendsCssNode>,
    // or it can be a variable type like Collection<T extends CssNode>.
    if (Collection.class.isAssignableFrom(field.getType())
        && field.getGenericType() instanceof ParameterizedType) {

      ParameterizedType collectionType =
          (ParameterizedType) field.getGenericType();

      for (Type type : collectionType.getActualTypeArguments()) {

        // This is a type that inherits from CssNode.
        if (type instanceof Class<?>
            && CssNode.class.isAssignableFrom((Class<?>) type)) {
          return true;
        }

        // Type is a variable type that extends CssNode.
        if (type instanceof TypeVariable<?>) {
          for (Type t : ((TypeVariable<?>) type).getBounds()) {
            if (t instanceof Class<?>
                && CssNode.class.isAssignableFrom((Class<?>) t)) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  private void assertCollectionEqual(
      Collection<? extends CssNode> collection1,
      Collection<? extends CssNode> collection2)
      throws IllegalArgumentException, IllegalAccessException {

    // Recursively compare two collections of {@code CssNode}s.
    Iterator<? extends CssNode> it1 = collection1.iterator();
    Iterator<? extends CssNode> it2 = collection2.iterator();

    while (it1.hasNext() && it2.hasNext()) {
      CssNode n1 = it1.next();
      CssNode n2 = it2.next();
      deepEquals(n1, n2);
    }
  }
}
