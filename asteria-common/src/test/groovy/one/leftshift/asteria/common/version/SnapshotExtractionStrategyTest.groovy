package one.leftshift.asteria.common.version

import one.leftshift.asteria.common.BuildProperties
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.ZonedDateTime

class SnapshotExtractionStrategyTest extends Specification {

    @Subject
    SnapshotExtractionStrategy classUnderTest

    void setup() {
        classUnderTest = SnapshotExtractionStrategy.instance
    }

    void "returns expected result when conditions are met"() {
        given:
            Properties p = new Properties()
            p.load(SnapshotExtractionStrategyTest.getClassLoader().getResourceAsStream("build-snapshot.properties"))
        when:
            def result = classUnderTest.extract(BuildProperties.from(p))
        then:
            result == "0.4.0-dev.20180619T211755Z.289eea3"
    }

    @Unroll
    void "returns null if not responsible"() {
        when:
            def result = classUnderTest.extract(BuildProperties.from(version, "123sd234234234", ZonedDateTime.now()))
        then:
            result == null
        where:
            version            || _
            "1.2.3"            || _
            "1.2-SNAPSHOT"     || _
            "1.2.3-SNAPSHOWWT" || _
            "A.B.C-SNAPSHOT"   || _
            "1.a.2-SNAPSHOT"   || _
    }
}
