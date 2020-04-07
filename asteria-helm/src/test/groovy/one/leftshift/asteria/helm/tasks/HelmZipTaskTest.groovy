package one.leftshift.asteria.helm.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class HelmZipTaskTest extends Specification {

    def "helm zip is available"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("helmPrepare", "helmZip", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def helmZip = new File("${project.projectDir.absolutePath}/project-deployment/build/distributions/project-deployment-0.0.1-SNAPSHOT.zip")

        then:
            result.output.contains "BUILD SUCCESSFUL"
            helmZip.exists()
            helmZip.size() > 1
    }
}
