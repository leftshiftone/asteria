package one.leftshift.asteria.report.tasks.model.test

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true, includePackage = false, ignoreNulls = true)
class MetaInformation {
    String buildNumber
    String repositoryName
    String repositoryBranch
}
