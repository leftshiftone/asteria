package one.leftshift.asteria.deploy.cloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DryRunCloudDeployer implements CloudDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DryRunCloudDeployer.class)

    @Override
    DeploymentResult deploy(DeploymentRequest request) {
        LOGGER.info("DRYRUN: ")
        LOGGER.info("{}", request.toString())
        return new DeploymentResult("DRYRUN")
    }
}
