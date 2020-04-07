package one.leftshift.asteria.deploy.cloud

import com.typesafe.config.Config
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.transform.builder.Builder
import org.gradle.api.Project

@Builder
@TypeChecked
@ToString
class CloudDeployerRequest {
    SupportedCloudProvider cloudProvider
    Project project
    Config config
}
