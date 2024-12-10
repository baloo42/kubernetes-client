/*
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.crd.generator.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class CrdGeneratorPluginExtension {

  public static final String NAME = "crd2java";

  private Project gradleProject;

  /**
   * The input directory to be used to scan for Custom Resource classes
   */
  private File classesToIndex;

  /**
   * Custom Resource classes, which should be considered to generate the CRDs. If set, scanning is
   * disabled.
   */
  private List<String> customResourceClassNames = new LinkedList<>();

  /**
   * Dependencies which should be scanned for Custom Resources.
   */
  private List<Dependency> dependenciesToScan = new LinkedList<>();

  /**
   * Inclusions, used to filter Custom Resource classes after scanning.
   */

  private FilterSet inclusions = new FilterSet();

  /**
   * Exclusions, used to filter Custom Resource classes after scanning.
   */
  private FilterSet exclusions = new FilterSet();

  /**
   * The classpath which should be used during the CRD generation.
   * <br>
   * Choice of:
   * <ul>
   * <li>{@code PROJECT_ONLY}: Only classes in the project.</li>
   * <li>{@code WITH_RUNTIME_DEPENDENCIES}: Classes from the project and any runtime dependencies.</li>
   * <li>{@code WITH_COMPILE_DEPENDENCIES}: Classes from the project and any compile time dependencies.</li>
   * <li>{@code WITH_ALL_DEPENDENCIES}: Classes from the project, compile time and runtime dependencies.</li>
   * <li>{@code WITH_ALL_DEPENDENCIES_AND_TESTS}: Classes from the project (including tests), compile time, runtime and test
   * dependencies.</li>
   * </ul>
   */
  private ClasspathType classpath = ClasspathType.PROJECT_ONLY;

  /**
   * The output directory where the CRDs are emitted.
   */
  private File outputDirectory;

  /**
   * If true, a Jandex index will be created even if the directory or JAR file contains an existing
   * index.
   */
  private boolean forceIndex;

  private boolean forceScan;

  /**
   * If enabled, the CRDs will be generated in parallel.
   */
  private boolean parallel;

  /**
   * If enabled, all objects are implicitly marked with x-kubernetes-preserve-unknown-fields: true.
   */
  private boolean implicitPreserveUnknownFields;

  /**
   * Skip execution if set.
   */
  private boolean skip;

  public CrdGeneratorPluginExtension(Project gradleProject) {
    this.gradleProject = gradleProject;
  }

  /**
   * The output directory where the CRDs are emitted.
   */
  public abstract DirectoryProperty getTarget();

  public abstract ListProperty<String> getCustomResourceClassNames();

  public File getClassesToIndex() {
    return classesToIndex;
  }

  public File getClassesToIndexOrDefault() {
    return this.getTarget().getAsFile()
      .getOrElse(this.gradleProject.getLayout().getProjectDirectory()
        .dir("build").getAsFile());
  }

  public void setClassesToIndex(File classesToIndex) {
    this.classesToIndex = classesToIndex;
  }

  public List<String> getCustomResourceClassNamesOrDefault() {
    return getCustomResourceClassNames().getOrElse(Collections.emptyList());
  }

  public boolean isForceIndex() {
    return forceIndex;
  }

  public void setForceIndex(boolean forceIndex) {
    this.forceIndex = forceIndex;
  }

  public Set<String> getClasspathElements() {
    return classpath.getClasspathElements(gradleProject);
  }

  public File getOutputDirectoryOrDefault() {
    return this.getTarget().getAsFile()
      .getOrElse(this.gradleProject.getLayout().getProjectDirectory()
        .dir("build")
        .dir("classes")
        .dir("java")
        .dir("main")
        .dir("META-INF")
        .dir("fabric8")
        .getAsFile());
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public boolean isParallel() {
    return parallel;
  }

  public void setParallel(boolean parallel) {
    this.parallel = parallel;
  }

  public boolean isImplicitPreserveUnknownFields() {
    return implicitPreserveUnknownFields;
  }

  public void setImplicitPreserveUnknownFields(boolean implicitPreserveUnknownFields) {
    this.implicitPreserveUnknownFields = implicitPreserveUnknownFields;
  }

  public boolean isSkip() {
    return skip;
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

  public FilterSet getExclusions() {
    return exclusions;
  }

  public void setExclusions(FilterSet exclusions) {
    this.exclusions = exclusions;
  }

  public FilterSet getInclusions() {
    return inclusions;
  }

  public void setInclusions(FilterSet inclusions) {
    this.inclusions = inclusions;
  }

  public List<Dependency> getDependenciesToScan() {
    return dependenciesToScan;
  }

  public void setDependenciesToScan(
    List<Dependency> dependenciesToScan) {
    this.dependenciesToScan = dependenciesToScan;
  }
}
