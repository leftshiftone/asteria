package one.leftshift.asteria.deploy.cloud.config

/**
 * Thrown if -Pdeployment.mode is not supplied.
 */
class MissingDeploymentModeException extends RuntimeException {
    MissingDeploymentModeException(String message) {
        super(message)
    }
}
