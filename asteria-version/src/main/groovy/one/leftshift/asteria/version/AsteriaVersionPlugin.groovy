package one.leftshift.asteria.version

import nebula.plugin.release.ReleasePlugin
import one.leftshift.asteria.version.tasks.CreateBuildPropertiesTask
import org.ajoberstar.gradle.git.release.opinion.Strategies
import org.ajoberstar.gradle.git.release.semver.PartialSemVerStrategy
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project

import static nebula.plugin.release.NetflixOssStrategies.DEVELOPMENT
import static nebula.plugin.release.NetflixOssStrategies.SNAPSHOT
import static org.ajoberstar.gradle.git.release.opinion.Strategies.PreRelease.*
import static org.ajoberstar.gradle.git.release.semver.ChangeScope.PATCH
import static org.ajoberstar.gradle.git.release.semver.SemVerStrategy.SCOPE_PROP
import static org.ajoberstar.gradle.git.release.semver.StrategyUtil.all
import static org.ajoberstar.gradle.git.release.semver.StrategyUtil.closure

class AsteriaVersionPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Version"
    static final String EXTENSION_NAME = "asteriaVersion"
    static final String CREATE_BUILD_PROPERTIES_TASK_NAME = "createBuildProperties"

    Grgit git

    @Override
    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, AsteriaVersionExtension)
        git = Grgit.open(dir: project.rootProject.projectDir.absolutePath)

        project.logger.debug("Applying nebula release plugin")
        project.pluginManager.apply ReleasePlugin

        project.logger.debug("Configuring nebula release plugin")
        project.configure(project) {
            release {
                versionStrategy DEVELOPMENT.copyWith(
                        preReleaseStrategy: all(STAGE_FLOAT, COUNT_COMMITS_SINCE_ANY, SHOW_UNCOMMITTED, GIT_REVISION),
                        buildMetadataStrategy: Strategies.BuildMetadata.NONE
                )
                defaultVersionStrategy = SNAPSHOT
            }

            project.afterEvaluate {
                if (project.hasProperty(SCOPE_PROP) && project.property(SCOPE_PROP)?.toUpperCase() == PATCH.name()) {
                    String branch = git.branch.current.name
                    project.logger.info("Checking if current branch ${branch} is a release branch")
                    if (branch ==~ extension.releaseBranchRegex) {
                        project.logger.debug("Current branch ${branch} is a release branch")
                    } else {
                        throw new AsteriaVersionException("Building patch version from branch ${branch} not allowed ()")
                    }
                }
            }
        }

        project.logger.debug("Adding tasks")
        project.task(CREATE_BUILD_PROPERTIES_TASK_NAME, type: CreateBuildPropertiesTask)
    }

    PartialSemVerStrategy GIT_REVISION = closure { state ->
        def inferred = state.inferredPreRelease ? "${state.inferredPreRelease}.${state.currentHead.abbreviatedId}" : state.currentHead.abbreviatedId
        state.copyWith(inferredPreRelease: inferred)
    }
}
