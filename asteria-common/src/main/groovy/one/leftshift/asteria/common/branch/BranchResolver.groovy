package one.leftshift.asteria.common.branch

import org.slf4j.Logger

import java.util.regex.Matcher


/**
 * @author Michael Mair
 */
abstract class BranchResolver {

    public static final String PATCH_RELEASE_BRANCH_REGEX = "^release\\/\\d+\\.\\d+\\.x\$"
    public static final String SNAPSHOT_BRANCH_REGEX = "^(feature|bug|bugfix|hotfix)\\/([A-Z]+-\\d+).*\$"
    public static final String SNAPSHOT_REPOSITORY_NAME_REGEX = "[A-Z]+-\\d+"

    static boolean isPatchReleaseBranch(String branchName, String branchRegex) {
        Matcher branchMatcher = branchName =~ branchRegex
        if (branchMatcher) return true
        return false
    }

    static String getSnapshotRepositoryUrlBasedOnBranch(boolean enableBranchSnapshotRepositories,
                                                        String defaultSnapshotRepositoryUrl,
                                                        String branchName,
                                                        String branchRegex,
                                                        String snapshotRepositoryRegex,
                                                        Logger logger) {

        if (enableBranchSnapshotRepositories) {
            logger.info("Snapshot repositories for branches are enabled")
            Matcher branchMatcher = branchName =~ branchRegex
            if (!branchMatcher) {
                logger.debug("Branch {} does not match regex {}", branchName, branchRegex)
                logger.info("Using snapshot repository {}", defaultSnapshotRepositoryUrl)
                return defaultSnapshotRepositoryUrl
            }
            Matcher snapshotRepositoryMatcher = branchName =~ snapshotRepositoryRegex
            if (!snapshotRepositoryMatcher.find()) {
                logger.warn("Unable to determine name for snapshot repository for branch {} and regex {}", branchName, snapshotRepositoryRegex)
                logger.info("Using snapshot repository {}", defaultSnapshotRepositoryUrl)
                return defaultSnapshotRepositoryUrl
            }
            String snapshotRepositoryNameSuffix = snapshotRepositoryMatcher.group(0)?.toLowerCase()
            if (!snapshotRepositoryNameSuffix) {
                logger.warn("Unable to determine name for snapshot repository for branch {} and regex {}", branchName, snapshotRepositoryRegex)
                logger.info("Using snapshot repository {}", defaultSnapshotRepositoryUrl)
                return defaultSnapshotRepositoryUrl
            }

            String snapshotRepositoryUrl = "${defaultSnapshotRepositoryUrl}-${snapshotRepositoryNameSuffix}"
            logger.info("Using snapshot repository {}", snapshotRepositoryUrl)
            return snapshotRepositoryUrl
        }
        logger.info("Using snapshot repository {}", defaultSnapshotRepositoryUrl)
        return defaultSnapshotRepositoryUrl
    }

}
