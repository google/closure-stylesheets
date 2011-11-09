/*
 * Copyright 2011 Google Inc.
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

package com.google.common.css.compiler.commandline;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

/**
 * Defines the values for the --output-renaming-map-format flag in Closure
 * Stylesheets.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
enum OutputRenamingMapFormat {
  /**
   * Writes the mapping as JSON, passed as an argument to
   * {@code goog.setCssNameMapping()}. Designed for use with the Closure
   * Library in compiled mode.
   */
  CLOSURE_COMPILED("goog.setCssNameMapping(%s);\n"),

  /**
   * Writes the mapping as JSON, assigned to the global JavaScript variable
   * {@code CLOSURE_CSS_NAME_MAPPING}. Designed for use with the Closure
   * Library in uncompiled mode.
   */
  CLOSURE_UNCOMPILED("CLOSURE_CSS_NAME_MAPPING = %s;\n"),

  /**
   * Writes the mapping as JSON.
   */
  JSON,

  /**
   * Writes the mapping in a .properties file format, such that it can be read
   * by {@link Properties}.
   */
  PROPERTIES {
    @Override
    void writeRenamingMap(Map<String, String> renamingMap,
        PrintWriter renamingMapWriter)  {
      // We write the properties directly rather than using
      // Properties#store() because it is impossible to suppress the timestamp
      // comment: http://goo.gl/6hsrN. As noted on the Stack Overflow thread,
      // the timestamp results in unnecessary diffs between runs. Further, those
      // who are using a language other than Java to parse this file should not
      // have to worry about adding support for comments. 
      for (Map.Entry<String, String> entry : renamingMap.entrySet()) {
        renamingMapWriter.format("%s=%s\n", entry.getKey(), entry.getValue());
      }
    }
  },
  ;

  private final String formatString;

  private OutputRenamingMapFormat(String formatString) {
    Preconditions.checkNotNull(formatString);
    this.formatString = formatString;
  }

  private OutputRenamingMapFormat() {
    this("%s");
  }

  /**
   * @see DefaultCommandLineCompiler#writeRenamingMap(Map, PrintWriter)
   */
  void writeRenamingMap(Map<String, String> renamingMap,
      PrintWriter renamingMapWriter) {
    // Build up the renaming map as a JsonObject.
    JsonObject properties = new JsonObject();
    for (Map.Entry<String, String> entry : renamingMap.entrySet()) {
      properties.addProperty(entry.getKey(), entry.getValue());
    }

    // Write the JSON wrapped in this output format's formatString.
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    renamingMapWriter.write(String.format(formatString,
        gson.toJson(properties)));
  }  
}
