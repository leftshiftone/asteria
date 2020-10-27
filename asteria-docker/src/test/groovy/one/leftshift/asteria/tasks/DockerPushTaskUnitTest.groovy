package one.leftshift.asteria.tasks

import com.spotify.docker.client.DockerClient
import one.leftshift.asteria.AsteriaDockerExtension
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class DockerPushTaskUnitTest extends Specification {

    @Subject
    DockerPushTask.DockerPushTaskExecution classUnderTest

    @Shared
    DockerClient mockedClient

    @Shared
    Project mockedProject

    @Shared
    AsteriaDockerExtension extension

    @Shared
    String tag


    void setup() {
        mockedClient = Mock(DockerClient)
        mockedProject = Stub(Project)
        mockedProject.buildDir >> new File("/tmp/mockedProject/build")
        mockedProject.file(_) >> new File("")
        mockedProject.version >> "1.2.3-SNAPSHOT"
        extension = new AsteriaDockerExtension(mockedProject)
        extension.repositoryURI = "someRepository"
        extension.name = "some-app"
        extension.versionPrefix = "foo-"
        tag = "foo-1.2.3-SNAPSHOT"
    }

    void "invokes docker build with expected values"() {
        given:
            classUnderTest = new DockerPushTask.DockerPushTaskExecution(
                    mockedClient, extension, tag)
        when:
            classUnderTest.execute()
        then:
            1 * mockedClient.push({ it == "someRepository/some-app:foo-1.2.3-SNAPSHOT"} as String, _)

    }

    void "if withLatestTag is enabled the image is pushed again with latest tag"() {
        given:
            classUnderTest = new DockerPushTask.DockerPushTaskExecution(
                    mockedClient, extension, tag)
            extension.withLatestTag = true
        when:
            classUnderTest.execute()
        then:
            1 * mockedClient.push({ it == "someRepository/some-app:foo-1.2.3-SNAPSHOT"} as String, _)
            1 * mockedClient.push({ it == "someRepository/some-app:latest"} as String, _)
    }
}
