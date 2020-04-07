package one.leftshift.asteria.docker.client

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.auth.FixedRegistryAuthSupplier
import com.spotify.docker.client.exceptions.DockerException
import com.spotify.docker.client.messages.RegistryAuth
import com.spotify.docker.client.messages.RegistryConfigs
import groovy.transform.TypeChecked
import one.leftshift.asteria.docker.client.exception.DockerDaemonUnreachableException
import one.leftshift.asteria.docker.client.exception.DockerUnverifyableAuthException
import one.leftshift.asteria.docker.registry.Registry
import one.leftshift.asteria.docker.registry.RegistryAuthProviderFactory
import one.leftshift.asteria.docker.registry.RegistryAuthRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Provides a default {@link DockerClient} implementation by either reading the URI from $DOCKER_HOST or if not present
 * falling back to the default OS URI e.g: unix:///var/run/docker.sock
 *
 */
@TypeChecked
abstract class DockerClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(DockerClientFactory.class)

    /**
     * Provides the default {@link DockerClient} implementation. If the host does not respond to a
     * {@link DockerClient#ping} it means the host is not reachable and an exception is thrown.
     * Furthermore
     * @return a ready {@link DockerClient} instance
     */
    static DockerClient getClient(Registry registry) {
        final RegistryAuth auth = RegistryAuthProviderFactory.provide(registry)
                .acquireAuth(RegistryAuthRequest.builder().build())
        final DockerClient client = DefaultDockerClient.fromEnv()
                .registryAuthSupplier(new FixedRegistryAuthSupplier(auth,
                RegistryConfigs.create([(auth?.serverAddress()): auth])))
                .build()
        try {
            client.ping()
            checkAuth(client, auth)
        } catch (DockerException ex) {
            logger.error("Could not connect to Docker host (Docker Daemon running?)", ex)
            throw new DockerDaemonUnreachableException()
        }
        return client
    }

    private static void checkAuth(DockerClient client, RegistryAuth auth) {
        try {
            int response = client.auth(auth)

            if (response != 200) {
                logger.error("Could not verify auth information")
                throw new DockerUnverifyableAuthException()
            }
        } catch (DockerException ex) {
            logger.error("Could not verify auth information", ex)
            throw new DockerUnverifyableAuthException()
        }
    }
}
