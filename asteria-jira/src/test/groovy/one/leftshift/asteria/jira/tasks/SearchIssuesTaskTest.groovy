package one.leftshift.asteria.jira.tasks

import groovy.json.JsonBuilder
import one.leftshift.asteria.jira.AsteriaJiraExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Ignore
import spock.lang.Specification

class SearchIssuesTaskTest extends Specification {

    @Ignore
    def "http request works as expected"() {
        given:
            def extension = new AsteriaJiraExtension()
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
                    "",
                    "",
                    Logging.getLogger("test") as Logger
            )
        then:
            result.total == 2
            result.issues.size() == 2
    }

    @Ignore
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
