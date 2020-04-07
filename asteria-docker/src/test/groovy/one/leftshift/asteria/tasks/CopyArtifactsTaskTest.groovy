package one.leftshift.asteria.tasks

import one.leftshift.asteria.test.util.GenericExtension
import org.gradle.api.Project
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Paths

import static one.leftshift.asteria.AsteriaDockerTask.DOCKER_COPY_ARTIFACTS
import static one.leftshift.asteria.test.util.TestDefaults.Default.BUILD_TASK
import static one.leftshift.asteria.test.util.TestDefaults.createBasicProjectStructureStub

class CopyArtifactsTaskTest extends Specification {

    void "JAR artifacts are copied to the expected location with the expected name"() {
        given:
            Project stubProject = createBasicProjectStructureStub("one.leftshift.asteria-docker").project
            Files.createFile(Paths.get(stubProject.projectDir.absolutePath, "Dockerfile"))
        when:
            GradleRunner.create()
                    .withProjectDir(stubProject.projectDir)
                    .withArguments(BUILD_TASK.stringValue, DOCKER_COPY_ARTIFACTS.taskName)
                    .withPluginClasspath()
                    .build()
            File result = new File("${stubProject.buildDir}/docker/app.jar")
        then:
            result.exists()
            result.isFile()
        cleanup:
            stubProject.projectDir.deleteDir()
    }

    @SuppressWarnings("GroovyPointlessBoolean")
    @Unroll
    void "When copyArtifacts is set to #copyArtifacts the artifact is copied to /builder/docker/app.jar is #expectation"() {
        given:
            GenericExtension extension = new GenericExtension(
                    "asteriaDocker",
                    [
                            "copyArtifacts": copyArtifacts,
                            "name"         : "\"hansi\"",
                    ]
            )
            Project stubProject = createBasicProjectStructureStub(extension, "one.leftshift.asteria-docker").project
            Files.createFile(Paths.get(stubProject.projectDir.absolutePath, "Dockerfile"))
        when:
            GradleRunner.create()
                    .withProjectDir(stubProject.projectDir)
                    .withArguments(BUILD_TASK.stringValue, DOCKER_COPY_ARTIFACTS.taskName)
                    .withPluginClasspath()
                    .build()
        then:
            File result = new File("${stubProject.buildDir}/docker/app.jar")
            result.exists() == expectation
        cleanup:
            stubProject.projectDir.deleteDir()
        where:
            copyArtifacts || expectation
            "true"        || true
            "false"       || false
    }
}
