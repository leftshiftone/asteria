package one.leftshift.asteria.report

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException
import org.apache.http.entity.mime.MultipartEntityBuilder
import spock.lang.Ignore
import spock.lang.Specification

import static groovyx.net.http.Method.POST
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA

@Ignore("This test is just for testing the capabilities of bitbucket upload")
class BitbucketUploadTest extends Specification {

    def "file can be uploaded"() {
        given:
            HTTPBuilder http = new HTTPBuilder("https:user:password@//api.bitbucket.org/2.0/repositories/leftshiftone/asteria/downloads")

        when:
            def fileToUpload = new File("/home/mmair/Dev/source/asteria/build/reports/tests/asteria-test-reports-master-unknown.zip")
            def payload = MultipartEntityBuilder.create()
                    .addBinaryBody("files", fileToUpload, MULTIPART_FORM_DATA, fileToUpload.name)
                    .build()

            http.request(POST) { request ->
                request.entity = payload

                response.success = { resp ->
                    println "Published test results."
                    println "Published with status ${resp.statusLine}"
                    assert resp.statusLine.statusCode == 201
                }
                response.failure = { resp ->
                    println "Publishing test results failed: ${resp.statusLine}"
                    throw new HttpResponseException(resp)
                }
            }

        then:
            true
    }
}
