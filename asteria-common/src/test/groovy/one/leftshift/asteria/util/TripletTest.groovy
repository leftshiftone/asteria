package one.leftshift.asteria.util

import spock.lang.Specification
import spock.lang.Subject

class TripletTest extends Specification {
    
    @Subject
    Triplet<String, String, String> classUnderTest
    
    void "prohibits null values"() {
        when:
            classUnderTest = Triplet.of(l, m, r)
        then:
            thrown(IllegalArgumentException)
        where:
            l    | m    | r    || _
            "a"  | "b"  | null || _
            "a"  | null | "c"  || _
            null | "b"  | "c"  || _
            null | null | null || _
    }
}
