# asteria-docs

This plugin enables publishing JVM artifacts based on the [Maven Publish](https://docs.gradle.org/current/userguide/publishing_maven.html) plugin provided by Gradle. The bottom line is that it preconfigures the repository and how the credentials for this repository are retrieved.

Furthermore, the plugin looks for other Asteria plugins and hooks itself in the required lifecycle for correct execution.

As a repository AWS S3 is used and preconfigured, which requires that AWS credentials are available. The credentials can be either provided via the AWS configuration file in your home directory or via environment variables.

## Configuration

This is the opinionated approach for configuring a Gradle multi-project.

*build.gradle*
```groovy
buildscript {
    dependencies {
        classpath "one.leftshift.asteria:asteria-docs:+"
    }
}

subprojects {
    apply plugin: "java"
    apply plugin: "one.leftshift.asteria-docs"
}
```

## Usage

The usage is rather simple in order to publish artifacts: `./gradlew docs`