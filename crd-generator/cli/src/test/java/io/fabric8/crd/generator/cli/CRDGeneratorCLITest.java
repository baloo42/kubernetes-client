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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CRDGeneratorCLITest {

  @Test
  public void givenNoInput_thenGenerateNoCRDs() {
    CRDGeneratorCLI cliApp = new CRDGeneratorCLI();
    CommandLine cmd = new CommandLine(cliApp);
    int exitCode = cmd.execute();
    assertEquals(0, exitCode);
    assertEquals(0, cliApp.getCrdGenerationInfo().numberOfGeneratedCRDs());
  }

  @Test
  public void givenSingleCRClassNameFromSameClasspath_thenGenerate(@TempDir Path tempDir) {
    CRDGeneratorCLI cliApp = new CRDGeneratorCLI();
    CommandLine cmd = new CommandLine(cliApp);
    String[] args = new String[] {
        "-cr", "io.fabric8.crd.generator.cli.examples.basic.Basic",
        "-o", tempDir.toString()
    };
    int exitCode = cmd.execute(args);
    assertEquals(0, exitCode);
    assertEquals(1, cliApp.getCrdGenerationInfo().numberOfGeneratedCRDs());
    assertTrue(Paths.get(tempDir.toString(), "basics.sample.fabric8.io-v1.yml").toFile().exists());
  }

  @Test
  public void givenSingleCRClassNameFromExternalClasspath_thenGenerate(@TempDir Path tempDir) {
    CRDGeneratorCLI cliApp = new CRDGeneratorCLI();
    CommandLine cmd = new CommandLine(cliApp);
    String[] args = new String[] {
        "-cp", "../api-v2/target/test-classes/",
        "-cr", "io.fabric8.crdv2.example.basic.Basic",
        "-o", tempDir.toString()
    };

    int exitCode = cmd.execute(args);
    assertEquals(0, exitCode);
    assertEquals(1, cliApp.getCrdGenerationInfo().numberOfGeneratedCRDs());
    assertTrue(Paths.get(tempDir.toString(), "basics.sample.fabric8.io-v1.yml").toFile().exists());
  }

  @Test
  public void givenClassesDirectory_thenScanAndGenerate(@TempDir Path tempDir) {
    CRDGeneratorCLI cliApp = new CRDGeneratorCLI();
    CommandLine cmd = new CommandLine(cliApp);
    String[] args = new String[] {
        "-c", "target/test-classes/",
        //"-k", "basics.sample.fabric8.io",
        "-o", tempDir.toString()
    };

    int exitCode = cmd.execute(args);
    assertEquals(0, exitCode);
    assertEquals(1, cliApp.getCrdGenerationInfo().numberOfGeneratedCRDs());
    assertTrue(Paths.get(tempDir.toString(), "basics.sample.fabric8.io-v1.yml").toFile().exists());
  }
}
