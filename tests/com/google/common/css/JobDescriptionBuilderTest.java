/*
 * Copyright 2008 Google Inc.
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

package com.google.common.css;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import junit.framework.TestCase;

/**
 * Unit tests for {@link JobDescriptionBuilder}.
 *
 */
public class JobDescriptionBuilderTest extends TestCase {
  private JobDescriptionBuilder builder;
  private JobDescription job;

  @Override
  protected void setUp() {
    builder = new JobDescriptionBuilder();
  }

  @Override
  protected void tearDown() {
    builder = null;
    job = null;
  }

  public void testSimpleCreation() {
    job = builder.getJobDescription();
    assertThat(job).isNotNull();
    assertThat(builder.getJobDescription()).isSameAs(job);
  }

  public void testSettingInputs1() {
    SourceCode sourceCode = new SourceCode("tempfile", "filecontents");
    builder.addInput(sourceCode);
    job = builder.getJobDescription();
    assertThat(job.inputs).hasSize(1);
    assertThat(job.inputs.get(0)).isSameAs(sourceCode);
  }

  public void testSettingInputs2() {
    builder.addInput(new SourceCode("bla", "bla")).clearInputs();
    SourceCode sourceCode = new SourceCode("tempfile", "filecontents");
    builder.addInput(sourceCode);
    job = builder.getJobDescription();
    assertThat(job.inputs).hasSize(1);
    assertThat(job.inputs.get(0)).isSameAs(sourceCode);
  }

  public void testSettingInputs3() {
    SourceCode sourceCode = new SourceCode("tempfile", "filecontents");
    builder.setInputs(ImmutableList.of(sourceCode));
    job = builder.getJobDescription();
    assertThat(job.inputs).hasSize(1);
    assertThat(job.inputs.get(0)).isSameAs(sourceCode);
  }

  public void testSettingConditions1() {
    String conditionName = "cond";
    builder.addTrueConditionName(conditionName);
    job = builder.getJobDescription();
    assertThat(job.trueConditionNames).hasSize(1);
    assertThat(job.trueConditionNames.get(0)).isSameAs(conditionName);
  }

  public void testSettingConditions2() {
    builder.addTrueConditionName("bla").clearTrueConditionNames();
    String conditionName = "cond";
    builder.addTrueConditionName(conditionName);
    job = builder.getJobDescription();
    assertThat(job.trueConditionNames).hasSize(1);
    assertThat(job.trueConditionNames.get(0)).isSameAs(conditionName);
  }

  public void testSettingConditions3() {
    String conditionName = "cond";
    builder.setTrueConditionNames(ImmutableList.of(conditionName));
    job = builder.getJobDescription();
    assertThat(job.trueConditionNames).hasSize(1);
    assertThat(job.trueConditionNames.get(0)).isSameAs(conditionName);
  }

  public void testSetCheckUnrecognizedProperties1() {
    builder.setAllowUnrecognizedProperties(false);
    job = builder.getJobDescription();
    assertThat(job.allowUnrecognizedProperties).isFalse();
  }

  public void testSetCheckUnrecognizedProperties2() {
    builder.setAllowUnrecognizedProperties(true);
    job = builder.getJobDescription();
    assertThat(job.allowUnrecognizedProperties).isTrue();
  }

  public void testSetCheckUnrecognizedProperties3() {
    builder.allowUnrecognizedProperties();
    job = builder.getJobDescription();
    assertThat(job.allowUnrecognizedProperties).isTrue();
  }

  public void testSetAllowUnrecognizedProperties() {
    List<String> properties = Lists.newArrayList("a", "b");
    builder.setAllowedUnrecognizedProperties(properties);
    job = builder.getJobDescription();
    assertThat(job.allowedUnrecognizedProperties).isEqualTo(Sets.newHashSet(properties));
  }

  public void testSetCopyrightNotice1() {
    builder.setCopyrightNotice(null);
    job = builder.getJobDescription();
    assertThat(job.copyrightNotice).isNull();
  }

  public void testSetCopyrightNotice2() {
    String copyrightNotice = "/* Copyright Google Inc. */";
    builder.setCopyrightNotice(copyrightNotice);
    job = builder.getJobDescription();
    assertThat(job.copyrightNotice).isEqualTo(copyrightNotice);
  }

  public void testCopyJobDescription() {
    JobDescription otherJob = new JobDescriptionBuilder().
        addInput(new SourceCode("tempFile", "contents")).
        setCopyrightNotice("/* Copyright Google Inc. */").
        setOutputFormat(JobDescription.OutputFormat.PRETTY_PRINTED).
        setInputOrientation(JobDescription.InputOrientation.RTL).
        setOutputOrientation(JobDescription.OutputOrientation.RTL).
        addTrueConditionName("TEST_COND").
        getJobDescription();
    job = builder.copyFrom(otherJob).getJobDescription();
    assertThat(job.inputs).isEqualTo(otherJob.inputs);
    assertThat(job.copyrightNotice).isEqualTo(otherJob.copyrightNotice);
    assertThat(job.outputFormat).isEqualTo(otherJob.outputFormat);
    assertThat(job.inputOrientation).isEqualTo(otherJob.inputOrientation);
    assertThat(job.outputOrientation).isEqualTo(otherJob.outputOrientation);
    assertThat(job.optimize).isEqualTo(otherJob.optimize);
    assertThat(job.trueConditionNames).isEqualTo(otherJob.trueConditionNames);
  }

  public void testCssRenamingPrefix() {
    String prefix = "PREFIX_";
    builder.setCssRenamingPrefix(prefix);
    job = builder.getJobDescription();
    assertThat(job.cssRenamingPrefix).isEqualTo(prefix);
  }

  public void testExcludedClasses() {
    List<String> exclude = Lists.newArrayList("foo", "bar");
    builder.setExcludedClassesFromRenaming(exclude);
    job = builder.getJobDescription();
    assertThat(job.excludedClassesFromRenaming).isEqualTo(exclude);
  }

  public void testAllowUndefinedConstants() {
    builder.setAllowUndefinedConstants(true);
    job = builder.getJobDescription();
    assertThat(job.allowUndefinedConstants).isTrue();

    builder = job.toBuilder();
    job = builder.getJobDescription();
    assertThat(job.allowUndefinedConstants).isTrue();
  }
}
