package one.leftshift.asteria.report.tasks.model.test

import groovy.transform.ToString

import static java.time.Duration.ZERO

@ToString(includeNames = true, includeFields = true, includePackage = false, ignoreNulls = true)
class AggregatedTestReport {

    MetaInformation metaInfo
    String timestamp
    String duration = ZERO.toString()

    Integer total = 0
    Integer passed = 0
    Integer skipped = 0
    Integer failed = 0

    List<TestReport> reports = []
    Set<String> skippedTests = []
    Set<String> failedTests = []
}
