package one.leftshift.asteria.docker.registry

/**
 * Contains all supported Registries
 */
enum Registry {
    AMAZON_ECR("ECR"),
    DEFAULT("Default")
    
    final String strValue
    
    Registry(String strValue) {
        this.strValue = strValue
    }
}