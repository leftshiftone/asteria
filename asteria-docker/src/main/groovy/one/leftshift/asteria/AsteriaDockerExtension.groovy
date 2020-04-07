package one.leftshift.asteria

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection

/**
 * Contains all configurable parameters of {@link AsteriaDockerPlugin}
 */
class AsteriaDockerExtension {
    
    File dockerfile
    String repositoryURI
    String name
    String versionPrefix
    ConfigurableFileCollection additionalFiles
    boolean copyArtifacts = true
    boolean withLatestTag = false

    final Project project
    
    AsteriaDockerExtension(Project project) {
        this.project = project
        this.dockerfile = project.file("Dockerfile")
        this.name = project.name
    }
}
