package one.leftshift.asteria.deploy.cloud.config

/**
 * Contains all available elasticbeanstalk configuration parameters
 *
 */
enum AWSElasticbeanstalkConfigAttribute implements ConfigAttribute {

    BUCKET_NAME("bucketName"),
    APPLICATION_NAME("applicationName"),
    VERSION_LABEL("versionLabel"),
    ENVIRONMENT_NAME("environmentName"),
    DOCKER_RUN_LOCATION("dockerrunLocation"),
    AWS_REGION("awsRegion"),
    ROOT_PATH("elasticbeanstalk")

    final String stringValue

    AWSElasticbeanstalkConfigAttribute(String stringValue) {
        this.stringValue = stringValue
    }
}
