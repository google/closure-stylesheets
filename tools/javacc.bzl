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

def _declare_output_files(ctx, files):
  if len(files) < 1:
    fail("files must not be empty.")

  sources = []
  for file in files:
    sources.append(ctx.actions.declare_file(file))

  return struct(
      files = sources,
      path = '/'.join(sources[0].path.split('/')[:-1]),
  )

def _javacc_impl(ctx):
  outputs = _declare_output_files(ctx, ctx.attr.outs)
  args = [
    '-OUTPUT_DIRECTORY=%s' % outputs.path,
    ctx.file.src.path,
  ]
  ctx.actions.run(
      inputs = [ctx.file.src],
      outputs = outputs.files,
      arguments = args,
      executable = ctx.executable._compiler,
  )

  return struct(
      files = depset(outputs.files),
  )

javacc = rule(
    implementation = _javacc_impl,
    output_to_genfiles = True,
    attrs={
        "src": attr.label(
            mandatory = True,
            allow_files = [".jj"],
            single_file = True,
        ),
        "outs": attr.string_list(
            mandatory = True,
        ),
        "_compiler": attr.label(
            default = Label("@javacc//:javacc"),
            executable = True,
            cfg = "host",
        ),
    },
)
