package one.leftshift.asteria.tasks

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.LoggingPushHandler
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

/**
 * Pushes a Docker image to a specified remote repository
 */
class DockerPushTask extends DefaultTask {

    @Input
    AsteriaDockerExtension extension

    @TaskAction
    void push() {
        final DockerClient dockerClient = DockerClientFactory.getClient(Registry.AMAZON_ECR)
        final DockerPushTaskExecution execution = new DockerPushTaskExecution(dockerClient, extension)
        execution.execute()
    }

    static class DockerPushTaskExecution {
        private final DockerClient dockerClient
        private final AsteriaDockerExtension extension

        DockerPushTaskExecution(DockerClient dockerClient, AsteriaDockerExtension extension) {
            this.dockerClient = dockerClient
            this.extension = extension
        }

        void execute() {
            final String extractedVersion = VersionExtractor
                    .defaultExtractor()
                    .addStrategies(SnapshotExtractionStrategy.instance, ReleaseExtractionStrategy.instance)
                    .extractVersion(BuildProperties.from(BuildPropertiesResolver.resolve(extension.project)), extension.project.version as String)
            final String version = extension?.versionPrefix ? "${extension.versionPrefix}$extractedVersion" : extractedVersion

            dockerClient.push("${extension?.repositoryURI}/${extension?.name}:$version", new LoggingPushHandler("${extension?.name}"))

            if (extension.withLatestTag) {
                final String latestTag = "${extension?.repositoryURI}/${extension?.name}:latest"
                dockerClient.tag("${extension?.repositoryURI}/${extension?.name}:$version", latestTag)
                dockerClient.push("${extension?.repositoryURI}/${extension?.name}:latest", new LoggingPushHandler("${extension?.name}"))
            }
        }
    }
}
