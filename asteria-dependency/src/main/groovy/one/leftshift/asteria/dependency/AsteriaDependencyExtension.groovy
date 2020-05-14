package one.leftshift.asteria.dependency

import one.leftshift.asteria.common.branch.BranchResolver


class AsteriaDependencyExtension {

    /**
     * Commit message for lock file commit.
     */
    String commitMessage = "[INFRA] (deps) dependency lock file"

    /**
     * Enable or disable dependency management.
     */
    Boolean dependencyManagementEnabled = true

    /**
     * Dependency management BOM which will be used for version resolution.
     */
    String dependencyManagementBom = "org.springframework.boot:spring-boot-dependencies:latest.release"

    /**
     * If enabled the plugin encounters if the code is currently in a feature branch and creates a custom maven
     * repository for uploading snapshot artifacts for this feature.
     */
    boolean enableBranchSnapshotRepositories = false

    /**
     * Branch regex.
     */
    String snapshotBranchRegex = BranchResolver.SNAPSHOT_BRANCH_REGEX

    /**
     * Regex to determine name for snapshot repository to be used or created based on the branch name (regex groups are not supported).
     */
    String snapshotRepositoryNameRegex = BranchResolver.SNAPSHOT_REPOSITORY_NAME_REGEX

    /**
     * The URL pointing to the maven repository.
     */
    String snapshotRepositoryUrl = "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots"
}
