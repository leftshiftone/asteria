package one.leftshift.asteria.deploy.cloud

import com.amazonaws.regions.Regions
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import one.leftshift.asteria.util.Assert

import static one.leftshift.asteria.deploy.cloud.config.AWSElasticbeanstalkConfigAttribute.AWS_REGION

/**
 * Provides a {@link CloudDeployer} to a corresponding {@link SupportedCloudProvider}
 *
 */
final class CloudDeployerFactory {

    static CloudDeployer get(CloudDeployerRequest request) {
        Assert.notNull(request, "request can not be null")
        Assert.notNull(request.cloudProvider, "cloudProvider can not be null")
        Assert.notNull(request.project, "project can not be null")

        if (request.project.hasProperty(ProjectProperty.DRY_RUN.strValue())) {
            return new DryRunCloudDeployer()
        }

        switch (request.cloudProvider) {
            case SupportedCloudProvider.AWS:
                return createAwsCloudDeployer(request)
            default:
                throw new IllegalArgumentException("Cloud provider ${request.cloudProvider.toString()} not supported.")
        }
    }

    /**
     * Creates an {@link AWSElasticbeanstalkDeployer}. If the awsRegion property is set in the configuration file
     * the client is configured using this region. Otherwise the usual AWS region provider chain is used.
     * @param request
     * @return configured AWSElasticbeanstalkDeployer
     */
    private static AWSElasticbeanstalkDeployer createAwsCloudDeployer(CloudDeployerRequest request) {
        if (!request.config.hasPath(AWS_REGION.stringValue)) {
            return new AWSElasticbeanstalkDeployer(
                    AmazonS3ClientBuilder.defaultClient(),
                    AWSElasticBeanstalkClientBuilder.defaultClient())
        }
        return new AWSElasticbeanstalkDeployer(
                AmazonS3ClientBuilder.standard().withRegion(request.config.getString(AWS_REGION.stringValue)).build(),
                AWSElasticBeanstalkClientBuilder.standard()
                        .withRegion(Regions.fromName(request.config.getString(AWS_REGION.stringValue))).build())
    }

}
