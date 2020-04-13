package one.leftshift.asteria.report.tasks

import spock.lang.Specification

class TestReportTaskTest extends Specification {

    def "reading junit test results file returns necessary data"() {
        given:
            File resultFile = new File(this.class.classLoader.getResource("TEST-atlas.nlp.similarity.StringSimilarityEnsembleTest.xml").file)

        when:
            def result = TestReportTask.readJunitResults(resultFile).first()

        then:
            result.name == "atlas.nlp.similiarity.StringSimiliarityEnsembleTest"
            result.timestamp.startsWith("2018-06-08T14:09:28")
            result.duration == "PT0.202S"
            result.total == 11
            result.passed == 9
            result.skipped == 1
            result.failed == 1

            result.tests.size() == 11
            result.tests.find { it.name == "albar ~ aller = false" }.passed
            !result.tests.find { it.name == "dem ~ den = false" }.passed
            result.tests.find { it.name == "test ~ test = true" }.passed
            result.tests.find { it.name == "such ~ sucht = true" }.passed
            result.tests.find { it.name == "such ~ suchen = false" }.passed
            result.tests.find { it.name == "grüssen ~ grüße = true" }.passed
            !result.tests.find { it.name == "grazer ~ größer = false" }.passed
            result.tests.find { it.name == "grazer ~ größer = false" }.skipped
            result.tests.find { it.name == "mir ~ mia = false" }.passed
            result.tests.find { it.name == "gibst ~ gibts = true" }.passed
            result.tests.find { it.name == "etaxi ~ taxi = false" }.passed
            result.tests.find { it.name == "ticket ~ tickets = true" }.passed

            result.skippedTests.size() == 1
            result.skippedTests.find {
                it == "atlas.nlp.similiarity.StringSimiliarityEnsembleTest.grazer ~ größer = false"
            }
            result.failedTests.size() == 1
            result.failedTests.find {
                it == "atlas.nlp.similiarity.StringSimiliarityEnsembleTest.dem ~ den = false"
            }
    }

    def "reading junit test suite results file returns necessary data"() {
        given:
            File resultFile = new File(this.class.classLoader.getResource("TEST-Frontend.xml").file)

        when:
            def result = TestReportTask.readJunitResults(resultFile)

        then:
            result.size() == 6
            result.find { it.name == "utils.test.js" }
            result.find { it.name == "ChatRenderer.test.js" }
            result.find { it.name == "ImmutableQueue.test.js" }
            result.find { it.name == "ImmutableAudioQueue.test.js" }
            result.find { it.name == "Base64ToArrayBufferConverter.test.js" }
            result.find { it.name == "MessageType.test.js" }
    }

    def "reading junit pytest test suite results file returns necessary data"() {
        given:
            File resultFile = new File(this.class.classLoader.getResource("TEST-pytest.xml").file)

        when:
            def result = TestReportTask.readJunitResults(resultFile)

        then:
            result.size() == 1
            result.find { it.name == "pytest" }
    }
}
