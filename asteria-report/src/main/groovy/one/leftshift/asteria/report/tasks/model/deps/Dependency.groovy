package one.leftshift.asteria.report.tasks.model.deps

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true, includePackage = false, ignoreNulls = true)
class Dependency {

    String group
    String name
    String currentVersion
    String latestVersion
    Boolean isUpdateAvailable
}
