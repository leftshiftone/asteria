package one.leftshift.asteria.common

import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Extracts build information provides by asteria-version from build.properties
 **/
abstract class BuildPropertiesResolver {

    final static String BUILD_PROPERTIES = "build.properties"

    static Properties resolve(Project project) {
        final Properties p = new Properties()
        if (buildPropertiesArePresent(project)) {
            p.load(new FileReader(resolvePath(project).toFile()))
        }
        return p
    }

    static Path resolvePath(Project project) {
        return Paths.get("${project?.tasks?.compileJava?.destinationDir?.path}", "/$BUILD_PROPERTIES")
    }

    private static buildPropertiesArePresent(Project project) {
        return resolvePath(project).toFile().exists() && resolvePath(project).toFile().isFile()
    }

}
