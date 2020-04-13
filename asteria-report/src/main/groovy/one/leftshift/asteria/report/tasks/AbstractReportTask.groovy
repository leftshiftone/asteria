package one.leftshift.asteria.report.tasks

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import one.leftshift.asteria.report.AsteriaReportExtension
import one.leftshift.asteria.report.tasks.model.test.MetaInformation
import org.apache.http.HttpRequest
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClients
import org.gradle.api.DefaultTask

import java.time.Duration

import static groovyx.net.http.Method.POST

abstract class AbstractReportTask extends DefaultTask {

    void httpRequest(String url, URI urlPath, Map queryParams, String reportAsJson) {
        def requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(20_000)
                .setConnectTimeout(20_000)
                .setSocketTimeout(20_000)
                .build()
        HTTPBuilder http = new HTTPBuilder(url)
        http.setClient(HttpClients.custom().setDefaultRequestConfig(requestConfig).build())
        try {
            http.request(POST) { HttpRequest request ->
                uri.path = urlPath.path
                if (queryParams) {
                    uri.query = queryParams
                }
                requestContentType = ContentType.JSON
                body = reportAsJson

                response.success = { resp ->
                    logger.info("Published test results.")
                    logger.debug("Published with status ${resp.statusLine}")
                    assert resp.statusLine.statusCode == 200
                }
                response.failure = { resp ->
                    logger.error("Publishing test results failed: ${resp.statusLine}\n$resp")
                }
            }
        } catch (Exception ex) {
            logger.error(ex.message, ex)
        }
    }

    MetaInformation determineMetaInformation() {
        def extension = project.rootProject.extensions.findByType(AsteriaReportExtension)
        String buildNumber = extension.buildNumber
        String repoName = extension.repositoryName
        String repoBranch = extension.branchName
        return new MetaInformation(buildNumber: buildNumber, repositoryName: repoName, repositoryBranch: repoBranch)
    }

    static Duration toDuration(String durationAsString) {
        return Duration.ofMillis(BigDecimal.valueOf(Double.valueOf(durationAsString)).multiply(1000).toLong())
    }
}
