package one.leftshift.asteria.report.tasks

import groovy.json.JsonBuilder
import groovy.util.slurpersupport.GPathResult
import one.leftshift.asteria.report.AsteriaReportExtension
import one.leftshift.asteria.report.AsteriaReportPlugin
import one.leftshift.asteria.report.tasks.model.test.AggregatedTestReport
import one.leftshift.asteria.report.tasks.model.test.TestCase
import one.leftshift.asteria.report.tasks.model.test.TestReport
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime

import static java.time.ZoneId.systemDefault
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class TestReportToThemisTask extends AbstractReportToThemisTask {

    @InputFiles
    FileCollection testResultFiles

    private String themisApiPath = "/api/junit/report"
    private String publish = "true"

    TestReportToThemisTask() {
        group = AsteriaReportPlugin.GROUP
        description = "Aggregate test results an send the information to the Themis dashboard."
    }

    @Input
    String getThemisApiPath() {
        return themisApiPath
    }

    @Option(option = "themisApiPath", description = "The path after the base URL where the report will be posted to (e.g. /api/junit/report).")
    void setThemisApiPath(String themisApiPath) {
        this.themisApiPath = themisApiPath
    }

    @Input
    String getPublish() {
        return publish
    }

    @Option(option = "publish", description = "Configures whether the report will be sent or not.")
    void setPublish(String publish) {
        this.publish = publish
    }

    @TaskAction
    def aggregateAndReport() {
        logger.info("Got ${testResultFiles.size()} test result files")

        AggregatedTestReport report = extractResults(testResultFiles)
        report.metaInfo = determineMetaInformation()
        logger.debug("Report data to be published: {}", report)

        if (Boolean.valueOf(publish)) {
            postToThemis(report)
        } else {
            logger.info("Skipped publishing")
        }
    }

    static AggregatedTestReport extractResults(FileCollection files) {
        AggregatedTestReport aggregatedTestReport = new AggregatedTestReport()

        files.each { file ->
            addToAggregatedReport(readJunitResults(file), aggregatedTestReport)
        }

        aggregatedTestReport.timestamp = ZonedDateTime.now().format(ISO_OFFSET_DATE_TIME)
        return aggregatedTestReport
    }

    private static List<TestReport> readJunitResults(File file) {
        List<TestReport> reports = []

        def xml = new XmlSlurper().parseText(file.text)
        if (xml.testsuite.size()) {
            xml.testsuite.each { testsuite ->
                reports.add(readJunitSingleTestSuite(testsuite))
            }
        } else {
            reports.add(readJunitSingleTestSuite(xml))
        }
        return reports
    }

    private static void addToAggregatedReport(List<TestReport> reports, AggregatedTestReport aggregatedTestReport) {
        reports?.each { report ->
            aggregatedTestReport.duration = Duration.parse(aggregatedTestReport.duration).plus(Duration.parse(report.duration))
            aggregatedTestReport.total += report.total
            aggregatedTestReport.passed += report.passed
            aggregatedTestReport.skipped += report.skipped
            aggregatedTestReport.failed += report.failed

            aggregatedTestReport.reports.add(report)
            aggregatedTestReport.skippedTests.addAll(report.skippedTests)
            aggregatedTestReport.failedTests.addAll(report.failedTests)
        }
    }

    private static TestReport readJunitSingleTestSuite(GPathResult xml) {
        def report = new TestReport()
        report.name = xml.@name
        if (xml.@timestamp.toString()) {
            report.timestamp = ZonedDateTime.of(LocalDateTime.parse(xml.@timestamp.toString(), ISO_DATE_TIME), systemDefault()).format(ISO_OFFSET_DATE_TIME)
        }
        report.duration = toDuration(xml.@time.toString()).toString()

        report.total = Integer.valueOf(xml.@tests.toString())
        report.skipped = Integer.valueOf(xml.@skipped.toString() ?: xml.@skips.toString() ?: "0")
        report.failed = Integer.valueOf(xml.@failures.toString() ?: "0")
        report.failed += Integer.valueOf(xml.@errors.toString() ?: "0")
        report.passed = report.total - report.skipped - report.failed

        xml.testcase.each { test ->
            def testCase = new TestCase(
                    name: test.@name,
                    testClassName: test.@classname,
                    duration: toDuration(test.@time.toString()),
                    passed: !(test.failure.size() || test.skipped.size()),
                    skipped: test.skipped.size() ? true : false,
                    failureMessage: test.failure.@message.toString()
            )
            report.tests.add(testCase)
            if (testCase.skipped) {
                report.skippedTests.add(testCase.fullName)
            } else if (!testCase.passed) {
                report.failedTests.add(testCase.fullName)
            }
        }
        return report
    }

    void postToThemis(AggregatedTestReport report) {
        String reportAsJson = new JsonBuilder(report).toString()
        logger.info("Json to report: ${reportAsJson}")

        def url = project.rootProject.extensions.findByType(AsteriaReportExtension).themisBaseUrl
        def urlPath = new URI(url + getThemisApiPath())
        def queryParams = urlPath.query?.split("&")?.collectEntries {
            def pair = it.split("=")
            pair.size() > 1 ? [(pair[0]): pair[1]] : [(pair[0]): null]
        }

        httpRequest(url, urlPath, queryParams, reportAsJson)
    }
}
