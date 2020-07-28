package one.leftshift.asteria.dependency

import groovy.json.JsonSlurper
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.*

class AsteriaDependencyPluginTest extends Specification {

    def "plugin can be instantiated"() {
        expect:
            new AsteriaDependencyPlugin() != null
    }

    @Unroll
    def "dependencies are resolved #description"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
                fileset(file: "src/test/resources/testProject/.gitignore")
            }
            setupGitRepo(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments(task, "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Using maven bom org.springframework.boot:spring-boot-dependencies:2.1.6.RELEASE"

            result.output.contains "Using version '2.9.9' for dependency 'com.fasterxml.jackson.core:jackson-core:'"
            result.output.contains "Attempting to resolve component for com.fasterxml.jackson.core:jackson-core:2.9.9 using repositories"

            result.output.contains "Evaluating dependency org.spockframework:spock-core:latest.smart"
            result.output.contains "Reassigned org.spockframework:spock-core to version ${expectedVersion} (was latest.smart)"
            result.output.contains "Attempting to resolve version for org.spockframework:spock-core:${expectedVersion} using repositories"
            result.output =~ /Using org\.spockframework:spock-core:\d\.\d.*-groovy-\d\.\d/

            result.output.contains "Evaluating dependency one.leftshift.canon:canon:"
            result.output.contains "Assigned one.leftshift.canon:canon to version ${expectedVersion} (no version set)"
            result.output.contains "Attempting to resolve version for one.leftshift.canon:canon:${expectedVersion} using repositories"

        where:
            description             | task          || expectedVersion
            "for non release build" | "build"       || "latest.integration"
            "for dev release build" | "devSnapshot" || "latest.release"
    }

    def "dependencies are resolved and pre-releases are considered"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
                fileset(file: "src/test/resources/testProject/.gitignore")
            }
            setupGitRepo(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "-Pdependency.prerelease.ignore=false", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Dependency resolution considers pre-releases like release candidates for dependencies starting with one.leftshift"
    }

    def "dependency updates report is generated"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGitRepo(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("dependencyUpdates", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            def dependencyUpdatesReport = new File("${project.buildDir.absolutePath}/dependencyUpdates/report.json")
            dependencyUpdatesReport.exists()
            def dependencyUpdates = new JsonSlurper().parse(dependencyUpdatesReport)

            // assert depending on found outdated dependencies
            dependencyUpdates.outdated.count in [1, 2]
            if (dependencyUpdates.outdated.count > 1) {
                dependencyUpdates.outdated.dependencies[0].name == "jackson-core"
                dependencyUpdates.outdated.dependencies[0].version == "2.9.9"
                dependencyUpdates.outdated.dependencies[0].available.release =~ /\d+\.\d+.\d+/
                dependencyUpdates.outdated.dependencies[1].name == "junit"
                dependencyUpdates.outdated.dependencies[1].version == "4.5"
                dependencyUpdates.outdated.dependencies[1].available.release =~ /\d+\.\d+/
            } else {
                dependencyUpdates.outdated.dependencies[0].name == "junit"
                dependencyUpdates.outdated.dependencies[0].version == "4.5"
                dependencyUpdates.outdated.dependencies[0].available.release =~ /\d+\.\d+/
            }
    }

    def "dependency lock file is generated"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGitRepo(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("generateLock", "saveLock", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            def lockFile1 = new File("${projectDir.absolutePath}/project1/dependencies.lock")
            lockFile1.exists()
            lockFile1.text.contains "org.spockframework:spock-core"
            lockFile1.text =~ /"locked": "\d\.\d.*-groovy-\d\.\d"/
            lockFile1.text.contains "junit:junit"
            lockFile1.text =~ /"locked": "4\.5"/
            def lockFile2 = new File("${projectDir.absolutePath}/project2/dependencies.lock")
            lockFile2.exists()
            !lockFile2.text.contains("org.spockframework:spock-core")
            lockFile1.text.contains "junit:junit"
            lockFile2.text =~ /"locked": "4\.5"/
    }

    def "dependency lock file is committed and pushed"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGitRepo(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("generateLock", "saveLock", "persistDependencyLock", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"

            Grgit git = Grgit.open(dir: projectDir.absolutePath)
            git.log().find { commit -> commit.shortMessage == "[INFRA] (deps) dependency lock file" }
    }

    @Unroll
    def "#task #description lock file"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project1") {
                fileset(file: "src/test/resources/project1/dependencies.lock")
            }
            new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project2") {
                fileset(file: "src/test/resources/project2/dependencies.lock")
            }
            setupGitRepo(projectDir)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments(task, "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains expectedLog
            result.output =~ expectedUsedVersion

        where:
            description | task          || expectedLog                                             | expectedUsedVersion
            "ignores"   | "build"       || "Dependency lock is ignored for version 0.1.0-SNAPSHOT" | /Using org\.spockframework:spock-core:\d\.\d.*-groovy-\d\.\d/
            "honors"    | "devSnapshot" || "Dependency lock is honored for version 0.1.0-dev.1"    | /Using org\.spockframework:spock-core:1\.0-groovy-2\.4/
    }

    def "lock file is honored for patch release branch"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project1") {
                fileset(file: "src/test/resources/project1/dependencies.lock")
            }
            new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project2") {
                fileset(file: "src/test/resources/project2/dependencies.lock")
            }
            setupGitRepo(projectDir)
            def branchGit = new ProcessBuilder(["git", "checkout", "-b", "release/0.0.x"]).directory(projectDir).start()
            branchGit.waitForProcessOutput(System.out, System.err)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "Currently on branch release/0.0.x"
            result.output.contains "Dependency lock is honored for branch release/0.0.x and version 0.0.1-SNAPSHOT"
            result.output =~ /Using org\.spockframework:spock-core:1\.0-groovy-2\.4/
    }

    def "lock file is ignored for minor release branch"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project1") {
                fileset(file: "src/test/resources/project1/dependencies.lock")
            }
            new AntBuilder().copy(todir: "${project.projectDir.absolutePath}/project2") {
                fileset(file: "src/test/resources/project2/dependencies.lock")
            }
            setupGitRepo(projectDir)
            def branchGit = new ProcessBuilder(["git", "checkout", "-b", "release/0.x"]).directory(projectDir).start()
            branchGit.waitForProcessOutput(System.out, System.err)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "Dependency lock is ignored for version 0.1.0-SNAPSHOT"
            result.output =~ /Using org\.spockframework:spock-core:\d\.\d.*-groovy-\d\.\d/
    }

    def "releasing minor not possible when current branch is behind tracked branch"() {
        given: "remote repository"
            Project remote = ProjectBuilder.builder().build()
            File remoteDir = remote.projectDir
            new AntBuilder().copy(todir: remote.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            new AntBuilder().copy(todir: "${remoteDir.absolutePath}/project1") {
                fileset(file: "src/test/resources/project1/dependencies.lock")
            }
            new AntBuilder().copy(todir: "${remoteDir.absolutePath}/project2") {
                fileset(file: "src/test/resources/project2/dependencies.lock")
            }
            setupGitRepo(remoteDir)

        and: "cloned project repository"
            def projectDir = new File("${remoteDir.absolutePath}Clone")
            projectDir.mkdir()
            def cloneGit = new ProcessBuilder(["git", "clone", remoteDir.absolutePath, projectDir.absolutePath]).directory(projectDir.parentFile).start()
            cloneGit.waitForProcessOutput(System.out, System.err)

            Project project = ProjectBuilder.builder()
                    .withProjectDir(projectDir)
                    .build()

        and: "commit is added to remote after cloning the remote repository"
            def gitignoreFile = new File(remoteDir, ".gitignore")
            gitignoreFile << "*.log"
            Grgit gitRemote = Grgit.open(dir: remoteDir.absolutePath)
            gitRemote.add(patterns: [".gitignore"])
            gitRemote.commit(message: "ignoring log files in SCM")

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("final", "-Prelease.scope=minor", "generateLock", "saveLock", "persistDependencyLock", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .buildAndFail()
            println result.output

        then:
            result.output.contains "BUILD FAILED"
            result.output.contains "Current branch is behind the tracked branch"
    }

    def "releasing minor works as expected"() {
        given: "remote repository"
            Project remote = ProjectBuilder.builder().build()
            File remoteDir = remote.projectDir
            new AntBuilder().copy(todir: remote.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            new AntBuilder().copy(todir: "${remoteDir.absolutePath}/project1") {
                fileset(file: "src/test/resources/project1/dependencies.lock")
            }
            new AntBuilder().copy(todir: "${remoteDir.absolutePath}/project2") {
                fileset(file: "src/test/resources/project2/dependencies.lock")
            }
            setupGitRepo(remoteDir)

        and: "cloned project repository"
            def projectDir = new File("${remoteDir.absolutePath}Clone")
            projectDir.mkdir()
            def cloneGit = new ProcessBuilder(["git", "clone", remoteDir.absolutePath, projectDir.absolutePath]).directory(projectDir.parentFile).start()
            cloneGit.waitForProcessOutput(System.out, System.err)

            Project project = ProjectBuilder.builder()
                    .withProjectDir(projectDir)
                    .build()

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("final", "-Prelease.scope=minor", "generateLock", "saveLock", "persistDependencyLock", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "BUILD SUCCESSFUL"
            def lockFile1 = new File("${projectDir.absolutePath}/project1/dependencies.lock")
            lockFile1.exists()
            lockFile1.text =~ /"locked": "4\.5"/
            lockFile1.text =~ /"locked": "\d\.\d.*-groovy-\d\.\d"/
            def lockFile2 = new File("${projectDir.absolutePath}/project2/dependencies.lock")
            lockFile2.exists()
            lockFile2.text =~ /"locked": "4\.5"/

            Grgit git = Grgit.open(dir: projectDir.absolutePath)
            def dependencyLockCommit = git.log().find { commit -> commit.shortMessage == "[INFRA] (deps) dependency lock file" }
            git.tag.list().find { it.name == "v0.1.0" }.commit.id == dependencyLockCommit.id
            git.status().isClean()

            Path fatJar = Paths.get("${projectDir.absolutePath}/project1/build/libs/project1-0.1.0.jar")
            FileSystem fatJarFileSystem = FileSystems.newFileSystem(fatJar, null)
            Path junitJar = fatJarFileSystem.getPath("/BOOT-INF/lib/junit-4.5.jar")
            Files.exists(junitJar)
            Path spockJar = Files.newDirectoryStream(fatJarFileSystem.getPath("/BOOT-INF/lib"), "spock-core-*.jar").first()
            Files.exists(spockJar)
    }

    def "custom snapshot repository is added if setting has been enabled"() {
        given:
            Project project = ProjectBuilder.builder().build()
            File projectDir = project.projectDir
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
            setupGitRepo(projectDir)
            def branchGit = new ProcessBuilder(["git", "checkout", "-b", "feature/FOO-666"]).directory(projectDir).start()
            branchGit.waitForProcessOutput(System.out, System.err)

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("assemble", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output

        then:
            result.output.contains "Currently on branch feature/FOO-666"
            result.output.contains "Snapshot repositories for branches are enabled"
            result.output.contains "Current repositories: [maven, BintrayJCenter, maven2]"
            result.output.contains "Using snapshot repository maven with url s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/test-snapshots-foo-666 at position 1 of 3"
    }

    static void setupGitRepo(File projectDir) {
        def gitignoreFile = new File(projectDir, ".gitignore")
        gitignoreFile << [".gradle", "build/", "userHome/"].join("\n")
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
    }
}
