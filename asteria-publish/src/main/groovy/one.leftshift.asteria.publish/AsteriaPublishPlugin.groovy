package one.leftshift.asteria.publish

import com.amazonaws.SdkBaseException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar

class AsteriaPublishPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Publish"
    static final String EXTENSION_NAME = "asteriaPublish"
    static final String SNAPSHOT_VERSION_SUFFIX = "-SNAPSHOT"
    static final String SOURCE_JAR_TASK_NAME = "sourceJar"

    @Override
    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, AsteriaPublishExtension)

        project.logger.debug("Applying maven publish plugin")
        project.pluginManager.apply MavenPublishPlugin

        project.logger.debug("Configuring maven publish plugin")
        project.configure(project) {
            publishing {
                repositories {
                    maven {
                        credentials(AwsCredentials) {
                            accessKey awsCredentials(project)?.AWSAccessKeyId
                            secretKey awsCredentials(project)?.AWSSecretKey
                        }
                        url project.version.toString().endsWith(SNAPSHOT_VERSION_SUFFIX) ? extension.snapshotRepositoryUrl : extension.releaseRepositoryUrl
                    }
                }
                publications {
                    mavenPom(MavenPublication) {
                        from components.java
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
            project.logger.error("Error reading AWS credentials: " + ex.message)
            return null
        }
    }
}
