package one.leftshift.asteria.report.tasks

import spock.lang.Specification

class DepsGraphReportTaskTest extends Specification {

    def "reading dependency graph results file returns necessary data"() {
        given:
            File resultFile = new File(this.class.classLoader.getResource("depsGraphReport.json").file)

        when:
            def result = DepsGraphReportTask.extractResults(resultFile)

        then:
            result.size() == 1
            result.first().dependencies.size() == 4
    }
}
