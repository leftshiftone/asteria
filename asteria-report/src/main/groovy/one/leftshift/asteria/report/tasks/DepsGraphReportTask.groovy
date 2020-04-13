package one.leftshift.asteria.report.tasks

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import one.leftshift.asteria.report.AsteriaReportExtension
import one.leftshift.asteria.report.AsteriaReportPlugin
import one.leftshift.asteria.report.tasks.model.deps.DependencyGraphNode
import one.leftshift.asteria.report.tasks.model.deps.DependencyGraphReport
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class DepsGraphReportTask extends AbstractReportTask {

    @InputFile
    File depsGraphResultFile

    private String apiPath = "/api/deps-graph/report"
    private String publish = "true"

    DepsGraphReportTask() {
        group = AsteriaReportPlugin.GROUP
        description = "Get dependencies starting with group id 'one.leftshift'."
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
    def retrieveAndReport() {
        DependencyGraphReport report = new DependencyGraphReport()
        report.metaInfo = determineMetaInformation()

        logger.info("Got ${depsGraphResultFile.size()} dependency updates result files")
        report.dependencies = extractResults(depsGraphResultFile)
        if (Boolean.valueOf(publish)) {
            postToApi(report)
        } else {
            logger.info("Skipped publishing")
        }
    }

    void postToApi(DependencyGraphReport report) {
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

    private static Set<DependencyGraphNode> extractResults(File resultFile) {
        def json = new JsonSlurper().parseText(resultFile.text) as List<Map>
        def result = new HashSet<DependencyGraphNode>()
        json.each { result.add(extractDependency(it)) }
        return result
    }

    private static DependencyGraphNode extractDependency(Map dependency) {
        def node = new DependencyGraphNode(
                group: dependency.group,
                name: dependency.name,
                version: dependency.version
        )
        if (dependency.dependencies) {
            dependency.dependencies.each { Map dep ->
                node.addDependency(extractDependency(dep))
            }
        }
        return node
    }
}
