package one.leftshift.asteria.publish

import com.amazonaws.SdkBaseException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import one.leftshift.asteria.common.branch.BranchResolver
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.jvm.tasks.Jar

import static one.leftshift.asteria.common.version.VersionCategorizer.isPreReleaseVersion
import static one.leftshift.asteria.common.version.VersionCategorizer.isReleaseVersion

class AsteriaPublishPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Publish"
    static final String EXTENSION_NAME = "asteriaPublish"
    static final String SOURCE_JAR_TASK_NAME = "sourceJar"

    @Override
    void apply(Project project) {
        AsteriaPublishExtension extension = project.extensions.create(EXTENSION_NAME, AsteriaPublishExtension)

        project.logger.debug("Applying maven publish plugin")
        project.pluginManager.apply MavenPublishPlugin

        project.logger.debug("Configuring maven publish plugin")
        project.configure(project) {
            publishing {
                repositories {
                    if (awsCredentials(project)) {
                        project.afterEvaluate {
                            String publishUrl = getSnapshotRepositoryUrl(extension, project)
                            if (isPreReleaseVersion(project.version.toString())) {
                                publishUrl = extension.preReleaseRepositoryUrl
                                if (!publishUrl) project.logger.warn("No pre-release repository url set")
                            } else if (isReleaseVersion(project.version.toString())) {
                                publishUrl = extension.releaseRepositoryUrl
                                if (!publishUrl) project.logger.warn("No release repository url set")
                            }

                            project.logger.info("Publishing to url {}", publishUrl)
                            maven {
                                credentials(AwsCredentials) {
                                    accessKey awsCredentials(project)?.AWSAccessKeyId
                                    secretKey awsCredentials(project)?.AWSSecretKey
                                }
                                url publishUrl
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
                                    project.logger.info("Handling groupId ${dependency.get("groupId")} (type ${dependency.get("groupId").getClass()}) and artifactId ${dependency.get("artifactId")} (type ${dependency.get("artifactId").getClass()})")
                                    if (!dependency.get("groupId") || !dependency.get("artifactId")) {
                                        project.logger.debug("Information missing for " + dependency)
                                        return false
                                    }
                                    project.logger.debug("Handling dependency " + dependency)
                                    NodeList rawGroupId = dependency.get("groupId")
                                    project.logger.debug("Handling groupId " + rawGroupId.first())
                                    project.logger.debug("Handling groupId value " + rawGroupId.first().value())
                                    String groupId = rawGroupId.first().value() in Collection ? rawGroupId.first().value().first() : rawGroupId.first().value()
                                    NodeList rawArtifactId = dependency.get("artifactId")
                                    project.logger.debug("Handling artifactId " + rawArtifactId.first())
                                    project.logger.debug("Handling artifactId value " + rawArtifactId.first().value())
                                    String artifactId = rawArtifactId.first().value() in Collection ? rawArtifactId.first().value().first() : rawArtifactId.first().value()
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

        // this is a workaround for the spring dependency management plugin (see https://github.com/gradle/gradle/issues/11862)
        // maybe the gradle native platform should be used or configured accordingly (see https://github.com/spring-gradle-plugins/dependency-management-plugin/issues/211)
        project.tasks.withType(GenerateModuleMetadata) {
            enabled = false
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

        return snapshotRepository
    }
}
