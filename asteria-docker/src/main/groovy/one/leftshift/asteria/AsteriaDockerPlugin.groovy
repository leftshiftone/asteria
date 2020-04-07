package one.leftshift.asteria

import one.leftshift.asteria.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

import static AsteriaDockerTask.*

/**
 * Bootstrap class for asteria-docker plugin
 */
class AsteriaDockerPlugin implements Plugin<Project> {

    final static String PLUGIN_NAME = "asteriaDocker"
    final static String GROUP = "Asteria Docker"

    @Override
    void apply(Project project) {
        applyJavaPlugin(project)
        AsteriaDockerExtension ext = project.extensions.create(PLUGIN_NAME, AsteriaDockerExtension, project)
        createTasks(project, ext)
    }
    
    /**
     * Creates all supported tasks
     * @param project
     * @param ext
     */
    private static void createTasks(Project project, AsteriaDockerExtension ext) {
        
        CopyDockerfileTask copyDockerfileTask = project.tasks.create(DOCKER_COPY.taskName, CopyDockerfileTask) {
            it.extension = ext
            it.group = GROUP
            it.description = DOCKER_COPY.description
        }
        
        CopyArtifactsTask copyArtifactsTask = project.tasks.create(DOCKER_COPY_ARTIFACTS.taskName, CopyArtifactsTask) {
            it.extension = ext
            it.group = GROUP
            it.description = DOCKER_COPY.description
        }
        
        CopyAdditionalFilesTask copyAdditionalFilesTask = project.tasks.create(DOCKER_COPY_ADDITIONAL_FILES.taskName,
                CopyAdditionalFilesTask) {
            it.extension = ext
            it.group = GROUP
            it.description = DOCKER_COPY_ADDITIONAL_FILES.description
        }
        
        DockerBuildTask dockerBuildTask = project.tasks.create(DOCKER_BUILD.taskName, DockerBuildTask) {
            it.extension = ext
            it.group = GROUP
            it.description = DOCKER_BUILD.description
        }
        
        DockerPushTask dockerPushTask = project.tasks.create(DOCKER_PUSH.taskName, DockerPushTask) {
            it.extension = ext
            it.group = GROUP
            it.description = DOCKER_PUSH.description
        }
        
        // Build dependsOn chain
        copyArtifactsTask.dependsOn(copyDockerfileTask)
        copyAdditionalFilesTask.dependsOn(copyArtifactsTask)
        dockerBuildTask.dependsOn(copyAdditionalFilesTask)
        dockerPushTask.dependsOn(dockerBuildTask)
    }
    
    /**
     * Applies the Java Plugin if not already applied
     * @param project
     */
    private static void applyJavaPlugin(Project project) {
        if (!project.pluginManager.hasPlugin("java")) {
            project.pluginManager.apply(JavaPlugin)
        }
    }
}
