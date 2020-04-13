package one.leftshift.asteria.report.tasks

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import one.leftshift.asteria.report.AsteriaReportExtension
import one.leftshift.asteria.report.AsteriaReportPlugin
import one.leftshift.asteria.report.tasks.model.deps.AggregatedDepsReport
import one.leftshift.asteria.report.tasks.model.deps.Dependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import java.math.RoundingMode
import java.time.ZonedDateTime

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class DepsReportTask extends AbstractReportTask {

    @InputFile
    File depsResultFile

    private String apiPath = "/api/deps/report"
    private String publish = "true"

    DepsReportTask() {
        group = AsteriaReportPlugin.GROUP
        description = "Aggregate dependency updates results and send the information to the specified API."
    }

    @Input
    String getApiPath() {
        return apiPath
    }

    @Option(option = "apiPath", description = "The path after the base URL where the report will be posted to (e.g. /api/deps/report).")
    void setApiPath(String apiPath) {
        this.apiPath = apiPath
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
        logger.info("Got ${depsResultFile.size()} dependency updates result files")
        def extension = project.rootProject.extensions.findByType(AsteriaReportExtension)

        AggregatedDepsReport report = extractResults(depsResultFile)
        report.metaInfo = determineMetaInformation()
        report.url = "https://bitbucket.org/${extension.repositoryOwner}/${extension.repositoryName}/downloads/${extension.repositoryName}-deps-report-${extension.escapedBranchName}-${extension.buildNumber}.json"

        logger.debug("Report data to be published: {}", report)
        if (Boolean.valueOf(publish)) {
            postToApi(report)
        } else {
            logger.info("Skipped publishing")
        }
    }

    static AggregatedDepsReport extractResults(File file) {
        def json = new JsonSlurper().parseText(file.text)

        AggregatedDepsReport aggregatedDepsReport = new AggregatedDepsReport()
        aggregatedDepsReport.gradleVersion = json.gradle.running.version
        aggregatedDepsReport.gradleLatestVersion = json.gradle.current.version
        aggregatedDepsReport.gradleUpdateAvailable = json.gradle.current.isUpdateAvailable

        aggregatedDepsReport.total = json.count ?: 0
        aggregatedDepsReport.current = json.current.count ?: 0
        aggregatedDepsReport.outdated = json.outdated.count ?: 0

        if (aggregatedDepsReport.total) {
            aggregatedDepsReport.outdatedPercentage = new BigDecimal(aggregatedDepsReport.outdated)
                    .divide(new BigDecimal(aggregatedDepsReport.total), 2, RoundingMode.HALF_UP)
                    .multiply(100d)
                    .toInteger()
        }

        json.current.dependencies.each { dependency ->
            aggregatedDepsReport.currentDeps.add(new Dependency(
                    group: dependency.group,
                    name: dependency.name,
                    currentVersion: dependency.version,
                    latestVersion: dependency.version
            ))
        }
        json.outdated.dependencies.each { dependency ->
            aggregatedDepsReport.outdatedDeps.add(new Dependency(
                    group: dependency.group,
                    name: dependency.name,
                    currentVersion: dependency.version,
                    latestVersion: dependency.available?.release ?: dependency.version
            ))
        }

        aggregatedDepsReport.timestamp = ZonedDateTime.now().format(ISO_OFFSET_DATE_TIME)
        return aggregatedDepsReport
    }

    void postToApi(AggregatedDepsReport report) {
        String reportAsJson = new JsonBuilder(report).toString()
        logger.info("Json to report: ${reportAsJson}")

        def url = project.rootProject.extensions.findByType(AsteriaReportExtension).reportingUrl
        def urlPath = new URI(url + getApiPath())
        def queryParams = urlPath.query?.split("&")?.collectEntries {
            def pair = it.split("=")
            pair.size() > 1 ? [(pair[0]): pair[1]] : [(pair[0]): null]
        }

        httpRequest(url, urlPath, queryParams, reportAsJson)
    }
}
