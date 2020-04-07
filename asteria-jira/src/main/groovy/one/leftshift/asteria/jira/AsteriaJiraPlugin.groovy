package one.leftshift.asteria.jira


import one.leftshift.asteria.jira.tasks.RetrieveReleaseNotesTask
import one.leftshift.asteria.jira.tasks.SearchIssuesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AsteriaJiraPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Jira"
    static final String EXTENSION_NAME = "asteriaJira"
    static final String JIRA_RELEASE_NOTES_TASK_NAME = "jiraReleaseNotes"
    static final String JIRA_SEARCH_ISSUES_TASK_NAME = "jiraSearchIssues"

    @Override
    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, AsteriaJiraExtension)
        File buildDir = project.file("${project.buildDir}/asteria/jira")
        if (!buildDir.exists()) {
            buildDir.mkdirs()
        }

        project.logger.debug("Adding tasks")
        def releaseNotesTask
        if (!project.rootProject.tasks.find { it.name == JIRA_RELEASE_NOTES_TASK_NAME }) {
            releaseNotesTask = project.rootProject.task(JIRA_RELEASE_NOTES_TASK_NAME, type: RetrieveReleaseNotesTask)
        }
        def searchIssuesTask
        if (!project.rootProject.tasks.find { it.name == JIRA_SEARCH_ISSUES_TASK_NAME }) {
            searchIssuesTask = project.rootProject.task(JIRA_SEARCH_ISSUES_TASK_NAME, type: SearchIssuesTask)
        }
    }
}
