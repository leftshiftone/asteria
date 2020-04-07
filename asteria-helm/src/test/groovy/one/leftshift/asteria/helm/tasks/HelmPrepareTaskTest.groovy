package one.leftshift.asteria.helm.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class HelmPrepareTaskTest extends Specification {

    def "helm prepare is copying files and replaces placeholders"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("helmPrepare", "-PdeploymentPropertiesFile=${project.projectDir}/build.properties", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def helmSource = new File("${project.projectDir.absolutePath}/project-deployment/build/helm")
            def chartVersionFile = new File("${project.projectDir.absolutePath}/project-deployment/build/helm/chart-version.txt")
            def chartFile = new File(helmSource, "foo/Chart.yaml")

        then:
            result.output.contains "BUILD SUCCESSFUL"
            helmSource.exists()
            chartFile.text.contains("appVersion: \"0.4.0-dev.20180619T211755Z.289eea3\"")
            chartFile.text.contains("version: \"0.2.0-dev.20180619T211755Z.289eea3\"")
            chartVersionFile.exists()
            chartVersionFile.text == "0.2.0-dev.20180619T211755Z.289eea3"
    }
}
