package one.leftshift.asteria.docker

import one.leftshift.asteria.AsteriaDockerExtension
import one.leftshift.asteria.common.BuildProperties
import one.leftshift.asteria.common.BuildPropertiesResolver
import one.leftshift.asteria.common.branch.BranchResolver
import one.leftshift.asteria.common.version.ReleaseExtractionStrategy
import one.leftshift.asteria.common.version.SnapshotExtractionStrategy
import one.leftshift.asteria.common.version.VersionExtractor

class DockerTagResolver {
    public static final String DOCKER_TAG_REGEX = ".*?([A-Za-z0-9_][A-Za-z0-9_.\\-]{0,127})\$"
    public static final String RELEASE_TAG_REGEX = "^\\d+\\.\\d+\\.\\d+.*"
    AsteriaDockerExtension extension
    String explicitTag

    DockerTagResolver(AsteriaDockerExtension extension, String explicitTag) {
        this.extension = extension
        this.explicitTag = explicitTag
    }

    String resolve() {
        if (explicitTag != null) {
            final ticketId = BranchResolver.getTicketIdBasedOnBranch(explicitTag)
            final fallback = explicitTag =~ DOCKER_TAG_REGEX
            fallback.find()
            final resolvedTag = ticketId ? ticketId : fallback.group(1)
            if ((resolvedTag =~ RELEASE_TAG_REGEX).find()) {
                throw new RuntimeException("The tag ($resolvedTag) inferred from the supplied explicitTag ($explicitTag)  must not match a release tag.")
            }
            return resolvedTag
        }
        final String extractedVersion = VersionExtractor
                .defaultExtractor()
                .addStrategies(SnapshotExtractionStrategy.instance, ReleaseExtractionStrategy.instance)
                .extractVersion(BuildProperties.from(BuildPropertiesResolver.resolve(extension.project)), extension.project.version as String)
        return extension?.versionPrefix ? "${extension.versionPrefix}$extractedVersion" : extractedVersion
    }
}