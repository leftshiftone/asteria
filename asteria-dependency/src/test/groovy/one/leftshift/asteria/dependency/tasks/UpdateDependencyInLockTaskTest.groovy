package one.leftshift.asteria.dependency.tasks


import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import static one.leftshift.asteria.dependency.AsteriaDependencyPluginTest.setupGitRepo

/**
 * @author Michael Mair
 */
class UpdateDependencyInLockTaskTest extends Specification {

    def "dependencies.lock is updated correctly"() {
        given:
        Project project = ProjectBuilder.builder().build()
        File projectDir = project.projectDir
        new AntBuilder().copy(todir: project.projectDir.absolutePath) {
            fileset(dir: "src/test/resources/testProject")
        }
        new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project1") {
            fileset(file: "src/test/resources/project1/dependencies.lock")
        }
        new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project2") {
            fileset(file: "src/test/resources/project2/dependencies.lock")
        }
        setupGitRepo(projectDir)

        when:
        def result = GradleRunner.create()
                .withProjectDir(project.projectDir)
                .withArguments("updateDependencyLock", "--dependency=one\\.leftshift\\.canon:canon", "--version=2.2.2", "--debug", "--stacktrace")
                .withPluginClasspath()
                .build()
        println result.output

        then:
        result.output.contains "BUILD SUCCESSFUL"
        def project1DependencyFile = new File(projectDir.absolutePath, "project1/dependencies.lock")
        project1DependencyFile.text.contains "2.2.2"
        !project1DependencyFile.text.contains("1.0.0")
    }

    def "dependencies.lock is updated correctly in both projects"() {
        given:
        Project project = ProjectBuilder.builder().build()
        File projectDir = project.projectDir
        new AntBuilder().copy(todir: project.projectDir.absolutePath) {
            fileset(dir: "src/test/resources/testProject")
        }
        new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project1") {
            fileset(file: "src/test/resources/project1/dependencies.lock")
        }
        new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project2") {
            fileset(file: "src/test/resources/project2/dependencies.lock")
        }
        setupGitRepo(projectDir)

        when:
        def result = GradleRunner.create()
                .withProjectDir(project.projectDir)
                .withArguments("updateDependencyLock", "--dependency=junit:junit", "--version=4.4.4", "--debug", "--stacktrace")
                .withPluginClasspath()
                .build()
        println result.output

        then:
        result.output.contains "BUILD SUCCESSFUL"
        def project1DependencyFile = new File(projectDir.absolutePath, "project1/dependencies.lock")
        project1DependencyFile.text.contains "4.4.4"
        !project1DependencyFile.text.contains("4.5")
        def project2DependencyFile = new File(projectDir.absolutePath, "project2/dependencies.lock")
        project2DependencyFile.text.contains "4.4.4"
        !project2DependencyFile.text.contains("4.5")
    }
}
