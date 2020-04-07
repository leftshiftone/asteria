package one.leftshift.asteria.deploy.cloud

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.*
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PutObjectResult
import com.typesafe.config.Config
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import one.leftshift.asteria.util.Assert
import one.leftshift.asteria.util.FileUtils

import static one.leftshift.asteria.deploy.cloud.config.AWSElasticbeanstalkConfigAttribute.*

/**
 * Takes all necessary steps to deploy a {@link DeploymentRequest#deployable} to Amazon Elasticbeanstalk
 *  1. upload to s3
 *  2. create an application version
 *  3. update the environment
 *
 */
@PackageScope
@TypeChecked
class AWSElasticbeanstalkDeployer implements CloudDeployer {
    private final AmazonS3 s3client
    private final AWSElasticBeanstalk awsElasticBeanstalk

    AWSElasticbeanstalkDeployer(AmazonS3 s3client, AWSElasticBeanstalk awsElasticBeanstalk) {
        this.s3client = s3client
        this.awsElasticBeanstalk = awsElasticBeanstalk
    }

    @Override
    DeploymentResult deploy(DeploymentRequest request) {
        Assert.notNull(request, "DeploymentRequest can not be null")
        uploadToS3(request)
        createApplicationVersion(request)
        UpdateEnvironmentResult updateEnvironmentResult = updateEnvironment(request)

        return new DeploymentResult(updateEnvironmentResult?.getStatus())
    }

    private PutObjectResult uploadToS3(DeploymentRequest request) {
        final Config config = request.config
        return s3client.putObject(config.getString(BUCKET_NAME.stringValue),
                request.deployable?.toFile()?.name,
                request.deployable?.toFile())
    }

    private CreateApplicationVersionResult createApplicationVersion(DeploymentRequest request) {
        final Config config = request.config
        return awsElasticBeanstalk.createApplicationVersion(
                new CreateApplicationVersionRequest(
                        config.getString(APPLICATION_NAME.stringValue),
                        getVersionLabel(request))
                        .withSourceBundle(new S3Location(config.getString(BUCKET_NAME.stringValue),
                        request.deployable?.toFile()?.getName()))
        )
    }

    private UpdateEnvironmentResult updateEnvironment(DeploymentRequest request) {
        return awsElasticBeanstalk.updateEnvironment(
                new UpdateEnvironmentRequest()
                        .withEnvironmentName(request.config.getString(ENVIRONMENT_NAME.stringValue))
                        .withVersionLabel(getVersionLabel(request)))
    }

    private static String getVersionLabel(DeploymentRequest request) {
        return request.config.getStringOrDefault(VERSION_LABEL.stringValue,
                FileUtils.getBaseName(request.deployable.toFile())) ?: ""
    }
}
