package one.leftshift.asteria.helm

import one.leftshift.asteria.common.BuildProperties
import one.leftshift.asteria.common.BuildPropertiesResolver
import one.leftshift.asteria.common.version.ReleaseExtractionStrategy
import one.leftshift.asteria.common.version.SnapshotExtractionStrategy
import one.leftshift.asteria.common.version.VersionExtractor
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class AsteriaHelmPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Helm"
    static final String EXTENSION_NAME = "asteriaHelm"
    static final String HELM_PREPARE_TASK_NAME = "helmPrepare"
    static final String HELM_ZIP_TASK_NAME = "helmZip"

    @Override
    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, AsteriaHelmExtension)

        project.logger.debug("Adding helmPrepare task")
        def helmPrepareTask = project.tasks.create(HELM_PREPARE_TASK_NAME, Copy)
        helmPrepareTask.group = GROUP
        helmPrepareTask.description = "Prepares helm source files for further processing with the helm command line tool"
        project.afterEvaluate {
            helmPrepareTask.configure {
                def outputDir = "${project.buildDir}/helm"

                Properties properties = BuildPropertiesResolver.resolve(project)
                if (project.hasProperty("deploymentPropertiesFile") && project.property("deploymentPropertiesFile")) {
                    String propertyFilePath = project.property("deploymentPropertiesFile")
                    project.logger.quiet("Using properties file ${propertyFilePath}")
                    properties = new Properties()
                    project.file(propertyFilePath).withInputStream { properties.load(it) }
                }
                BuildProperties buildProperties = BuildProperties.from(properties)
                String extractedVersion = VersionExtractor
                        .defaultExtractor()
                        .addStrategies(SnapshotExtractionStrategy.instance, ReleaseExtractionStrategy.instance)
                        .extractVersion(buildProperties, project.version as String)

                BuildProperties chartBuildProperties = BuildProperties.from(
                        SnapshotExtractionStrategy.instance.isApplicable(buildProperties) ? "${extension.chartVersion}${SnapshotExtractionStrategy.SNAPSHOT_IDENTIFIER}" : extension.chartVersion,
                        buildProperties.revision,
                        buildProperties.timestamp
                )
                String extractedChartVersion = VersionExtractor
                        .defaultExtractor()
                        .addStrategies(SnapshotExtractionStrategy.instance, ReleaseExtractionStrategy.instance)
                        .extractVersion(chartBuildProperties, extension.chartVersion)

                from "${project.projectDir}/src/main/helm"
                into outputDir
                filter(ReplaceTokens, tokens: [
                        version     : (project.hasProperty("deploymentVersion") ? project.property("deploymentVersion") : extractedVersion),
                        chartVersion: extractedChartVersion
                ])
                doLast {
                    project.logger.quiet("Deployment files created in $outputDir")
                    File chartVersionFile = new File(outputDir, "chart-version.txt")
                    chartVersionFile.write(extractedChartVersion)
                    project.logger.quiet("Wrote helm chart version ${extractedChartVersion} to ${chartVersionFile.absolutePath}")
                }
            }
        }


        project.logger.debug("Adding helmZip task")
        def docsZipTask = project.tasks.create(HELM_ZIP_TASK_NAME, Zip)
        docsZipTask.group = GROUP
        docsZipTask.description = "Creates a zip file with helm chart"
        project.afterEvaluate {
            docsZipTask.configure {
                from project.files("${project.buildDir}/helm")
            }
        }
    }
}
