package one.leftshift.asteria.deploy

import one.leftshift.asteria.deploy.task.DeployElasticbeanstalkTask
import one.leftshift.asteria.deploy.task.PrepareDockerrunTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import static one.leftshift.asteria.deploy.AsteriaDeployTask.DEPLOY_ELASTICBEANSTALK
import static one.leftshift.asteria.deploy.AsteriaDeployTask.PREPARE_DOCKER_RUN

/**
 * Bootstrap class for asteria-deploy
 */
class AsteriaDeployPlugin implements Plugin<Project> {

    private static final String GROUP = "Asteria Deploy"

    @Override
    void apply(Project project) {
        createTasks(project)
    }

    private static void createTasks(Project project) {
        PrepareDockerrunTask prepareDockerrunTask = project.tasks.create(
                PREPARE_DOCKER_RUN.taskName,
                PrepareDockerrunTask) {
            it.group = GROUP
            it.description = PREPARE_DOCKER_RUN.description
        }

        DeployElasticbeanstalkTask deployAmazonEBTask = project.tasks.create(
                DEPLOY_ELASTICBEANSTALK.taskName,
                DeployElasticbeanstalkTask) {
            it.group = GROUP
            it.description = DEPLOY_ELASTICBEANSTALK.description
        }

        deployAmazonEBTask.dependsOn(prepareDockerrunTask)
    }
}
