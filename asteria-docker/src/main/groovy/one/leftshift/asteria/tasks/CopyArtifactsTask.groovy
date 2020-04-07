package one.leftshift.asteria.tasks

import one.leftshift.asteria.AsteriaDockerExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path

/**
 * Copies the JAR to project.buildDir/docker
 */
class CopyArtifactsTask extends DefaultTask {

    @Input
    AsteriaDockerExtension extension

    @TaskAction
    void copy() {
        if (extension.copyArtifacts) {
            File output = new File("$project.buildDir/docker/app.jar")
            if (!output.exists()) {
                Files.copy(project.jar.archivePath.toPath() as Path, output.toPath())
            }
        }
    }
}
