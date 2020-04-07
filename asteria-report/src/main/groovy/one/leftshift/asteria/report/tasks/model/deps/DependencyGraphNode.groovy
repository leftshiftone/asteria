package one.leftshift.asteria.report.tasks.model.deps

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(excludes = ["dependencies"])
@ToString(includeNames = true, includeFields = true, includePackage = false, ignoreNulls = true, excludes = ["dependencies"])
class DependencyGraphNode {

    boolean hasParent = false
    String group
    String name
    String version
    Set<DependencyGraphNode> dependencies = new HashSet<>()

    void addDependency(DependencyGraphNode dependency) {
        dependencies.add(dependency)
        dependency.hasParent = true
    }
}
