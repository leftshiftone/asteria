package one.leftshift.asteria.deploy.test.util

import groovy.transform.builder.Builder
import org.gradle.api.Project

import java.nio.file.Path

/**
 * Encapsulates a basic gradle project structure for testing purposes.
 */
@Builder
class ProjectDescription {
    Project project
    Path srcMainJava
    Path srcMain
    Path srcMainResources
    Path buildGradle
}
