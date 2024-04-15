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
package io.fabric8.crd.generator.apt.multiple;

import io.fabric8.crd.generator.apt.AbstractCRDGeneratorCompilerTest;

public class MultipleVersionsCRDTest extends AbstractCRDGeneratorCompilerTest {

  @Override
  protected String[] getJavaSources() {
    return new String[] {
        "v1/Multiple.java",
        "v2/Multiple.java"
    };
  }

  @Override
  protected String[] getExpectedCRDs() {
    return new String[] {
        "multiples.sample.fabric8.io-v1.yml",
        "multiples.sample.fabric8.io-v1beta1.yml"
    };
  }
}