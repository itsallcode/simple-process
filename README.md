# simple-process

Wrapper to simplify working with external processes.

**This project is at an early development stage and the API will change without backwards compatibility.**

[![Java CI](https://github.com/itsallcode/simple-process/actions/workflows/build.yml/badge.svg)](https://github.com/itsallcode/simple-process/actions/workflows/build.yml)
[![CodeQL](https://github.com/itsallcode/simple-process/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/itsallcode/simple-process/actions/workflows/codeql-analysis.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Asimple-process&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=org.itsallcode%3Asimple-process)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Asimple-process&metric=coverage)](https://sonarcloud.io/summary/new_code?id=org.itsallcode%3Asimple-process)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Asimple-process&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=org.itsallcode%3Asimple-process)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Asimple-process&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=org.itsallcode%3Asimple-process)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Asimple-process&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=org.itsallcode%3Asimple-process)
[![Maven Central](https://img.shields.io/maven-central/v/org.itsallcode/simple-process)](https://search.maven.org/artifact/org.itsallcode/simple-process)

* [Changelog](CHANGELOG.md)
* [API JavaDoc](https://blog.itsallcode.org/simple-process/javadoc/org.itsallcode.process/module-summary.html)
* [Test report](https://blog.itsallcode.org/simple-process/reports/tests/test/index.html)
* [Coverage report](https://blog.itsallcode.org/simple-process/reports/jacoco/test/html/index.html)

## Usage

This project requires Java 17 or later.

### Add Dependency

Add dependency to your Gradle project:

```groovy
dependencies {
    implementation 'org.itsallcode:simple-process:0.3.1'
}
```

Add dependency to your Maven project:

```xml
<dependency>
  <groupId>org.itsallcode</groupId>
  <artifactId>simple-process</artifactId>
  <version>0.3.1</version>
</dependency>
```

### Features

Simplified API for starting external processes and executable JARs. Allows easy capturing stdout and stderr and forwarding to log output.

## Development

### Check if dependencies are up-to-date

```sh
./gradlew dependencyUpdates
```

### Building

Install to local maven repository:

```sh
./gradlew publishToMavenLocal
```

### Test Coverage

To calculate and view test coverage:

```sh
./gradlew check jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### View Generated Javadoc

```sh
./gradlew javadoc
open build/docs/javadoc/index.html
```

### Publish to Maven Central

#### Preparations

1. Checkout the `main` branch, create a new branch.
2. Update version number in `build.gradle` and `README.md`.
3. Add changes in new version to `CHANGELOG.md`.
4. Commit and push changes.
5. Create a new pull request, have it reviewed and merged to `main`.

#### Perform the Release

1. Start the release workflow
  * Run command `gh workflow run release.yml --repo itsallcode/simple-process --ref main`
  * or go to [GitHub Actions](https://github.com/itsallcode/simple-process/actions/workflows/release.yml) and start the `release.yml` workflow on branch `main`.
2. Update title and description of the newly created [GitHub release](https://github.com/itsallcode/simple-process/releases).
3. After some time the release will be available at [Maven Central](https://repo1.maven.org/maven2/org/itsallcode/simple-process/).
