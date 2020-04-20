package one.leftshift.asteria.tasks

import com.spotify.docker.client.DockerClient
import one.leftshift.asteria.AsteriaDockerExtension
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Path

class DockerBuildTaskUnitTest extends Specification {

    @Subject
    DockerBuildTask.DockerBuildTaskExecution classUnderTest


    void "invokes docker build with expected values"() {
        given:
            DockerClient mockedClient = Mock(DockerClient)
            Project mockedProject = Stub(Project)
            mockedProject.buildDir >> new File("/tmp/mockedProject/build")
            mockedProject.file(_) >> new File("")
            mockedProject.version >> "1.2.3-SNAPSHOT"
            AsteriaDockerExtension extension = new AsteriaDockerExtension(mockedProject)
            extension.repositoryURI = "someRepository"
            extension.name = "some-app"
            extension.versionPrefix = "foo-"
            classUnderTest = new DockerBuildTask.DockerBuildTaskExecution(
                    mockedClient, extension
            )
            Path arg1
            DockerClient.BuildParam arg2
        when:
            classUnderTest.execute()
        then:
            1 * mockedClient.build(*_) >> { args ->
                arg1 = args[0]
                arg2 = args[2][0]
            }
            arg1.toString() == "/tmp/mockedProject/build/docker"
            arg2.name() == "t"
            arg2.value() == "someRepository/some-app:foo-1.2.3-SNAPSHOT"
    }


    void "invokes docker build with build arguments"() {
        given:
        DockerClient mockedClient = Mock(DockerClient)
        Project mockedProject = Stub(Project)
        mockedProject.buildDir >> new File("/tmp/mockedProject/build")
        mockedProject.file(_) >> new File("")
        mockedProject.version >> "1.2.3-SNAPSHOT"
        AsteriaDockerExtension extension = new AsteriaDockerExtension(mockedProject)
        extension.repositoryURI = "someRepository"
        extension.name = "some-app"
        extension.buildParameters = ["{\"argumentName1\":\"argumentValue1\",\"argumentName2\":\"argumentValue2\"}"]
        extension.versionPrefix = "foo-"
        classUnderTest = new DockerBuildTask.DockerBuildTaskExecution(
                mockedClient, extension
        )
        Path arg1
        DockerClient.BuildParam arg2, arg3, arg4
        when:
        classUnderTest.execute()
        then:
        1 * mockedClient.build(*_) >> { args ->
            arg1 = args[0]
            arg2 = args[2][0]
            arg3 = args[2][1]
        }
        arg1.toString() == "/tmp/mockedProject/build/docker"
        arg2.name() == "t"
        arg2.value() == "someRepository/some-app:foo-1.2.3-SNAPSHOT"
        arg3.name() == "buildargs"
        arg3.value() == URLEncoder.encode("{\"argumentName1\":\"argumentValue1\",\"argumentName2\":\"argumentValue2\"}","UTF-8")

    }
}
