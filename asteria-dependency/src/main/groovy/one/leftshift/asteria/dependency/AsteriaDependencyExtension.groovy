package one.leftshift.asteria.dependency

class AsteriaDependencyExtension {

    /**
     * Commit message for lock file commit.
     */
    String commitMessage = "[INFRA] (deps) dependency lock file"

    /**
     * Enable or disable dependency management.
     */
    Boolean dependencyManagementEnabled = true

    /**
     * Dependency management BOM which will be used for version resolution.
     */
    String dependencyManagementBom = "org.springframework.boot:spring-boot-dependencies:latest.release"

}
