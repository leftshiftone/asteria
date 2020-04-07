# asteria-version

This plugin manages dependency management (version interception, dependency locking) based on the [Nebula Dependency Lock](https://github.com/nebula-plugins/gradle-dependency-lock-plugin) plugin provided by Netflix. It includes [Spring's Dependency Management](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-tools/spring-boot-gradle-plugin) and presets versions coming from the global pom.xml (deps repository).

Furthermore, the plugin introduces the `latest.smart` version which decides whether to use `latest.integration` or `latest.release` based on the current version being built.

## Configuration

*build.gradle*
```groovy
buildscript {
    dependencies {
        classpath "one.leftshift.asteria:asteria-dependency:+"
    }
}

allprojects {
    apply plugin: "one.leftshift.asteria-dependency"
}
```

Optionally dependency versions can be overridden
*build.gradle*
```groovy
subprojects {
    dependencyManagement {
        imports {
            mavenBom "one.leftshift:deps:0.4.0"
        }

        dependencies {
            dependency "one.leftshift.ekho:ekho-asr:0.2.0"
        }
    }
}
```

## Usage

The plugin will be executed on every build to determine dependency versions.

### Release

`/gradlew generateGlobalLock` Generate a global dependency lock file which will be created in the root project's build directory <br/>
`/gradlew saveGlobalLock` Copies the generated dependency lock file into the root project's directory <br/>
`/gradlew persistDependencyLock` Release a candidate version 
`/gradlew dependencyUpdates` Create report in JSON format which displays the dependencies which have new versions 

In order to get deeper insights, please refer to the tests.