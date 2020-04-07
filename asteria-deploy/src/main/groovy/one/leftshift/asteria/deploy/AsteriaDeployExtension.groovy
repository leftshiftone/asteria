package one.leftshift.asteria.deploy

import org.gradle.api.Project

class AsteriaDeployExtension {

    File deploymentDescription
    private final Project project

    AsteriaDeployExtension(Project project) {
        this.project = project
    }
}
