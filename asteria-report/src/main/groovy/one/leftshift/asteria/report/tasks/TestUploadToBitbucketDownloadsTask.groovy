package one.leftshift.asteria.report.tasks

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException
import one.leftshift.asteria.report.AsteriaReportExtension
import one.leftshift.asteria.report.AsteriaReportPlugin
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

import static groovyx.net.http.Method.POST
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA

class TestUploadToBitbucketDownloadsTask extends DefaultTask {

    @InputFile
    File zippedTestReport

    TestUploadToBitbucketDownloadsTask() {
        group = AsteriaReportPlugin.GROUP
        description = "Post zipped test report to Bitbucket downloads."
    }

    @TaskAction
    def uploadToBitbucketDownloads() {
        File reportDir = project.rootProject.tasks."${AsteriaReportPlugin.ROOT_TEST_REPORT_TASK_NAME}".destinationDir
        logger.info("Using content from ${reportDir.path}")
        postToBitbucketDownloads()
    }

    void postToBitbucketDownloads() {
        logger.info("File to upload: ${zippedTestReport.absolutePath}")
        def extension = project.rootProject.extensions.findByType(AsteriaReportExtension)

        def fileToUpload = new File(zippedTestReport.absolutePath)
        logger.debug("Using file name ${fileToUpload.name}")

        def payload = MultipartEntityBuilder.create()
                .addBinaryBody("files", fileToUpload, MULTIPART_FORM_DATA, fileToUpload.name)
                .build()

        HTTPBuilder http = new HTTPBuilder("${extension.bitbucketBaseUrl}/2.0/repositories/${extension.repositoryOwner}/${extension.repositoryName}/downloads")
        http.request(POST) { request ->
            request.entity = payload

            if (extension.bitbucketAuthString) {
                logger.info("Preparing authorization")
                headers."Authorization" = "Basic ${extension.bitbucketAuthString.bytes.encodeBase64().toString()}"
            }

            response.success = { resp ->
                logger.info("Published test results.")
                logger.debug("Published with status ${resp.statusLine}")
            }
            response.failure = { resp ->
                logger.error("Publishing test results failed: ${resp.statusLine}")
                throw new HttpResponseException(resp)
            }
        }
    }
}
