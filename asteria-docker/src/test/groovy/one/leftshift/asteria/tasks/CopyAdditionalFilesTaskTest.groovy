package one.leftshift.asteria.tasks

import one.leftshift.asteria.test.util.ProjectDescription
import one.leftshift.asteria.test.util.TestDefaults
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static one.leftshift.asteria.test.util.TestDefaults.Default.BUILD_TASK
import static one.leftshift.asteria.test.util.TestDefaults.Default.PLUGIN
import static one.leftshift.asteria.AsteriaDockerTask.DOCKER_COPY_ADDITIONAL_FILES

class CopyAdditionalFilesTaskTest extends Specification {

    void "Copies specified files to the expected directory"() {
        given:
            ProjectDescription pd = TestDefaults.createBasicProjectStructureStub(PLUGIN.stringValue)
            Files.createDirectories(Paths.get(pd.srcMain.toString(), "python"))
            Path f1 = Files.createFile(Paths.get(pd.srcMain.toString(), "python", "somePythonFile.py"))
            Path f2 = Files.createFile(Paths.get(pd.srcMain.toString(), "python", "anotherPythonFile.py"))
            Files.createFile(Paths.get(pd.project.projectDir.absolutePath, "Dockerfile"))
            pd.buildGradle.toFile() << "\nasteriaDocker { additionalFiles = files(\"${f1.toString()}\", \"${f2.toString()}\")}"
        when:
            GradleRunner.create()
                    .withProjectDir(pd.project.projectDir)
                    .withArguments(BUILD_TASK.stringValue, DOCKER_COPY_ADDITIONAL_FILES.taskName)
                    .withPluginClasspath()
                    .build()

            def result = new File("${pd.project.buildDir.absolutePath}/docker")
        then:
            result.listFiles().toList().collect { it.getName() }.containsAll([f1.fileName.toString(), f2.fileName.toString()])
        cleanup:
            pd.project.projectDir.deleteDir()
    }
    void "Copies specified directory to the expected directory"() {
        given:
            ProjectDescription pd = TestDefaults.createBasicProjectStructureStub(PLUGIN.stringValue)
            Files.createDirectories(Paths.get(pd.srcMain.toString(), "python"))
            Path f1 = Files.createFile(Paths.get(pd.srcMain.toString(), "python", "somePythonFile.py"))
            Path f2 = Files.createFile(Paths.get(pd.srcMain.toString(), "python", "anotherPythonFile.py"))
            Files.createFile(Paths.get(pd.project.projectDir.absolutePath, "Dockerfile"))
            pd.buildGradle.toFile() << "\nasteriaDocker { additionalFiles = files(\"${Paths.get(pd.srcMain.toString(), "python")}\")}"
        when:
            GradleRunner.create()
                    .withProjectDir(pd.project.projectDir)
                    .withArguments(BUILD_TASK.stringValue, DOCKER_COPY_ADDITIONAL_FILES.taskName)
                    .withPluginClasspath()
                    .build()

            def resultFiles = new File("${pd.project.buildDir.absolutePath}/docker")
        then:
            resultFiles.listFiles().toList().collect { it.getName() }.containsAll(["Dockerfile", "app.jar","python"])
            new File("${pd.project.buildDir.absolutePath}/docker/python").listFiles().toList().collect { it.getName() }.containsAll([f1.fileName.toString(), f2.fileName.toString()])

        cleanup:
            pd.project.projectDir.deleteDir()
    }
}
