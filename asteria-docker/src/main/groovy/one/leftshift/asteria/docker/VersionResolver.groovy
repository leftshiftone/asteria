package one.leftshift.asteria.docker

import one.leftshift.asteria.AsteriaDockerExtension
import one.leftshift.asteria.common.BuildProperties
import one.leftshift.asteria.common.BuildPropertiesResolver
import one.leftshift.asteria.common.version.ReleaseExtractionStrategy
import one.leftshift.asteria.common.version.SnapshotExtractionStrategy
import one.leftshift.asteria.common.version.VersionExtractor

class VersionResolver {
    AsteriaDockerExtension extension
    String explicitVersion

    VersionResolver(AsteriaDockerExtension extension, String explicitVersion) {
        this.extension = extension
        this.explicitVersion = explicitVersion
    }

    String resolve() {
        if (explicitVersion != null) return explicitVersion.split("/").last()
        final String extractedVersion = VersionExtractor
                .defaultExtractor()
                .addStrategies(SnapshotExtractionStrategy.instance, ReleaseExtractionStrategy.instance)
                .extractVersion(BuildProperties.from(BuildPropertiesResolver.resolve(extension.project)), extension.project.version as String)
        return extension?.versionPrefix ? "${extension.versionPrefix}$extractedVersion" : extractedVersion
    }
}