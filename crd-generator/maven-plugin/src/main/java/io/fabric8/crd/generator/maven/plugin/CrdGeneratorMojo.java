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
package io.fabric8.crd.generator.maven.plugin;

import io.fabric8.crd.generator.collector.CustomResourceCollector;
import io.fabric8.crdv2.generator.CRDGenerationInfo;
import io.fabric8.crdv2.generator.CRDGenerator;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = true)
public class CrdGeneratorMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject mavenProject;

  /**
   * The input directory to be used to scan for Custom Resource classes
   */
  @Parameter(property = "fabric8.crd-generator.classesToIndex", defaultValue = "${project.build.outputDirectory}", readonly = true)
  File classesToIndex;

  /**
   * Custom Resource classes, which should be considered to generate the CRDs.
   * If set, scanning is disabled.
   */
  @Parameter(property = "fabric8.crd-generator.customResourceClasses")
  private List<String> customResourceClassNames = new LinkedList<>();

  /**
   * Dependencies which should be scanned for Custom Resources.
   */
  @Parameter(property = "fabric8.crd-generator.dependenciesToIndex")
  List<Dependency> dependenciesToIndex = new LinkedList<>();

  /**
   * Inclusions, used to filter Custom Resource classes after scanning.
   */
  @Parameter(property = "fabric8.crd-generator.inclusions")
  private FilterSet inclusions = new FilterSet();

  /**
   * Exclusions, used to filter Custom Resource classes after scanning.
   */
  @Parameter(property = "fabric8.crd-generator.exclusions")
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
  @Parameter(property = "fabric8.crd-generator.classpath", defaultValue = "WITH_RUNTIME_DEPENDENCIES")
  ClasspathType classpath;

  /**
   * The output directory where the CRDs are emitted.
   */
  @Parameter(property = "fabric8.crd-generator.outputDirectory", defaultValue = "${project.build.outputDirectory}/META-INF/fabric8/")
  File outputDirectory;

  /**
   * If true, a Jandex index will be created even if the directory or JAR file contains an existing index.
   */
  @Parameter(property = "fabric8.crd-generator.forceIndex", defaultValue = "false")
  private boolean forceIndex;

  /**
   * If enabled, the CRDs will be generated in parallel.
   */
  @Parameter(property = "fabric8.crd-generator.parallel", defaultValue = "true")
  private boolean parallel;

  /**
   * If enabled, all objects are implicitly marked with x-kubernetes-preserve-unknown-fields: true.
   */
  @Parameter(property = "fabric8.crd-generator.implicitPreserveUnknownFields", defaultValue = "false")
  private boolean implicitPreserveUnknownFields;

  /**
   * Skip execution if set.
   */
  @Parameter(property = "fabric8.crd-generator.skip", defaultValue = "false")
  private boolean skip;

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("CRD-Generator execution skipped");
      return;
    }

    List<File> filesToIndex = new LinkedList<>();
    if (classesToIndex.exists()) {
      filesToIndex.add(classesToIndex);
    }
    filesToIndex.addAll(getDependencyArchives());

    CustomResourceCollector customResourceCollector = new CustomResourceCollector()
        .withParentClassLoader(Thread.currentThread().getContextClassLoader())
        .withClasspathElements(classpath.getClasspathElements(mavenProject))
        .withFilesToIndex(filesToIndex)
        .withForceIndex(forceIndex)
        .withIncludePackages(inclusions.getPackages())
        .withExcludePackages(exclusions.getPackages())
        .withCustomResourceClasses(customResourceClassNames);

    Class<? extends HasMetadata>[] customResourceClasses = customResourceCollector.findCustomResourceClasses();

    try {
      Files.createDirectories(outputDirectory.toPath());
    } catch (IOException e) {
      throw new MojoExecutionException("Could not create output directory: " + e.getMessage());
    }

    CRDGenerator crdGenerator = new CRDGenerator()
        .customResourceClasses(customResourceClasses)
        .withParallelGenerationEnabled(parallel)
        .withImplicitPreserveUnknownFields(implicitPreserveUnknownFields)
        .inOutputDir(outputDirectory);

    CRDGenerationInfo crdGenerationInfo = crdGenerator.detailedGenerate();
    crdGenerationInfo.getCRDDetailsPerNameAndVersion().forEach((crdName, versionToInfo) -> {
      getLog().info("Generated CRD " + crdName + ":");
      versionToInfo.forEach(
          (version, info) -> getLog().info(" " + version + " -> " + info.getFilePath()));
    });
  }

  /**
   * Returns a list of archive files derived from the given dependencies.
   *
   * @return the archive files
   */
  private List<File> getDependencyArchives() {
    return dependenciesToIndex.stream()
        .map(this::getDependencyArchive)
        .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
        .collect(Collectors.toList());
  }

  private Optional<File> getDependencyArchive(Dependency dependency) {
    for (Artifact artifact : mavenProject.getArtifacts()) {
      if (artifact.getGroupId().equals(dependency.getGroupId())
          && artifact.getArtifactId().equals(dependency.getArtifactId())
          && (dependency.getClassifier() == null || artifact.getClassifier()
              .equals(dependency.getClassifier()))) {
        File jarFile = artifact.getFile();
        if (jarFile == null) {
          getLog().warn(
              "Skip indexing dependency, artifact file does not exist for dependency: " + dependency);
          return Optional.empty();
        }

        return Optional.of(jarFile);
      }
    }
    getLog().warn("Skip indexing dependency, artifact for dependency not found: " + dependency);
    return Optional.empty();
  }

}
