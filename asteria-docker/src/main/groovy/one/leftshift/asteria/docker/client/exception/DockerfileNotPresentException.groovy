package one.leftshift.asteria.docker.client.exception

/**
 * Indicates a missing Dockerfile
 */
class DockerfileNotPresentException extends RuntimeException {
    
    DockerfileNotPresentException(String message) {
        super(message)
    }
}
