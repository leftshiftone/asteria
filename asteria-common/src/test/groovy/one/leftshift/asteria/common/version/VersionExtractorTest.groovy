package one.leftshift.asteria.common.version

import one.leftshift.asteria.common.BuildProperties
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Unroll

class VersionExtractorTest extends Specification {
    @Unroll
    void "extracts version from #resource as expected"() {
        given:
            VersionExtractor extractor = VersionExtractor.defaultExtractor()
            extractor.addStrategies(
                    ReleaseExtractionStrategy.instance,
                    SnapshotExtractionStrategy.instance)
            Properties p = new Properties()
        when:
            p.load(VersionExtractorTest.classLoader.getResourceAsStream(resource))
            def result = extractor.extractVersion(BuildProperties.from(p), "3.3.3")
        then:
            result == expectation
        where:
            resource                       || expectation
            "1_invalid-build.properties"   || "0.8.0"
            "2_invalid-build.properties"   || "0.8.0"
            "3_invalid-build.properties"   || "3.3.3"
            "build.properties"             || "0.8.0"
            "build-snapshot.properties"    || "0.4.0-dev.20180619T211755Z.289eea3"
            "dev-build.properties"         || "0.4.0-dev.86.uncommitted.289eea3"
            "uncommitted-build.properties" || "0.4.0-dev.86.uncommitted.289eea3"
    }

    @Unroll
    void "extracts expected version from project"() {
        given:
            VersionExtractor extractor = VersionExtractor.defaultExtractor()
            extractor.addStrategies(
                    ReleaseExtractionStrategy.instance,
                    SnapshotExtractionStrategy.instance)
        when:
            def result = extractor.extractVersion(BuildProperties.from(Mock(Project).with {
                it.version >> version; it
            }), "3.3.3")
        then:
            result == expectation
        where:
            version          || expectation
            "0.8.0"          || "0.8.0"
            "0.4.0-SNAPSHOT" || "0.4.0-SNAPSHOT"
            "A.B.C"          || "A.B.C"
            "1.2.3.4"        || "1.2.3.4"
            null             || "3.3.3"
    }
}
