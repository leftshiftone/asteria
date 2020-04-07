package one.leftshift.asteria.common.version

import one.leftshift.asteria.common.BuildProperties
import one.leftshift.asteria.common.version.DefaultVersionExtractorStrategy
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.ZonedDateTime

class DefaultVersionExtractorStrategyTest extends Specification {

    @Subject
    DefaultVersionExtractorStrategy classUnderTest

    void setup() {
        classUnderTest = DefaultVersionExtractorStrategy.instance
    }

    @Unroll
    void "returns the given version"() {
        when:
            def result = classUnderTest.extract(BuildProperties.from(version, "jdh23jh423h42g73z4g", ZonedDateTime.now()))
        then:
            result == version
        where:
            version          || _
            "A.V.C"          || _
            "1.2.3-SNSNDASD" || _
            "1.2"            || _
            "X.Y"            || _
            "!.?.!"          || _

    }
}
