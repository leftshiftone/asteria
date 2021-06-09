package one.leftshift.asteria.tasks

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.LoggingBuildHandler
import one.leftshift.asteria.AsteriaDockerExtension
import one.leftshift.asteria.docker.DockerTagResolver
import one.leftshift.asteria.docker.client.DockerClientFactory
import one.leftshift.asteria.docker.registry.Registry
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import java.nio.file.Paths

/**
 * Triggers a docker build
 */
class DockerBuildTask extends DefaultTask {

    @Input
    AsteriaDockerExtension extension

    @Option(description = "Sets an explicit tag of the docker image overriding version inference and version prefix. If the explicitTag is a branch name, the according ticket number is inferred.")
    @Input
    String explicitTag = null

    @TaskAction
    void dockerBuild() {
        final DockerTagResolver versionResolver = new DockerTagResolver(extension, explicitTag)
        final DockerBuildTaskExecution execution = new DockerBuildTaskExecution(
                DockerClientFactory.getClient(Registry.AMAZON_ECR),
                extension, versionResolver.resolve()
        )
        execution.execute()
    }

    /**
     * Execution class wraps the task logic to provide easy unit testing and increase my happiness.
     */
    static class DockerBuildTaskExecution {
        private final DockerClient dockerClient
        private final AsteriaDockerExtension extension
        private final String tag

        DockerBuildTaskExecution(DockerClient dockerClient, AsteriaDockerExtension extension, String tag) {
            this.dockerClient = dockerClient
            this.extension = extension
            this.tag = tag
        }

        void execute() {
            List<DockerClient.BuildParam> params = [DockerClient.BuildParam.name("${extension?.repositoryURI}/${extension?.name}:$tag")] as LinkedList

            extension.buildParameters?.forEach { parameter ->
                params.add(DockerClient.BuildParam.create("buildargs", URLEncoder.encode(parameter, "UTF-8")))
            }


            extension.project.logger.quiet("Using version $tag for image tag")
            dockerClient.build(Paths.get(extension.project.buildDir.toString(), "docker"),
                    new LoggingBuildHandler(), *params)
        }
    }
}
