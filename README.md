[![CircleCI branch](https://img.shields.io/circleci/project/github/leftshiftone/asteria/master.svg?style=flat-square)](https://circleci.com/gh/leftshiftone/asteria)
[![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/leftshiftone/asteria.svg?style=flat-square)](https://github.com/leftshiftone/asteria/tags)
[![Bintray](https://img.shields.io/badge/dynamic/json.svg?label=bintray&query=name&style=flat-square&url=https%3A%2F%2Fapi.bintray.com%2Fpackages%2Fleftshiftone%2Fasteria%2Fone.leftshift.asteria.asteria%2Fversions%2F_latest)](https://bintray.com/leftshiftone/asteria/one.leftshift.asteria.asteria/_latestVersion)

# Asteria

Available in [jcenter](https://bintray.com/leftshiftone/asteria/one.leftshift.asteria.asteria/_latestVersion). Can be included like `compile 'one.leftshift.asteria:asteria:3.0.0'`.

## Development

### Release
Releases are triggered locally. Just a tag will be pushed and CI pipelines take care of the rest.

#### Major
Run `./gradlew final -x bintrayUpload -Prelease.scope=major` locally.

#### Minor
Run `./gradlew final -x bintrayUpload -Prelease.scope=minor` locally.

#### Patch
Run `./gradlew final -x bintrayUpload -Prelease.scope=patch` locally.
