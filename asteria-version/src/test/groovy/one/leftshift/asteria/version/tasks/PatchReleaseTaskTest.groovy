package one.leftshift.asteria.version.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Unroll

import static org.ajoberstar.gradle.git.release.semver.ChangeScope.*

class PatchReleaseTaskTest extends Specification {

    @Unroll
    def "applying #scope version on branch #branch"() {
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
            if (branch != "master") {
                def createBranchGit = new ProcessBuilder(["git", "checkout", "-b", branch]).directory(projectDir).start()
                createBranchGit.waitForProcessOutput(System.out, System.err)
            }
            def stageGit = new ProcessBuilder(["git", "add", "--all"]).directory(projectDir).start()
            stageGit.waitForProcessOutput(System.out, System.err)
            def commitGit = new ProcessBuilder(["git", "commit", "-m", "initial"]).directory(projectDir).start()
            commitGit.waitForProcessOutput(System.out, System.err)
            def showBranchGit = new ProcessBuilder(["git", "rev-parse", "--abbrev-ref", "HEAD"]).directory(projectDir).start()
            showBranchGit.waitForProcessOutput(System.out, System.err)

        when:
            def gradleRunner = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("snapshot", "-Prelease.scope=${scope}", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .forwardOutput()
            def result = success ? gradleRunner.build() : gradleRunner.buildAndFail()

        then:
            if (success) {
                assert !result.output.contains("Building patch version from branch ${branch} not allowed")
                assert result.output.contains("BUILD SUCCESSFUL")
            } else {
                assert result.output.contains("Building patch version from branch ${branch} not allowed")
                assert result.output.contains("BUILD FAILED")
            }

        where:
            branch             | scope                      || success
            "master"           | MAJOR.name().toLowerCase() || true
            "master"           | MINOR.name().toLowerCase() || true
            "master"           | PATCH.name().toLowerCase() || false
            "release/hotfix"   | MAJOR.name().toLowerCase() || true
            "release/hotfix"   | MINOR.name().toLowerCase() || true
            "release/hotfix"   | PATCH.name().toLowerCase() || false
            "release/0.1.x"    | PATCH.name().toLowerCase() || true
            "release/1.0.x"    | PATCH.name().toLowerCase() || true
            "release/120.43.x" | PATCH.name().toLowerCase() || true
    }
}
