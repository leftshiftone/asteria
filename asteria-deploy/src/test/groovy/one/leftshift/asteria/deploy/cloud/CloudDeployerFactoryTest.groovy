package one.leftshift.asteria.deploy.cloud

import com.typesafe.config.Config
import one.leftshift.asteria.deploy.cloud.config.AWSElasticbeanstalkConfigAttribute
import org.gradle.api.Project
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Unroll

import static one.leftshift.asteria.deploy.cloud.ProjectProperty.DRY_RUN

@IgnoreIf({ System.getenv("CI") })
class CloudDeployerFactoryTest extends Specification {

    void "throws exception if cloudProvider is null"() {
        when:
            CloudDeployerFactory.get(null)
        then:
            thrown(IllegalArgumentException)
    }

    @SuppressWarnings("GroovyPointlessBoolean")
    @Unroll
    void "returns #clazz.getSimpleName() if dryRun = #dryRun"() {
        given:
            Project stub = Stub(Project).with { it.hasProperty(DRY_RUN.strValue()) >> dryRun; it }
            CloudDeployerRequest request = CloudDeployerRequest
                    .builder()
                    .cloudProvider(SupportedCloudProvider.AWS)
                    .config(MockConfig())
                    .project(stub).build()
        when:
            def result = CloudDeployerFactory.get(request)
        then:
            result.getClass() == clazz
        where:
            dryRun || clazz
            false  || AWSElasticbeanstalkDeployer
            true   || DryRunCloudDeployer
    }

    void "sets aws region if present in configuration file"() {
        given:
            Project projectStub = Stub(Project)
            Config configStub = Mock(Config)
            configStub.hasPath(_) >> true
            CloudDeployerRequest request = CloudDeployerRequest
                    .builder()
                    .cloudProvider(SupportedCloudProvider.AWS)
                    .config(configStub)
                    .project(projectStub).build()
        when:
            CloudDeployerFactory.get(request)
        then:
            2 * configStub.getString(AWSElasticbeanstalkConfigAttribute.AWS_REGION.stringValue) >> "eu-central-1"
    }

    void "uses standard region provider chain if AWS_REGION is not set"() {
        given:
            Project projectStub = Stub(Project)
            Config configStub = Mock(Config)
            CloudDeployerRequest request = CloudDeployerRequest
                    .builder()
                    .cloudProvider(SupportedCloudProvider.AWS)
                    .config(configStub)
                    .project(projectStub).build()
        when:
            CloudDeployerFactory.get(request)
        then:
            1 * configStub.hasPath(AWSElasticbeanstalkConfigAttribute.AWS_REGION.stringValue) >> false
    }

    private Config MockConfig() {
        return Mock(Config).with {
            it.getString(AWSElasticbeanstalkConfigAttribute.AWS_REGION.stringValue) >> "eu-central-1"
            it
        }
    }
}
