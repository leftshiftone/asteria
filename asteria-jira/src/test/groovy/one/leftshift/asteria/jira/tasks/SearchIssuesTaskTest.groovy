package one.leftshift.asteria.jira.tasks

import groovy.json.JsonBuilder
import one.leftshift.asteria.jira.AsteriaJiraExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Requires
import spock.lang.Specification

@Requires({ System.getenv("CI") })
// execute on CI only
class SearchIssuesTaskTest extends Specification {

    def "http request works as expected"() {
        given:
            def extension = new AsteriaJiraExtension()
            extension.baseUrl = System.getenv("ASTERIA_JIRA_BASEURL")
            extension.username = System.getenv("ASTERIA_JIRA_USERNAME")
            extension.apiToken = System.getenv("ASTERIA_JIRA_APITOKEN")
            Map<String, Object> request = [
                    jql       : "project = 'GAIA' AND fixVersion = 'GAIA-1.3.1' AND 'Visible for Release Notes' = yes ORDER BY type,created",
                    fields    : SearchIssuesTask.FIELDS,
                    maxResults: 100
            ]
        when:
            Map result = SearchIssuesTask.httpRequest(
                    extension.baseUrl,
                    new URI(extension.baseUrl + SearchIssuesTask.API_PATH),
                    [:],
                    new JsonBuilder(request).toString(),
                    extension.username,
                    extension.apiToken,
                    Logging.getLogger("test") as Logger
            )
        then:
            result.total == 2
            result.issues.size() == 2
    }

    def "jiraSearchIssues task retrieves issues and stores it on disk"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("jiraSearchIssues", "--jql=project = GAIA AND fixVersion = GAIA-1.3.1 AND 'Visible for Release Notes' = yes ORDER BY type,created", "--fields=issuetype,assignee,components,status", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

            def releaseNotesFile = new File("${project.buildDir}/asteria/jira/issues.json")
        then:
            result.output.contains "BUILD SUCCESSFUL"
            releaseNotesFile.exists()
            releaseNotesFile.size() > 1000
    }
}
