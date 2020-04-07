package one.leftshift.asteria.deploy.cloud

enum ProjectProperty {
    DRY_RUN("dryrun"),
    DEPLOYMENT_MODE("deployment.mode")

    private final String strValue

    ProjectProperty(String strValue) {
        this.strValue = strValue
    }

    String strValue() {
        return strValue
    }
}
