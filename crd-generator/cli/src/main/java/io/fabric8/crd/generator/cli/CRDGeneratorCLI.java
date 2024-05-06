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
import org.slf4j.simple.SimpleLogger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CRD-Generator Command Line Interface.
 */
@CommandLine.Command(name = "crd-gen", mixinStandardHelpOptions = true, helpCommand = true, versionProvider = KubernetesClientVersionProvider.class, sortOptions = false, description = "\nFabric8 CRD-Generator:\nGenerate Custom Resource Definitions (CRD) for Kubernetes from Java classes.\n")
public class CRDGeneratorCLI implements Runnable {

  private static final CRDGenerationInfo EMPTY_INFO = new CRDGenerationInfo();

  private Logger log;

  @CommandLine.Spec
  CommandLine.Model.CommandSpec spec;

  @CommandLine.Parameters(arity = "1..*", converter = SourceTypeConverter.class, description = "A directory or JAR file to scan for Custom Resource classes, or a full qualified Custom Resource class name.")
  List<Source> source;

  @CommandLine.Option(names = {
      "--classpath" }, description = "Additional classpath elements, e.g. a dependency packaged as JAR file or a directory of class files.")
  List<String> classpathElements = new ArrayList<>();

  @CommandLine.Option(names = { "-o",
      "--output-dir" }, description = "The output directory for the generated CRDs.", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
  File outputDirectory = new File(".");

  @CommandLine.Option(names = {
      "--force-index" }, description = "Create Jandex index even if the directory or JAR file contains an existing index.", defaultValue = "false")
  Boolean forceIndex;

  @CommandLine.Option(names = { "--no-parallel" }, description = "Disable parallel generation.", defaultValue = "false")
  Boolean parallelDisabled;

  @CommandLine.Option(names = {
      "--implicit-preserve-unknown-fields" }, description = "Mark all objects with x-kubernetes-preserve-unknown-fields: true.", defaultValue = "false")
  Boolean implicitPreserveUnknownFields;

  @CommandLine.Option(names = {
      "--include-package" }, description = "Use only Custom Resource classes of one or more packages.")
  List<String> includedPackages = new LinkedList<>();
  @CommandLine.Option(names = { "--include-group" }, description = "Use only Custom Resources classes of one or more groups.")
  List<String> includedGroups = new LinkedList<>();
  @CommandLine.Option(names = {
      "--include-version" }, description = "Use only Custom Resource classes of one or more versions.")
  List<String> includedVersions = new LinkedList<>();
  @CommandLine.Option(names = { "--exclude-package" }, description = "Exclude Custom Resource classes by package.")
  List<String> excludedPackages = new LinkedList<>();
  @CommandLine.Option(names = { "--exclude-group" }, description = "Exclude Custom Resource classes by group.")
  List<String> excludedGroups = new LinkedList<>();
  @CommandLine.Option(names = { "--exclude-version" }, description = "Exclude Custom Resource classes by version.")
  List<String> excludedVersions = new LinkedList<>();

  @CommandLine.Option(names = {
      "-v" }, description = "Verbose mode. Helpful for troubleshooting. Multiple -v options increase the verbosity.")
  List<Boolean> verbose = new LinkedList<>();

  private CRDGenerationInfo crdGenerationInfo = EMPTY_INFO;

  @Override
  public void run() {
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, getLogLevel());
    log = LoggerFactory.getLogger(CRDGeneratorCLI.class);

    List<String> customResourceClasses = source.stream()
        .filter(s -> s.customResourceClass != null)
        .map(s -> s.customResourceClass)
        .collect(Collectors.toList());

    List<File> filesToIndex = source.stream()
        .filter(s -> s.fileToIndex != null)
        .map(s -> s.fileToIndex)
        .collect(Collectors.toList());

    List<String> allClasspathElements = new LinkedList<>(this.classpathElements);
    // add all files which are considered to index automatically to classpath elements
    filesToIndex.stream()
        .map(File::toString)
        .forEach(allClasspathElements::add);

    CustomResourceCollector customResourceCollector = new CustomResourceCollector()
        .withClasspaths(allClasspathElements)
        .withFilesToIndex(filesToIndex)
        .withForceIndex(forceIndex)
        .withCustomResourceClasses(customResourceClasses)
        .withIncludePackages(includedPackages)
        .withIncludeGroups(includedGroups)
        .withIncludeVersions(includedVersions)
        .withExcludePackages(excludedPackages)
        .withExcludeGroups(excludedGroups)
        .withExcludeVersions(excludedVersions);

    CustomResourceInfo[] customResourceInfos = customResourceCollector.findCustomResources();

    log.info("Found {} CustomResources", customResourceInfos.length);

    File sanitizedOutputDirectory;
    try {
      sanitizedOutputDirectory = outputDirectory.getCanonicalFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      Files.createDirectories(sanitizedOutputDirectory.toPath());
    } catch (IOException e) {
      throw new RuntimeException("Could not create output directory: " + e.getMessage());
    }

    CRDGenerator crdGenerator = new CRDGenerator()
        .customResources(customResourceInfos)
        .withParallelGenerationEnabled(!parallelDisabled)
        .withImplicitPreserveUnknownFields(implicitPreserveUnknownFields)
        .inOutputDir(sanitizedOutputDirectory);

    crdGenerationInfo = crdGenerator.detailedGenerate();
    crdGenerationInfo.getCRDDetailsPerNameAndVersion().forEach((crdName, versionToInfo) -> {
      System.out.printf("Generated CRD %s:%n", crdName);
      versionToInfo.forEach(
          (version, info) -> System.out.printf(" %s -> %s%n", version, info.getFilePath()));
    });
  }

  public CRDGenerationInfo getCrdGenerationInfo() {
    return crdGenerationInfo;
  }

  private String getLogLevel() {
    switch (verbose.size()) {
      case 1:
        return "info";
      case 2:
        return "debug";
      case 3:
        return "trace";
      default:
        return "warn";
    }
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new CRDGeneratorCLI()).execute(args);
    System.exit(exitCode);
  }


  /**
   * Wraps a positional argument.
   */
  private static class Source {
    File fileToIndex;
    String customResourceClass;

    static Source from(String value) {
      Source source = new Source();
      File f = new File(value);
      if (f.exists()) {
        try {
          source.fileToIndex = f.getCanonicalFile();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        source.customResourceClass = value;
      }
      return source;
    }
  }

  private static class SourceTypeConverter implements CommandLine.ITypeConverter<Source> {
    @Override
    public Source convert(String value) {
      return Source.from(value);
    }
  }
}
