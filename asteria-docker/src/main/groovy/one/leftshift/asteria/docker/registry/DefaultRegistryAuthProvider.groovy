package one.leftshift.asteria.docker.registry

import com.spotify.docker.client.messages.RegistryAuth
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import one.leftshift.asteria.util.Assert

/**
 * Provides a default {@link RegistryAuth} instance by using the specified username, password and serverAddress
 * from an {@link RegistryAuthRequest}.
 */
@PackageScope
@CompileStatic
class DefaultRegistryAuthProvider implements RegistryAuthProvider {
    @Override
    RegistryAuth acquireAuth(RegistryAuthRequest authRequest) {
        Assert.notNull(authRequest?.username, "Username can not be null")
        Assert.notNull(authRequest?.password, "Password can not be null")
        Assert.notNull(authRequest?.serverAddress, "Username can not be null")

        return RegistryAuth.builder()
                .username(authRequest.username)
                .password(authRequest.password)
                .serverAddress(authRequest.serverAddress)
                .build()
    }
}
