package one.leftshift.asteria.publish.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class SourceJarTaskTest extends Specification {

    def "task publish is available"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "sourceJar", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            def sourceJar = new File("${project.projectDir.absolutePath}/project1/build/libs/project1-0.0.1-SNAPSHOT-sources.jar")
            sourceJar.exists()
            sourceJar.size() > 1
    }
}
