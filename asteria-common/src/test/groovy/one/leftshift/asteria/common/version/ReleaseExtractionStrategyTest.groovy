package one.leftshift.asteria.common.version

import one.leftshift.asteria.common.BuildProperties
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.ZonedDateTime

class ReleaseExtractionStrategyTest extends Specification {
    @Subject
    ReleaseExtractionStrategy classUnderTest

    void setup() {
        classUnderTest = ReleaseExtractionStrategy.instance
    }

    void "returns expected result when conditions are met"() {
        given:
            Properties p = new Properties()
            p.load(ReleaseExtractionStrategyTest.getClassLoader().getResourceAsStream("build.properties"))
        when:
            def result = classUnderTest.extract(BuildProperties.from(p))
        then:
            result == "0.8.0"
    }

    @Unroll
    void "returns null if not responsible"() {
        when:
            def result = classUnderTest.extract(BuildProperties.from(version, "123sd234234234", ZonedDateTime.now()))
        then:
            result == null
        where:
            version            || _
            "1.2.x"            || _
            "1.2.1-SNAPSHOT"   || _
            "1.2.3-SNAPSHOWWT" || _
            "A.B.C"            || _
            "1.a.2"            || _
    }
}
