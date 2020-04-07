package one.leftshift.asteria.report.tasks.model.test

import groovy.transform.ToString

import static java.time.Duration.ZERO

/**
 * Represents a single Junit test result in XML format.
 */
@ToString(includeNames = true, includeFields = true, includePackage = false, ignoreNulls = true)
class TestReport {
    String name
    String timestamp
    String duration = ZERO.toString()

    Integer total = 0
    Integer passed = 0
    Integer skipped = 0
    Integer failed = 0

    List<TestCase> tests = []
    Set<String> skippedTests = []
    Set<String> failedTests = []
}
