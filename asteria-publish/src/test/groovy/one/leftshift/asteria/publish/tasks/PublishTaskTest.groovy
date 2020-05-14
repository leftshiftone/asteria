package one.leftshift.asteria.publish.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Ignore
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
                    .withArguments("clean", "build", "publishToMavenLocal", "--debug", "--stacktrace")
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

    //todo: test master branch

    @Ignore
    def "custom snapshot repository is used for feature branch"() {
        given:
            //todo: test with test project
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            File projectBuildFile = new File(projectDir, "build.gradle")
            projectBuildFile << """
            plugins {
                id "java"
                id "one.leftshift.asteria-publish"
            }
            asteriaPublish {
                snapshotRepositoryUrl = "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/test-snapshots"
                enableBranchSnapshotRepositories = true
                createSnapshotRepositories = false
            }
            """.stripIndent()

            File javaSrcDir = new File("${projectDir.absolutePath}/src/main/java")
            javaSrcDir.mkdirs()
            new File(javaSrcDir, "Foo.java").createNewFile()
            File resourcesDir = new File("${projectDir.absolutePath}/src/main/resources")
            resourcesDir.mkdirs()

            def initGit = new ProcessBuilder(["git", "init"]).directory(projectDir).start()
            initGit.waitForProcessOutput(System.out, System.err)
            def configEmailGit = new ProcessBuilder(["git", "config", "user.email", "pipelines@bitbucket.org"]).directory(projectDir).start()
            configEmailGit.waitForProcessOutput(System.out, System.err)
            def configNameGit = new ProcessBuilder(["git", "config", "user.name", "Bitbucket Pipelines"]).directory(projectDir).start()
            configNameGit.waitForProcessOutput(System.out, System.err)
            def stageGit = new ProcessBuilder(["git", "add", "--all"]).directory(projectDir).start()
            stageGit.waitForProcessOutput(System.out, System.err)
            def commitGit = new ProcessBuilder(["git", "commit", "-m", "initial"]).directory(projectDir).start()
            commitGit.waitForProcessOutput(System.out, System.err)
            def branchGit = new ProcessBuilder(["git", "checkout", "-b", "feature/FOO-666"]).directory(projectDir).start()
            branchGit.waitForProcessOutput(System.out, System.err)

        when:
            String result = GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withArguments("assemble", "publishToMavenLocal", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build().output

        then:
            result.contains "Currently on branch feature/FOO-666"
            result.contains "Snapshot repositories for branches are enabled"
            result.contains "Using snapshot repository url s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/test-snapshots-foo-666"
    }

}
