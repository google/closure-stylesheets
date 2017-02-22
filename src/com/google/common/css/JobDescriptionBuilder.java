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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.css.JobDescription.InputOrientation;
import com.google.common.css.JobDescription.OptimizeStrategy;
import com.google.common.css.JobDescription.OutputFormat;
import com.google.common.css.JobDescription.OutputOrientation;
import com.google.common.css.JobDescription.SourceMapDetailLevel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Aids in the creation of inputs for the compiler. A builder can be used for
 * creating a single {@link JobDescription}.
 *
 */
public class JobDescriptionBuilder {
  List<SourceCode> inputs;
  String copyrightNotice;
  OutputFormat outputFormat;
  InputOrientation inputOrientation;
  OutputOrientation outputOrientation;
  OptimizeStrategy optimize;
  List<String> trueConditionNames;
  boolean useInternalBidiFlipper;
  boolean swapLtrRtlInUrl;
  boolean swapLeftRightInUrl;
  boolean simplifyCss;
  boolean eliminateDeadStyles;
  boolean allowDefPropagation;
  boolean allowUnrecognizedFunctions;
  Set<String> allowedNonStandardFunctions;
  boolean allowUnrecognizedProperties;
  Set<String> allowedUnrecognizedProperties;
  boolean allowUndefinedConstants;
  boolean allowMozDocument;
  Vendor vendor;
  boolean allowKeyframes;
  boolean allowWebkitKeyframes;
  boolean processDependencies;
  String cssRenamingPrefix;
  List<String> excludedClassesFromRenaming;
  GssFunctionMapProvider gssFunctionMapProvider;
  SubstitutionMapProvider cssSubstitutionMapProvider;
  OutputRenamingMapFormat outputRenamingMapFormat;
  Map<String, String> inputRenamingMap;
  boolean preserveComments;
  boolean suppressDependencyCheck;
  Map<String, Integer> compileConstants;
  boolean preserveImportantComments;

