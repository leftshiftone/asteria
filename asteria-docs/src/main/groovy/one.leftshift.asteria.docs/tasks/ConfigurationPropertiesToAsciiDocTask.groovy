package one.leftshift.asteria.docs.tasks

import groovy.json.JsonSlurper
import io.github.swagger2markup.markup.builder.MarkupDocBuilder
import io.github.swagger2markup.markup.builder.MarkupDocBuilders
import io.github.swagger2markup.markup.builder.MarkupLanguage
import one.leftshift.asteria.docs.AsteriaDocsPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets

class ConfigurationPropertiesToAsciiDocTask extends DefaultTask {

    final String METADATA_PATH_IN_JAR = "META-INF/spring-configuration-metadata.json"

    @OutputFile
    File asciidocFile

    ConfigurationPropertiesToAsciiDocTask() {
        group = AsteriaDocsPlugin.GROUP
        description = "Convert configuration properties to Asciidoc documentation."
        asciidocFile = project.file("${project.buildDir}/asciidoc/generated/configuration.adoc")
    }

    @TaskAction
    def convertToAsciiDoc() {
        logger.info("Converting configuration properties to AsciiDoc")

        MarkupDocBuilder asciidoc = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC)

        def jars = project.configurations.generateConfigDocsFor.collect()
        List<ConfigProperty> properties = []

        jars.each { jar ->
            properties.addAll(collectPropertiesFromJar(jar))
        }

        properties = properties.sort { it.name }

        generateAsciiDocFromProperties(properties, asciidoc)

        asciidoc.writeToFileWithoutExtension(asciidocFile.toPath(), StandardCharsets.UTF_8)
        logger.info("Configuration properties in Asciidoc format written to ${asciidocFile.absolutePath}")
    }

    List<ConfigProperty> collectPropertiesFromJar(File jar) {
        logger.debug("Searching for ${METADATA_PATH_IN_JAR} in JAR ${jar.name}")
        def jarZip = new java.util.zip.ZipFile(jar)
        def metadataEntry = jarZip.getEntry(METADATA_PATH_IN_JAR)

        if (metadataEntry) {
            logger.debug("Found ${METADATA_PATH_IN_JAR} in JAR ${jar.name}")
            def metadataInputStream = jarZip.getInputStream(metadataEntry)
            def metadataJson = new JsonSlurper().parse(metadataInputStream)
            return collectPropertiesFromJson(metadataJson)
        } else {
            logger.debug("Unable to find ${METADATA_PATH_IN_JAR} in JAR ${jar.name}")
            return []
        }
    }

    private List<ConfigProperty> collectPropertiesFromJson(metadataJson) {
        def properties = []

        def propertiesJson = metadataJson['properties']
        propertiesJson.each { property ->
            def prop = new ConfigProperty()
            prop.with {
                name = property['name']
                text = property['description'] ?: "TODO: missing description"
                type = property['type'] ?: "-"
                defaultValue = property['defaultValue']?.toString() ?: "-"
                isDeprecated = property['deprecated']?.asBoolean() ?: false
                deprecationReason = property['deprecation']?.getAt('reason')?.toString() ?: ""
                deprecationReplacement = property['deprecation']?.getAt('replacement')?.toString() ?: ""
            }
            properties.add(prop)
        }

        return properties
    }

    private void generateAsciiDocFromProperties(List<ConfigProperty> properties, MarkupDocBuilder asciidoc) {
        properties.each { prop ->
            asciidoc.literalText(prop.name).newLine(true)
            // for deprecations use
            // @DeprecatedConfigurationProperty(reason = "renamed", replacement = "gaia.example.new")
            // on the getter of the property
            if (prop.isDeprecated) {
                asciidoc.newLine()
                asciidoc.text("IMPORTANT: Deprecated: ").text(prop.deprecationReason)
                if (!prop.deprecationReplacement.isEmpty()) {
                    asciidoc.text(" (use ").literalText(prop.deprecationReplacement).text(")")
                }
                asciidoc.newLine()
                asciidoc.newLine()
            }
            asciidoc.text(prop.text).newLine(true)
            asciidoc.boldText("Type: ").literalText(prop.type).newLine(true)
            asciidoc.boldText("Default: ").literalTextLine(prop.defaultValue)
            asciidoc.newLine()
            asciidoc.newLine()
        }
    }

    class ConfigProperty {
        String name
        String text
        String type
        String defaultValue
        Boolean isDeprecated
        String deprecationReason
        String deprecationReplacement

        ConfigProperty() {}
    }
}

