package one.leftshift.asteria.code.analytics

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class AsteriaCodeAnalyticsPluginTest extends Specification {

    def "plugin can be instantiated"() {
        expect:
            new AsteriaCodeAnalyticsPlugin() != null
    }

    @Unroll
    def "task #task is available"() {
        given:
            Project project = ProjectBuilder.builder().build()

        when:
            project.pluginManager.apply "java"
            project.pluginManager.apply "one.leftshift.asteria-code-analytics"

        then:
            project.tasks."${task}" instanceof Task

        where:
            task                             | _
            "jacocoTestCoverageVerification" | _
            "jacocoTestReport"               | _
            "codeCoverage"                   | _
            "dependencyCheckAnalyze"         | _
    }
}
