package one.leftshift.asteria.jira

import spock.lang.Specification
import spock.lang.Subject

class AsteriaJiraPluginTest extends Specification {

    @Subject
    AsteriaJiraPlugin asteriaJiraPlugin

    void setup() {
        asteriaJiraPlugin = new AsteriaJiraPlugin()
    }

    void "works"() {
        expect:
            asteriaJiraPlugin != null
    }
}
