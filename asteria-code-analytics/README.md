# asteria-code-analytics

This plugin enables code coverage with [JaCoCo](http://www.eclemma.org/jacoco/). Furthermore it includes the [SonarQube](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Gradle) plugin.

## Configuration

*build.gradle*
```groovy
buildscript {
    dependencies {
        classpath "one.leftshift.asteria:asteria-code-analytics:+"
    }
}

allprojects {
    apply plugin: "one.leftshift.asteria-version"
    
    asteriaCodeAnalytics {
        xmlCoverageReportEnabled = true
        htmlCoverageReportEnabled = true
        coverageExcludes = [
                "**/api/**",
                "**/dto/**",
        ]
    }
}
```

## Usage

`/gradlew :subproject1:test :subproject1:jacocoTestReport` Create the code coverage for project subproject1 <br/>
`/gradlew test codeCoverage` Create the code coverage for all subprojects; the result can be found in the root project's build folder <br/>
`/gradlew test codeCoverageCodacyUpload` Create the code coverage for all subprojects and upload the results to codacy. Not that the environment variable _CODACY_PROJECT_TOKEN_ needs to be set correctly and the Jacoco report must be created in XML format.<br/>
