# CRD-Generator CLI

Generate CRDs from Java model.

## Install

The CRD-Generator CLI is available for download on Sonatype at the link:

```
https://oss.sonatype.org/content/repositories/releases/io/fabric8/crd-generator-cli/<version>/crd-generator-cli-<version>.sh
```

you can get it working locally with few lines:

```bash
export VERSION=$(wget -q -O - https://github.com/fabric8io/kubernetes-client/releases/latest --header "Accept: application/json" | jq -r '.tag_name' | cut -c 2-)
wget https://oss.sonatype.org/content/repositories/releases/io/fabric8/crd-generator-cli/$VERSION/crd-generator-cli-$VERSION.sh
chmod a+x crd-generator-cli-$VERSION.sh
./crd-generator-cli-$VERSION.sh --version
```

Alternatively, if you already have [jbang](https://www.jbang.dev/) installed, you can run the CLI by using the following command:

```bash
jbang io.fabric8:crd-generator-cli:<version>
```

## Usage

```
Usage: crd-gen [-hVv] [--force-index] [--force-scan]
               [--implicit-preserve-unknown-fields] [--no-parallel]
               [-o=<outputDirectory>] [--classpath=<classpathElements>]...
               [--exclude-package=<excludedPackages>]...
               [--include-package=<includedPackages>]... <source>...

Fabric8 CRD-Generator:
Generate Custom Resource Definitions (CRD) for Kubernetes from Java model.

      <source>...     A directory or JAR file to scan for Custom Resource
                        classes, or a full qualified Custom Resource class name.
      --classpath=<classpathElements>
                      Additional classpath elements, e.g. a dependency packaged
                        as JAR file or a directory of class files.
  -o, --output-dir=<outputDirectory>
                      The output directory for the generated CRDs.
                        Default: .
      --force-index   Create Jandex index even if the directory or JAR file
                        contains an existing index.
      --force-scan    Create Jandex index even if the directory or JAR file
                        contains an existing index.
      --no-parallel   Disable parallel generation.
      --implicit-preserve-unknown-fields
                      `x-kubernetes-preserve-unknown-fields: true` will be
                        added to objects which contain an any-setter or
                        any-getter
      --include-package=<includedPackages>
                      Use only Custom Resource classes of one or more packages.
      --exclude-package=<excludedPackages>
                      Exclude Custom Resource classes by package.
  -v                  Verbose mode. Helpful for troubleshooting. Multiple -v
                        options increase the verbosity.
  -h, --help          Show this help message and exit.
  -V, --version       Print version information and exit.
```
