package one.leftshift.asteria.publish

import one.leftshift.asteria.common.branchsnapshots.BranchSnapshotResolver

class AsteriaPublishExtension {

    /**
     * The URL pointing to the maven repository.
     */
    String releaseRepositoryUrl = "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/releases"

    /**
     * The URL pointing to the maven repository.
     */
    String snapshotRepositoryUrl = "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots"

    /**
     * If enabled the plugin encounters if the code is currently in a feature branch and creates a custom maven
     * repository for uploading snapshot artifacts for this feature.
     */
    boolean enableBranchSnapshotRepositories = false

    /**
     * If enabled the plugin creates snapshot repositories in AWS S3.
     */
    boolean createSnapshotRepositories = false

    /**
     * Branch regex.
     */
    String snapshotBranchRegex = BranchSnapshotResolver.SNAPSHOT_BRANCH_REGEX

    /**
     * Regex to determine name for snapshot repository to be used or created based on the branch name (regex groups are not supported).
     */
    String snapshotRepositoryNameRegex = BranchSnapshotResolver.SNAPSHOT_REPOSITORY_NAME_REGEX

    /**
     * Snapshot expiration date in days.
     */
    int snapshotsExpirationInDays = 30

    /**
     * Additional artifacts to publish which will be added like 'artifact "path/to/jar"'.
     */
    List<Object> additionalArtifacts = Collections.emptyList()
}
