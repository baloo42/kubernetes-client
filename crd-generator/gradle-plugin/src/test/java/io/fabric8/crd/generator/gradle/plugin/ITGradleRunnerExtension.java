package io.fabric8.crd.generator.gradle.plugin;

import io.fabric8.kubernetes.client.Version;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ITGradleRunnerExtension implements BeforeEachCallback, AfterEachCallback {

  private static final String DOWNLOAD_URL = "https://services.gradle.org/distributions/gradle-%s-bin.zip";

  private static String getDownloadUrl(String version){
    return String.format(DOWNLOAD_URL, version);
  }

  private GradleRunner gradleRunner;

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    gradleRunner = GradleRunner.create()
        .withGradleDistribution(new URI(getDownloadUrl(System.getProperty("gradle.version", "8.2.1"))))
        .withDebug(true)
        .withPluginClasspath(Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
            .map(File::new).collect(Collectors.toList()));
  }

  @Override
  public void afterEach(ExtensionContext context) {
    gradleRunner = null;
  }

  public ITGradleRunnerExtension withITProject(String name) {
    gradleRunner = gradleRunner
        .withProjectDir(new File("src/it", name));
    return this;
  }

  public ITGradleRunnerExtension withArguments(String... originalArguments) {
    final String[] arguments = new String[originalArguments.length + 2];
    arguments[0] = "-Pfabric8ClientVersion=" + System.getProperty("fabric8-client.version", Version.clientVersion());
    arguments[1] = "--console=plain";
    System.arraycopy(originalArguments, 0, arguments, 2, originalArguments.length);
    gradleRunner = gradleRunner.withArguments(arguments);
    return this;
  }

  public List<? extends File> pluginClassPath() {
    return gradleRunner.getPluginClasspath();
  }

  public ITGradleRunnerExtension withPluginClassPath(Iterable<? extends File> pluginClassPath) {
    gradleRunner = gradleRunner.withPluginClasspath(pluginClassPath);
    return this;
  }

  public File resolveFile(String... relativePaths) {
    Path path = gradleRunner.getProjectDir().toPath();
    for (String rp : relativePaths) {
      path = path.resolve(rp);
    }
    return path.toFile();
  }

  public File resolveCustomResourceFile(String file) {
    return resolveFile("build", "classes", "java", "main", "META-INF", "fabric8", file);
  }

  public BuildResult build() {
    return gradleRunner.build();
  }
}
