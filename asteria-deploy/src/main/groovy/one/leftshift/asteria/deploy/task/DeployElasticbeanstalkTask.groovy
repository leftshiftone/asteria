package one.leftshift.asteria.deploy.task

import groovy.transform.TypeChecked
import one.leftshift.asteria.common.BuildProperties
import one.leftshift.asteria.common.BuildPropertiesResolver
import one.leftshift.asteria.common.version.ReleaseExtractionStrategy
import one.leftshift.asteria.common.version.SnapshotExtractionStrategy
import one.leftshift.asteria.common.version.VersionExtractor
import one.leftshift.asteria.deploy.cloud.*
import one.leftshift.asteria.deploy.cloud.config.EnrichedConfig
import one.leftshift.asteria.util.Tuple
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static one.leftshift.asteria.deploy.cloud.config.AWSElasticbeanstalkConfigAttribute.ENVIRONMENT_NAME
import static one.leftshift.asteria.deploy.cloud.config.AWSElasticbeanstalkConfigAttribute.ROOT_PATH
/**
 * Deploys a ZIP containing the deploymentDescription file to AWS Elasticbeanstalk
 *
 */
@TypeChecked
class DeployElasticbeanstalkTask extends DefaultTask {

    /**
     * Deploys a deployment request to AWS Elasticbeanstalk
     */
    @TaskAction
    void deployToAwsEb() {
        final List<Tuple<String, EnrichedConfig>> configs = EnrichedConfig.loadConfigs(project)

        configs.each {
            EnrichedConfig config = EnrichedConfig.toEnriched(it.right.getObject(ROOT_PATH.stringValue).toConfig())
            String outputFileName = config.getStringOrDefault(ENVIRONMENT_NAME.stringValue, project.getName())
            String version = VersionExtractor
                    .defaultExtractor()
                    .addStrategies(SnapshotExtractionStrategy.instance, ReleaseExtractionStrategy.instance)
                    .extractVersion(BuildProperties.from(BuildPropertiesResolver.resolve(project)), project.version as String)
            Path destFilePath = Paths.get("$project.buildDir/deployment/${it.left}/${outputFileName}-${version}.zip")

            createDeploymentZip(destFilePath, it.left)
            //@formatter:off
            final CloudDeployerRequest cloudDeployerRequest = CloudDeployerRequest.builder()
                                                                        .project(project)
                                                                        .cloudProvider(SupportedCloudProvider.AWS)
                                                                        .config(config).build()
            final CloudDeployer deployer = CloudDeployerFactory.get(cloudDeployerRequest)
            deployer.deploy(DeploymentRequest.builder()
                    .deployable(destFilePath)
                    .config(config)
                    .build())
            //@formatter:on
        }
    }

    void createDeploymentZip(Path destFilePath, String deploymentMode) {
        final FileOutputStream fos = new FileOutputStream(destFilePath.toFile())
        final ZipOutputStream zos = new ZipOutputStream(fos)

        new File(project.buildDir.getPath() + "/deployment/${deploymentMode}")
                .listFiles({File dir, String name -> return name.endsWith(".aws.json") } as FilenameFilter)
                .each { File f ->
                            zos.putNextEntry(new ZipEntry(f.getName()))
                            Files.copy(f.toPath(), zos)
                            zos.closeEntry()
                }
        zos.close()
    }
}
