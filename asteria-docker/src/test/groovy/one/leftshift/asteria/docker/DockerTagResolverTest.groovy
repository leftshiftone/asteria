package one.leftshift.asteria.tasks


import one.leftshift.asteria.AsteriaDockerExtension
import one.leftshift.asteria.docker.DockerTagResolver
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class DockerTagResolverTest extends Specification {
    @Subject
    DockerTagResolver classUnderTest

    @Shared
    Project mockedProject

    @Shared
    AsteriaDockerExtension extension

    void setup() {
        mockedProject = Stub(Project)
        mockedProject.buildDir >> new File("/tmp/mockedProject/build")
        mockedProject.file(_) >> new File("")
        mockedProject.version >> "1.2.3-SNAPSHOT"
        extension = new AsteriaDockerExtension(mockedProject)
        extension.repositoryURI = "someRepository"
        extension.name = "some-app"
        extension.versionPrefix = "foo-"
    }

    void "resolver uses project version"() {
        given:
        extension.versionPrefix = null
        classUnderTest = new DockerTagResolver(extension, null)
        expect:
        classUnderTest.resolve() == "1.2.3-SNAPSHOT"
    }

    void "resolver combines prefix and version"() {
        given:
        classUnderTest = new DockerTagResolver(extension, null)
        expect:
        classUnderTest.resolve() == "foo-1.2.3-SNAPSHOT"
    }

    @Unroll
    void "resolver resolves #explicitTag to #resolvedTag"() {
        given:
        extension.versionPrefix = null
        classUnderTest = new DockerTagResolver(extension, explicitTag)
        expect:
        classUnderTest.resolve() == resolvedTag

        where:
        explicitTag          || resolvedTag
        "GAIA-123"           || "GAIA-123"
        "feature/GAIA-123"   || "GAIA-123"
        "feature/GAIA-123v2" || "GAIA-123"
        "feature/GAIA/123v2" || "123v2"
        "feature/.-GAIA-123" || "GAIA-123"
        "bug/GAIA-123"       || "GAIA-123"
        "release/1.0.x"      || "1.0.x"
    }

    @Unroll
    void "resolver does not resolve explicitTag #explicitTag if it matches a release tag"() {
        given:
        extension.versionPrefix = null
        classUnderTest = new DockerTagResolver(extension, explicitTag)
        when:
        classUnderTest.resolve()
        then:
        def error = thrown(RuntimeException)
        error.message == "The supplied explicitTag $resolvedTag must not match a release tag."

        where:
        explicitTag            || resolvedTag
        "3.0.1"                || "3.0.1"
        "asdf/3.0.0-rc2"       || "3.0.0-rc2"
        "bugfix/0.1.0"         || "0.1.0"
        "release/10.11.123123" || "10.11.123123"
    }


    void "resolver uses explicit version and ignores prefix"() {
        given:
        classUnderTest = new DockerTagResolver(extension, "GAIA-123")
        expect:
        classUnderTest.resolve() == "GAIA-123"
    }
}
