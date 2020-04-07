package one.leftshift.asteria.version

import spock.lang.Specification
import spock.lang.Subject

class AsteriaVersionPluginTest extends Specification {

    @Subject
    AsteriaVersionPlugin asteriaVersionPlugin

    void setup() {
        asteriaVersionPlugin = new AsteriaVersionPlugin()
    }

    void "works"() {
        expect:
            asteriaVersionPlugin != null
    }
}
