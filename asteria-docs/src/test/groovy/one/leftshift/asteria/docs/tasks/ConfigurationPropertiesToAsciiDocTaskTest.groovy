package one.leftshift.asteria.docs.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

/**
 * @author Michael Mair
 */
class ConfigurationPropertiesToAsciiDocTaskTest extends Specification {

    def "configuration asciidoc file has been written"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/configProps/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("configToAsciidoc", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def configFile = new File("${project.projectDir}/project-docs/build/asciidoc/generated/configuration.adoc")

        then:
            result.output.contains "BUILD SUCCESSFUL"
            configFile.exists()
            configFile.size() > 1000
    }

}
