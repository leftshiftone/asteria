package one.leftshift.asteria.util

abstract class Assert {

    static <T> void notNull(T object, String message) {
        if (object == null)
            throw new IllegalArgumentException(message)
    }

    static <T> void notNull(T... objects) {
        objects.each { notNull(it, "Null arguments are not allowed") }
    }
}
