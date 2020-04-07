package one.leftshift.asteria.helm

import spock.lang.Specification
import spock.lang.Subject

class AsteriaHelmPluginTest extends Specification {

    @Subject
    AsteriaHelmPlugin asteriaHelmPlugin

    void setup() {
        asteriaHelmPlugin = new AsteriaHelmPlugin()
    }

    void "works"() {
        expect:
            asteriaHelmPlugin != null
    }
}
