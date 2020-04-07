package one.leftshift.asteria.common.version

import one.leftshift.asteria.common.BuildProperties

@Singleton
class ReleaseExtractionStrategy extends AbstractVersionExtractionStrategy {
    @Override
    boolean isApplicable(BuildProperties properties) {
        return (properties?.version != null && properties.version.matches(/\d+\.\d+\.\d+/))
    }

    @Override
    String handle(BuildProperties properties) {
        return properties.version
    }
}
