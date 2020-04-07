package one.leftshift.asteria.docker.registry

import com.amazonaws.services.ecr.AmazonECR
import com.amazonaws.services.ecr.model.AuthorizationData
import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult
import com.spotify.docker.client.messages.RegistryAuth
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import one.leftshift.asteria.util.Assert
import one.leftshift.asteria.util.Triplet
/**
 * Gathers and extracts all necessary authentication tokens to provide a {@link RegistryAuth} which can be used
 * to push/pull Docker images from AWS ECR.
 */
@PackageScope
@TypeChecked
class AmazonECRAuthProvider implements RegistryAuthProvider {

    private final AmazonECR amazonECR

    AmazonECRAuthProvider(AmazonECR amazonECR) {
        Assert.notNull(amazonECR, "AmazonECR can not be null")
        this.amazonECR = amazonECR
    }

    @Override
    RegistryAuth acquireAuth(RegistryAuthRequest authRequest) {
        final GetAuthorizationTokenResult result = amazonECR?.getAuthorizationToken(new GetAuthorizationTokenRequest())

        final Triplet<String, String, String> loginInformation = extractLoginInformation(result)

        return RegistryAuth.builder()
                .username(loginInformation.left)
                .password(loginInformation.middle)
                .serverAddress(loginInformation.right)
                .build()
    }

    private static Triplet<String, String, String> extractLoginInformation(GetAuthorizationTokenResult result) {
        if (result.getAuthorizationData().empty) {
            throw new IllegalStateException("Could not acquire AuthorizationData from AWS ECR.")
        }
        final AuthorizationData authorizationData = result.getAuthorizationData().first()
        final String[] tokens = new String(authorizationData.authorizationToken.decodeBase64()).split(":")

        return Triplet.of(tokens[0], tokens[1], authorizationData.proxyEndpoint)
    }
}
