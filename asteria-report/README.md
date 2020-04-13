# asteria-report

This plugin is responsible for test reporting.

## Configuration

*build.gradle*
```groovy
buildscript {
    dependencies {
        classpath "one.leftshift.asteria:asteria-report:+"
    }
}

apply plugin: "one.leftshift.asteria-report"

asteriaReport {
    junitXmlResults = fileTree(rootProject.projectDir) {
        include("**/test-results/*/TEST-*.xml")
    }
    junitBinaryResults = files(subprojects.collect { subproject ->
        ["test"].collect { "${subproject.buildDir}/test-results/${it}/binary" }
    }.flatten())
}
```

If you want to ensure reporting tasks are executed even if tests fail, then add the following to your *build.gradle* file.
```groovy
subprojects.each { subproject ->
    subproject.tasks.findAll { it instanceof Test }*.each {
        it.finalizedBy(testReport, rootTestReport, zipTestReport, testUploadToBitbucketDownloads)
    }
}
```

## Usage

Several tasks are available as shown below.

### Report

`/gradlew test testReport` Report executed test results to API <br/>

### Root Test Report

`/gradlew test rootTestReport` Creates an aggregated test report inside your root project's build folder <br/>

### Upload Root Test Report to Bitbucket downloads

`/gradlew test rootTestReport zipTestReport testUploadToBitbucketDownloads` Upload the zipped, aggregated test report to Bitbucket downloads of the current repository 
