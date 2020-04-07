package one.leftshift.asteria.common.date

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

import static java.time.chrono.IsoChronology.INSTANCE
import static java.time.format.ResolverStyle.STRICT
import static java.time.format.SignStyle.EXCEEDS_PAD
import static java.time.temporal.ChronoField.*

class SemVerDateTimeFormatter {

    public static final DateTimeFormatter SEMVER_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 10, EXCEEDS_PAD)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendValue(SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(NANO_OF_SECOND, 0, 9, true)
            .appendPattern("z")
            .toFormatter(STRICT, INSTANCE)
}
