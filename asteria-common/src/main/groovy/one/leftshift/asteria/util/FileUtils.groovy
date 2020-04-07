package one.leftshift.asteria.util

/**
 * Provides basic file operations that are not covered in {@link java.nio.file.Files}
 */
final class FileUtils {

    private final static String FILE_EXTENSION_SEPARATOR = "."

    static String getBaseName(File file) {
        final String fqName = file.getName()
        int lastIndex = indexOfExtension(file)
        return lastIndex == -1 ? file.getName() : fqName.substring(0, lastIndex)
    }

    static int indexOfExtension(File file) {
        if (file == null)
            return -1
        final String fqName = file.getName() ?: ""

        return fqName.lastIndexOf(FILE_EXTENSION_SEPARATOR)
    }
}
