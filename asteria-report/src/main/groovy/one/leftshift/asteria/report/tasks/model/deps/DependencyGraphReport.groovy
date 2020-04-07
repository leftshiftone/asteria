package one.leftshift.asteria.report.tasks.model.deps

import groovy.transform.ToString
import one.leftshift.asteria.report.tasks.model.test.MetaInformation

@ToString(includeNames = true, includeFields = true, includePackage = false, ignoreNulls = true, excludes = ["dependencies"])
class DependencyGraphReport {

    MetaInformation metaInfo
    String timestamp
    Set<DependencyGraphNode> dependencies = new HashSet<>()
}
