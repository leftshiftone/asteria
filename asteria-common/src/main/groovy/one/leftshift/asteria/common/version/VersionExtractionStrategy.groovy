package one.leftshift.asteria.common.version

import one.leftshift.asteria.common.BuildProperties

interface VersionExtractionStrategy {

    /**
     * Extracts a version string (e.g: 0.1.2-dev.4j3h4h) from {@link BuildProperties}
     * @param properties
     * @return a version string or null if the strategy is not applicable
     */
    String extract(BuildProperties properties)
}