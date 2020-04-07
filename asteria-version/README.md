# asteria-version

This plugin enables versioning based on the [Nebula Release](https://github.com/nebula-plugins/nebula-release-plugin) plugin provided by Netflix. It simply configures the Maven Snapshot style strategy.

Furthermore, the plugin looks for other Asteria plugins and hooks itself in the required lifecycle for correct execution (in this case Nebula recognizes the Maven Publish plugin).

## Configuration

*build.gradle*
```groovy
buildscript {
    dependencies {
        classpath "one.leftshift.asteria:asteria-version:+"
    }
}

allprojects {
    apply plugin: "one.leftshift.asteria-version"
}
```

## Usage

The plugin will be executed on every build to determine the version. Additionally a properties file `build.properties` is created in order to provide the version and build properties at runtime. Just read the `build.properties` from classpath.

### Release

`/gradlew snapshot` Release a snapshot version <br/>
`/gradlew devSnapshot` Release a dev snapshot version <br/>
`/gradlew candidate` Release a candidate version 

`./gradlew final -Prelease.scope=major` Release next major version<br/>
`./gradlew final` Release next major version <br/>
`./gradlew final -Prelease.scope=patch` Release next patch version <br/>
`./gradlew final -Prelease.useLastTag=true` Release from last tag 
