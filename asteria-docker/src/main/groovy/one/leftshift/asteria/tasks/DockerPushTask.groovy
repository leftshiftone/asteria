package one.leftshift.asteria.tasks

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.LoggingPushHandler
import one.leftshift.asteria.AsteriaDockerExtension
import one.leftshift.asteria.docker.VersionResolver
import one.leftshift.asteria.docker.client.DockerClientFactory
import one.leftshift.asteria.docker.registry.Registry
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * Pushes a Docker image to a specified remote repository
 */
class DockerPushTask extends DefaultTask {

    @Input
    AsteriaDockerExtension extension

    @Option(description = "Sets an explicit version of the docker image overriding version inference and version prefix.")
    String explicitVersion

    @TaskAction
    void push() {
        final DockerClient dockerClient = DockerClientFactory.getClient(Registry.AMAZON_ECR)
        final VersionResolver versionResolver = new VersionResolver(extension, explicitVersion)
        final DockerPushTaskExecution execution = new DockerPushTaskExecution(dockerClient, extension, versionResolver.resolve())
        execution.execute()
    }

    static class DockerPushTaskExecution {
        private final DockerClient dockerClient
        private final AsteriaDockerExtension extension
        private final String version

        DockerPushTaskExecution(DockerClient dockerClient, AsteriaDockerExtension extension, String version) {
            this.dockerClient = dockerClient
            this.extension = extension
            this.version = version
        }

        void execute() {
            dockerClient.push("${extension?.repositoryURI}/${extension?.name}:$version", new LoggingPushHandler("${extension?.name}"))

            if (extension.withLatestTag) {
                final String latestTag = "${extension?.repositoryURI}/${extension?.name}:latest"
                dockerClient.tag("${extension?.repositoryURI}/${extension?.name}:$version", latestTag)
                dockerClient.push("${extension?.repositoryURI}/${extension?.name}:latest", new LoggingPushHandler("${extension?.name}"))
            }
        }
    }
}
