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
package io.fabric8.crd.generator.cli;

import io.fabric8.crd.generator.collector.CustomResourceCollector;
import io.fabric8.crdv2.generator.CRDGenerationInfo;
import io.fabric8.crdv2.generator.CRDGenerator;
import io.fabric8.crdv2.generator.CustomResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "crd-gen", mixinStandardHelpOptions = true, helpCommand = true, versionProvider = KubernetesClientVersionProvider.class)
public class CRDGeneratorCLI implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(CustomResourceCollector.class);
  private static final CRDGenerationInfo EMPTY_INFO = new CRDGenerationInfo();

  @CommandLine.Spec
  CommandLine.Model.CommandSpec spec;

  @CommandLine.Option(names = { "-c",
      "--classes" }, description = "Directories or JAR files to be used to scan for Custom Resource classes")
  List<File> classesToIndex;

  @CommandLine.Option(names = { "-cr",
      "--custom-resource" }, description = "Custom Resource classes, which should be considered to generate the CRDs. If set, scanning is disabled.")
  List<String> customResourceClasses;

  @CommandLine.Option(names = { "-cp", "--classpath" }, description = "The classpath which be used during the CRD generation")
  List<String> classpaths = new ArrayList<>();

  @CommandLine.Option(names = { "-o", "--output-dir" }, description = "The output directory for the generated CRDs")
  File outputDirectory = new File(".");

  @CommandLine.Option(names = { "-f",
      "--force-index" }, description = "If true, a Jandex index will be created even if the directory or JAR file contains an existing index.", defaultValue = "false")
  Boolean forceIndex;

  @CommandLine.Option(names = { "-p", "--parallel" }, description = "Enable parallel generation", defaultValue = "true")
  Boolean parallel;

  @CommandLine.Option(names = {
      "--implicit-preserve-unknown-fields" }, description = "If enabled, all objects are implicitly marked with x-kubernetes-preserve-unknown-fields: true.", defaultValue = "false")
  Boolean implicitPreserveUnknownFields;

  private CRDGenerationInfo crdGenerationInfo = EMPTY_INFO;

  @Override
  public void run() {
    CustomResourceCollector customResourceCollector = new CustomResourceCollector()
        .withParentClassLoader(Thread.currentThread().getContextClassLoader())
        .withClasspaths(classpaths)
        .withFilesToIndex(classesToIndex)
        .withForceIndex(forceIndex)
        // TODO: allow to exclude / include
        /*
         * .withIncludePackages(inclusions.getPackages())
         * .withIncludeGroups(inclusions.getGroups())
         * .withIncludeVersions(inclusions.getVersions())
         * .withExcludePackages(exclusions.getPackages())
         * .withExcludeGroups(exclusions.getGroups())
         * .withExcludeVersions(exclusions.getVersions())
         */
        .withCustomResourceClasses(customResourceClasses);

    CustomResourceInfo[] customResourceInfos = customResourceCollector.findCustomResources();

    log.info("Found {} CustomResources", customResourceInfos.length);

    try {
      Files.createDirectories(outputDirectory.toPath());
    } catch (IOException e) {
      throw new RuntimeException("Could not create output directory: " + e.getMessage());
    }

    CRDGenerator crdGenerator = new CRDGenerator()
        .customResources(customResourceInfos)
        .withParallelGenerationEnabled(parallel)
        .withImplicitPreserveUnknownFields(implicitPreserveUnknownFields)
        .inOutputDir(outputDirectory);

    crdGenerationInfo = crdGenerator.detailedGenerate();
    crdGenerationInfo.getCRDDetailsPerNameAndVersion().forEach((crdName, versionToInfo) -> {
      log.info("Generated CRD {}:", crdName);
      versionToInfo.forEach(
          (version, info) -> log.info(" {} -> {}", version, info.getFilePath()));
    });
  }

  public CRDGenerationInfo getCrdGenerationInfo() {
    return crdGenerationInfo;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new CRDGeneratorCLI()).execute(args);
    System.exit(exitCode);
  }
}
