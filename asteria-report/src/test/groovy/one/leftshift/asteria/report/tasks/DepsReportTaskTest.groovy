package one.leftshift.asteria.report.tasks

import spock.lang.Specification

class DepsReportTaskTest extends Specification {

    def "reading dependency updates results file returns necessary data"() {
        given:
            File resultFile = new File(this.class.classLoader.getResource("report.json").file)

        when:
            def result = DepsReportTask.extractResults(resultFile)

        then:
            result.timestamp

            result.gradleVersion == "4.8"
            result.gradleLatestVersion == "4.8.1"
            result.gradleUpdateAvailable

            result.total == 14
            result.current == 9
            result.currentDeps.size() == 9
            result.outdated == 3
            result.outdatedDeps.size() == 3
            result.outdatedPercentage == 21
    }
}
