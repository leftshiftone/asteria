package one.leftshift.asteria.common.version

/**
 * @author Michael Mair
 */
abstract class VersionCategorizer {

    public static final String SNAPSHOT_VERSION_SUFFIX = "-SNAPSHOT"
    public static final String[] PRERELEASE_VERSION_INDICATORS = ["alpha", "beta", "rc", "cr"]

    static boolean isSnapshotVersion(String version) {
        return version?.endsWith(SNAPSHOT_VERSION_SUFFIX)
    }

    static boolean isPreReleaseVersion(String version) {
        return PRERELEASE_VERSION_INDICATORS.any { indicator ->
            version ==~ /(?i).*[.-]${indicator}[.\d-]*/
        }
    }

    static boolean isReleaseVersion(String version) {
        return version ==~ /[\d\.]+/
    }
}
