package one.leftshift.asteria.docker.registry

import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@Builder
@EqualsAndHashCode
class RegistryAuthRequest {
    String username
    String password
    String serverAddress
    Registry registry
}
