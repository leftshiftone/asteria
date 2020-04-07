package one.leftshift.asteria.docker.registry

import com.spotify.docker.client.messages.RegistryAuth

/**
 * A {@link RegistryAuthProvider} provides a simple interface to trigger a login to docker registries such as ECR or Docker Hub
 */
interface RegistryAuthProvider {

    /**
     * Acquires a {@link RegistryAuth} from a specified {@link RegistryAuthRequest}
     * @param authRequest
     * @return
     */
    RegistryAuth acquireAuth(RegistryAuthRequest authRequest)

}