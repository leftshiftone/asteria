package one.leftshift.asteria.publish

class AsteriaPublishExtension {

    /**
     * The URL pointing to the maven repository.
     */
    String releaseRepositoryUrl = "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/releases"

    /**
     * The URL pointing to the maven repository.
     */
    String snapshotRepositoryUrl = "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots"
}
