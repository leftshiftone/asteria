package one.leftshift.asteria

/**
 * Contains all available asteria-docker tasks and their corresponding name
 */
enum AsteriaDockerTask {

    DOCKER_COPY("dockerCopy", "Copies the Dockerfile to project.buildDir/docker"),
    DOCKER_COPY_ARTIFACTS("dockerCopyArtifacts", "Copies the JAR to project.buildDir/docker"),
    DOCKER_COPY_ADDITIONAL_FILES("dockerCopyAdditionalFiles", "Copies all additionally specified files to project.buildDir/docker"),
    DOCKER_BUILD("dockerBuild", "Triggers a docker build"),
    DOCKER_PUSH("dockerPush", "Triggers a docker push")


    final String taskName
    final String description

    AsteriaDockerTask(String taskName, String description) {
        this.taskName = taskName
        this.description = description
    }
}