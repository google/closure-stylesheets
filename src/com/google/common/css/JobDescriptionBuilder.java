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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.css.JobDescription.InputOrientation;
import com.google.common.css.JobDescription.OptimizeStrategy;
import com.google.common.css.JobDescription.OutputFormat;
import com.google.common.css.JobDescription.OutputOrientation;

import java.util.List;
import java.util.Set;

/**
 * Aids in the creation of inputs for the compiler. A builder can be used for
 * creating a single {@link JobDescription}.
 *
 */
public class JobDescriptionBuilder {
  private List<SourceCode> inputs;
  private String copyrightNotice;
  private OutputFormat outputFormat;
  private InputOrientation inputOrientation;
  private OutputOrientation outputOrientation;
  private OptimizeStrategy optimize;
  private List<String> trueConditionNames;
  private boolean useInternalBidiFlipper;
  private boolean swapLtrRtlInUrl;
  private boolean swapLeftRightInUrl;
  private boolean simplifyCss;
  private boolean eliminateDeadStyles;
  private boolean allowUnrecognizedFunctions;
  private Set<String> allowedNonStandardFunctions;
  private boolean allowUnrecognizedProperties;
  private Set<String> allowedUnrecognizedProperties;
  private Vendor vendor;
  private boolean allowKeyframes;
  private boolean allowWebkitKeyframes;
  private boolean processDependencies;
  private String cssRenamingPrefix;
  private List<String> excludedClassesFromRenaming;
  private GssFunctionMapProvider gssFunctionMapProvider;
  private SubstitutionMapProvider cssSubstitutionMapProvider;
  private OutputRenamingMapFormat outputRenamingMapFormat;

  private JobDescription job = null;

  public JobDescriptionBuilder() {
    this.inputs = Lists.newArrayList();
    this.copyrightNotice = null;
    this.outputFormat = OutputFormat.COMPRESSED;
    this.inputOrientation = InputOrientation.LTR;
    this.outputOrientation = OutputOrientation.LTR;
    this.optimize = OptimizeStrategy.SAFE;
    this.trueConditionNames = Lists.newArrayList();
    this.useInternalBidiFlipper = false;
    this.swapLtrRtlInUrl = false;
    this.swapLeftRightInUrl = false;
    this.simplifyCss = false;
    this.eliminateDeadStyles = false;
    this.allowUnrecognizedFunctions = false;
    this.allowedNonStandardFunctions = Sets.newHashSet();
    this.allowUnrecognizedProperties = false;
    this.allowedUnrecognizedProperties = Sets.newHashSet();
    this.vendor = null;
    this.allowKeyframes = true;
    this.allowWebkitKeyframes = true;
    this.processDependencies = false;
    this.cssRenamingPrefix = "";
    this.excludedClassesFromRenaming = Lists.newArrayList();
    this.gssFunctionMapProvider = null;
    this.cssSubstitutionMapProvider = null;
    this.outputRenamingMapFormat = OutputRenamingMapFormat.JSCOMP_VARIABLE_MAP;
  }

  public JobDescriptionBuilder copyFrom(JobDescription jobToCopy) {
    setInputs(Lists.newArrayList(jobToCopy.inputs));
    this.copyrightNotice = jobToCopy.copyrightNotice;
    setOutputFormat(jobToCopy.outputFormat);
    setInputOrientation(jobToCopy.inputOrientation);
    setOutputOrientation(jobToCopy.outputOrientation);
    this.optimize = jobToCopy.optimize;
    setTrueConditionNames(Lists.newArrayList(jobToCopy.trueConditionNames));
    this.useInternalBidiFlipper = jobToCopy.useInternalBidiFlipper;
    this.swapLtrRtlInUrl = jobToCopy.swapLtrRtlInUrl;
    this.swapLeftRightInUrl = jobToCopy.swapLeftRightInUrl;
    this.simplifyCss = jobToCopy.simplifyCss;
    this.eliminateDeadStyles = jobToCopy.eliminateDeadStyles;
    this.allowUnrecognizedFunctions = jobToCopy.allowUnrecognizedFunctions;
    this.allowedNonStandardFunctions =
        ImmutableSet.copyOf(jobToCopy.allowedNonStandardFunctions);
    this.allowUnrecognizedProperties = jobToCopy.allowUnrecognizedProperties;
    this.allowedUnrecognizedProperties =
        ImmutableSet.copyOf(jobToCopy.allowedUnrecognizedProperties);
    this.vendor = jobToCopy.vendor;
    this.allowKeyframes = jobToCopy.allowKeyframes;
    this.allowWebkitKeyframes = jobToCopy.allowWebkitKeyframes;
    this.cssRenamingPrefix = jobToCopy.cssRenamingPrefix;
    setExcludedClassesFromRenaming(
        ImmutableList.copyOf(jobToCopy.excludedClassesFromRenaming));
    this.gssFunctionMapProvider = jobToCopy.gssFunctionMapProvider;
    this.cssSubstitutionMapProvider = jobToCopy.cssSubstitutionMapProvider;
    this.outputRenamingMapFormat = jobToCopy.outputRenamingMapFormat;
    return this;
  }

