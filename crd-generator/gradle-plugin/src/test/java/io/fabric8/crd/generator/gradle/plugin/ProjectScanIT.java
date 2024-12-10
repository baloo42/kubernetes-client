package io.fabric8.crd.generator.gradle.plugin;


import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

class ProjectScanIT {

  @RegisterExtension
  public final ITGradleRunnerExtension gradleRunner = new ITGradleRunnerExtension();

  @Test
  void test() throws IOException {
    // When
    final BuildResult result = gradleRunner.withITProject("project-scan")
      .withArguments("clean", "build")
      .build();

    // Then
    List<String> expectedCustomResources =
      Collections.singletonList("multiples.sample.fabric8.io-v1");

    for (String expectedCustomResource : expectedCustomResources) {
      Verify.verifyContentEquals(
        gradleRunner.resolveFile("expected", expectedCustomResource + ".yml"),
        gradleRunner.resolveCustomResourceFile(expectedCustomResource + ".yml")
      );
    }
  }

}
