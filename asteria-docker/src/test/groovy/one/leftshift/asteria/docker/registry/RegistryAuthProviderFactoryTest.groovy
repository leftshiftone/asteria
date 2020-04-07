package one.leftshift.asteria.docker.registry

import spock.lang.Ignore
import spock.lang.Specification

class RegistryAuthProviderFactoryTest extends Specification {

    @Ignore("todo: lazy")
    void "provides expected AuthProvider implementation"() {
        when:
            def result = RegistryAuthProviderFactory.provide(registry)
        then:
            result.class == expectedClass
        where:
            registry            || expectedClass
            Registry.AMAZON_ECR || AmazonECRAuthProvider
            Registry.DEFAULT    || DefaultRegistryAuthProvider
    }

    void "throws exception if registry is null"() {
        when:
            RegistryAuthProviderFactory.provide(null)
        then:
            thrown(IllegalArgumentException)
    }
}
