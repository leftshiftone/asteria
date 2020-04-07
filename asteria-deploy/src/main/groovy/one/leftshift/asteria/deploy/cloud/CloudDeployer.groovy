package one.leftshift.asteria.deploy.cloud

/**
 * Deploys a {@link DeploymentRequest#deployable} to a cloud provider such as Amazon AWS
 * @see {@link AWSElasticbeanstalkDeployer}
 *
 */
interface CloudDeployer {

    /**
     * Triggers the necessary deployment steps
     * @param request
     * @return {@link DeploymentResult}
     */
    DeploymentResult deploy(DeploymentRequest request)
}