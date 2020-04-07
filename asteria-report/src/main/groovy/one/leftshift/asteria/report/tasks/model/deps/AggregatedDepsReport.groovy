package one.leftshift.asteria.report.tasks.model.deps

import groovy.transform.ToString
import one.leftshift.asteria.report.tasks.model.test.MetaInformation

@ToString(includeNames = true, includeFields = true, includePackage = false, ignoreNulls = true)
class AggregatedDepsReport {

    MetaInformation metaInfo
    String timestamp
    String url

    String gradleVersion
    String gradleLatestVersion
    Boolean gradleUpdateAvailable

    Integer total = 0
    Integer current = 0
    Integer outdated = 0

    Double outdatedPercentage = 0

    List<Dependency> currentDeps = []
    List<Dependency> outdatedDeps = []
}
