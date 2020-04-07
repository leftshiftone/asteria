package one.leftshift.asteria.common.version

import one.leftshift.asteria.common.BuildProperties

abstract class AbstractVersionExtractionStrategy implements VersionExtractionStrategy {

    abstract boolean isApplicable(BuildProperties properties)
    abstract String handle(BuildProperties properties)

    @Override
    String extract(BuildProperties properties) {
        if (isApplicable(properties)) {
            return handle(properties)
        }
        return null
    }
}
