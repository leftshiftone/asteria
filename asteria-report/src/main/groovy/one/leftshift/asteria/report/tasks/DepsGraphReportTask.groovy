package one.leftshift.asteria.report.tasks

import groovy.json.JsonBuilder
import one.leftshift.asteria.report.AsteriaReportPlugin
import one.leftshift.asteria.report.tasks.model.deps.DependencyGraphNode
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import static one.leftshift.asteria.report.AsteriaReportPlugin.DEPS_GRAPH_REPORT_TASK_NAME

class DepsGraphReportTask extends DefaultTask {

    private String startsWithGroup = "one.leftshift"
    private String print = "true"

    @OutputFile
    File depsGraphFile

    DepsGraphReportTask() {
        group = AsteriaReportPlugin.GROUP
        description = "Get dependency graph for dependencies starting with certain group."
        def fileDir = new File("${project.buildDir}/report/${DEPS_GRAPH_REPORT_TASK_NAME}")
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        depsGraphFile = new File(fileDir, "report.json")
    }

    @Input
    String getStartsWithGroup() {
        return startsWithGroup
    }

    @Option(option = "startsWithGroup", description = "The group prefix for filtering the dependencies. Default: 'one.leftshift'")
    void setStartsWithGroup(String startsWithGroup) {
        this.startsWithGroup = startsWithGroup
    }

    @Input
    String getPrint() {
        return print
    }

    @Option(option = "print", description = "Whether or not to print the graph. Default: true")
    void setPrint(String print) {
        this.print = print
    }

    @TaskAction
    def buildDependencyGraph() {
        def allDependencies = new HashSet<DependencyGraphNode>()
        project.subprojects { subproject ->
            subproject.configurations.compile.incoming.getResolutionResult().getAllDependencies().each { DependencyResult dependency ->
                project.logger.debug("Evaluating ${dependency.requested.toString()}")
                if (startsWithGroup && !dependency.requested.toString().startsWith(startsWithGroup)) {
                    return false
                }

                def parent = allDependencies.find {
                    it.group == dependency.from.moduleVersion.group && it.name == dependency.from.moduleVersion.name
                }
                def dep = new DependencyGraphNode()
                if (dependency.requested instanceof ModuleComponentSelector) {
                    dep.group = dependency.requested.group
                    dep.name = dependency.requested.module
                    dep.version = dependency.requested.version
                } else {
                    dep.name = dependency.requested.toString()
                }
                if (parent) {
                    parent.addDependency(dep)
                }
                allDependencies.add(dep)
            }
        }
        def dependencyGraph = allDependencies.findAll { (!it.hasParent) }
        dependencyGraph.sort(true) { it.group }

        if (Boolean.valueOf(print)) {
            printDependencies(dependencyGraph)
        }
        def jsonBuilder = new JsonBuilder(dependencyGraph)
        depsGraphFile.text = jsonBuilder.toPrettyString()
    }

    static void printDependencies(Set<DependencyGraphNode> dependencyGraph, int depth = 0) {
        if (dependencyGraph.isEmpty()) return
        dependencyGraph.each {
            def string = ""
            if (depth > 0) {
                (0..depth).each { string += "  " }
            }
            println "${string}${it.group}:${it.name}"
            printDependencies(it.dependencies, ++depth)
        }
    }
}
