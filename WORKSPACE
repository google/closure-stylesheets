## Copyright 2018 The Closure Stylesheets Authors.
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.

workspace(name = "com_google_closure_stylesheets")

load("//tools:maven_jar.bzl", "maven_jar")

maven_jar(
    name = "args4j",
    artifact_id = "args4j",
    group_id = "args4j",
    sha1 = "01ebb18ebb3b379a74207d5af4ea7c8338ebd78b",
    version = "2.0.26",
)

maven_jar(
    name = "com_google_auto_value",
    artifact_id = "auto-value",
    group_id = "com.google.auto.value",
    sha1 = "a3b1b1404f8acaa88594a017185e013cd342c9a8",
    version = "1.6",
)

maven_jar(
    name = "com_google_guava",
    artifact_id = "guava",
    group_id = "com.google.guava",
    sha1 = "89507701249388e1ed5ddcf8c41f4ce1be7831ef",
    version = "20.0",
)

maven_jar(
    name = "com_google_code_gson",
    artifact_id = "gson",
    group_id = "com.google.code.gson",
    sha1 = "751f548c85fa49f330cecbb1875893f971b33c4e",
    version = "2.7",
)

new_http_archive(
    name = "javacc",
    build_file_content = """
java_import(
  name = "javacc_lib",
  jars = [
    "bin/lib/javacc.jar",
  ],
)

java_binary(
  name = "javacc",
  runtime_deps = [":javacc_lib"],
  main_class = "javacc",
  visibility = ["//visibility:public"],
)
""",
    sha256 = "c8750906c5495dcc00a7477d6a435440a48dabbc758ea1c4f13a17f25c5fa28a",
    strip_prefix = "javacc-6.0",
    url = "https://javacc.org/downloads/javacc-6.0.zip",
)

maven_jar(
    name = "com_google_code_findbugs_jsr305",
    artifact_id = "jsr305",
    group_id = "com.google.code.findbugs",
    sha1 = "f7be08ec23c21485b9b5a1cf1654c2ec8c58168d",
    version = "3.0.1",
)

maven_jar(
    name = "com_google_javascript_closure_compiler",
    artifact_id = "closure-compiler-unshaded",
    group_id = "com.google.javascript",
    sha1 = "7df7b683e16c93f65361a15356283599ba012c78",
    version = "v20160713",
)

maven_jar(
    name = "org_mockito_all",
    artifact_id = "mockito-all",
    group_id = "org.mockito",
    sha1 = "539df70269cc254a58cccc5d8e43286b4a73bf30",
    version = "1.10.19",
)

maven_jar(
    name = "com_google_truth",
    artifact_id = "truth",
    group_id = "com.google.truth",
    sha1 = "7485219d2c1d341097a19382c02bde07e69ff5d2",
    version = "0.36",
)
