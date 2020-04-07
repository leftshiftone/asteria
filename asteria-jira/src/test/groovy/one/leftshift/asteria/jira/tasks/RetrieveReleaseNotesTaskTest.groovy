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

class RetrieveReleaseNotesTaskTest extends Specification {

    @Ignore
    def "http request works as expected"() {
        given:
            def extension = new AsteriaJiraExtension()
            Map<String, Object> request = [
                    jql       : String.format(RetrieveReleaseNotesTask.JQL, "GAIA", "GAIA-0.9.0"),
                    fields    : RetrieveReleaseNotesTask.FIELDS,
                    maxResults: 100
            ]
        when:
            Map result = RetrieveReleaseNotesTask.httpRequest(
                    extension.baseUrl,
                    new URI(extension.baseUrl + RetrieveReleaseNotesTask.API_PATH),
                    [:],
                    new JsonBuilder(request).toString(),
                    "",
                    "",
                    Logging.getLogger("test") as Logger
            )
        then:
            result.total > 3
            result.issues.size() > 3
    }

    @Ignore
    def "jiraReleaseNotes task retrieves release notes and stores it on disk"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("jiraReleaseNotes", "--jiraProject=GAIA", "--jiraProjectVersion=GAIA-0.9.0", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

            def releaseNotesFile = new File("${project.buildDir}/asteria/jira/release-notes.json")
        then:
            result.output.contains "BUILD SUCCESSFUL"
            releaseNotesFile.exists()
            releaseNotesFile.size() > 1000
    }
}
