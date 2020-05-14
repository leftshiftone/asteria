package one.leftshift.asteria.dependency

import one.leftshift.asteria.common.branchsnapshots.BranchSnapshotResolver

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
    String snapshotBranchRegex = BranchSnapshotResolver.SNAPSHOT_BRANCH_REGEX

    /**
     * Regex to determine name for snapshot repository to be used or created based on the branch name (regex groups are not supported).
     */
    String snapshotRepositoryNameRegex = BranchSnapshotResolver.SNAPSHOT_REPOSITORY_NAME_REGEX

}
