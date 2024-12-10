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
package io.fabric8.crd.generator.gradle.plugin.task;

import io.fabric8.crd.generator.collector.CustomResourceCollector;
import io.fabric8.crd.generator.gradle.plugin.CrdGeneratorPluginExtension;

import io.fabric8.crdv2.generator.CRDGenerationInfo;
import io.fabric8.crdv2.generator.CRDGenerator;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

public class CrdGeneratorJava2CrdTask extends DefaultTask {

  private final CrdGeneratorPluginExtension extension;

  public static final String NAME = "java2crd";

  @Inject
  public CrdGeneratorJava2CrdTask(Class<? extends CrdGeneratorPluginExtension> extensionClass) {
    this.extension = getProject().getExtensions().getByType(extensionClass);
    setDescription("Generate CRDs from Java model.");
  }

  @TaskAction
  public void runTask() {
    File outputDirectory = extension.getOutputDirectoryOrDefault();

    if (extension.isSkip()) {
      //getLog().info("CRD-Generator execution skipped");
      return;
    }

    getLogger().info("parallel: {}", extension.isParallel());

    List<File> filesToIndex = new LinkedList<>();
    if (extension.getClassesToIndexOrDefault().exists()) {
      filesToIndex.add(extension.getClassesToIndexOrDefault());
    }
    //filesToIndex.addAll(getDependencyArchives());

    CustomResourceCollector customResourceCollector = new CustomResourceCollector()
        .withParentClassLoader(Thread.currentThread().getContextClassLoader())
        .withClasspathElements(extension.getClasspathElements())
        .withFilesToScan(filesToIndex)
        .withForceIndex(extension.isForceIndex())
        .withIncludePackages(extension.getInclusions().getPackages())
        .withExcludePackages(extension.getExclusions().getPackages())
        .withCustomResourceClasses(extension.getCustomResourceClassNamesOrDefault());

    List<Class<? extends HasMetadata>> customResourceClasses = customResourceCollector.findCustomResourceClasses();

    try {
      Files.createDirectories(outputDirectory.toPath());
    } catch (IOException e) {
      //throw new MojoExecutionException("Could not create output directory: " + e.getMessage());
    }

    CRDGenerator crdGenerator = new CRDGenerator()
        .customResourceClasses(customResourceClasses)
        .withParallelGenerationEnabled(extension.isParallel())
        .withImplicitPreserveUnknownFields(extension.isImplicitPreserveUnknownFields())
        .inOutputDir(outputDirectory);

    CRDGenerationInfo crdGenerationInfo = crdGenerator.detailedGenerate();
    crdGenerationInfo.getCRDDetailsPerNameAndVersion().forEach((crdName, versionToInfo) -> {
      getLogger().info("Generated CRD {}:", crdName);
      versionToInfo.forEach(
          (version, info) -> getLogger().info(" {} -> {}", version, info.getFilePath()));
    });
  }

  private void log() {
  }
}
