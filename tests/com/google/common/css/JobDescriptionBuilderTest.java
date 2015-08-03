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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

import java.util.List;

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
    assertNotNull(job);
    assertSame(job, builder.getJobDescription());
  }

  public void testSettingInputs1() {
    SourceCode sourceCode = new SourceCode("tempfile", "filecontents");
    builder.addInput(sourceCode);
    job = builder.getJobDescription();
    assertEquals(1, job.inputs.size());
    assertSame(sourceCode, job.inputs.get(0));
  }

  public void testSettingInputs2() {
    builder.addInput(new SourceCode("bla", "bla")).clearInputs();
    SourceCode sourceCode = new SourceCode("tempfile", "filecontents");
    builder.addInput(sourceCode);
    job = builder.getJobDescription();
    assertEquals(1, job.inputs.size());
    assertSame(sourceCode, job.inputs.get(0));
  }

  public void testSettingInputs3() {
    SourceCode sourceCode = new SourceCode("tempfile", "filecontents");
    builder.setInputs(ImmutableList.of(sourceCode));
    job = builder.getJobDescription();
    assertEquals(1, job.inputs.size());
    assertSame(sourceCode, job.inputs.get(0));
  }

  public void testSettingConditions1() {
    String conditionName = "cond";
    builder.addTrueConditionName(conditionName);
    job = builder.getJobDescription();
    assertEquals(1, job.trueConditionNames.size());
    assertSame(conditionName, job.trueConditionNames.get(0));
  }

  public void testSettingConditions2() {
    builder.addTrueConditionName("bla").clearTrueConditionNames();
    String conditionName = "cond";
    builder.addTrueConditionName(conditionName);
    job = builder.getJobDescription();
    assertEquals(1, job.trueConditionNames.size());
    assertSame(conditionName, job.trueConditionNames.get(0));
  }

  public void testSettingConditions3() {
    String conditionName = "cond";
    builder.setTrueConditionNames(ImmutableList.of(conditionName));
    job = builder.getJobDescription();
    assertEquals(1, job.trueConditionNames.size());
    assertSame(conditionName, job.trueConditionNames.get(0));
  }

  public void testSetCheckUnrecognizedProperties1() {
    builder.setAllowUnrecognizedProperties(false);
    job = builder.getJobDescription();
    assertFalse(job.allowUnrecognizedProperties);
  }

  public void testSetCheckUnrecognizedProperties2() {
    builder.setAllowUnrecognizedProperties(true);
    job = builder.getJobDescription();
    assertTrue(job.allowUnrecognizedProperties);
  }

  public void testSetCheckUnrecognizedProperties3() {
    builder.allowUnrecognizedProperties();
    job = builder.getJobDescription();
    assertTrue(job.allowUnrecognizedProperties);
  }

  public void testSetAllowUnrecognizedProperties() {
    List<String> properties = Lists.newArrayList("a", "b");
    builder.setAllowedUnrecognizedProperties(properties);
    job = builder.getJobDescription();
    assertEquals(Sets.newHashSet(properties),
        job.allowedUnrecognizedProperties);
  }

  public void testSetCopyrightNotice1() {
    builder.setCopyrightNotice(null);
    job = builder.getJobDescription();
    assertNull(job.copyrightNotice);
  }

  public void testSetCopyrightNotice2() {
    String copyrightNotice = "/* Copyright Google Inc. */";
    builder.setCopyrightNotice(copyrightNotice);
    job = builder.getJobDescription();
    assertEquals(copyrightNotice, job.copyrightNotice);
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
    assertEquals(otherJob.inputs, job.inputs);
    assertEquals(otherJob.copyrightNotice, job.copyrightNotice);
    assertEquals(otherJob.outputFormat, job.outputFormat);
    assertEquals(otherJob.inputOrientation, job.inputOrientation);
    assertEquals(otherJob.outputOrientation, job.outputOrientation);
    assertEquals(otherJob.optimize, job.optimize);
    assertEquals(otherJob.trueConditionNames, job.trueConditionNames);
  }

  public void testCssRenamingPrefix() {
    String prefix = "PREFIX_";
    builder.setCssRenamingPrefix(prefix);
    job = builder.getJobDescription();
    assertEquals(prefix, job.cssRenamingPrefix);
  }

  public void testExcludedClasses() {
    List<String> exclude = Lists.newArrayList("foo", "bar");
    builder.setExcludedClassesFromRenaming(exclude);
    job = builder.getJobDescription();
    assertEquals(exclude, job.excludedClassesFromRenaming);
  }

  public void testAllowUndefinedConstants() {
    builder.setAllowUndefinedConstants(true);
    job = builder.getJobDescription();
    assertTrue(job.allowUndefinedConstants);

    builder = job.toBuilder();
    job = builder.getJobDescription();
    assertTrue(job.allowUndefinedConstants);
  }
}
