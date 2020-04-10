package one.leftshift.asteria.report.tasks

import groovy.json.JsonBuilder
import one.leftshift.asteria.report.AsteriaReportExtension
import one.leftshift.asteria.report.AsteriaReportPlugin
import one.leftshift.asteria.report.tasks.model.test.MetaInformation
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class VersionReportToThemisTask extends AbstractReportToThemisTask {

    private String themisApiPath = "/api/version/report"
    private String publish = "true"

    VersionReportToThemisTask() {
        group = AsteriaReportPlugin.GROUP
        description = "Send version information to the Themis dashboard."
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
    def report() {
        def versionString = project.version.toString()
        logger.info("Reporting version ${versionString}")
        MetaInformation metaInfo = determineMetaInformation()

        if (Boolean.valueOf(publish)) {
            postToThemis([
                    metaInfo: metaInfo,
                    version : versionString
            ])
        } else {
            logger.info("Skipped publishing")
        }
    }

    void postToThemis(Map<String, Object> report) {
        String reportAsJson = new JsonBuilder(report).toString()
        logger.info("Json to report: ${reportAsJson}")

        def url = project.rootProject.extensions.findByType(AsteriaReportExtension).reportingUrl
        def urlPath = new URI(url + getThemisApiPath())
        def queryParams = urlPath.query?.split("&")?.collectEntries {
            def pair = it.split("=")
            pair.size() > 1 ? [(pair[0]): pair[1]] : [(pair[0]): null]
        }

        httpRequest(url, urlPath, queryParams, reportAsJson)
    }
}
