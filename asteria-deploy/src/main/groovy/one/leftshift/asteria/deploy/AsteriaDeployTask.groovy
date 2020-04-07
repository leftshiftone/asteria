package one.leftshift.asteria.deploy

enum AsteriaDeployTask {

    PREPARE_DOCKER_RUN("prepareDockerrun", "Replaces tokens in the deploymentDescription file and" +
            " copies the resolved file to the project.buildDir/Dockerrun.aws.json"),
    DEPLOY_ELASTICBEANSTALK("deployElasticbeanstalk", " Deploys a ZIP containging the " +
            "deploymentDescription file to AWS Elasticbeanstalk")

    final String taskName
    final String description

    AsteriaDeployTask(String taskName, String description) {
        this.taskName = taskName
        this.description = description
    }
}
