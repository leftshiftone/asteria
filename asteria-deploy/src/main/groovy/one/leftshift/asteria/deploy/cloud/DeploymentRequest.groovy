package one.leftshift.asteria.deploy.cloud

import groovy.transform.TypeChecked
import groovy.transform.builder.Builder
import one.leftshift.asteria.deploy.cloud.config.EnrichedConfig

import java.nio.file.Path

@Builder
@TypeChecked
class DeploymentRequest {
    Path deployable
    EnrichedConfig config
}