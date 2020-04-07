package one.leftshift.asteria.report

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import static one.leftshift.asteria.report.AsteriaReportPlugin.getDEPS_GRAPH_REPORT_TASK_NAME

class AsteriaReportPluginTest extends Specification {

    def "plugin can be instantiated"() {
        expect:
            new AsteriaReportPlugin() != null
    }

    def "root test report is created"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGit(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "rootTestReport", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output =~ /Finished generating test html results \([0-9.]+ secs\) into: \/tmp\/\w+\/build\/reports\/tests\/all/
    }

    def "root test report is zipped"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGit(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "rootTestReport", "zipTestReport", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output =~ /Finished generating test html results \([0-9.]+ secs\) into: \/tmp\/\w+\/build\/reports\/tests\/all/
            project.zipTree("${project.buildDir}/reports/foo-test-reports-master-unknown.zip")
                    .files
                    .find { it.name == "index.html" }
    }

    def "reporting extracts test information"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGit(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "testReportToThemis", "--publish=false", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Got 2 test result files"
            result.output.contains "buildNumber:unknown, repositoryName:foo, repositoryBranch:master"
            result.output.contains "total:2, passed:2, skipped:0, failed:0"
            result.output.contains "testClassName:project1.Project1Test, passed:true, skipped:false, failureMessage:, fullName:project1.Project1Test.stateIsTrue)"
            result.output.contains "testClassName:project2.Project2Test, passed:true, skipped:false, failureMessage:, fullName:project2.Project2Test.stateIsTrue)"
    }

    def "reporting get version information"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGit(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "versionReportToThemis", "--publish=false", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Reporting version 0.1.0-SNAPSHOT"
    }

    def "reporting extracts dependency information"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            File depsReportDir = new File("${projectDir.absolutePath}/build/reports/dependencyUpdates")
            depsReportDir.mkdirs()
            new AntBuilder().copy(file: "src/test/resources/report.json", todir: depsReportDir.absolutePath)
            setupGit(projectDir)

        when:
            //test assumes that dependency report has been already generated
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("depsReportToThemis", "--publish=false", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "repositoryName:foo, repositoryBranch:master"
            result.output.contains "gradleVersion:4.8, gradleLatestVersion:4.8.1, gradleUpdateAvailable:true"
            result.output.contains "total:14, current:9, outdated:3"
    }

    def "dependency graph information"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGit(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("depsGraphReport", "--startsWithGroup=org.springframework", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def resultFile = new File("${project.buildDir}/report/${DEPS_GRAPH_REPORT_TASK_NAME}/report.json")

        then:

            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "[system.out] org.springframework.boot:spring-boot-starter"
            result.output.contains "[system.out]       org.springframework.boot:spring-boot"
            result.output.contains "[system.out]           org.springframework:spring-context"
            result.output.contains "[system.out]             org.springframework:spring-expression"
            result.output.contains "[system.out]           org.springframework:spring-core"
            result.output.contains "[system.out]               org.springframework:spring-aop"
            result.output.contains "[system.out]             org.springframework:spring-core"
            result.output.contains "[system.out]                   org.springframework:spring-beans"
            result.output.contains "[system.out]                 org.springframework:spring-core"
            result.output.contains "[system.out]             org.springframework:spring-core"
            result.output.contains "[system.out]                   org.springframework:spring-beans"
            result.output.contains "[system.out]             org.springframework:spring-core"
            result.output.contains "[system.out]               org.springframework:spring-jcl"
            result.output.contains "[system.out]               org.springframework:spring-core"
            result.output.contains "[system.out]       org.springframework.boot:spring-boot-starter-logging"
            result.output.contains "[system.out]     org.springframework.boot:spring-boot-autoconfigure"
            result.output.contains "[system.out]         org.springframework.boot:spring-boot"
            resultFile.exists()
            !resultFile.text.isEmpty()
    }

    private void setupGit(File projectDir) {
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
        def addRemoteGit = new ProcessBuilder(["git", "remote", "add", "origin", "https://github.com/leftshiftone/foo.git"]).directory(projectDir).start()
        addRemoteGit.waitForProcessOutput(System.out, System.err)
    }
}
