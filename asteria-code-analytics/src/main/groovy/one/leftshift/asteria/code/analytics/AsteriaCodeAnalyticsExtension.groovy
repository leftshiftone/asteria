package one.leftshift.asteria.code.analytics

class AsteriaCodeAnalyticsExtension {

    String sonarUrl = ""
    String sonarLoginToken = ""
    Map<String, Object> sonarProperties = [:]

    /**
     * Java Code Coverage tool version.
     */
    String jacocoVersion = "0.8.5"

    /**
     * Path to XML reports to be sent to Sonar.
     */
    String jacocoXmlReportsPath = "build/reports/jacoco/test/jacocoTestReport.xml"
    /**
     * Exclude paths (e.g. **&#47;dto&#47;**).
     */
    List<String> coverageExcludes = []

    /**
     * Create HTML report or not.
     */
    Boolean htmlCoverageReportEnabled = false
    /**
     * Create XML report or not.
     */
    Boolean xmlCoverageReportEnabled = false
    /**
     * Create CSV report or not.
     */
    Boolean csvCoverageReportEnabled = false
}
