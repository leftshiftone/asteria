package one.leftshift.asteria.docker.registry

import com.amazonaws.services.ecr.AmazonECRClientBuilder
import one.leftshift.asteria.util.Assert

import static one.leftshift.asteria.docker.registry.Registry.AMAZON_ECR
import static one.leftshift.asteria.docker.registry.Registry.DEFAULT

/**
 * Provides an {@link RegistryAuthProvider} based on the given {@link Registry}
 */
abstract class RegistryAuthProviderFactory {
    static RegistryAuthProvider provide(Registry registry) {
        Assert.notNull(registry, "Registry can not be null")

        switch (registry) {
            case AMAZON_ECR:
                return new AmazonECRAuthProvider(AmazonECRClientBuilder.defaultClient())
            case DEFAULT:
                return new DefaultRegistryAuthProvider()
            default:
                // not reachable
                throw new IllegalStateException()
        }
    }
}
