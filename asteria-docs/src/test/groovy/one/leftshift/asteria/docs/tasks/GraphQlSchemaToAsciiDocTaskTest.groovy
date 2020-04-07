package one.leftshift.asteria.docs.tasks

import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class GraphQlSchemaToAsciiDocTaskTest extends Specification {

    def "graphql asciidoc file has been written"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/graphql/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("graphqlToAsciidoc", "--schemaFilePath=${project.projectDir}/project-docs/schema.json", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def graphqlApiFile = new File("${project.projectDir}/project-docs/build/asciidoc/graphql-api.adoc")

        then:
            result.output.contains "BUILD SUCCESSFUL"
            graphqlApiFile.exists()
            graphqlApiFile.size() > 1
    }

    def "graphql schema is converted to asciidoc"() {
        given:
            def schema = new JsonSlurper().parse(getClass().getResourceAsStream("/graphql/test-schema.json"))
        when:
            def result = GraphQlSchemaToAsciiDocTask.convertGraphQlSchemaToAsciidoc(schema)
        then:
            result == getClass().getResource("/graphql/test-schema.adoc").text
    }
}
