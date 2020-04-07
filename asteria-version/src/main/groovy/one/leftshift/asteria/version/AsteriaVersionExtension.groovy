package one.leftshift.asteria.version

class AsteriaVersionExtension {

    /**
     * The regex to check whether the current branch is a release branch or not. This is used in case of patch scope releases.
     */
    String releaseBranchRegex = /(release(-|\/))?\d+(\.\d+)?\.x/
}
