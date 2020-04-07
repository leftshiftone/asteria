package one.leftshift.asteria.common.date

import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AsteriaDateFormatTest extends Specification {

    void "valid date strings are parsed according to the pattern"() {
        when:
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AsteriaDateFormat.ASTERIA_DEFAULT.pattern)
            ZonedDateTime.parse(dateString, formatter)
        then:
            noExceptionThrown()
        where:
            dateString << [
                    "2018.05.30-14.30.09-CEST",
                    "2018.05.30-14.30.09-PST",
                    "2018.05.30-14.30.09-WIB"
            ]

    }

    void "formats dates"() {
        when:
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AsteriaDateFormat.ASTERIA_DEFAULT.pattern)
            date.format(formatter)
        then:
            noExceptionThrown()
        where:
            date << ZoneId.getAvailableZoneIds().collect {
                ZonedDateTime.of(LocalDateTime.of(
                        2018, 5, 30, 14, 30, 9
                ), ZoneId.of(it))
            }

    }
}