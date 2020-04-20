package one.leftshift.asteria.tasks

import one.leftshift.asteria.AsteriaDockerExtension
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.component.external.model.ComponentVariant

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Copies all additionally specified files to project.buildDir/docker
 */
class CopyAdditionalFilesTask extends DefaultTask {

    @Input
    AsteriaDockerExtension extension
    
    @TaskAction
    void copyAdditionalFiles() {
        extension.additionalFiles?.each { file ->
            if(file.isDirectory()){
                FileUtils.copyDirectory(file, new File("$project.buildDir/docker/${file.getName()}"))
            }else {
                Files.copy(file.toPath(), Paths.get("$project.buildDir/docker/", file.getName()))
            }
        }
    }
}
