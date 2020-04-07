package one.leftshift.asteria.code.analytics

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class MavenPomPublishTest extends Specification {

    def "maven pom for publishing artifacts contains dependencies although code coverage plugin is applied"() {
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
            new File("${project.projectDir}/project1/build/publications/mavenJava/pom-default.xml").text.contains("""<version>unspecified</version>
  <dependencies>
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-lang3</artifactId>
      <version>3.1</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>""")
    }
}
