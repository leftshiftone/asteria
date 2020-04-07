package one.leftshift.asteria.deploy.test.util

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Contains common test methods
 */
final class TestDefaults {

    static enum Default {
        BUILD_FILE("build.gradle"),
        DOCKERFILE("Dockerfile"),
        PLUGIN("one.leftshift.asteria-deploy")

        final String stringValue

        Default(String stringValue) {
            this.stringValue = stringValue
        }
    }

    /**
     *  Creates a basic Project structure and applies the supplied plugins
     * @param plugins e.g: "one.leftshift.asteria-docker"
     * @return assembled {@link Project}
     */
    static ProjectDescription createBasicProjectStructureStub(String... plugins) {
        final Project p = ProjectBuilder.builder().build()
        final File stubBuildFile = new File(p.projectDir, Default.BUILD_FILE.stringValue)
        stubBuildFile.createNewFile()
        stubBuildFile << """
                          plugins {\n
                            id "java"\n
                          """
        plugins.each { stubBuildFile << "id \"$it\"" }
        stubBuildFile << "\n}\n"
        stubBuildFile << "version='1.2.3-CONFIGURED'\n"
        Path src = Files.createDirectories(Paths.get(p.projectDir.absolutePath, "src/main/java"))
        Path srcMainResources = Files.createDirectories(Paths.get(p.projectDir.absolutePath, "src/main/resources"))
        Files.createFile(Paths.get(src.toString(), "Application.java"))

        return ProjectDescription.builder()
                .project(p)
                .buildGradle(stubBuildFile.toPath())
                .srcMain(Paths.get(p.projectDir.absolutePath, "src/main"))
                .srcMainJava(src)
                .srcMainResources(srcMainResources)
                .build()
    }
}
