package one.leftshift.asteria.deploy.cloud.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import one.leftshift.asteria.util.Assert
import one.leftshift.asteria.util.Tuple
import org.gradle.api.Project

/**
 * Wraps a {@link Config} to provide default values if necessary and further convenience methods.
 *
 */
class EnrichedConfig implements Config {
    @Delegate
    private final Config delegate

    static final String DEPLOYMENT_CONFIG_PROPERTY = "deployment.mode"
    static final String DEFAULT_CONFIG_NAME = "deployment.config"

    private static enum DeploymentMode {
        SNAPSHOT("snapshot"),
        RELEASE("release")

        final String stringValue

        DeploymentMode(String stringValue) {
            this.stringValue = stringValue
        }
    }

    EnrichedConfig(Config delegate) {
        Assert.notNull(delegate, "Delegate can not be null")
        this.delegate = delegate
    }

    /**
     * Provides either the value from the underlying {@link Config} or a specified defaultValue
     * @param path config path
     * @param defaultValue
     * @return config value or defaultValue
     */
    String getStringOrDefault(String path, String defaultValue) {
        if (this.delegate.hasPath(path)) {
            return delegate.getString(path)
        }
        return defaultValue
    }

    /**
     * Turns a plain {@link Config} to an {@link EnrichedConfig}.
     * This method may be used in synergy with {@link Config#getObject} to obtain a specific config sub node and
     * transform it into an {@link EnrichedConfig}
     *
     * @param config
     * @return an enriched config
     */
    static EnrichedConfig toEnriched(Config config) {
        return new EnrichedConfig(config)
    }

    /**
     * Loads an {@link EnrichedConfig} instance for each set deployment.mode specified by the -Pdeployment.mode project
     * property.
     *
     * @param project
     * @return a list of Tuples in the form of (deployment.mode, EnrichedConfig instance) e.g: (snapshot, EnrichedConfig)
     */
    static List<Tuple<String, EnrichedConfig>> loadConfigs(Project project) {
        String deploymentMode = project.properties.get(DEPLOYMENT_CONFIG_PROPERTY) as String

        if (Objects.isNull(deploymentMode)) {
            throw new MissingDeploymentModeException("Property 'deployment.mode' not supplied. Consider supplying '-Pdeployment.mode=YOUR_MODE_FROM_CFG' and check deployment.conf")
        }

        Config fullConfig = loadConfig(project)

        return deploymentMode.split(",")?.collect {
            Tuple.of(it.trim(), toEnriched(fullConfig.getObject(it).toConfig()))
        }
    }

    private static Config loadConfig(Project project) {
        File config = new File("${project.projectDir}/$DEFAULT_CONFIG_NAME")

        if (!config.exists() || !config.isFile()) {
            throw new IllegalArgumentException("Configuration file deployment.config does not exist in ${project.projectDir}")
        }

        return ConfigFactory.parseFile(config)
    }
}
