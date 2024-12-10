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
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum ClasspathType {
  /**
   * Only classes in the project.
   */
  PROJECT_ONLY,
  /**
   * Classes from the project and any runtime dependencies.
   */
  WITH_RUNTIME_DEPENDENCIES,
  /**
   * Classes from the project and any compile time dependencies.
   */
  WITH_COMPILE_DEPENDENCIES,
  /**
   * Classes from the project, compile time and runtime dependencies.
   */
  WITH_ALL_DEPENDENCIES,
  /**
   * Classes from the project (including tests), compile time, runtime and test dependencies.
   */
  WITH_ALL_DEPENDENCIES_AND_TESTS;

  public Set<String> getClasspathElements(Project project) {
    Set<String> classpathElements = new HashSet<>();
    switch (this) {
      case PROJECT_ONLY:
        project.getExtensions().getByType(SourceSetContainer.class)
            .forEach(sourceSet -> sourceSet.getOutput().getClassesDirs()
                .forEach(file -> classpathElements.add(file.toString())));
        break;
      case WITH_COMPILE_DEPENDENCIES:
        classpathElements.addAll(getCompileClasspathElements(project));
        break;
      case WITH_RUNTIME_DEPENDENCIES:
        classpathElements.addAll(getRuntimeClasspathElements(project));
        break;
      case WITH_ALL_DEPENDENCIES:
         classpathElements.addAll(getRuntimeClasspathElements(project));
         classpathElements.addAll(getCompileClasspathElements(project));
        break;
      case WITH_ALL_DEPENDENCIES_AND_TESTS:
        classpathElements.addAll(getRuntimeClasspathElements(project));
        classpathElements.addAll(getCompileClasspathElements(project));
        // TODO
        break;
    }
    return classpathElements;
  }

  private List<String> getCompileClasspathElements(Project project){
    return project.getConfigurations().getByName("compileClasspath").getFiles().stream()
      .map(File::toString)
      .collect(Collectors.toList());
  }

  private List<String> getRuntimeClasspathElements(Project project) {
    return project.getConfigurations().getByName("runtimeClasspath").getFiles().stream()
      .map(File::toString)
      .collect(Collectors.toList());
  }

}
