package one.leftshift.asteria.util

import spock.lang.Specification
import spock.lang.Unroll

class FileUtilsTest extends Specification {

    @Unroll
    void "extracts base name: #expectation from path: #path"() {
        given:
            File f = new File(path)
        when:
            def result = FileUtils.getBaseName(f)
        then:
            result == expectation
        where:
            path                          || expectation
            "/home/user/somefile.txt"     || "somefile"
            "/home/user/some.file.txt"    || "some.file"
            "/home/u.s.e.r/some.file.txt" || "some.file"
            "/home/abc"                   || "abc"
            "somefile.txt"                || "somefile"
            "some.file.txt"               || "some.file"
            "a."                          || "a"
            "."                           || ""
    }
}
