package one.leftshift.asteria.code.analytics

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin
import org.sonarqube.gradle.SonarQubePlugin
import org.sonarqube.gradle.SonarQubeTask

import static org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL

class AsteriaCodeAnalyticsPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Code Analytics"
    static final String EXTENSION_NAME = "asteriaCodeAnalytics"
    static final String CODE_COVERAGE_TASK_NAME = "codeCoverage"

    @Override
    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, AsteriaCodeAnalyticsExtension)

        project.logger.debug("Applying java code coverage plugin")
        project.pluginManager.apply JacocoPlugin

        project.logger.debug("Applying SonarQube plugin")
        if (!project.rootProject.pluginManager.hasPlugin("org.sonarqube")) {
            project.rootProject.pluginManager.apply SonarQubePlugin
        }

        project.logger.debug("Applying OWASP dependency check plugin")
        if (!project.rootProject.pluginManager.hasPlugin("org.owasp.dependencycheck")) {
            project.rootProject.pluginManager.apply DependencyCheckPlugin
        }

        project.plugins.withType(JavaPlugin) {
            project.afterEvaluate {
                project.logger.debug("Configuring java code coverage plugin")
                project.configure(project) {
                    jacoco {
                        toolVersion = extension.jacocoVersion
                    }
                    jacocoTestReport {
                        mustRunAfter tasks.withType(Test)
                        afterEvaluate {
                            executionData.from fileTree(dir: "${project.buildDir}/jacoco", include: "*.exec")
                            classDirectories.from fileTree(dir: "${project.buildDir}/classes", exclude: extension.coverageExcludes)
                        }
                        reports {
                            html.enabled = extension.htmlCoverageReportEnabled
                            xml.enabled = extension.xmlCoverageReportEnabled
                            csv.enabled = extension.csvCoverageReportEnabled
                        }
                    }
                }

                project.logger.debug("Configuring dependency check plugin")
                project.rootProject.configure(project.rootProject) {
                    dependencyCheck {
                        format = ALL
                        analyzers {
                            nuspecEnabled = false
                            assemblyEnabled = false
                        }
                    }
                }
                project.logger.debug("Configuring SonarQube plugin")
                project.rootProject.configure(project.rootProject) {
                    def sonarProperties = [
                            "sonar.projectName"                   : project.rootProject.name,
                            "sonar.projectKey"                    : project.rootProject.group,
                            "sonar.host.url"                      : extension.sonarUrl,
                            "sonar.login"                         : extension.sonarLoginToken,
                            "sonar.exclusions"                    : extension.coverageExcludes.join(","),
                            "sonar.sources"                       : "src",
                            "sonar.binaries"                      : "build/classes",
                            "sonar.coverage.jacoco.xmlReportPaths": extension.jacocoXmlReportsPath,
                            "sonar.java.binaries"                 : "build/classes/java",
                            "sonar.java.coveragePlugin"           : "jacoco",
                            "sonar.groovy.binaries"               : "build/classes/groovy",
                            "sonar.groovy.coveragePlugin"         : "jacoco",
                            "sonar.kotlin.binaries"               : "build/classes/kotlin",
                            "sonar.kotlin.coveragePlugin"         : "jacoco",
                            "sonar.scala.binaries"                : "build/classes/scala",
                            "sonar.scala.coveragePlugin"          : "jacoco",
                            "sonar.dependencyCheck.reportPath"    : "${project.rootProject.buildDir}/reports/dependency-check-report.xml",
                    ]
                    extension.sonarProperties.entrySet().each { sonarProperties.put(it.key, it.value) }

                    sonarqube {
                        properties {
                            sonarProperties.entrySet().each {
                                property it.key, it.value
                            }
                        }
                    }
                }
                project.tasks.withType(JacocoReport) { task ->
                    task.dependsOn project.tasks.getByName(JavaPlugin.TEST_TASK_NAME)
                }
                project.tasks.withType(SonarQubeTask) { task ->
                    task.dependsOn project.tasks.jacocoTestReport
                    task.dependsOn project.tasks.dependencyCheckAggregate
                }
            }
        }

        project.logger.debug("Adding tasks")
        if (!project.rootProject.tasks.find { it.name == CODE_COVERAGE_TASK_NAME }) {
            def codeCoverageTask = project.rootProject.tasks.create(CODE_COVERAGE_TASK_NAME, JacocoReport)
            codeCoverageTask.group = GROUP
            codeCoverageTask.description = "Creates an aggregated code coverage report based on Jacoco in the root of a multi-project build"
            project.afterEvaluate {
                if (project.subprojects) {
                    codeCoverageTask.dependsOn project.subprojects*.tasks.jacocoTestReport
                    codeCoverageTask.configure {
                        additionalSourceDirs.from project.files(project.subprojects*.sourceSets.main.allSource.srcDirs)
                        sourceDirectories.from project.files(project.subprojects*.sourceSets.main.allSource.srcDirs)
                        classDirectories.from project.files(project.subprojects*.buildDir.collect {
                            project.fileTree(dir: "${it}/classes", exclude: extension.coverageExcludes)
                        })
                        executionData.from project.fileTree(dir: "${project.rootDir}", include: "*/build/jacoco/*.exec")
                        reports {
                            html.enabled = extension.htmlCoverageReportEnabled
                            xml.enabled = extension.xmlCoverageReportEnabled
                            csv.enabled = extension.csvCoverageReportEnabled
                        }
                    }
                } else {
                    codeCoverageTask.dependsOn project.tasks.jacocoTestReport
                }
            }
        }
    }
}