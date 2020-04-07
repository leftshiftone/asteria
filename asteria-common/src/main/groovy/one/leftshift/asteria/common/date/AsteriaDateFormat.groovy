package one.leftshift.asteria.common.date

enum AsteriaDateFormat {

    ASTERIA_DEFAULT("uuuu.MM.dd-HH.mm.ss-z")

    final String pattern

    AsteriaDateFormat(String pattern) {
        this.pattern = pattern
    }
}
