package one.leftshift.asteria.tasks

import one.leftshift.asteria.AsteriaDockerExtension
import one.leftshift.asteria.docker.client.exception.DockerBuildDirInexistentException
import one.leftshift.asteria.docker.client.exception.DockerfileNotPresentException
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files

/**
 * Copies the Dockerfile to project.buildDir/docker
 */
class CopyDockerfileTask extends DefaultTask {

    @Input
    AsteriaDockerExtension extension
    
    @TaskAction
    void process() {
        throwExceptionIfBuildDirDoesNotExist(project)
        throwExceptionIfDockerfileNotPresent(extension?.dockerfile)

        final File outputDir = new File("$project.buildDir/docker")
        final File outputFile = new File(outputDir, "Dockerfile")
        if (!outputDir.exists()) {
            outputDir.mkdir()
        }

        if (!outputFile.exists()) {
            Files.copy(extension.dockerfile.toPath(), outputFile.toPath())
        }
    }
    
    private static void throwExceptionIfDockerfileNotPresent(File dockerfile) {
        if (!dockerfile.exists() || !dockerfile.isFile()) {
            throw new DockerfileNotPresentException("Location of Dockerfile is not configured and not present in sensible default location.")
        }
    }

    private static void throwExceptionIfBuildDirDoesNotExist(Project project) {
        if (!project.buildDir.exists()) {
            throw new DockerBuildDirInexistentException("Could not find buildDir. Consider running the 'build' task first.")
        }
    }
}
