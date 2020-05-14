package one.leftshift.asteria.publish

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkBaseException
import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.ObjectTagging
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.Tag
import one.leftshift.asteria.common.branchsnapshots.BranchResolver
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar

import java.time.ZoneOffset
import java.time.ZonedDateTime

class AsteriaPublishPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Publish"
    static final String EXTENSION_NAME = "asteriaPublish"
    static final String SNAPSHOT_VERSION_SUFFIX = "-SNAPSHOT"
    static final String SOURCE_JAR_TASK_NAME = "sourceJar"

    @Override
    void apply(Project project) {
        AsteriaPublishExtension extension = project.extensions.create(EXTENSION_NAME, AsteriaPublishExtension)

        project.logger.debug("Applying maven publish plugin")
        project.pluginManager.apply MavenPublishPlugin

        project.logger.debug("Configuring maven publish plugin")
        project.configure(project) {
            boolean isSnapshotVersion = project.version.toString().endsWith(SNAPSHOT_VERSION_SUFFIX)
            publishing {
                repositories {
                    if (awsCredentials(project)) {
                        project.afterEvaluate {
                            if (!isSnapshotVersion && !extension.releaseRepositoryUrl) {
                                project.logger.info("No release repository url set")
                            } else {
                                maven {
                                    credentials(AwsCredentials) {
                                        accessKey awsCredentials(project)?.AWSAccessKeyId
                                        secretKey awsCredentials(project)?.AWSSecretKey
                                    }
                                    url isSnapshotVersion ? getSnapshotRepositoryUrl(extension, project) : extension.releaseRepositoryUrl
                                }
                            }
                        }
                    }
                }
                publications {
                    mavenPom(MavenPublication) {
                        from components.java
                        project.afterEvaluate {
                            if (extension.additionalArtifacts) {
                                extension.additionalArtifacts.each {
                                    artifact it
                                }
                            }
                        }
                        pom.withXml {
                            Map resolvedVersionMap = [:]
                            Set<ResolvedArtifact> resolvedArtifacts = []
                            try {
                                resolvedArtifacts.addAll(project.configurations.compile.getResolvedConfiguration().getResolvedArtifacts())
                                resolvedArtifacts.addAll(project.configurations.runtime.getResolvedConfiguration().getResolvedArtifacts())
                                resolvedArtifacts.addAll(project.configurations.testCompile.getResolvedConfiguration().getResolvedArtifacts())
                                resolvedArtifacts.addAll(project.configurations.testRuntime.getResolvedConfiguration().getResolvedArtifacts())
                            } catch (Exception ex) {
                                project.logger.debug("Unable to get resolved artifacts from configuration")
                            }
                            resolvedArtifacts.each {
                                ModuleVersionIdentifier moduleVersionIdentifier = it.getModuleVersion().getId();
                                resolvedVersionMap.put("${moduleVersionIdentifier.getGroup()}:${moduleVersionIdentifier.getName()}", moduleVersionIdentifier.getVersion())
                            }
                            project.logger.info("Dependency map: " + resolvedVersionMap.toString())
                            if (!asNode().dependencies.isEmpty()) {
                                project.logger.info("Dependencies: " + asNode().dependencies.first().toString())
                                asNode().dependencies.first().each { Node dependency ->
                                    if (!dependency.get("groupId") || !dependency.get("artifactId")) {
                                        project.logger.debug("Information missing for " + dependency)
                                        return false
                                    }
                                    project.logger.debug("Handling dependency " + it)
                                    String groupId = dependency.get("groupId").first().value().first()
                                    String artifactId = dependency.get("artifactId").first().value().first()
                                    String version = resolvedVersionMap.get("${groupId}:${artifactId}")
                                    if (!version) {
                                        project.logger.info("No version found for ${groupId}:${artifactId}")
                                        return false
                                    }

                                    project.logger.info("Updating dependency ${groupId}:${artifactId}:${version}")
                                    if (!dependency.get("version")) {
                                        dependency.appendNode("version", version)
                                    } else {
                                        dependency.get("version").first().value = version
                                    }
                                }
                            } else {
                                project.logger.info("No dependencies available")
                            }
                        }
                    }
                }
            }
        }

        project.logger.debug("Adding sourceJar task")
        def sourceJarTask = project.tasks.create(SOURCE_JAR_TASK_NAME, Jar)
        sourceJarTask.group = GROUP
        sourceJarTask.description = "Creates a jar file with sources"
        project.afterEvaluate {
            sourceJarTask.configure {
                from project.files(project.sourceSets.main.allSource.srcDirs)
                classifier "sources"
            }
        }
    }

    static AWSCredentials awsCredentials(Project project) {
        project.logger.info("Looking for AWS credentials")
        try {
            return new AWSCredentialsProviderChain(
                    new ProfileCredentialsProvider(),
                    new EnvironmentVariableCredentialsProvider()
            ).credentials
        } catch (IllegalArgumentException | SdkBaseException ex) {
            if (project.logger.isInfoEnabled()) {
                project.logger.error("Error reading AWS credentials: " + ex.message)
            }
            return null
        }
    }

    private static String getSnapshotRepositoryUrl(AsteriaPublishExtension extension, Project project) {
        String branchName = null
        try {
            Grgit git = Grgit.open(dir: project.rootProject.projectDir.absolutePath)
            branchName = git.branch.current.name ?: "unknown"
        } catch (Exception ex) {
            project.logger.warn("Unable to get current branch: ${ex.message}")
            if (project.logger.isInfoEnabled()) {
                project.logger.error(ex.message, ex)
            }
            return extension.snapshotRepositoryUrl
        }
        project.logger.info("Currently on branch {}", branchName)

        String snapshotRepository = BranchResolver.getSnapshotRepositoryUrlBasedOnBranch(extension.enableBranchSnapshotRepositories,
                extension.snapshotRepositoryUrl,
                branchName,
                extension.snapshotBranchRegex,
                extension.snapshotRepositoryNameRegex,
                project.logger)

        if (!extension.createSnapshotRepositories) {
            return snapshotRepository
        }

        URI s3Uri = new URI(snapshotRepository)
        String[] s3UriElements = s3Uri.getHost().split(".")
        String bucket = s3UriElements[0]
        String region = s3UriElements[2]
        String snapshotRepositoryPath = s3Uri.getPath()

        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build()

        boolean snapshotRepositoryExists = s3.doesObjectExist(bucket, snapshotRepositoryPath)
        if (snapshotRepositoryExists) {
            return snapshotRepository
        }

        try {
            ObjectMetadata metadata = new ObjectMetadata().with {
                it.setExpirationTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(extension.snapshotsExpirationInDays).toInstant().toDate())
                return it
            }
            PutObjectRequest request = new PutObjectRequest(bucket, snapshotRepositoryPath, null)
                    .withTagging(new ObjectTagging([new Tag("temporary", "true")]))
                    .withMetadata(metadata)
            def response = s3.putObject(request)
            project.logger.info("Created object {}", response.metadata?.rawMetadata)
        } catch (SdkClientException | AmazonServiceException ex) {
            project.logger.error("Failed to create object ${snapshotRepositoryPath}: ${ex.message}", ex)
        }

        return snapshotRepository
    }
}
