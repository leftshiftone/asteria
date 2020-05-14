package one.leftshift.asteria.common.branchsnapshots

import org.slf4j.Logger

import java.util.regex.Matcher


/**
 * @author Michael Mair
 */
abstract class BranchSnapshotResolver {

    static String getSnapshotRepositoryUrl(boolean enableBranchSnapshotRepositories,
                                           String defaultSnapshotRepositoryUrl,
                                           String branchName,
                                           String branchRegex,
                                           String snapshotRepositoryRegex,
                                           Logger logger) {

        if (enableBranchSnapshotRepositories) {
            logger.info("Snapshot repositories for branches are enabled")
            Matcher branchMatcher = branchName =~ branchRegex
            if (!branchMatcher) {
                logger.debug("Branch {} does not matches regex {}", branchName, branchRegex)
                logger.info("Using snapshot repository {}", defaultSnapshotRepositoryUrl)
                return defaultSnapshotRepositoryUrl
            }
            Matcher snapshotRepositoryMatcher = branchName =~ snapshotRepositoryRegex
            if (!snapshotRepositoryMatcher.find()) {
                logger.warn("Unable to determine name for snapshot repository for branch {} and regex {}", branchName, snapshotRepositoryRegex)
                return defaultSnapshotRepositoryUrl
            }
            String snapshotRepositoryNameSuffix = snapshotRepositoryMatcher.group(0)?.toLowerCase()
            if (!snapshotRepositoryNameSuffix) {
                logger.warn("Unable to determine name for snapshot repository for branch {} and regex {}", branchName, snapshotRepositoryRegex)
                return defaultSnapshotRepositoryUrl
            }

            String snapshotRepositoryUrl = "${defaultSnapshotRepositoryUrl}-${snapshotRepositoryNameSuffix}"
            logger.info("Using snapshot repository url {}", snapshotRepositoryUrl)
            return snapshotRepositoryUrl
        }
        logger.info("Using snapshot repository {}", defaultSnapshotRepositoryUrl)
        return defaultSnapshotRepositoryUrl
    }

}
