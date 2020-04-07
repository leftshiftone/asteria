package one.leftshift.asteria.publish

import spock.lang.Specification
import spock.lang.Subject

class AsteriaPublishPluginTest extends Specification {

    @Subject
    AsteriaPublishPlugin asteriaPublishPlugin

    void setup() {
        asteriaPublishPlugin = new AsteriaPublishPlugin()
    }

    void "works"() {
        expect:
        asteriaPublishPlugin != null
    }
}
