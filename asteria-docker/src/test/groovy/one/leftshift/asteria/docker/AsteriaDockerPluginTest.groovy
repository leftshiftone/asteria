package one.leftshift.asteria.docker

import one.leftshift.asteria.AsteriaDockerPlugin
import spock.lang.Specification
import spock.lang.Subject

class AsteriaDockerPluginTest extends Specification {
    @Subject
    AsteriaDockerPlugin classUnderTest

    void setup() {
        classUnderTest = new AsteriaDockerPlugin()
    }

    void "works"() {
        expect:
            classUnderTest != null
    }
}
