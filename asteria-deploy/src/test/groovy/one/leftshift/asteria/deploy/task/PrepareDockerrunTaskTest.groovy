package one.leftshift.asteria.deploy.task

import groovy.json.JsonSlurper
import one.leftshift.asteria.deploy.cloud.config.EnrichedConfig
import one.leftshift.asteria.deploy.test.util.ProjectDescription
import one.leftshift.asteria.deploy.test.util.TestDefaults
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

import static one.leftshift.asteria.deploy.AsteriaDeployTask.PREPARE_DOCKER_RUN
import static one.leftshift.asteria.deploy.test.util.TestDefaults.Default.PLUGIN
import static org.gradle.testkit.runner.TaskOutcome.FAILED

class PrepareDockerrunTaskTest extends Specification {

    void "missing gradle property throws exception and FAILS"() {
        given:
            ProjectDescription pd = TestDefaults.createBasicProjectStructureStub(PLUGIN.stringValue)
            copyTestFilesToProjectDir(pd.project)
        when:
            BuildResult result = GradleRunner.create()
                    .withProjectDir(pd.project.projectDir)
                    .withArguments("build", PREPARE_DOCKER_RUN.taskName, "-s")
                    .withDebug(true)
                    .withPluginClasspath()
                    .buildAndFail()
        then:
            result.task(":${PREPARE_DOCKER_RUN.taskName}").outcome == FAILED
        cleanup:
            pd.project.projectDir.deleteDir()
    }

    void "missing config file throws exception and FAILS"() {
        given:
            ProjectDescription pd = TestDefaults.createBasicProjectStructureStub(PLUGIN.stringValue)
        when:
            BuildResult result = GradleRunner.create()
                    .withProjectDir(pd.project.projectDir)
                    .withArguments("build", PREPARE_DOCKER_RUN.taskName)
                    .withDebug(true)
                    .withPluginClasspath()
                    .buildAndFail()
        then:
            result.task(":${PREPARE_DOCKER_RUN.taskName}").outcome == FAILED
        cleanup:
            pd.project.projectDir.deleteDir()
    }

    void "expected files exists and contain the expected values"() {
        given:
            ProjectDescription pd = TestDefaults.createBasicProjectStructureStub(PLUGIN.stringValue)
            copyTestFilesToProjectDir(pd.project)
        when:
            BuildResult buildResult = GradleRunner.create()
                    .withProjectDir(pd.project.projectDir)
                    .withArguments("build", PREPARE_DOCKER_RUN.taskName,
                    "-P${EnrichedConfig.DEPLOYMENT_CONFIG_PROPERTY}=snapshot", "-s")
                    .withDebug(true)
                    .withPluginClasspath()
                    .build()
            def result = new File("${pd.project.buildDir}/deployment/snapshot/Dockerrun.aws.json")
        then:
            result.exists()
            def slurper = new JsonSlurper()
            def config = slurper.parse(result)
            config.containerDefinitions.image[0] == "007098893018.dkr.ecr.eu-central-1.amazonaws.com/gaia-web:1.2.3-CONFIGURED"
        cleanup:
            pd.project.projectDir.deleteDir()
    }

    void copyTestFilesToProjectDir(Project p) {
        ["Dockerrun.aws.json", "deployment.config"].each { String it ->
            Files.copy(
                    Paths.get(getClass().getClassLoader().getResource(it).toURI()),
                    Paths.get(p.projectDir.path, it))
        }
    }
}
