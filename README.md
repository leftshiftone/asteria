[![CircleCI branch](https://img.shields.io/circleci/project/github/leftshiftone/asteria/master.svg?style=flat-square)](https://circleci.com/gh/leftshiftone/asteria)
[![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/leftshiftone/asteria.svg?style=flat-square)](https://github.com/leftshiftone/asteria/tags)
[![Maven Central](https://img.shields.io/maven-central/v/one.leftshift.asteria/asteria-version?style=flat-square)](https://mvnrepository.com/artifact/one.leftshift.asteria/asteria-version)

# Asteria

Further information can be found in the sub projects.

## Development

### Release
Releases are triggered locally. Just a tag will be pushed and CI pipelines take care of the rest.

#### Major
Run `./gradlew final -Prelease.scope=major` locally.

#### Minor
Run `./gradlew final -Prelease.scope=minor` locally.

#### Patch
Must be executed from a release branch like `release/3.0.x`.
Run `./gradlew final -Prelease.scope=patch` locally to create a patch.