  private void checkJobIsNotAlreadyCreated() {
    Preconditions.checkState(job == null, "You cannot set job properties " +
        "after the message was created.");
  }

  public JobDescriptionBuilder clearInputs() {
    checkJobIsNotAlreadyCreated();
    this.inputs.clear();
    return this;
  }

  public JobDescriptionBuilder setInputs(List<SourceCode> newInputs) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkState(this.inputs.isEmpty());
    Preconditions.checkArgument(!newInputs.contains(null));
    this.inputs = Lists.newArrayList(newInputs);
    return this;
  }

  public JobDescriptionBuilder addInput(SourceCode sourceCode) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkNotNull(sourceCode);
    this.inputs.add(sourceCode);
    return this;
  }

  public JobDescriptionBuilder setOptimizeStrategy(OptimizeStrategy optimize) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkNotNull(optimize);
    this.optimize = optimize;
    return this;
  }

  public JobDescriptionBuilder clearTrueConditionNames() {
    checkJobIsNotAlreadyCreated();
    this.trueConditionNames.clear();
    return this;
  }

  public JobDescriptionBuilder setTrueConditionNames(
      List<String> newTrueConditionNames) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkState(this.trueConditionNames.isEmpty());
    Preconditions.checkArgument(!newTrueConditionNames.contains(null));
    this.trueConditionNames = Lists.newArrayList(newTrueConditionNames);
    return this;
  }

  public JobDescriptionBuilder addTrueConditionName(String conditionName) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkNotNull(conditionName);
    this.trueConditionNames.add(conditionName);
    return this;
  }

  public JobDescriptionBuilder setExcludedClassesFromRenaming(
      List<String> excludedClassesFromRenaming) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkState(this.excludedClassesFromRenaming.isEmpty());
    Preconditions.checkArgument(!excludedClassesFromRenaming.contains(null));
    this.excludedClassesFromRenaming =
        Lists.newArrayList(excludedClassesFromRenaming);
    return this;
  }

  public JobDescriptionBuilder setCssRenamingPrefix(String cssRenamingPrefix) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkNotNull(cssRenamingPrefix);
    this.cssRenamingPrefix = cssRenamingPrefix;
    return this;
  }

  public JobDescriptionBuilder setCopyrightNotice(String copyrightNotice) {
    checkJobIsNotAlreadyCreated();
    this.copyrightNotice = copyrightNotice;
    return this;
  }

  public JobDescriptionBuilder setUseInternalBidiFlipper(boolean use) {
    checkJobIsNotAlreadyCreated();
    this.useInternalBidiFlipper = use;
    return this;
  }

  public JobDescriptionBuilder useInternalBidiFlipper() {
    return setUseInternalBidiFlipper(true);
  }

  public JobDescriptionBuilder setSwapLtrRtlInUrl(boolean swap) {
    checkJobIsNotAlreadyCreated();
    if (useInternalBidiFlipper) {
      this.swapLtrRtlInUrl = swap;
    }
    return this;
  }

  public JobDescriptionBuilder swapLtrRtlInUrl() {
    return setSwapLtrRtlInUrl(true);
  }

  public JobDescriptionBuilder setSwapLeftRightInUrl(boolean swap) {
    checkJobIsNotAlreadyCreated();
    if (useInternalBidiFlipper) {
      this.swapLeftRightInUrl = swap;
    }
    return this;
  }

  public JobDescriptionBuilder swapLeftRightInUrl() {
    return setSwapLeftRightInUrl(true);
  }

  public JobDescriptionBuilder setInputOrientation(
      InputOrientation newInputOrientation) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkNotNull(newInputOrientation);
    this.inputOrientation = newInputOrientation;
    return this;
  }

  public JobDescriptionBuilder setOutputOrientation(
      OutputOrientation newOutputOrientation) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkNotNull(newOutputOrientation);
    this.outputOrientation = newOutputOrientation;
    return this;
  }

  public JobDescriptionBuilder setOutputFormat(OutputFormat newOutputFormat) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkNotNull(newOutputFormat);
    this.outputFormat = newOutputFormat;
    return this;
  }

  public JobDescriptionBuilder setSimplifyCss(boolean simplify) {
    checkJobIsNotAlreadyCreated();
    this.simplifyCss = simplify;
    return this;
  }

  public JobDescriptionBuilder simplifyCss() {
    return setSimplifyCss(true);
  }

  public JobDescriptionBuilder setEliminateDeadStyles(boolean eliminate) {
    checkJobIsNotAlreadyCreated();
    this.eliminateDeadStyles = eliminate;
    return this;
  }

  public JobDescriptionBuilder eliminateDeadStyles() {
    return setEliminateDeadStyles(true);
  }

  public JobDescriptionBuilder setGssFunctionMapProvider(
      GssFunctionMapProvider gssFunctionMapProvider) {
    checkJobIsNotAlreadyCreated();
    this.gssFunctionMapProvider = gssFunctionMapProvider;
    return this;
  }

  public JobDescriptionBuilder setCssSubstitutionMapProvider(
      SubstitutionMapProvider cssSubstitutionMapProvider) {
    checkJobIsNotAlreadyCreated();
    this.cssSubstitutionMapProvider = cssSubstitutionMapProvider;
    return this;
  }

  public JobDescriptionBuilder setAllowUnrecognizedFunctions(boolean allow) {
    checkJobIsNotAlreadyCreated();
    this.allowUnrecognizedFunctions = allow;
    return this;
  }

  public JobDescriptionBuilder allowUnrecognizedFunctions() {
    return setAllowUnrecognizedFunctions(true);
  }

  public JobDescriptionBuilder setAllowedNonStandardFunctions(
      List<String> functionNames) {
    checkJobIsNotAlreadyCreated();
    this.allowedNonStandardFunctions.addAll(functionNames);
    return this;
  }

  public JobDescriptionBuilder setAllowUnrecognizedProperties(boolean allow) {
    checkJobIsNotAlreadyCreated();
    this.allowUnrecognizedProperties = allow;
    return this;
  }

  public JobDescriptionBuilder allowUnrecognizedProperties() {
    return setAllowUnrecognizedProperties(true);
  }

  public JobDescriptionBuilder setAllowedUnrecognizedProperties(
      List<String> propertyNames) {
    checkJobIsNotAlreadyCreated();
    this.allowedUnrecognizedProperties.addAll(propertyNames);
    return this;
  }

  public JobDescriptionBuilder setVendor(Vendor vendor) {
    checkJobIsNotAlreadyCreated();
    this.vendor = vendor;
    return this;
  }

  public JobDescriptionBuilder setAllowKeyframes(boolean allow) {
    checkJobIsNotAlreadyCreated();
    this.allowKeyframes = allow;
    return this;
  }

  public JobDescriptionBuilder allowKeyframes() {
    return setAllowKeyframes(true);
  }

  public JobDescriptionBuilder setAllowWebkitKeyframes(boolean allow) {
    checkJobIsNotAlreadyCreated();
    this.allowWebkitKeyframes = allow;
    return this;
  }

  public JobDescriptionBuilder allowWebkitKeyframes() {
    return setAllowWebkitKeyframes(true);
  }

  public JobDescriptionBuilder setProcessDependencies(boolean process) {
    checkJobIsNotAlreadyCreated();
    this.processDependencies = process;
    return this;
  }

  public JobDescriptionBuilder setOutputRenamingMapFormat(
      OutputRenamingMapFormat format) {
    checkJobIsNotAlreadyCreated();
    this.outputRenamingMapFormat = format;
    return this;
  }


  public JobDescription getJobDescription() {
    if (job != null) {
      return job;
    }

    Set<String> allowedAtRules = Sets.newHashSet();

    job = new JobDescription(inputs,
        copyrightNotice, outputFormat, inputOrientation, outputOrientation,
        optimize, trueConditionNames, useInternalBidiFlipper, swapLtrRtlInUrl,
        swapLeftRightInUrl, simplifyCss, eliminateDeadStyles,
        allowUnrecognizedFunctions, allowedNonStandardFunctions,
        allowUnrecognizedProperties, allowedUnrecognizedProperties, vendor,
        allowKeyframes, allowWebkitKeyframes, processDependencies,
        allowedAtRules, cssRenamingPrefix, excludedClassesFromRenaming,
        gssFunctionMapProvider, cssSubstitutionMapProvider,
        outputRenamingMapFormat);
    return job;
  }
}
