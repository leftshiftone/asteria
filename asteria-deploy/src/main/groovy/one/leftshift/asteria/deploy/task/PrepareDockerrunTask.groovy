package one.leftshift.asteria.deploy.task

import groovy.transform.TypeChecked
import one.leftshift.asteria.common.BuildProperties
import one.leftshift.asteria.common.BuildPropertiesResolver
import one.leftshift.asteria.common.version.ReleaseExtractionStrategy
import one.leftshift.asteria.common.version.SnapshotExtractionStrategy
import one.leftshift.asteria.common.version.VersionExtractor
import one.leftshift.asteria.deploy.cloud.config.EnrichedConfig
import one.leftshift.asteria.deploy.replace.ReplaceRequest
import one.leftshift.asteria.deploy.replace.SimpleTokenReplacer
import one.leftshift.asteria.deploy.replace.TokenReplacer
import one.leftshift.asteria.util.Tuple
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path
import java.nio.file.Paths

import static one.leftshift.asteria.deploy.cloud.config.AWSElasticbeanstalkConfigAttribute.*

/**
 * Replaces tokens in the deploymentDescription file and copies the resolved file to the project.buildDir/Dockerrun.aws.json
 *
 */
@TypeChecked
class PrepareDockerrunTask extends DefaultTask {

    @TaskAction
    void replaceTokens() {
        final List<Tuple<String, EnrichedConfig>> configs = EnrichedConfig.loadConfigs(project)

        configs.each {
            EnrichedConfig config = EnrichedConfig.toEnriched(it.right.getObject(ROOT_PATH.stringValue).toConfig())
            final TokenReplacer tokenReplacer = new SimpleTokenReplacer()
            Path dockerrunLocation = Paths.get(project.projectDir.path, config.getString(DOCKER_RUN_LOCATION.stringValue))
            Writable writable = tokenReplacer.replace(new ReplaceRequest(dockerrunLocation, tokenMap(project, config)))
            File outDir = createDirectories(it)
            writable.writeTo(new FileWriter(new File(outDir, "Dockerrun.aws.json")))
        }
    }

    File createDirectories(Tuple<String, EnrichedConfig> it) {
        File outDir = new File("${project.buildDir}/deployment/${it.left}")
        if (!outDir.exists()) {
            outDir.mkdirs()
        }
        return outDir
    }

    Map<String, String> tokenMap(Project project, EnrichedConfig config) {
        final String extractedVersion = VersionExtractor.defaultExtractor()
                .addStrategies(SnapshotExtractionStrategy.instance, ReleaseExtractionStrategy.instance)
                .extractVersion(BuildProperties.from(BuildPropertiesResolver.resolve(project)), project.version as String)
        return ["version": config.getStringOrDefault(VERSION_LABEL.stringValue,extractedVersion)]
    }
}
