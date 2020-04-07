package one.leftshift.asteria.docker.registry

import spock.lang.Specification
import spock.lang.Subject

class DefaultRegistryAuthProviderTest extends Specification {

    @Subject
    DefaultRegistryAuthProvider classUnderTest

    void "returns expected result"() {
        given:
            classUnderTest = new DefaultRegistryAuthProvider()
        when:
            //@formatter: off
            def result = classUnderTest.acquireAuth(RegistryAuthRequest.builder()
                    .username("test")
                    .password("testpw")
                    .serverAddress("127.0.0.1:1337")
                    .build()
            )
            //@formatter: on
        then:
            result.username() == "test"
            result.password() == "testpw"
            result.serverAddress() == "127.0.0.1:1337"
    }
}
