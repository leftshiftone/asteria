package one.leftshift.asteria.code.analytics.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class CodeCoverageTaskTest extends Specification {

    def "code coverage report is generated for multi project build"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("test", "codeCoverage", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            new File("${project.buildDir}/reports/jacoco/codeCoverage/codeCoverage.csv").exists()
            new File("${project.projectDir}/project1/build/jacoco/test.exec").exists()
            new File("${project.projectDir}/project1/build/reports/jacoco/test/jacocoTestReport.csv").exists()
            new File("${project.projectDir}/project2/build/jacoco/test.exec").exists()
            new File("${project.projectDir}/project2/build/reports/jacoco/test/jacocoTestReport.csv").exists()
    }

    def "code coverage report is generated for single project build"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/singleTestProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("test", "codeCoverage", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            new File("${project.projectDir}/build/jacoco/test.exec").exists()
            new File("${project.projectDir}/build/reports/jacoco/test/jacocoTestReport.csv").exists()
    }
}
