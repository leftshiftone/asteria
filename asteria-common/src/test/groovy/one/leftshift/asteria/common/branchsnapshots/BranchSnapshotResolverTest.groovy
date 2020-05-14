package one.leftshift.asteria.common.branchsnapshots

import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Michael Mair
 */
@Slf4j
class BranchSnapshotResolverTest extends Specification {

    static final String BRANCH_REGEX = "^(feature|bug)\\/([A-Z]+-\\d+).*\$"
    static final String SNAPSHOT_REPOSITORY_REGEX = "[A-Z]+-\\d+"
    static final String DEFAULT_URL = "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots"

    @Unroll
    def "when: is enabled: #enabled, branch #branch return #url"() {
        expect:
            def result = BranchSnapshotResolver.getSnapshotRepositoryUrl(
                    enabled,
                    DEFAULT_URL,
                    branch,
                    BRANCH_REGEX,
                    SNAPSHOT_REPOSITORY_REGEX,
                    log)
            result == url
        where:
            enabled | branch                        || url
            false   | "master"                      || "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots"
            true    | "master"                      || "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots"
            true    | null                          || "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots"
            true    | "feature/GAIA-1000"           || "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots-gaia-1000"
            true    | "feature/GAIA-1000-something" || "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots-gaia-1000"
            true    | "some/GAIA-1000"              || "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots"
            true    | "bug/GAIA-1000"               || "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots-gaia-1000"
            true    | "bug/GAIA-1000/something"     || "s3://leftshiftone-maven-artifacts.s3.eu-central-1.amazonaws.com/snapshots-gaia-1000"
    }
}
