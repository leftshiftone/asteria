package one.leftshift.asteria.deploy.task

import groovy.json.JsonSlurper
import one.leftshift.asteria.deploy.test.util.ProjectDescription
import one.leftshift.asteria.deploy.test.util.TestDefaults
import org.gradle.api.Project
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static one.leftshift.asteria.deploy.AsteriaDeployTask.DEPLOY_ELASTICBEANSTALK
import static one.leftshift.asteria.deploy.cloud.ProjectProperty.DEPLOYMENT_MODE
import static one.leftshift.asteria.deploy.cloud.ProjectProperty.DRY_RUN
import static one.leftshift.asteria.deploy.test.util.TestDefaults.Default.PLUGIN

class DeployElasticbeanstalkTaskTest extends Specification {

    void "creates expected deployment directory structure"() {
        given:
            ProjectDescription pd = TestDefaults.createBasicProjectStructureStub(PLUGIN.stringValue)
            copyTestFilesToProjectDir(pd.project)
        when:
            //@formatter:off
            GradleRunner.create()
                    .withProjectDir(pd.project.projectDir)
                    .withArguments("build",
                    DEPLOY_ELASTICBEANSTALK.taskName,
                    "-s",
                    "-P${DRY_RUN.strValue()}",
                    "-P${DEPLOYMENT_MODE.strValue()}=release,readonly"
            )
                    .withDebug(true)
                    .withPluginClasspath()
                    .build()
            //@formatter:on
        then:
            assertDeploymentZipFilesExist(pd)
            assertCorrectFileContent(pd)
        cleanup:
            pd.project.projectDir.deleteDir()
    }

    void assertDeploymentZipFilesExist(ProjectDescription pd) {
        Path releaseDir = Paths.get(pd.project.buildDir.getPath(), "deployment", "release")
        Path readonlyDir = Paths.get(pd.project.buildDir.getPath(), "deployment", "readonly")

        assert releaseDir.resolve(Paths.get("release-1.2.3-CONFIGURED.zip")).toFile().exists()
        assert releaseDir.resolve(Paths.get("release-1.2.3-CONFIGURED.zip")).toFile().isFile()
        assert readonlyDir.resolve(Paths.get("readonly-1.2.3-CONFIGURED.zip")).toFile().isFile()
        assert readonlyDir.resolve(Paths.get("readonly-1.2.3-CONFIGURED.zip")).toFile().exists()
    }

    void assertCorrectFileContent(ProjectDescription pd) {
        Path releaseDockerrun = Paths.get(pd.project.buildDir.getPath(), "deployment", "release", "Dockerrun.aws.json")
        Path readOnlyDockerrun = Paths.get(pd.project.buildDir.getPath(), "deployment", "readonly", "Dockerrun.aws.json")

        JsonSlurper slurper = new JsonSlurper()
        assert slurper.parse(releaseDockerrun.toFile()).mode == "release" && slurper.parse(readOnlyDockerrun.toFile()).mode == "readonly"
    }

    private static void copyTestFilesToProjectDir(Project project) {
        new File(Thread.currentThread().contextClassLoader.getResource("deployelasticbeanstalktest_1").toURI())
                .listFiles().each { file ->
            Files.copy(file.toPath(),
                    Paths.get(project.projectDir.toString(), file.getName()))
        }
    }
}
