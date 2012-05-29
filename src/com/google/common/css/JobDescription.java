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
import java.util.List;
import java.util.Set;

/**
 * Provides inputs and options to Closure Stylesheets.
 * To construct an instance, use a {@link JobDescriptionBuilder}.
 *
 * <p>Instances of this class are immutable.
 *
 */
public class JobDescription {
  public final List<SourceCode> inputs;
  public final String copyrightNotice;
  public final OutputFormat outputFormat;
  public final InputOrientation inputOrientation;
  public final OutputOrientation outputOrientation;
  public final OptimizeStrategy optimize;
  public final List<String> trueConditionNames;
  public final boolean useInternalBidiFlipper;
  public final boolean swapLtrRtlInUrl;
  public final boolean swapLeftRightInUrl;
  public final boolean simplifyCss;
  public final boolean eliminateDeadStyles;
  public final boolean allowUnrecognizedFunctions;
  public final Set<String> allowedNonStandardFunctions;
  public final boolean allowUnrecognizedProperties;
  public final Set<String> allowedUnrecognizedProperties;
  public final Vendor vendor;
  public final boolean allowKeyframes;
  public final boolean allowWebkitKeyframes;
  public final boolean processDependencies;
  public final ImmutableSet<String> allowedAtRules;
  public final String cssRenamingPrefix;
  public final List<String> excludedClassesFromRenaming;
  public final GssFunctionMapProvider gssFunctionMapProvider;
  public final SubstitutionMapProvider cssSubstitutionMapProvider;

  static final String CONDITION_FOR_LTR = "GSS_LTR";
  static final String CONDITION_FOR_RTL = "GSS_RTL";

  /**
   * The output format.
   */
  public enum OutputFormat {
    /** Pretty-prints the initial parse tree built from the input. */
    DEBUG,

    /** Prints a compact representation of the compiled CSS. */
    COMPRESSED,

    /** Pretty-prints the compiled CSS. */
    PRETTY_PRINTED,
  }

  /**
   * The input orientation.
   */
  public enum InputOrientation {
    LTR,
    RTL
  }

  /**
   * The output orientation.
   */
  public enum OutputOrientation {
    LTR,
    RTL,
    NOCHANGE
  }

  /**
   * The optimization strategy.
   */
  public enum OptimizeStrategy {
    NONE,
    SAFE,
    MAXIMUM
  }

  JobDescription(List<SourceCode> inputs,
      String copyrightNotice, OutputFormat outputFormat,
      InputOrientation inputOrientation, OutputOrientation outputOrientation,
      OptimizeStrategy optimize, List<String> trueConditionNames,
      boolean useInternalBidiFlipper, boolean swapLtrRtlInUrl,
      boolean swapLeftRightInUrl, boolean simplifyCss,
      boolean eliminateDeadStyles, boolean allowUnrecognizedFunctions,
      Set<String> allowedNonStandardFunctions,
      boolean allowUnrecognizedProperties,
      Set<String> allowedUnrecognizedProperties, Vendor vendor,
      boolean allowKeyframes, boolean allowWebkitKeyframes,
      boolean processDependencies, Set<String> allowedAtRules,
      String cssRenamingPrefix, List<String> excludedClassesFromRenaming,
      GssFunctionMapProvider gssFunctionMapProvider,
      SubstitutionMapProvider cssSubstitutionMapProvider) {
    Preconditions.checkArgument(!inputs.contains(null));
    Preconditions.checkNotNull(outputFormat);
    Preconditions.checkNotNull(inputOrientation);
    Preconditions.checkNotNull(outputOrientation);
    Preconditions.checkNotNull(optimize);
    Preconditions.checkNotNull(trueConditionNames);
    Preconditions.checkNotNull(allowedAtRules);
    Preconditions.checkNotNull(excludedClassesFromRenaming);
    this.inputs = ImmutableList.copyOf(inputs);
    this.copyrightNotice = copyrightNotice;
    this.outputFormat = outputFormat;
    this.inputOrientation = inputOrientation;
    this.outputOrientation = outputOrientation;
    this.optimize = optimize;
    this.trueConditionNames = ImmutableList.copyOf(trueConditionNames);
    this.useInternalBidiFlipper = useInternalBidiFlipper;
    this.swapLtrRtlInUrl = swapLtrRtlInUrl;
    this.swapLeftRightInUrl = swapLeftRightInUrl;
    this.simplifyCss = simplifyCss;
    this.eliminateDeadStyles = eliminateDeadStyles;
    this.allowUnrecognizedFunctions = allowUnrecognizedFunctions;
    this.allowedNonStandardFunctions = ImmutableSet.copyOf(
        allowedNonStandardFunctions);
    this.allowUnrecognizedProperties = allowUnrecognizedProperties;
    this.allowedUnrecognizedProperties = ImmutableSet.copyOf(
        allowedUnrecognizedProperties);
    this.vendor = vendor;
    this.allowKeyframes = allowKeyframes;
    this.allowWebkitKeyframes = allowWebkitKeyframes;
    this.processDependencies = processDependencies;
    this.allowedAtRules = ImmutableSet.copyOf(allowedAtRules);
    this.cssRenamingPrefix = cssRenamingPrefix;
    this.excludedClassesFromRenaming =
        ImmutableList.copyOf(excludedClassesFromRenaming);
    this.gssFunctionMapProvider = gssFunctionMapProvider;
    this.cssSubstitutionMapProvider = cssSubstitutionMapProvider;
  }

  /**
   * @return the total length of all the inputs' contents
   */
  public int getAllInputsLength() {
    int totalLength = 0;
    for (SourceCode input : inputs) {
      totalLength += input.getFileContentsLength();
    }
    return totalLength;
  }

  /**
   * Whether an input orientation is the same as an output orientation, meaning
   * that no flipping is required.
   */
  static boolean orientationsAreTheSame(InputOrientation inputOrientation,
      OutputOrientation outputOrientation) {
    return inputOrientation.toString().equals(outputOrientation.toString());
  }

  /**
   * Whether the job requires that the output orientation be different
   * from the input orientation.
   */
  public boolean needsBiDiFlipping() {
    return !(outputOrientation == OutputOrientation.NOCHANGE ||
        JobDescription.orientationsAreTheSame(
            inputOrientation, outputOrientation));
  }
}
