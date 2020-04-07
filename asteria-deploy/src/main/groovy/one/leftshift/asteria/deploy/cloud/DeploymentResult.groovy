package one.leftshift.asteria.deploy.cloud

import groovy.transform.PackageScope

@PackageScope
class DeploymentResult {
    String status

    DeploymentResult(String status) {
        this.status = status
    }
}
