package one.leftshift.asteria.tasks

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.LoggingPushHandler
import one.leftshift.asteria.AsteriaDockerExtension
import one.leftshift.asteria.docker.DockerTagResolver
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

    @Option(description = "Sets an explicit tag of the docker image overriding version inference and version prefix. If the explicitTag is a branch name, the according ticket number is inferred.")
    String explicitTag

    @TaskAction
    void push() {
        final DockerClient dockerClient = DockerClientFactory.getClient(Registry.AMAZON_ECR)
        final DockerTagResolver versionResolver = new DockerTagResolver(extension, explicitTag)
        final DockerPushTaskExecution execution = new DockerPushTaskExecution(dockerClient, extension, versionResolver.resolve())
        execution.execute()
    }

    static class DockerPushTaskExecution {
        private final DockerClient dockerClient
        private final AsteriaDockerExtension extension
        private final String tag

        DockerPushTaskExecution(DockerClient dockerClient, AsteriaDockerExtension extension, String tag) {
            this.dockerClient = dockerClient
            this.extension = extension
            this.tag = tag
        }

        void execute() {
            dockerClient.push("${extension?.repositoryURI}/${extension?.name}:$tag", new LoggingPushHandler("${extension?.name}"))

            if (extension.withLatestTag) {
                final String latestTag = "${extension?.repositoryURI}/${extension?.name}:latest"
                dockerClient.tag("${extension?.repositoryURI}/${extension?.name}:$tag", latestTag)
                dockerClient.push("${extension?.repositoryURI}/${extension?.name}:latest", new LoggingPushHandler("${extension?.name}"))
            }
        }
    }
}
