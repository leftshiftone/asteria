package one.leftshift.asteria.dependency

import com.github.benmanes.gradle.versions.VersionsPlugin
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import nebula.plugin.dependencylock.DependencyLockPlugin
import nebula.plugin.release.ReleasePlugin
import one.leftshift.asteria.dependency.tasks.PersistDependencyLockTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentSelection
import org.gradle.api.artifacts.DependencyResolveDetails

class AsteriaDependencyPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Dependency"
    static final String EXTENSION_NAME = "asteriaDependency"
    static final String SNAPSHOT_VERSION_SUFFIX = "-SNAPSHOT"
    static final String LATEST_SMART_VERSION = "latest.smart"
    static final String PERSIST_DEPENDENCY_LOCK_TASK_NAME = "persistDependencyLock"

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
        }

        project.logger.debug("Adding tasks")
        if (!project.rootProject.tasks.find { it.name == PERSIST_DEPENDENCY_LOCK_TASK_NAME }) {
            project.rootProject.task(PERSIST_DEPENDENCY_LOCK_TASK_NAME, type: PersistDependencyLockTask)
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
                        boolean rejected = ['alpha', 'beta', 'rc', 'cr', 'm'].any { qualifier ->
                            selection.candidate.version ==~ /(?i).*[.-]${qualifier}[.\d-]*/
                        }
                        if (rejected) {
                            selection.reject('Release candidate')
                        }
                    }
                }
            }

            project.logger.debug("Configuring tasks to ignore dependency lock for non release builds")
            if (!project.hasProperty("dependencyLock.ignore")) {
                if (project.version.toString().endsWith(SNAPSHOT_VERSION_SUFFIX)) {
                    project.logger.info("Dependency lock is ignored for version ${project.version}")
                    project.ext.set("dependencyLock.ignore", true)
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

    private static void interceptVersion(Project project, DependencyResolveDetails dependency) {
        if (project.version.toString().endsWith(SNAPSHOT_VERSION_SUFFIX)) {
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
