package one.leftshift.asteria.version.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Unroll

class ReleaseTaskTest extends Specification {

    @Unroll
    def "task #task is available"() {
        given:
            Project project = ProjectBuilder.builder().build()
            def initGit = new ProcessBuilder(["git", "init"]).directory(project.projectDir).start()
            initGit.waitForProcessOutput(System.out, System.err)
            def configEmailGit = new ProcessBuilder(["git", "config", "user.email", "pipelines@bitbucket.org"]).directory(project.projectDir).start()
            configEmailGit.waitForProcessOutput(System.out, System.err)
            def configNameGit = new ProcessBuilder(["git", "config", "user.name", "Bitbucket Pipelines"]).directory(project.projectDir).start()
            configNameGit.waitForProcessOutput(System.out, System.err)

        when:
            project.pluginManager.apply "java"
            project.pluginManager.apply "one.leftshift.asteria-version"

        then:
            project.tasks."${task}" instanceof Task

        where:
            task          | _
            "devSnapshot" | _
            "snapshot"    | _
            "candidate"   | _
            "final"       | _
    }

    def "task build sets maven snapshot version"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            File projectBuildFile = new File(projectDir, "build.gradle")
            projectBuildFile << "plugins {\n"
            projectBuildFile << "    id \"java\"\n"
            projectBuildFile << "    id \"one.leftshift.asteria-version\"\n"
            projectBuildFile << "}"

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

        when:
            GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withArguments("assemble", "--stacktrace")
                    .withPluginClasspath()
                    .build()

            Properties result = new Properties()
            new File("${projectDir.absolutePath}/build/classes/java/main/build.properties").withInputStream {
                result.load(it)
            }

        then:
            result.version == "0.1.0-SNAPSHOT"
            result.revision ==~ /\w+/
    }

    def "task devSnapshot sets expected version"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            File projectBuildFile = new File(projectDir, "build.gradle")
            projectBuildFile << "plugins {\n"
            projectBuildFile << "    id \"java\"\n"
            projectBuildFile << "    id \"one.leftshift.asteria-version\"\n"
            projectBuildFile << "}"

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

        when:
            GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withArguments("devSnapshot", "--stacktrace")
                    .withPluginClasspath()
                    .build()

            Properties result = new Properties()
            new File("${projectDir.absolutePath}/build/classes/java/main/build.properties").withInputStream {
                result.load(it)
            }

        then:
            result.version ==~ /\d+\.\d+\.\d+\-dev\.\d+\.uncommitted.[a-z0-9].*/
            result.revision ==~ /\w+/
    }
}
