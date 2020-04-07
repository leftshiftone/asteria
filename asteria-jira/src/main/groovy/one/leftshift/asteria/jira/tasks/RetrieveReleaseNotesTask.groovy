package one.leftshift.asteria.jira.tasks

import groovy.json.JsonBuilder
import one.leftshift.asteria.jira.AsteriaJiraExtension
import one.leftshift.asteria.jira.AsteriaJiraPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class RetrieveReleaseNotesTask extends AbstractHttpRequestTask {

    private static final String API_PATH = "/rest/api/2/search"
    private final static String JQL = "project = '%s' AND fixVersion = '%s' AND 'Visible for Release Notes' = yes ORDER BY type,created"
    private final static List<String> FIELDS = [
            "issuetype",
            "customfield_10717", // release notes title
            "customfield_10718", // release notes content
            "components",
            "resolution"
    ]

    private String jiraProject = null
    private String jiraProjectVersion = null

    @OutputFile
    File releaseNotesFile

    RetrieveReleaseNotesTask() {
        group = AsteriaJiraPlugin.GROUP
        description = "Retrieve release notes from Jira."
        releaseNotesFile = project.file("${project.buildDir}/asteria/jira/release-notes.json")
    }

    @Input
    String getJiraProject() {
        return jiraProject
    }

    @Option(option = "jiraProject", description = "The Jira project.")
    void setJiraProject(String jiraProject) {
        this.jiraProject = jiraProject
    }

    @Input
    String getJiraProjectVersion() {
        return jiraProjectVersion
    }

    @Option(option = "jiraProjectVersion", description = "The Jira project version.")
    void setJiraProjectVersion(String jiraProjectVersion) {
        this.jiraProjectVersion = jiraProjectVersion
    }

    @TaskAction
    def retrieve() {
        if (!jiraProjectVersion) {
            throw new RuntimeException("jira project version not specified")
        }

        Map<String, Object> request = [
                jql       : String.format(JQL, getJiraProject(), getJiraProjectVersion()),
                fields    : FIELDS,
                startAt   : 0,
                maxResults: 100
        ]

        def extension = project.rootProject.extensions.findByType(AsteriaJiraExtension)
        def username = extension.username
        def password = extension.apiToken
        def url = extension.baseUrl
        def urlPath = new URI(url + API_PATH)

        def response = httpRequest(url, urlPath, null, new JsonBuilder(request).toString(), username, password, logger)
        def issues = parseResponse(response as Map)
        if (response.total > 100) {
            (1..Math.ceil(response.total / 100)).each {
                request.startAt = (it * 100) as Integer
                issues.addAll(parseResponse(
                        httpRequest(url, urlPath, null, new JsonBuilder(request).toString(), username, password, logger) as Map
                ))
            }
        }
        releaseNotesFile.text = new JsonBuilder([project: jiraProject, version: jiraProjectVersion, issues: issues]).toPrettyString()
        logger.info("Release notes written to ${releaseNotesFile.absolutePath}")
    }

    private static List<Map> parseResponse(Map response) {
        List<Map> parsed = []
        response.issues.each { issue ->
            Map parsedIssue = [:]
            parsedIssue.key = issue.key
            parsedIssue.type = issue.fields.issuetype.name
            parsedIssue.releaseNotesTitle = issue.fields.customfield_10717
            parsedIssue.releaseNotesDescription = issue.fields.customfield_10718
            parsed.add(parsedIssue)
        }
        return parsed
    }
}