  JobDescription job = null;
  boolean createSourceMap;
  SourceMapDetailLevel sourceMapLevel;

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
    this.allowDefPropagation = false;
    this.allowUnrecognizedFunctions = false;
    this.allowedNonStandardFunctions = Sets.newHashSet();
    this.allowUnrecognizedProperties = false;
    this.allowedUnrecognizedProperties = Sets.newHashSet();
    this.allowUndefinedConstants = false;
    this.allowMozDocument = false;
    this.vendor = null;
    this.allowKeyframes = true;
    this.allowWebkitKeyframes = true;
    this.processDependencies = false;
    this.cssRenamingPrefix = "";
    this.excludedClassesFromRenaming = Lists.newArrayList();
    this.gssFunctionMapProvider = null;
    this.cssSubstitutionMapProvider = null;
    this.outputRenamingMapFormat = OutputRenamingMapFormat.JSCOMP_VARIABLE_MAP;
    this.inputRenamingMap = ImmutableMap.of();
    this.preserveComments = false;
    this.suppressDependencyCheck = false;
    this.compileConstants = new HashMap<>();
    this.createSourceMap = false;
    this.sourceMapLevel = SourceMapDetailLevel.DEFAULT;
    this.preserveImportantComments = false;
  }

  public JobDescriptionBuilder copyFrom(JobDescription jobToCopy) {
    setInputs(Lists.newArrayList(jobToCopy.inputs));
    this.copyrightNotice = jobToCopy.copyrightNotice;
    setOutputFormat(jobToCopy.outputFormat);
    setInputOrientation(jobToCopy.inputOrientation);
    setOutputOrientation(jobToCopy.outputOrientation);
    this.optimize = jobToCopy.optimize;
    setTrueConditionNames(jobToCopy.trueConditionNames);
    this.useInternalBidiFlipper = jobToCopy.useInternalBidiFlipper;
    this.swapLtrRtlInUrl = jobToCopy.swapLtrRtlInUrl;
    this.swapLeftRightInUrl = jobToCopy.swapLeftRightInUrl;
    this.simplifyCss = jobToCopy.simplifyCss;
    this.eliminateDeadStyles = jobToCopy.eliminateDeadStyles;
    this.allowDefPropagation = jobToCopy.allowDefPropagation;
    this.allowUnrecognizedFunctions = jobToCopy.allowUnrecognizedFunctions;
    this.allowedNonStandardFunctions =
        ImmutableSet.copyOf(jobToCopy.allowedNonStandardFunctions);
    this.allowUnrecognizedProperties = jobToCopy.allowUnrecognizedProperties;
    this.allowedUnrecognizedProperties =
        ImmutableSet.copyOf(jobToCopy.allowedUnrecognizedProperties);
    this.allowUndefinedConstants = jobToCopy.allowUndefinedConstants;
    this.allowMozDocument = jobToCopy.allowMozDocument;
    this.vendor = jobToCopy.vendor;
    this.allowKeyframes = jobToCopy.allowKeyframes;
    this.allowWebkitKeyframes = jobToCopy.allowWebkitKeyframes;
    this.cssRenamingPrefix = jobToCopy.cssRenamingPrefix;
    setExcludedClassesFromRenaming(
        ImmutableList.copyOf(jobToCopy.excludedClassesFromRenaming));
    this.gssFunctionMapProvider = jobToCopy.gssFunctionMapProvider;
    this.cssSubstitutionMapProvider = jobToCopy.cssSubstitutionMapProvider;
    this.outputRenamingMapFormat = jobToCopy.outputRenamingMapFormat;
    this.inputRenamingMap = jobToCopy.inputRenamingMap;
    this.preserveComments = jobToCopy.preserveComments;
    this.suppressDependencyCheck = jobToCopy.suppressDependencyCheck;
    setCompileConstants(jobToCopy.compileConstants);
    this.createSourceMap = jobToCopy.createSourceMap;
    this.sourceMapLevel = jobToCopy.sourceMapLevel;
    this.preserveImportantComments = jobToCopy.preserveImportantComments;
    return this;
  }

  void checkJobIsNotAlreadyCreated() {
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
      OutputRenamingMapFormat outputFormat) {
    checkJobIsNotAlreadyCreated();
    this.outputRenamingMapFormat = outputFormat;
    return this;
  }

  public JobDescriptionBuilder setInputRenamingMap(Map<String, String> inputRenamingMap) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkNotNull(inputRenamingMap);
    this.inputRenamingMap = ImmutableMap.copyOf(inputRenamingMap);
    return this;
  }

  public JobDescriptionBuilder setAllowMozDocument(boolean allow) {
    checkJobIsNotAlreadyCreated();
    this.allowMozDocument = allow;
    return this;
  }

  public JobDescriptionBuilder allowMozDocument() {
    return setAllowMozDocument(true);
  }

  public JobDescriptionBuilder setAllowDefPropagation(boolean allow) {
    checkJobIsNotAlreadyCreated();
    this.allowDefPropagation = allow;
    return this;
  }

  public JobDescriptionBuilder allowDefPropagation() {
    return setAllowDefPropagation(true);
  }

  public JobDescriptionBuilder setAllowUndefinedConstants(boolean allow) {
    checkJobIsNotAlreadyCreated();
    this.allowUndefinedConstants = allow;
    return this;
  }

  public JobDescriptionBuilder setSuppressDependencyCheck(boolean suppress) {
    checkJobIsNotAlreadyCreated();
    this.suppressDependencyCheck = suppress;
    return this;
  }

  public JobDescriptionBuilder setPreserveComments(boolean preserve) {
    checkJobIsNotAlreadyCreated();
    this.preserveComments = preserve;
    return this;
  }

  public JobDescriptionBuilder preserveComments() {
    return setPreserveComments(true);
  }

  public JobDescriptionBuilder setCompileConstants(
      Map<String, Integer> newCompileConstants) {
    checkJobIsNotAlreadyCreated();
    Preconditions.checkState(this.compileConstants.isEmpty());
    Preconditions.checkArgument(!newCompileConstants.containsKey(null));
    this.compileConstants = new HashMap<>(newCompileConstants);
    return this;
  }

  public JobDescriptionBuilder setPreserveImportantComments(boolean preserve) {
    checkJobIsNotAlreadyCreated();
    this.preserveImportantComments = preserve;
    return this;
  }

  public JobDescriptionBuilder preserveImportantComments() {
    return setPreserveImportantComments(true);
  }

  public JobDescription getJobDescription() {
    if (job != null) {
      return job;
    }

    Set<String> allowedAtRules = Sets.newHashSet();
    if (allowKeyframes || allowWebkitKeyframes) {
      allowedAtRules.add("keyframes");
      allowedAtRules.add("-moz-keyframes");
      allowedAtRules.add("-ms-keyframes");
      allowedAtRules.add("-o-keyframes");
      allowedAtRules.add("-webkit-keyframes");
    }
    if (allowMozDocument) {
      allowedAtRules.add("-moz-document");
    }


    job = new JobDescription(inputs,
        copyrightNotice, outputFormat, inputOrientation, outputOrientation,
        optimize, trueConditionNames, useInternalBidiFlipper, swapLtrRtlInUrl,
        swapLeftRightInUrl, simplifyCss, eliminateDeadStyles,
        allowDefPropagation, allowUnrecognizedFunctions, allowedNonStandardFunctions,
        allowUnrecognizedProperties, allowedUnrecognizedProperties, allowUndefinedConstants,
        allowMozDocument, vendor,
        allowKeyframes, allowWebkitKeyframes, processDependencies,
        allowedAtRules, cssRenamingPrefix, excludedClassesFromRenaming,
        gssFunctionMapProvider, cssSubstitutionMapProvider,
        outputRenamingMapFormat, inputRenamingMap, preserveComments,
        suppressDependencyCheck, compileConstants,
        createSourceMap, sourceMapLevel, preserveImportantComments);
    return job;
  }

  public JobDescriptionBuilder setSourceMapLevel(SourceMapDetailLevel sourceMapLevel) {
    this.sourceMapLevel = sourceMapLevel;
    return this;
  }

  public JobDescriptionBuilder setCreateSourceMap(boolean createSourceMap) {
    this.createSourceMap = createSourceMap;
    return this;
  }

}
