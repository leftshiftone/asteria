package one.leftshift.asteria.docs

import spock.lang.Specification
import spock.lang.Subject

class AsteriaDocsPluginTest extends Specification {

    @Subject
    AsteriaDocsPlugin asteriaDocsPlugin

    void setup() {
        asteriaDocsPlugin = new AsteriaDocsPlugin()
    }

    void "works"() {
        expect:
            asteriaDocsPlugin != null
    }
}
