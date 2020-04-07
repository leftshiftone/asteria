package one.leftshift.asteria.deploy.replace

import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.nio.file.Paths

class SimpleTokenReplacerTest extends Specification {

    @Subject
    SimpleTokenReplacer classUnderTest

    void setup() {
        classUnderTest = new SimpleTokenReplacer()
    }

    @Unroll
    void "replaces tokens as expected"() {
        given:
            ReplaceRequest replaceRequest = new ReplaceRequest(Paths.get(getClass().getClassLoader()
                    .getResource("Dockerrun.aws.json").toURI()),
                    ["version": version])
            JsonSlurper jsonSlurper = new JsonSlurper()
        when:
            def result = classUnderTest.replace(replaceRequest)
        then:
            StringWriter sw = new StringWriter()
            result.writeTo(sw)
            def json = jsonSlurper.parseText(sw.toString())
            json.containerDefinitions.image.first() == "007098893018.dkr.ecr.eu-central-1.amazonaws.com/gaia-web:${version}"
        where:
            version                           || _
            "1.2.3"                           || _
            "0.5.0-dev.0.uncommitted.a394e92" || _
    }
}
