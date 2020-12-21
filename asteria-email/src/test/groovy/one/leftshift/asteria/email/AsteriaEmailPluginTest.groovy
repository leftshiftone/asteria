package one.leftshift.asteria.email

import com.icegreen.greenmail.junit.GreenMailRule
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP

class AsteriaEmailPluginTest extends Specification {

    @Subject
    AsteriaEmailPlugin asteriaEmailPlugin

    @Rule
    public final GreenMailRule smtpServer = new GreenMailRule(SMTP)

    void setup() {
        smtpServer.setUser("admin", "admin")
        asteriaEmailPlugin = new AsteriaEmailPlugin()
    }

    void "works"() {
        expect:
            asteriaEmailPlugin != null
    }

    @Timeout(120)
    void "email can be sent"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGitRepo(projectDir)
        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("assemble", "sendEmail", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Sending email to noreply@leftshift.one"
            smtpServer.waitForIncomingEmail(1)
            smtpServer.receivedMessages.first().subject == "Test"
        cleanup:
            smtpServer.reset()
    }

    @Timeout(120)
    void "email can be sent with attachement"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGitRepo(projectDir)
        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("assemble", "sendEmail", "--attachments=${projectDir.absolutePath}/foo.xml", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Sending email to noreply@leftshift.one"
            smtpServer.waitForIncomingEmail(1)
            smtpServer.receivedMessages.first().subject == "Test"
        cleanup:
            smtpServer.reset()
    }

    @Timeout(120)
    void "release email can be sent"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGitRepo(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("snapshot", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Sending email to foo@leftshift.one,bar@leftshift.one"
            smtpServer.waitForIncomingEmail(1)
            smtpServer.receivedMessages.first().subject == "Release test-project 0.1.0-SNAPSHOT"
            smtpServer.receivedMessages.first().allRecipients.size() == 2
        cleanup:
            smtpServer.reset()
    }

    private static void setupGitRepo(File projectDir) {
        def gitignoreFile = new File(projectDir, ".gitignore")
        gitignoreFile << [".gradle", "build/", "userHome/"].join("\n")
        def initGit = new ProcessBuilder(["git", "init"]).directory(projectDir).start()
        initGit.waitForProcessOutput(System.out, System.err)
        def configEmailGit = new ProcessBuilder(["git", "config", "user.email", "pipelines@bitbucket.org"]).directory(projectDir).start()
        configEmailGit.waitForProcessOutput(System.out, System.err)
        def configNameGit = new ProcessBuilder(["git", "config", "user.name", "Bitbucket Pipelines"]).directory(projectDir).start()
        configNameGit.waitForProcessOutput(System.out, System.err)
        def stageGit = new ProcessBuilder(["git", "add", "--all"]).directory(projectDir).start()
        stageGit.waitForProcessOutput(System.out, System.err)
        def commitGit = new ProcessBuilder(["git", "commit", "-m", "initial"]).directory(projectDir).start()
        commitGit.waitForProcessOutput(System.out, System.err)
    }
}
