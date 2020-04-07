package one.leftshift.asteria.version.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class CreateBuildPropertiesTaskTest extends Specification {

    def "task is available"() {
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
            project.tasks.createBuildProperties instanceof CreateBuildPropertiesTask
    }

    def "task is available even though java plugin is not applied"() {
        given:
            Project project = ProjectBuilder.builder().build()
            def initGit = ["git", "init", project.projectDir.absolutePath].execute()
            initGit.waitFor()

        when:
            project.pluginManager.apply "one.leftshift.asteria-version"

        then:
            project.tasks.createBuildProperties instanceof CreateBuildPropertiesTask
    }

    def "task created version properties file"() {
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

            def initGit = new ProcessBuilder(["git", "init"]).directory(project.projectDir).start()
            initGit.waitForProcessOutput(System.out, System.err)
            def configEmailGit = new ProcessBuilder(["git", "config", "user.email", "pipelines@bitbucket.org"]).directory(project.projectDir).start()
            configEmailGit.waitForProcessOutput(System.out, System.err)
            def configNameGit = new ProcessBuilder(["git", "config", "user.name", "Bitbucket Pipelines"]).directory(project.projectDir).start()
            configNameGit.waitForProcessOutput(System.out, System.err)
            def stageGit = new ProcessBuilder(["git", "add", "--all"]).directory(projectDir).start()
            stageGit.waitForProcessOutput(System.out, System.err)
            def commitGit = new ProcessBuilder(["git", "commit", "-m", "initial"]).directory(projectDir).start()
            commitGit.waitForProcessOutput(System.out, System.err)
            def file = new File("${project.projectDir}/README.md")
            file.write "Test Project"
            def stage2Git = new ProcessBuilder(["git", "add", "--all"]).directory(projectDir).start()
            stage2Git.waitForProcessOutput(System.out, System.err)
            def commit2Git = new ProcessBuilder(["git", "commit", "-m", "initial"]).directory(projectDir).start()
            commit2Git.waitForProcessOutput(System.out, System.err)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withArguments("build", "--info", "--stacktrace")
                    .withPluginClasspath()
                    .build()

        then:
            result.output.contains("Created file ${projectDir.absolutePath}/build/classes/java/main/build.properties")

            def resultPropertiesFile = new File("${projectDir.absolutePath}/build/classes/java/main/build.properties")
            resultPropertiesFile.exists()
            resultPropertiesFile.readLines().find { it.startsWith "version" }
            resultPropertiesFile.readLines().find { it.matches(/revision=\w{10}/) }
            resultPropertiesFile.readLines().find { it.startsWith "timestamp" }
            print(resultPropertiesFile.readLines())
    }
}
