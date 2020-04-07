package one.leftshift.asteria.docker.client.exception

class DockerUnverifyableAuthException extends RuntimeException {
    DockerUnverifyableAuthException() {
        super()
    }

    DockerUnverifyableAuthException(String message) {
        super(message)
    }
}
