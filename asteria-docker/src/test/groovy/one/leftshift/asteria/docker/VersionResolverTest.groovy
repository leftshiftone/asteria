package one.leftshift.asteria.tasks


import one.leftshift.asteria.AsteriaDockerExtension
import one.leftshift.asteria.docker.VersionResolver
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class VersionResolverTest extends Specification {
    @Subject
    VersionResolver classUnderTest

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

    void "resolver combines uses version"() {
        given:
        extension.versionPrefix = null
        classUnderTest = new VersionResolver(extension, null)
        expect:
        classUnderTest.resolve() == "1.2.3-SNAPSHOT"
    }

    void "resolver combines prefix and version"() {
        given:
        classUnderTest = new VersionResolver(extension, null)
        expect:
        classUnderTest.resolve() == "foo-1.2.3-SNAPSHOT"
    }

    void "resolver uses explicit version"() {
        given:
        extension.versionPrefix = null
        classUnderTest = new VersionResolver(extension, "GAIA-123")
        expect:
        classUnderTest.resolve() == "GAIA-123"
    }


    void "resolver uses explicit version and ignores prefix"() {
        given:
        classUnderTest = new VersionResolver(extension, "GAIA-123")
        expect:
        classUnderTest.resolve() == "GAIA-123"
    }

    void "resolver splits explicit version at dash and uses last"() {
        given:
        classUnderTest = new VersionResolver(extension, "feature/GAIA-1234")
        expect:
        classUnderTest.resolve() == "GAIA-1234"
    }
}
