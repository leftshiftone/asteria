package one.leftshift.asteria.tasks

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.LoggingBuildHandler
import one.leftshift.asteria.AsteriaDockerExtension
import one.leftshift.asteria.common.BuildProperties
import one.leftshift.asteria.common.BuildPropertiesResolver
import one.leftshift.asteria.common.version.ReleaseExtractionStrategy
import one.leftshift.asteria.common.version.SnapshotExtractionStrategy
import one.leftshift.asteria.common.version.VersionExtractor
import one.leftshift.asteria.docker.client.DockerClientFactory
import one.leftshift.asteria.docker.registry.Registry
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

/**
 * Triggers a docker build
 */
class DockerBuildTask extends DefaultTask {

    @Input
    AsteriaDockerExtension extension

    @TaskAction
    void dockerBuild() {
        final DockerBuildTaskExecution execution = new DockerBuildTaskExecution(
                DockerClientFactory.getClient(Registry.AMAZON_ECR),
                extension
        )
        execution.execute()
    }

    /**
     * Execution class wraps the task logic to provide easy unit testing and increase my happiness.
     */
    static class DockerBuildTaskExecution {
        private final DockerClient dockerClient
        private final AsteriaDockerExtension extension

        DockerBuildTaskExecution(DockerClient dockerClient, AsteriaDockerExtension extension) {
            this.dockerClient = dockerClient
            this.extension = extension
        }

        void execute() {
            final String extractedVersion = VersionExtractor
                    .defaultExtractor()
                    .addStrategies(SnapshotExtractionStrategy.instance, ReleaseExtractionStrategy.instance)
                    .extractVersion(BuildProperties.from(BuildPropertiesResolver.resolve(extension.project)), extension.project.version as String)
            final String version = extension?.versionPrefix ? "${extension.versionPrefix}$extractedVersion" : extractedVersion
            extension.project.logger.quiet("Using version $version for image tag")
            dockerClient.build(Paths.get(extension.project.buildDir.toString(), "docker"),
                    new LoggingBuildHandler(),
                    DockerClient.BuildParam.name("${extension?.repositoryURI}/${extension?.name}:$version"))
        }
    }
}
