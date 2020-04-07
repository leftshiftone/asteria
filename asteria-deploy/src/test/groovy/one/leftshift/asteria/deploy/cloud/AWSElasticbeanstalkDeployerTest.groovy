package one.leftshift.asteria.deploy.cloud

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest
import com.amazonaws.services.elasticbeanstalk.model.S3Location
import com.amazonaws.services.s3.AmazonS3
import com.typesafe.config.Config
import one.leftshift.asteria.deploy.cloud.config.AWSElasticbeanstalkConfigAttribute
import one.leftshift.asteria.deploy.cloud.config.EnrichedConfig
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Path

class AWSElasticbeanstalkDeployerTest extends Specification {

    @Subject
    AWSElasticbeanstalkDeployer classUnderTest

    void "triggers expected deployment steps"() {
        given:
            AmazonS3 mockedS3 = Mock(AmazonS3)
            AWSElasticBeanstalk mockedEb = Mock(AWSElasticBeanstalk)
            classUnderTest = new AWSElasticbeanstalkDeployer(mockedS3, mockedEb)
        when:
            classUnderTest.deploy(DeploymentRequest.builder()
                    .config(StubConfig())
                    .deployable(Mock(Path).with { it.toFile() >> new File("gaia-beta-1.0.0.jar"); it})
                    .build())
        then:
            1 * mockedS3.putObject("someBucket", "gaia-beta-1.0.0.jar", _)
            1 * mockedEb.createApplicationVersion(new CreateApplicationVersionRequest("gaia-beta",
                    "gaia-beta-1.0.0").withSourceBundle(new S3Location("someBucket",
                    "gaia-beta-1.0.0.jar")))
            1 * mockedEb.updateEnvironment(*_)

    }

    void "throws exception if request is null"() {
        given:
            classUnderTest = new AWSElasticbeanstalkDeployer(Stub(AmazonS3), Stub(AWSElasticBeanstalk))
        when:
            classUnderTest.deploy(null)
        then:
            thrown(IllegalArgumentException)
    }

    private EnrichedConfig StubConfig() {
        return EnrichedConfig.toEnriched(Stub(Config).with {
            it.getString(AWSElasticbeanstalkConfigAttribute.APPLICATION_NAME.stringValue) >> "gaia-beta"
            it.getString(AWSElasticbeanstalkConfigAttribute.ENVIRONMENT_NAME.stringValue) >> "gaia-web-beta"
            it.getString(AWSElasticbeanstalkConfigAttribute.BUCKET_NAME.stringValue) >> "someBucket"
            it.getString(AWSElasticbeanstalkConfigAttribute.AWS_REGION.stringValue) >> "eu-central-1"
            it
        })
    }

}
