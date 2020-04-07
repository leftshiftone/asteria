package one.leftshift.asteria.jira.tasks

import groovy.json.JsonBuilder
import one.leftshift.asteria.jira.AsteriaJiraExtension
import one.leftshift.asteria.jira.AsteriaJiraPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class SearchIssuesTask extends AbstractHttpRequestTask {

    private static final String API_PATH = "/rest/api/2/search"
    private final static List<String> FIELDS = [
            "issuetype",
            "assignee",
            "customfield_10717", // release notes title
            "customfield_10718", // release notes content
            "components",
            "resolution",
            "status"
    ]

    private String jql = null
    private List<String> fields = null
    private Integer startAt = 0
    private Integer maxResults = 100

    @OutputFile
    File issuesFile

    SearchIssuesTask() {
        group = AsteriaJiraPlugin.GROUP
        description = "Search for Issues from Jira."
        issuesFile = project.file("${project.buildDir}/asteria/jira/issues.json")
    }

    @Input
    String getJql() {
        return jql
    }

    @Option(option = "jql", description = "The Jira Query.")
    void setJql(String jql) {
        this.jql = jql
    }

    @Input
    List<String> getFields() {
        return fields
    }

    @Option(option = "fields", description = "The fields to retrieve separated by commas.")
    void setFields(String fields) {
        this.fields = FIELDS
        if (fields) {
            this.fields = fields.split(",").each { it.trim() }
        }
    }

    @Input
    Integer getStartAt() {
        return startAt
    }

    @Option(option = "startAt", description = "The offset of results to retrieve.")
    void setStartAt(String startAt) {
        this.startAt = Integer.valueOf(startAt)
    }

    @Input
    Integer getMaxResults() {
        return maxResults
    }

    @Option(option = "maxResults", description = "The maximal results to retrieve.")
    void setMaxResults(String maxResults) {
        this.maxResults = Integer.valueOf(maxResults)
    }

    @TaskAction
    def retrieve() {
        if (!fields) {
            throw new RuntimeException("fields not specified")
        }

        Map<String, Object> request = [
                jql       : getJql(),
                fields    : getFields(),
                startAt   : getStartAt() ?: 0,
                maxResults: getMaxResults() ?: 100
        ]

        def extension = project.rootProject.extensions.findByType(AsteriaJiraExtension)
        def username = extension.username
        def password = extension.apiToken
        def url = extension.baseUrl
        def urlPath = new URI(url + API_PATH)

        def response = httpRequest(url, urlPath, null, new JsonBuilder(request).toString(), username, password, logger)

        issuesFile.text = new JsonBuilder([
                jql       : getJql(),
                fields    : getFields(),
                startAt   : getStartAt(),
                maxResults: getMaxResults(),
                response  : response
        ]).toPrettyString()
        logger.info("Issues written to ${issuesFile.absolutePath}")
    }
}
