package one.leftshift.asteria.version.tasks

import one.leftshift.asteria.common.BuildPropertiesResolver
import one.leftshift.asteria.version.AsteriaVersionException
import one.leftshift.asteria.version.AsteriaVersionPlugin
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException
import org.apache.tools.ant.filters.ReplaceTokens
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.time.ZonedDateTime

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import static java.time.format.DateTimeFormatter.ofPattern
import static one.leftshift.asteria.common.date.AsteriaDateFormat.ASTERIA_DEFAULT

class CreateBuildPropertiesTask extends DefaultTask {

    @Input
    String version
    @Input
    String revision
    @Input
    ZonedDateTime timestamp
    @OutputFile
    File buildPropertiesFile

    @Internal
    Grgit git

    CreateBuildPropertiesTask() {
        group = AsteriaVersionPlugin.GROUP
        description = "Create build.properties file containing build information like version, revision and build time"

        try {
            git = Grgit.open(dir: project.rootProject.projectDir.absolutePath)
        } catch (RepositoryNotFoundException e) {
            throw new AsteriaVersionException("Git repository not found at ${project.rootProject.projectDir.absolutePath}")
        }

        project.plugins.withType(JavaPlugin) {
            dependsOn project.tasks.processResources
            project.tasks.classes.dependsOn this

            version = project.version as String
            revision = currentRevision()
            timestamp = ZonedDateTime.now()

            project.afterEvaluate {
                project.configure(project) {
                    processResources {
                        def replacePaths = ["config/**", "**/Dockerfile"]

                        replacePaths.each {
                            filesMatching(it) {
                                filter(ReplaceTokens, tokens: [
                                        version  : version.toString(),
                                        revision : revision,
                                        timestamp: timestamp.format(ISO_OFFSET_DATE_TIME)
                                ])
                            }
                        }
                    }
                }
            }
            buildPropertiesFile = new File(BuildPropertiesResolver.resolvePath(project).toUri())
        }
    }

    @TaskAction
    def createVersionProperties() {
        File resourcesBuildDir
        project.plugins.withType(JavaPlugin) {
            resourcesBuildDir = project.tasks.compileJava.destinationDir
        }

        if (!resourcesBuildDir.exists()) {
            return
        }

        Properties properties = [
                version  : version,
                revision : revision,
                timestamp: timestamp.format(ofPattern(ASTERIA_DEFAULT.pattern)),
        ] as Properties
        properties.store(buildPropertiesFile.newWriter(), null)

        logger.info("Created file ${buildPropertiesFile.absolutePath}")
    }

    String currentRevision() {
        String revision
        try {
            revision = git.log(includes: ["HEAD"]).first().id
        } catch (GrgitException ex) {
            revision = "uncommitted"
        }
        return revision.substring(0, 10)
    }
}
