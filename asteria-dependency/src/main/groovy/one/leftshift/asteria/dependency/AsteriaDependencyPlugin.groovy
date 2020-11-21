package one.leftshift.asteria.dependency

import com.amazonaws.SdkBaseException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.github.benmanes.gradle.versions.VersionsPlugin
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import nebula.plugin.dependencylock.DependencyLockPlugin
import nebula.plugin.release.ReleasePlugin
import one.leftshift.asteria.common.branch.BranchResolver
import one.leftshift.asteria.dependency.tasks.CleanBranchSnapshotArtifactsTask
import one.leftshift.asteria.dependency.tasks.PersistDependencyLockTask
import one.leftshift.asteria.dependency.tasks.UpdateDependencyInLockTask
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentSelection
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.credentials.AwsCredentials

import static one.leftshift.asteria.common.version.VersionCategorizer.isPreReleaseVersion
import static one.leftshift.asteria.common.version.VersionCategorizer.isSnapshotVersion

class AsteriaDependencyPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Dependency"
    static final String EXTENSION_NAME = "asteriaDependency"
    static final String LATEST_SMART_VERSION = "latest.smart"
    static final String PERSIST_DEPENDENCY_LOCK_TASK_NAME = "persistDependencyLock"
    static final String UPDATE_DEPENDENCY_LOCK_TASK_NAME = "updateDependencyLock"
    static final String CLEAN_BRANCH_SNAPSHOT_ARTIFACTS_TASK_NAME = "cleanBranchArtifacts"

    @Override
    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, AsteriaDependencyExtension)

        project.logger.debug("Applying spring dependency management plugin")
        project.pluginManager.apply DependencyManagementPlugin

        project.afterEvaluate {
            if (extension.dependencyManagementEnabled) {
                project.logger.debug("Configuring spring dependency management plugin")
                project.dependencyManagement {
                    imports {
                        project.logger.info("Using maven bom ${extension.dependencyManagementBom}")
                        mavenBom extension.dependencyManagementBom
                    }
                }
            }
        }

        project.logger.debug("Applying dependency versions updates check plugin")
        project.pluginManager.apply VersionsPlugin

        project.logger.debug("Applying nebula dependency lock plugin")
        project.pluginManager.apply DependencyLockPlugin
        project.ext.set("dependencyLock.includeTransitives", true)

        project.logger.debug("Applying dependency interception")
        project.configurations.all {
            resolutionStrategy.cacheDynamicVersionsFor 30, "minutes"
            resolutionStrategy.cacheChangingModulesFor 0, "seconds"
            resolutionStrategy.eachDependency { DependencyResolveDetails dep ->
                project.logger.debug("Evaluating dependency ${dep.requested.group}:${dep.requested.name}:${dep.requested.version}")

                if (dep.requested.group.startsWith("one.leftshift") && !dep.requested.version) {
                    interceptVersion(project, dep)
                    project.logger.info("Assigned ${dep.requested.group}:${dep.requested.name} to version ${dep.target.version} (no version set)")
                } else if (dep.requested.version == LATEST_SMART_VERSION) {
                    interceptVersion(project, dep)
                    project.logger.info("Reassigned ${dep.requested.group}:${dep.requested.name} to version ${dep.target.version} (was ${dep.requested.version})")
                }
            }

            if (project.hasProperty("dependency.prerelease.ignore") && project.property("dependency.prerelease.ignore") == "true") {
                project.logger.quiet("Dependency resolution ignores pre-releases like release candidates for dependencies starting with one.leftshift")
                resolutionStrategy {
                    componentSelection { rules ->
                        rules.all { ComponentSelection selection ->
                            if (selection.candidate.group.startsWith("one.leftshift")) {
                                if (isPreReleaseVersion(selection.candidate.version)) {
                                    project.logger.info("Rejecting version ${selection.candidate.group}:${selection.candidate.module}:${selection.candidate.version}")
                                    selection.reject("Release candidates are ignored")
                                }
                            }
                        }
                    }
                }
            }

        }

        project.logger.debug("Adding custom snapshot repository if applicable")
        // in order for this to be used as the repository with the highest priority, the plugin must be applied before
        // the declaration of the repositories in a gradle build file
        MavenArtifactRepository customSnapshotRepo = project.repositories.maven {
            url extension.snapshotRepositoryUrl
        }
        project.repositories {
            customSnapshotRepo
        }
        project.afterEvaluate {
            if (extension.enableBranchSnapshotRepositories) {
                String branchName = getCurrentGitBranch(project)
                String snapshotRepoUrl = BranchResolver.getSnapshotRepositoryUrlBasedOnBranch(
                        true,
                        extension.snapshotRepositoryUrl,
                        branchName,
                        extension.snapshotBranchRegex,
                        extension.snapshotRepositoryNameRegex,
                        project.logger)

                if (snapshotRepoUrl == extension.snapshotRepositoryUrl || snapshotRepoUrl == null || extension.snapshotRepositoryUrl == null) {
                    project.logger.info("No custom snapshot repository url detected.")
                    project.repositories.remove(customSnapshotRepo)
                } else {
                    project.logger.info("Snapshot repository url is {}", snapshotRepoUrl)
                    AWSCredentials awsCredentials = awsCredentials(project)
                    if (awsCredentials) {
                        project.logger.debug("Current repositories: {}", project.repositories.collect { it.name })
                        customSnapshotRepo.credentials(AwsCredentials) {
                            accessKey awsCredentials.AWSAccessKeyId
                            secretKey awsCredentials.AWSSecretKey
                        }
                        customSnapshotRepo.url = snapshotRepoUrl
                        MavenArtifactRepository usedRepo = project.repositories.findByName(customSnapshotRepo.name) as MavenArtifactRepository
                        int usedRepoIndex = project.repositories.indexOf(usedRepo)
                        project.logger.quiet("Using snapshot repository {} with url {} at position {} of {}", usedRepo.name, usedRepo.url, ++usedRepoIndex, project.repositories.size())
                    } else {
                        project.logger.warn("Snapshot repository credentials not found. Snapshot repository {} cannot be added. Please specify AWS credentials.", snapshotRepoUrl)
                        project.repositories.remove(customSnapshotRepo)
                    }
                }
            } else {
                project.repositories.remove(customSnapshotRepo)
            }
        }

        project.logger.debug("Adding tasks")
        if (!project.rootProject.tasks.find { it.name == PERSIST_DEPENDENCY_LOCK_TASK_NAME }) {
            project.rootProject.task(PERSIST_DEPENDENCY_LOCK_TASK_NAME, type: PersistDependencyLockTask)
        }
        if (!project.rootProject.tasks.find { it.name == UPDATE_DEPENDENCY_LOCK_TASK_NAME }) {
            project.rootProject.task(UPDATE_DEPENDENCY_LOCK_TASK_NAME, type: UpdateDependencyInLockTask)
        }
        if (!project.rootProject.tasks.find { it.name == UPDATE_DEPENDENCY_LOCK_TASK_NAME }) {
            project.rootProject.task(CLEAN_BRANCH_SNAPSHOT_ARTIFACTS_TASK_NAME, type: CleanBranchSnapshotArtifactsTask)
        }

        project.logger.debug("Configuring tasks")
        project.afterEvaluate {
            project.logger.debug("Configuring tasks to run after the other")
            project.rootProject.tasks.getByName(PERSIST_DEPENDENCY_LOCK_TASK_NAME).mustRunAfter(
                    project.tasks.getByName(DependencyLockPlugin.GENERATE_LOCK_TASK_NAME),
                    project.tasks.getByName(DependencyLockPlugin.UPDATE_LOCK_TASK_NAME),
                    project.tasks.getByName("saveLock")
            )
            project.logger.debug("Configuring updateDependencies task")
            project.tasks.dependencyUpdates.doFirst {
                if (!System.hasProperty("revision")) {
                    project.logger.info("Configure dependency updates check for releases")
                    System.setProperty("revision", "release")
                }
                if (!System.hasProperty("outputFormatter")) {
                    project.logger.info("Configure dependency updates check report format")
                    System.setProperty("outputFormatter", "json")
                }
            }
            project.tasks.dependencyUpdates.resolutionStrategy {
                componentSelection { rules ->
                    rules.all { ComponentSelection selection ->
                        if (isPreReleaseVersion(selection.candidate.version)) {
                            selection.reject('Release candidate')
                        }
                    }
                }
            }

            project.logger.debug("Configuring tasks to ignore dependency lock for non release builds")
            if (!project.hasProperty("dependencyLock.ignore")) {
                if (isSnapshotVersion(project.version.toString())) {
                    String branchName = getCurrentGitBranch(project)
                    boolean isPatchReleaseBranch = BranchResolver.isPatchReleaseBranch(branchName, extension.patchReleaseBranchRegex)
                    if (isPatchReleaseBranch) {
                        project.logger.quiet("Dependency lock is honored for branch ${branchName} and version ${project.version}")
                        project.ext.set("dependencyLock.ignore", false)
                    } else {
                        project.logger.info("Dependency lock is ignored for version ${project.version}")
                        project.ext.set("dependencyLock.ignore", true)
                    }
                } else {
                    project.logger.info("Dependency lock is honored for version ${project.version}")
                    project.ext.set("dependencyLock.ignore", false)
                }
            }
            project.tasks.generateLock.doFirst {
                project.ext.set("dependencyLock.ignore", false)
            }
            project.tasks.saveLock.doFirst {
                project.ext.set("dependencyLock.ignore", false)
            }

            project.plugins.withType(ReleasePlugin) {
                project.logger.debug("Configuring tasks to be executed before release tasks")
                def generateLockTask = project.rootProject.tasks.getByName(DependencyLockPlugin.GENERATE_LOCK_TASK_NAME)
                def saveLockTask = project.rootProject.tasks.getByName("saveLock")
                def persistLockTask = project.rootProject.tasks.getByName(PERSIST_DEPENDENCY_LOCK_TASK_NAME)
                project.rootProject.tasks.release.dependsOn generateLockTask, saveLockTask, persistLockTask
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

    private static String getCurrentGitBranch(Project project) {
        String branchName = null
        try {
            Grgit git = Grgit.open(dir: project.rootProject.projectDir.absolutePath)
            branchName = git.branch.current.name ?: "unknown"
            project.logger.info("Currently on branch {}", branchName)
        } catch (Exception ex) {
            project.logger.warn("Unable to get current branch: ${ex.message}")
            if (project.logger.isInfoEnabled()) {
                project.logger.error(ex.message, ex)
            }
        }
        return branchName
    }

    private static void interceptVersion(Project project, DependencyResolveDetails dependency) {
        if (isSnapshotVersion(project.version.toString())) {
            project.logger.debug("Found non release version ${project.version}")
            dependency.useVersion "latest.integration"
            dependency.because "latest integration version is used for non release versions"
        } else {
            project.logger.debug("Found release version ${project.version}")
            dependency.useVersion "latest.release"
            dependency.because "latest release version is used for release versions"
        }
    }
}
