package one.leftshift.asteria.docker.client.exception

/**
 * Thrown when the buildDir is not existent.
 */
class DockerBuildDirInexistentException extends RuntimeException {
    DockerBuildDirInexistentException(String message) {
        super(message)
    }
}
