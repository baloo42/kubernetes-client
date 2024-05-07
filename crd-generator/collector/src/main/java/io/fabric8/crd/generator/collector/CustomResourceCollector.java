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
package io.fabric8.crd.generator.collector;

import io.fabric8.crdv2.generator.CustomResourceInfo;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.jboss.jandex.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Collects Custom Resource class files from various places and loads them by using
 * {@link CustomResourceCollector#findCustomResources()}.
 */
public class CustomResourceCollector {

  private static final Logger log = LoggerFactory.getLogger(CustomResourceCollector.class);

  private final CustomResourceClassLoader customResourceClassLoader = new CustomResourceClassLoader();
  private final CustomResourceJandexCollector jandexCollector = new CustomResourceJandexCollector();

  private final Set<String> customResourceClassNames = new HashSet<>();

  private final List<Predicate<String>> classNamePredicates = new LinkedList<>();
  private final List<Predicate<CustomResourceInfo>> customResourceInfoPredicates = new LinkedList<>();

  public CustomResourceCollector withParentClassLoader(ClassLoader classLoader) {
    this.customResourceClassLoader.withParentClassLoader(classLoader);
    return this;
  }

  public CustomResourceCollector withClasspathElement(String... classpathElement) {
    this.customResourceClassLoader.withClasspathElement(classpathElement);
    return this;
  }

  public CustomResourceCollector withClasspathElements(Collection<String> classpathElements) {
    this.customResourceClassLoader.withClasspathElements(classpathElements);
    return this;
  }

  public CustomResourceCollector withCustomResourceClass(String... className) {
    if (className != null) {
      withCustomResourceClasses(Arrays.asList(className));
    }
    return this;
  }

  public CustomResourceCollector withCustomResourceClasses(Collection<String> classNames) {
    if (classNames != null) {
      classNames.stream()
          .filter(Objects::nonNull)
          .forEach(this.customResourceClassNames::add);
    }
    return this;
  }

  public CustomResourceCollector withIndex(IndexView... index) {
    jandexCollector.withIndex(index);
    return this;
  }

  public CustomResourceCollector withIndices(Collection<IndexView> indices) {
    jandexCollector.withIndices(indices);
    return this;
  }

  public CustomResourceCollector withFileToIndex(File... files) {
    jandexCollector.withFileToIndex(files);
    return this;
  }

  public CustomResourceCollector withFilesToIndex(Collection<File> files) {
    jandexCollector.withFilesToIndex(files);
    return this;
  }

  public CustomResourceCollector withForceIndex(boolean forceIndex) {
    jandexCollector.withForceIndex(forceIndex);
    return this;
  }

  public CustomResourceCollector withIncludePackages(Collection<String> packages) {
    if (packages != null) {
      packages.stream()
          .filter(Objects::nonNull)
          .map(pkg -> (Predicate<String>) s -> s.startsWith(pkg))
          .reduce(Predicate::or)
          .ifPresent(this.classNamePredicates::add);
    }
    return this;
  }

  public CustomResourceCollector withExcludePackages(Collection<String> packages) {
    if (packages != null) {
      packages.stream()
          .filter(Objects::nonNull)
          .map(pkg -> (Predicate<String>) s -> !s.startsWith(pkg))
          .reduce(Predicate::or)
          .ifPresent(this.classNamePredicates::add);
    }
    return this;
  }

  public CustomResourceCollector withIncludeGroups(Collection<String> groups) {
    if (groups != null) {
      groups.stream()
          .filter(Objects::nonNull)
          .map(group -> (Predicate<CustomResourceInfo>) cr -> group.equals(cr.group()))
          .reduce(Predicate::or)
          .ifPresent(customResourceInfoPredicates::add);
    }
    return this;
  }

  public CustomResourceCollector withExcludeGroups(Collection<String> groups) {
    if (groups != null) {
      groups.stream()
          .filter(Objects::nonNull)
          .map(groupToFilter -> (Predicate<CustomResourceInfo>) cr -> !groupToFilter.equals(cr.group()))
          .reduce(Predicate::or)
          .ifPresent(customResourceInfoPredicates::add);
    }
    return this;
  }

  public CustomResourceCollector withIncludeVersions(Collection<String> versions) {
    if (versions != null) {
      versions.stream()
          .filter(Objects::nonNull)
          .map(versionToFilter -> (Predicate<CustomResourceInfo>) cr -> versionToFilter.equals(cr.version()))
          .reduce(Predicate::or)
          .ifPresent(customResourceInfoPredicates::add);
    }
    return this;
  }

  public CustomResourceCollector withExcludeVersions(Collection<String> versions) {
    if (versions != null) {
      versions.stream()
          .filter(Objects::nonNull)
          .map(versionToFilter -> (Predicate<CustomResourceInfo>) cr -> !versionToFilter.equals(cr.version()))
          .reduce(Predicate::or)
          .ifPresent(customResourceInfoPredicates::add);
    }
    return this;
  }

  public CustomResourceInfo[] findCustomResources() {
    Set<String> customResourcesClassNames = new HashSet<>(customResourceClassNames);

    // use indices only if custom resource class names are not explicitly given
    if (customResourcesClassNames.isEmpty()) {
      customResourcesClassNames.addAll(jandexCollector.findCustomResourceClasses());
      log.debug("Found {} custom resource classes before filtering", customResourcesClassNames.size());
    } else {
      log.debug("Using explicit {} custom resource classes and skip scanning", customResourcesClassNames);
    }

    Predicate<String> classNamePredicate = classNamePredicates.stream()
        .reduce(Predicate::and)
        .orElse(className -> true);

    Predicate<CustomResourceInfo> customResourceInfoPredicate = customResourceInfoPredicates.stream()
        .reduce(Predicate::and)
        .orElse(info -> true);

    CustomResourceInfo[] infos = customResourcesClassNames.stream()
        .filter(classNamePredicate)
        .map(customResourceClassLoader::loadCustomResourceClass)
        .map(this::createCustomResourceInfo)
        .filter(customResourceInfoPredicate)
        .toArray(CustomResourceInfo[]::new);

    log.debug("Found {} custom resource classes after filtering", infos.length);
    return infos;
  }

  private CustomResourceInfo createCustomResourceInfo(Class<? extends HasMetadata> customResourceClass) {
    return CustomResourceInfo.fromClass(customResourceClass);
  }

}
