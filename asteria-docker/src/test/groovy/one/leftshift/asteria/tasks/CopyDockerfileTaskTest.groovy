package one.leftshift.asteria.tasks

import org.gradle.api.Project
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static one.leftshift.asteria.AsteriaDockerTask.DOCKER_COPY
import static one.leftshift.asteria.test.util.TestDefaults.Default.*
import static one.leftshift.asteria.test.util.TestDefaults.createBasicProjectStructureStub

class CopyDockerfileTaskTest extends Specification {

    void "Missing Dockerfile throws Exception"() {
        given:
            Project stubProject = createBasicProjectStructureStub(PLUGIN.stringValue).project
        when:
            GradleRunner.create()
                    .withProjectDir(stubProject.projectDir)
                    .withArguments(BUILD_TASK.stringValue, DOCKER_COPY.taskName)
                    .withPluginClasspath()
                    .build()
        then:
            thrown(Exception)
        cleanup:
            stubProject.projectDir.deleteDir()
    }

    void "A present Dockerfile is copied to the expected location"() {
        given:
            Project stubProject = createBasicProjectStructureStub(PLUGIN.stringValue).project
            Files.createFile(Paths.get(stubProject.projectDir.toString(), DOCKERFILE.stringValue))
        when:
            GradleRunner.create()
                    .withProjectDir(stubProject.projectDir)
                    .withArguments(BUILD_TASK.stringValue, DOCKER_COPY.taskName)
                    .withPluginClasspath()
                    .build()
        then:
            Paths.get(stubProject.buildDir.absolutePath, "docker", DOCKERFILE.stringValue).toFile().exists()
        cleanup:
            stubProject.projectDir.deleteDir()
    }

    void "The location of the Dockerfile is configurable"() {
        given:
            Project stubProject = createBasicProjectStructureStub(PLUGIN.stringValue).project
            Path tmpDockerfile = Files.createTempFile(DOCKERFILE.stringValue, null)
            tmpDockerfile << "Test"
            File buildFile = Paths.get(stubProject.projectDir.absolutePath, BUILD_FILE.stringValue).toFile()
            buildFile << "\n asteriaDocker {\n dockerfile = file(\"${tmpDockerfile.toString()}\") \n}"
        when:
            GradleRunner.create()
                    .withProjectDir(stubProject.projectDir)
                    .withArguments(BUILD_TASK.stringValue, DOCKER_COPY.taskName)
                    .withPluginClasspath()
                    .build()
            def result = Paths.get(stubProject.buildDir.absolutePath, "docker", DOCKERFILE.stringValue).toFile()
        then:
            result.exists()
            result.isFile()
            result.readLines().find { it == "Test" } != null
        cleanup:
            stubProject.projectDir.deleteDir()
    }

    void "Missing buildDir throws Exception"() {
        given:
            Project stubProject = createBasicProjectStructureStub(PLUGIN.stringValue).project
        when:
            GradleRunner.create()
                    .withProjectDir(stubProject.projectDir)
                    .withArguments(DOCKER_COPY.taskName)
                    .withPluginClasspath()
                    .forwardOutput()
                    .build()
        then:
            thrown(Exception)
        cleanup:
            stubProject.projectDir.deleteDir()
    }
}
