package one.leftshift.asteria.jira.tasks

import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import org.apache.http.HttpRequest
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClients
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger

import static groovyx.net.http.Method.POST

abstract class AbstractHttpRequestTask extends DefaultTask {

    protected static Object httpRequest(String url, URI urlPath, Map queryParams, String requestBody, String username, String password, Logger logger) {
        def requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(20_000)
                .setConnectTimeout(20_000)
                .setSocketTimeout(20_000)
                .build()

        HTTPBuilder http = new HTTPBuilder(url)
        http.setClient(HttpClients.custom().setDefaultRequestConfig(requestConfig).build())
        String auth = Base64.getUrlEncoder().encodeToString("${username}:${password}".bytes)
        try {
            http.request(POST) { HttpRequest request ->
                uri.path = urlPath.path
                request.addHeader("Content-Type", "application/json")
                request.addHeader("Authorization", "Basic ${auth}")
                request.addHeader("Accept", "*/*")
                if (queryParams) {
                    uri.query = queryParams
                }
                requestContentType = ContentType.JSON
                body = requestBody

                response.success = { resp ->
                    logger.info("Retrieved data.")
                    return new JsonSlurper().parse(resp.entity.content)
                }
                response.failure = { resp ->
                    logger.error("Failed to retrieve data: ${resp.statusLine}\n$resp")
                    return [error: resp.statusLine]
                }
            }
        } catch (Exception ex) {
            logger.error(ex.message, ex)
            return null
        }
    }
}
