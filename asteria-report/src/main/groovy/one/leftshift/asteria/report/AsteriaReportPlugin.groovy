package one.leftshift.asteria.report

import one.leftshift.asteria.report.tasks.*
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.testing.TestReport

class AsteriaReportPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Test Reporter"
    static final String EXTENSION_NAME = "asteriaReport"
    static final String ROOT_TEST_REPORT_TASK_NAME = "rootTestReport"
    static final String ZIP_TEST_REPORT_TASK_NAME = "zipTestReport"
    static final String DEPS_UPLOAD_TO_BITBUCKET_DOWNLOADS_TASK_NAME = "depsUploadToBitbucketDownloads"
    static final String TEST_UPLOAD_TO_BITBUCKET_DOWNLOADS_TASK_NAME = "testUploadToBitbucketDownloads"
    static final String DEPS_REPORT_TASK_NAME = "depsReport"
    static final String DEPS_GRAPH_TASK_NAME = "depsGraph"
    static final String DEPS_GRAPH_REPORT_TASK_NAME = "depsGraphReport"
    static final String TEST_REPORT_TASK_NAME = "testReport"
    static final String VERSION_REPORT_TASK_NAME = "versionReport"

    @Override
    void apply(Project project) {
        def extension = project.rootProject.extensions.create(EXTENSION_NAME, AsteriaReportExtension)
        if (!extension.git) {
            extension.git = Grgit.open(dir: project.rootProject.projectDir.absolutePath)
        }

        project.logger.debug("Adding tasks")
        def depsReportTask
        if (!project.rootProject.tasks.find { it.name == DEPS_REPORT_TASK_NAME }) {
            depsReportTask = project.rootProject.task(DEPS_REPORT_TASK_NAME, type: DepsReportTask)
        }
        def depsGraphTask
        if (!project.rootProject.tasks.find { it.name == DEPS_GRAPH_TASK_NAME }) {
            depsGraphTask = project.rootProject.task(DEPS_GRAPH_TASK_NAME, type: DepsGraphTask)
        }
        def depsGraphReportTask
        if (!project.rootProject.tasks.find { it.name == DEPS_GRAPH_REPORT_TASK_NAME }) {
            depsGraphReportTask = project.rootProject.task(DEPS_GRAPH_REPORT_TASK_NAME, type: DepsGraphReportTask)
            depsGraphReportTask.dependsOn depsGraphTask
        }
        def depsUploadToBitbucketDownloadsTask
        if (!project.rootProject.tasks.find { it.name == DEPS_UPLOAD_TO_BITBUCKET_DOWNLOADS_TASK_NAME }) {
            depsUploadToBitbucketDownloadsTask = project.rootProject.task(DEPS_UPLOAD_TO_BITBUCKET_DOWNLOADS_TASK_NAME, type: DepsUploadToBitbucketDownloadsTask)
        }
        def testReportTask
        if (!project.rootProject.tasks.find { it.name == TEST_REPORT_TASK_NAME }) {
            testReportTask = project.rootProject.task(TEST_REPORT_TASK_NAME, type: TestReportTask)
        }
        def versionReportTask
        if (!project.rootProject.tasks.find { it.name == VERSION_REPORT_TASK_NAME }) {
            versionReportTask = project.rootProject.task(VERSION_REPORT_TASK_NAME, type: VersionReportTask)
        }
        def rootTestReportTask
        if (!project.rootProject.tasks.find { it.name == ROOT_TEST_REPORT_TASK_NAME }) {
            rootTestReportTask = project.rootProject.task(ROOT_TEST_REPORT_TASK_NAME, type: TestReport)
        }
        def zipTestReportTask
        if (!project.rootProject.tasks.find { it.name == ZIP_TEST_REPORT_TASK_NAME }) {
            zipTestReportTask = project.rootProject.task(ZIP_TEST_REPORT_TASK_NAME, type: Zip)
        }
        def testUploadToBitbucketDownloadsTask
        if (!project.rootProject.tasks.find { it.name == TEST_UPLOAD_TO_BITBUCKET_DOWNLOADS_TASK_NAME }) {
            testUploadToBitbucketDownloadsTask = project.rootProject.task(TEST_UPLOAD_TO_BITBUCKET_DOWNLOADS_TASK_NAME, type: TestUploadToBitbucketDownloadsTask)
        }

        project.logger.debug("Configuring tasks")
        project.afterEvaluate {
            depsReportTask.depsResultFile = extension.depsJsonResult
            depsGraphReportTask.depsGraphResultFile = extension.depsGraphResultFile
            testReportTask.testResultFiles = extension.junitXmlResults

            if (project.plugins.hasPlugin("com.github.ben-manes.versions")) {
                depsUploadToBitbucketDownloadsTask.mustRunAfter project.tasks.dependencyUpdates
            }
            depsUploadToBitbucketDownloadsTask.depsReport = extension.depsJsonResult

            project.logger.debug("Got the following binary results:")
            extension.junitBinaryResults.each { project.logger.debug("\t${it.path}") }
            rootTestReportTask.destinationDir = project.file("${project.rootProject.buildDir}/reports/tests/all")
            if (extension.junitBinaryResults) {
                rootTestReportTask.reportOn extension.junitBinaryResults
            }

            zipTestReportTask.mustRunAfter project.rootProject.tasks."${ROOT_TEST_REPORT_TASK_NAME}"
            zipTestReportTask.from project.fileTree("${project.rootProject.buildDir}/reports")
            zipTestReportTask.archiveName "${extension.repositoryName}-test-reports-${extension.escapedBranchName}-${extension.buildNumber}.zip"
            zipTestReportTask.destinationDir project.file("${project.rootProject.buildDir}/reports")

            testUploadToBitbucketDownloadsTask.mustRunAfter project.rootProject.tasks."${ZIP_TEST_REPORT_TASK_NAME}"
            testUploadToBitbucketDownloadsTask.zippedTestReport = project.file("${project.rootProject.buildDir}/reports/${extension.repositoryName}-test-reports-${extension.escapedBranchName}-${extension.buildNumber}.zip")
        }
    }
}
