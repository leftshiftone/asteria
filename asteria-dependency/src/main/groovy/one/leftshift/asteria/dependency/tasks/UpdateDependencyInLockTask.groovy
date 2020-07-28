package one.leftshift.asteria.dependency.tasks

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import one.leftshift.asteria.dependency.AsteriaDependencyPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import java.util.regex.Pattern

/**
 * @author Michael Mair
 */
class UpdateDependencyInLockTask extends DefaultTask {

    private String rootDirectory = null
    private String dependencyRegex = null
    private String version = null

    UpdateDependencyInLockTask() {
        group = AsteriaDependencyPlugin.GROUP
        description = "Updated dependency versions in dependencies.lock files."
    }

    @Input
    String getRootDirectory() {
        if (rootDirectory == null) {
            String path = project.rootProject.rootDir.absolutePath
            logger.quiet("Using root directory $path")
            return path
        }
        return rootDirectory
    }

    @Option(option = "rootDir", description = "The root path which is the starting point for gathering all the dependency lock files (e.g. /tmp/my-directory).")
    void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory
    }

    @Input
    String getDependencyRegex() {
        if (dependencyRegex == null) throw new RuntimeException("Dependency not set: use --dependency=org\\.junit:junit-[\\w-_]+")
        return dependencyRegex
    }

    @Option(option = "dependency", description = "The regex which must match in order to replace the version (e.g. org\\.junit:junit-[\\w-_]+).")
    void setDependencyRegex(String dependencyRegex) {
        this.dependencyRegex = dependencyRegex
    }

    @Input
    String getVersion() {
        if (version == null) throw new RuntimeException("Version not set: use --version=4.2.0")
        return version
    }

    @Option(option = "version", description = "The new version of the dependency (e.g. 4.2.0).")
    void setVersion(String version) {
        this.version = version
    }

    @TaskAction
    void updatedDependencies() {
        project.fileTree(getRootDirectory()).matching {
            include "**/dependencies.lock"
        }.each { file ->
            logger.quiet("Changing version to ${getVersion()} in file ${file.path}")
            setVersion(logger, file, getDependencyRegex(), getVersion())
            logger.quiet("Successfully changed version in file ${file.path}")
        }
    }

    static void setVersion(def logger, File file, String regex, String version) {
        def pattern = Pattern.compile(regex)

        def parsedContent = new JsonSlurper().parseText(file.text)
        parsedContent.each { config, dependencies ->
            dependencies.each { groupIdAndArtifactId, dependency ->
                if (pattern.matcher(groupIdAndArtifactId).matches()) {
                    logger.quiet("$groupIdAndArtifactId:${dependency.locked} --> $groupIdAndArtifactId:$version")
                    dependency.locked = version
                }
            }
        }
        file.text = new JsonBuilder(parsedContent).toPrettyString()
    }

}
