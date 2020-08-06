package one.leftshift.asteria.common.version


import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Michael Mair
 */
class VersionCategorizerTest extends Specification {

    static String SNAPSHOT = "snapshot"
    static String PRE = "pre"
    static String RELEASE = "release"

    @Unroll
    def "version #version is #category: #expected"() {
        expect:
        Boolean result = null
        switch (category) {
            case SNAPSHOT:
                result = VersionCategorizer.isSnapshotVersion(version)
                break
            case PRE:
                result = VersionCategorizer.isPreReleaseVersion(version)
                break
            case RELEASE:
                result = VersionCategorizer.isReleaseVersion(version)
                break
        }
        result == expected

        where:
        version          | category || expected
        null             | SNAPSHOT || false
        null             | PRE      || false
        null             | RELEASE  || false
        "0.1.0-SNAPSHOT" | SNAPSHOT || true
        "0.1.0-SNAPSHOT" | PRE      || false
        "0.1.0-SNAPSHOT" | RELEASE  || false
        "0.1.0-rc.1"     | SNAPSHOT || false
        "0.1.0-rc.1"     | PRE      || true
        "0.1.0-rc.1"     | RELEASE  || false
        "0.1.0-RC.20"    | SNAPSHOT || false
        "0.1.0-RC.20"    | PRE      || true
        "0.1.0-RC.20"    | RELEASE  || false
        "0.11.0-alpha.1" | SNAPSHOT || false
        "0.11.0-alpha.1" | PRE      || true
        "0.11.0-alpha.1" | RELEASE  || false
        "0.11.0-BETA"    | SNAPSHOT || false
        "0.11.0-BETA"    | PRE      || true
        "0.11.0-BETA"    | RELEASE  || false
        "0.1.0"          | SNAPSHOT || false
        "0.1.0"          | PRE      || false
        "0.1.0"          | RELEASE  || true
        "0"              | SNAPSHOT || false
        "0"              | PRE      || false
        "0"              | RELEASE  || true
        "2.1"            | SNAPSHOT || false
        "2.1"            | PRE      || false
        "2.1"            | RELEASE  || true
        "2.1.0.567.9"    | SNAPSHOT || false
        "2.1.0.567.9"    | PRE      || false
        "2.1.0.567.9"    | RELEASE  || true
        "2.1.0.567,9"    | SNAPSHOT || false
        "2.1.0.567,9"    | PRE      || false
        "2.1.0.567,9"    | RELEASE  || false
    }

}
