package one.leftshift.asteria.common.version

import groovy.transform.PackageScope
import one.leftshift.asteria.common.BuildProperties

/**
 * Returns the version without any modifications.
 */
@PackageScope
@Singleton
class DefaultVersionExtractorStrategy extends AbstractVersionExtractionStrategy {

    @Override
    boolean isApplicable(BuildProperties properties) {
        return true
    }

    @Override
    String handle(BuildProperties properties) {
        return properties?.version
    }
}
