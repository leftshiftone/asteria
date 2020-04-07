package one.leftshift.asteria.deploy.cloud.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Paths

class EnrichedConfigTest extends Specification {

    @Subject
    EnrichedConfig classUnderTest

    @Shared
    File tmpDir

    void setupSpec() {
        tmpDir = Files.createTempDirectory("xyz").toFile()
    }

    void cleanupSpec() {
        tmpDir.delete()
    }

    void "delegates to underlying config"() {
        given:
            classUnderTest = new EnrichedConfig(Mock(Config).with { it._ >> "abc"; it })
        when:
            classUnderTest.getString("abc")
        then:
            1 * classUnderTest.@delegate.getString("abc")
    }

    void "null delegate throws exception"() {
        when:
            classUnderTest = new EnrichedConfig(null)
        then:
            thrown(IllegalArgumentException)
    }

    void "config can be loaded from gradle project properties"() {
        given:
            File tmpConfig = Files.createFile(Paths.get(tmpDir.path as String, "deployment.config")).toFile()
            tmpConfig << """
                        snapshot { test = 'abc' }
                        """
            Project stubProject = Stub(Project).with {
                it.properties >> [(EnrichedConfig.DEPLOYMENT_CONFIG_PROPERTY): "snapshot"]
                it.projectDir >> tmpDir
                it
            }
        when:
            def result = EnrichedConfig.loadConfigs(stubProject)
        then:
            result != null
        cleanup:
            tmpConfig.delete()
    }

    @Unroll
    void "throws exception if config file does not exist"() {
        when:
            EnrichedConfig.loadConfigs(Stub(Project).with {
                it.projectDir >> tmpDir
                it.properties >> ["deployment.mode" : "snapshot"]
                it
            })
        then:
            thrown(IllegalArgumentException)
        cleanup:
            tmpDir.deleteDir()
    }

    void "throws exception if deployment.mode is not supplied"() {
        when:
            EnrichedConfig.loadConfigs(Stub(Project).with {
                it.projectDir >> new File(Thread.currentThread().contextClassLoader.getResource("config").toURI()); it
            })
        then:
            thrown(MissingDeploymentModeException)
    }

    void "throws exception if unknown deployment.mode is supplied"() {
        when:
            Project project = Stub(Project).with {
                it.projectDir >> new File(Thread.currentThread().contextClassLoader.getResource("config").toURI());
                it.properties >> ["deployment.mode": "xyz"]
                it
            }
            EnrichedConfig.loadConfigs(project)
        then:
            thrown(ConfigException.Missing)
    }

    @Unroll
    void "loads configs if correct deployment.mode is supplied"() {
        when:
            Project project = Stub(Project).with {
                it.projectDir >> new File(Thread.currentThread().contextClassLoader.getResource("config").toURI());
                it.properties >> ["deployment.mode": deploymentMode]
                it
            }
            EnrichedConfig.loadConfigs(project)
        then:
            noExceptionThrown()
        where:
            deploymentMode || _
            "snapshot"     || _
            "release"      || _
            "readonly"     || _
    }

    void "multiple configs can be loaded"() {
        given:
            Project project = Stub(Project).with {
                it.projectDir >> new File(Thread.currentThread().contextClassLoader.getResource("config").toURI());
                it.properties >> ["deployment.mode": "release, readonly"]
                it
            }
        when:
            def result = EnrichedConfig.loadConfigs(project)
        then:
            result.size() == 2
            result[0].left == "release"
            result[1].left == "readonly"
    }
}
