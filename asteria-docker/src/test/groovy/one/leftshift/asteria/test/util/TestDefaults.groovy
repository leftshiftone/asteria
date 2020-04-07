package one.leftshift.asteria.test.util

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static one.leftshift.asteria.test.util.TestDefaults.Default.BUILD_FILE

/**
 * Contains common test methods
 */
final class TestDefaults {

    static enum Default {
        BUILD_FILE("build.gradle"),
        DOCKERFILE("Dockerfile"),
        PLUGIN("one.leftshift.asteria-docker"),
        BUILD_TASK("build")

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
        final File stubBuildFile = new File(p.projectDir, BUILD_FILE.stringValue)
        stubBuildFile.createNewFile()
        stubBuildFile << """
                          plugins {\n
                            id "java"\n
                          """
        plugins.each { stubBuildFile << "id \"$it\"" }
        stubBuildFile << "\n}"
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

    static ProjectDescription createBasicProjectStructureStub(GenericExtension extension, String... plugins) {
        ProjectDescription pd = createBasicProjectStructureStub(plugins)
        StringBuilder sb = new StringBuilder()
        sb.append("\n\n${extension.name}")
        sb.append(" {\n")
        extension.parameters.each { k,v ->
            sb.append("$k = $v\n")
        }
        sb.append(" }\n")

        pd.buildGradle.toFile() << sb.toString()

        return pd
    }
}
