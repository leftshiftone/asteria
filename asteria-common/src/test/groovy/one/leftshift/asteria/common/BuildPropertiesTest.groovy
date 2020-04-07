package one.leftshift.asteria.common

import one.leftshift.asteria.common.date.AsteriaDateFormat
import spock.lang.Specification

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BuildPropertiesTest extends Specification {

    void "can be instantiated through a corresponding properties file"() {
        given:
            Properties p = new Properties()
            p.load(BuildPropertiesTest.getClassLoader().getResourceAsStream("build.properties"))
        when:
            def result = BuildProperties.from(p)
        then:
            result != null
            result.version == "0.8.0"
            result.revision == "28dde56abe7475c6d77c25ac9f3a8789ee5dad26b6a1c98374c3f865fb209bee3bc8f709d202ebf598f1c5bb94c133d5ad4bbb5392bf2be65e2a5a9da156f5aaf28ab4e022652315207806937b080393b95f73f6c22b537d227543bdd62938351f40e1d2348affec7d421e3d04fd324055015d38005b2b8ff5f0a37f15b875bad0373bbae157e0b5b99cd6e9f54191ec72fbab5e11347763f1b056a969f35d03d1b40bf746e34d9a38d4286e8eb949878aa406907505a2cb8b2ff3893cbe2fa6741b431d3677c685c790ddf136e8b90ec1e78b60f1e528ce3355d7aa951717a24012679b910468fd6a7e5c5929d9078e9b3ac7b214604e62c8bbe56dec53c3b0b3b4ae9130055b990bd7dd95339a69b33d9d7d61980e19f4"
            result.timestamp == ZonedDateTime.parse("2018.06.19-23.17.55-CEST", DateTimeFormatter.ofPattern(AsteriaDateFormat.ASTERIA_DEFAULT.pattern))
    }
}
