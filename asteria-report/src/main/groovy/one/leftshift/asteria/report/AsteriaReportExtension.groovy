package one.leftshift.asteria.report

import org.ajoberstar.grgit.Grgit
import org.gradle.api.file.FileCollection

class AsteriaReportExtension {

    Grgit git

    /**
     * Base URL to send the reports to.
     */
    String reportingUrl = ""
    /**
     * Base URL of Bitbucket API
     */
    String bitbucketBaseUrl = "https://api.bitbucket.org"
    /**
     * File holding dependency updates results in JSON format.
     */
    File depsJsonResult
    /**
     * File holding dependency graph results in JSON format.
     */
    File depsGraphResultFile
    /**
     * Files holding Junit test results in XML format.
     */
    FileCollection junitXmlResults
    /**
     * Files holding Junit test results in binary format.
     */
    FileCollection junitBinaryResults
    /**
     * Name of the environment variable holding the Bitbucket authentication string (e.g. user:password).
     */
    String envVarAuthString = "BITBUCKET_AUTH_STRING"
    /**
     * Name of the environment variable holding the name of the repository.
     */
    String envVarBuildNumber = "BITBUCKET_BUILD_NUMBER"
    /**
     * Name of the environment variable holding the name of the repository owner.
     */
    String envVarRepositoryOwner = "BITBUCKET_REPO_OWNER"
    /**
     * Name of the environment variable holding the name of the repository.
     */
    String envVarRepository = "BITBUCKET_REPO_SLUG"
    /**
     * Name of the environment variable holding the name of the branch.
     */
    String envVarBranch = "BITBUCKET_BRANCH"

    String getBitbucketAuthString() {
        return System.getenv(envVarAuthString)
    }

    String getBuildNumber() {
        return System.getenv(envVarBuildNumber) ?: "unknown"
    }

    String getRepositoryOwner() {
        return System.getenv(envVarRepositoryOwner) ?: git.remote.list()?.find {
            it?.name == "origin"
        }?.url?.replace(".git", "")?.split(":")?.last()?.split("/")?.first()
    }

    String getRepositoryName() {
        return System.getenv(envVarRepository) ?: git.remote.list()?.find {
            it?.name == "origin"
        }?.url?.replace(".git", "")?.split(":")?.last()?.split("/")?.last()
    }

    String getBranchName() {
        return System.getenv(envVarBranch) ?: git.branch.current.name
    }

    String getEscapedBranchName() {
        return getBranchName()?.replace("/", "-")
    }
}
