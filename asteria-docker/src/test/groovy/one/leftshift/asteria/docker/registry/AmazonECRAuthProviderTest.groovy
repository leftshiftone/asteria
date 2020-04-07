package one.leftshift.asteria.docker.registry

import com.amazonaws.services.ecr.AmazonECR
import com.amazonaws.services.ecr.model.AuthorizationData
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult
import spock.lang.Specification
import spock.lang.Subject

class AmazonECRAuthProviderTest extends Specification {

    @Subject
    AmazonECRAuthProvider classUnderTest

    AmazonECR mockedAmazonECR

    void setup() {
        mockedAmazonECR = Stub(AmazonECR)
        classUnderTest = new AmazonECRAuthProvider(mockedAmazonECR)
    }

    void "extracts login information as expected"() {
        given:
            mockedAmazonECR.getAuthorizationToken(_) >> GetAuthorizationTokenResult()
        when:
            //@formatter:off
            def result = classUnderTest.acquireAuth(RegistryAuthRequest.builder()
                    .build()
            )
            //@formatter:on
        then:
            result.username() == "username"
            result.password() == "password"
            result.serverAddress() == "http://someurl.com"
    }

    void "empty authorizationData throws exception"() {
        given:
            mockedAmazonECR.getAuthorizationToken(_) >> new GetAuthorizationTokenResult().withAuthorizationData()
        when:
            //@formatter:off
            def result = classUnderTest.acquireAuth(RegistryAuthRequest.builder()
                    .build()
            )
            //@formatter:on
        then:
            thrown(IllegalStateException)
    }

    void "disallows null values"() {
        when:
            classUnderTest = new AmazonECRAuthProvider(null)
        then:
            thrown(IllegalArgumentException)
    }

    private static GetAuthorizationTokenResult GetAuthorizationTokenResult() {
        return new GetAuthorizationTokenResult().withAuthorizationData(new AuthorizationData().with {
            it.proxyEndpoint = "http://someurl.com"
            it.authorizationToken = Base64.encoder.encodeToString("username:password".bytes)
            it
        })
    }
}
