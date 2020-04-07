package one.leftshift.asteria.docs.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class DocsJarTaskTest extends Specification {

    def "docs jar is available"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("docsJar", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def docsJar = new File("${project.projectDir.absolutePath}/project-docs/build/libs/project-docs-0.0.1-SNAPSHOT-docs.jar")

        then:
            result.output.contains "BUILD SUCCESSFUL"
            docsJar.exists()
            docsJar.size() > 1
    }
}
