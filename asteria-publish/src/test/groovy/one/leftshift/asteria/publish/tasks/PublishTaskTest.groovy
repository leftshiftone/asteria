package one.leftshift.asteria.publish.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class PublishTaskTest extends Specification {

    def setup() {
        new File("${System.getenv("HOME")}/.m2/repository/com/example/test").deleteDir()
    }

    def "pom with dependencies is published to local maven repo"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "publishToMavenLocal", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"

            def pomFileProject1 = new File("${System.getenv("HOME")}/.m2/repository/com/example/test/project1/0.0.1-SNAPSHOT/project1-0.0.1-SNAPSHOT.pom")
            pomFileProject1.exists()
            pomFileProject1.text.contains("<artifactId>junit</artifactId>")
    }



    def "pom with dependencies is published to local maven repo when spring dependency plugin is enabled"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/springTestProject")
            }

        when:

            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("clean","build", "publishToMavenLocal", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"

            def pomFileProject1 = new File("${System.getenv("HOME")}/.m2/repository/com/example/test/project1/0.0.1-SNAPSHOT/project1-0.0.1-SNAPSHOT.pom")
            pomFileProject1.exists()
            pomFileProject1.text.contains(
                    """<version>0.0.1-SNAPSHOT</version>
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <scope>compile</scope>
      <version>4.12</version>
    </dependency>
  </dependencies>""")
    }
}
