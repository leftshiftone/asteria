package one.leftshift.asteria.email

import one.leftshift.asteria.email.tasks.SendEmailTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AsteriaEmailPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Email"
    static final String EXTENSION_NAME = "asteriaEmail"
    static final String SEND_EMAIL_TASK_NAME = "sendEmail"
    static final String SEND_RELEASE_EMAIL_TASK_NAME = "sendReleaseEmail"

    @Override
    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, AsteriaEmailExtension)
        List<String> programmersWisdom = []
        getClass().getResource("/programmers-wisdom.txt").eachLine { programmersWisdom << it }

        project.logger.debug("Adding tasks")
        def sendEmailTask
        if (!project.rootProject.tasks.find { it.name == SEND_EMAIL_TASK_NAME }) {
            sendEmailTask = project.rootProject.task(SEND_EMAIL_TASK_NAME, type: SendEmailTask)
        }

        def sendReleaseEmailTask
        if (!project.rootProject.tasks.find { it.name == SEND_RELEASE_EMAIL_TASK_NAME }) {
            sendReleaseEmailTask = project.rootProject.task(SEND_RELEASE_EMAIL_TASK_NAME, type: SendEmailTask)
            sendReleaseEmailTask.sender = "Release Leftshift One <no-reply@leftshift.one>"
            sendReleaseEmailTask.recipients = "devs@leftshift.one,5751fae1.leftshift.one@emea.teams.ms"
            project.afterEvaluate {
                sendReleaseEmailTask.subject = "Release ${project.rootProject.name} ${project.version.toString()}"
                sendReleaseEmailTask.content = """${project.rootProject.name} ${project.version.toString()} has been released.

Programmers wisdom of this release:

${programmersWisdom.get(new Random().nextInt(programmersWisdom.size()))}

Bleib geil!

Sincerely,
CI Leftshift One
"""
            }
        }
    }
}


