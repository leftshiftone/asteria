package one.leftshift.asteria.common.version

import one.leftshift.asteria.common.BuildProperties
import java.time.ZonedDateTime

import static java.time.ZoneOffset.UTC
import static one.leftshift.asteria.common.date.SemVerDateTimeFormatter.SEMVER_DATE_TIME_FORMATTER

@Singleton
class SnapshotExtractionStrategy extends AbstractVersionExtractionStrategy {

    public static final String SNAPSHOT_IDENTIFIER = "-SNAPSHOT"
    public static final String DEV_VERSION_IDENTIFIER = "dev"

    @Override
    boolean isApplicable(BuildProperties properties) {
        return (properties?.version != null
                && properties?.revision != null
                && properties?.timestamp != null
                && properties?.revision?.length() >= 7
                && properties.version.matches(/\d+\.\d+\.\d+-SNAPSHOT/))
    }

    @Override
    String handle(BuildProperties properties) {
        final String version = properties.version - SNAPSHOT_IDENTIFIER
        return "${version}-${DEV_VERSION_IDENTIFIER}.${formatTimestamp(properties.timestamp)}.${assembleRevision(properties)}"
    }

    private static String assembleRevision(BuildProperties properties) {
        return properties.revision.substring(0, 7)
    }

    private static String formatTimestamp(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(UTC).format(SEMVER_DATE_TIME_FORMATTER)
    }
}
