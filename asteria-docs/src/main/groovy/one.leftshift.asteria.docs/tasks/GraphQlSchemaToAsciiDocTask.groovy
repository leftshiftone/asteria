package one.leftshift.asteria.docs.tasks

import groovy.json.JsonSlurper
import one.leftshift.asteria.docs.AsteriaDocsExtension
import one.leftshift.asteria.docs.AsteriaDocsPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class GraphQlSchemaToAsciiDocTask extends DefaultTask {

    public static final String ANCHOR_PREFIX_TYPES = "graphql-api-types"

    String schemaFilePath

    @OutputFile
    File asciidocFile

    GraphQlSchemaToAsciiDocTask() {
        group = AsteriaDocsPlugin.GROUP
        description = "Convert GraphQL schema to Asciidoc documentation."
        asciidocFile = project.file("${project.buildDir}/asciidoc/generated/graphql-api.adoc")
    }

    @Input
    String getSchemaFilePath() {
        return this.schemaFilePath
    }

    @Option(option = "schemaFilePath", description = "The path to the GraphQL schema file.")
    void setSchemaFilePath(String schemaFilePath) {
        this.schemaFilePath = schemaFilePath
    }

    @TaskAction
    def convertToAsciiDoc() {
        if (schemaFilePath == null) {
            throw new RuntimeException("Schema file path not set")
        } else if (!new File(schemaFilePath).exists()) {
            throw new RuntimeException("Schema file does not exist")
        }
        File schemaFile = new File(schemaFilePath)

        logger.info("Converting schema file ${schemaFile.absolutePath}")
        logger.debug("JSON schema to convert: ${schemaFile.text}")
        def extension = project.rootProject.extensions.findByType(AsteriaDocsExtension)

        def schema = new JsonSlurper().parse(schemaFile) as Map
        String asciidoc = convertGraphQlSchemaToAsciidoc(schema)
        asciidocFile.text = asciidoc
        logger.info("GraphQL API in Asciidoc format written to ${asciidocFile.absolutePath}")
    }

    private static String convertGraphQlSchemaToAsciidoc(Map<String, Object> schema) {
        //todo: refactor with https://github.com/Swagger2Markup/markup-document-builder
        def asciidoc = StringBuilder.newInstance()
        def types = schema.data.__schema.types
        asciidoc << "= Query\n\n"
        convertQueriesOrMutations(asciidoc, schema.data.__schema.queryType)
        asciidoc << "= Mutation\n\n"
        convertQueriesOrMutations(asciidoc, schema.data.__schema.mutationType)
        convertTypes(asciidoc, types)
        return asciidoc.toString()
    }

    private static void convertQueriesOrMutations(StringBuilder asciidoc, Map<String, Object> input) {
        asciidoc << "== ${input.name}\n\n"
        asciidoc << "${input.description ?: ""}\n\n"
        asciidoc << "See ${ref(ANCHOR_PREFIX_TYPES, input.name)} for more information.\n\n"
    }

    private static void convertTypes(StringBuilder asciidoc, List<Map<String, Object>> input) {
        asciidoc << "= Types\n\n"
        input.findAll { !it.name.startsWith("__") }.each { type ->
            convertType(asciidoc, type)
        }
    }

    private static void convertType(StringBuilder asciidoc, Map<String, Object> input) {
        switch (input.kind) {
            case "SCALAR":
                convertTypeScalar(asciidoc, input)
                break
            case "ENUM":
                convertTypeEnum(asciidoc, input)
                break
            case "OBJECT":
                convertTypeObject(asciidoc, input)
                break
            case "INPUT_OBJECT":
                convertTypeInputObject(asciidoc, input)
                break
            default:
                throw new RuntimeException("GraphQL type ${input.kind} not implemented")
        }
    }

    private static void convertTypeScalar(StringBuilder asciidoc, Map<String, Object> input) {
        asciidoc << "== ${anchor(ANCHOR_PREFIX_TYPES, input.name)}${input.name}\n\n"
        asciidoc << "*Type:* `${input.kind}`\n\n"
        asciidoc << "${input.description ?: ""}\n\n"
    }

    private static void convertTypeEnum(StringBuilder asciidoc, Map<String, Object> input) {
        asciidoc << "== ${anchor(ANCHOR_PREFIX_TYPES, input.name)}${input.name}\n\n"
        asciidoc << "*Type:* `${input.kind}`\n\n"
        asciidoc << "${input.description ?: ""}\n\n"

        List<Object> enumValues = input.enumValues as List
        asciidoc << ".Values\n"
        asciidoc << "|===\n|Name |Description\n\n"
        enumValues.eachWithIndex { enumEntry, i ->
            boolean isDeprecated = Boolean.valueOf(enumEntry.isDeprecated)
            asciidoc << (isDeprecated ? "|`[.line-through]#${enumEntry.name}#`\n" : "|`${enumEntry.name}`\n")
            asciidoc << (isDeprecated ? "|_DEPRECATED_ ${enumEntry.description ?: ""}\n" : "|${enumEntry.description ?: ""}\n")
        }
        asciidoc << "|===\n\n"
    }

    private static void convertTypeObject(StringBuilder asciidoc, Map<String, Object> input) {
        asciidoc << "== ${anchor(ANCHOR_PREFIX_TYPES, input.name)}${input.name}\n\n"
        asciidoc << "${input.description ?: ""}\n\n"

        asciidoc << "=== Fields\n\n"
        input.fields.each { field ->
            def fieldIsDeprecated = Boolean.valueOf(field.isDeprecated)
            def fieldDeprecationReason = field.deprecationReason

            asciidoc << (fieldIsDeprecated ? "==== [.line-through]#${field.name}#\n\n" : "==== ${field.name}\n\n")
            if (fieldDeprecationReason) {
                asciidoc << ".Deprecated\n"
                asciidoc << "WARNING: ${fieldDeprecationReason}\n\n"
            }
            asciidoc << (field.type?.ofType ? "*Type:* `${field.type.kind}` of type ${ref(ANCHOR_PREFIX_TYPES, field.type.ofType.name)}\n\n" : "*Type:* ${ref(ANCHOR_PREFIX_TYPES, field.type.name)}\n\n")
            asciidoc << (fieldIsDeprecated ? "_DEPRECATED_ ${field.description ?: ""}\n\n" : "${field.description ?: ""}\n\n")

            if (field.args) {
                asciidoc << ".Arguments\n"
                asciidoc << "|===\n|Name |Default |Type |Description\n\n"
                field.args.eachWithIndex { arg, i ->
                    asciidoc << "|`${arg.name}`\n"
                    asciidoc << "|`${arg.defaultValue}`\n"
                    asciidoc << (arg.type?.ofType ? "|`${arg.type.kind}` of type ${ref(ANCHOR_PREFIX_TYPES, arg.type.ofType.name)}\n" : "|${ref(ANCHOR_PREFIX_TYPES, arg.type.name)}\n")
                    asciidoc << "|${arg.description ?: ""}\n"
                }
                asciidoc << "|===\n\n"
            }
        }
    }

    private static void convertTypeInputObject(StringBuilder asciidoc, Map<String, Object> input) {
        asciidoc << "== ${anchor(ANCHOR_PREFIX_TYPES, input.name)}${input.name}\n\n"
        asciidoc << "${input.description ?: ""}\n\n"

        asciidoc << "=== Input Fields\n\n"
        input.inputFields.each { field ->
            def fieldIsDeprecated = Boolean.valueOf(field.isDeprecated)
            def fieldDeprecationReason = field.deprecationReason

            asciidoc << (fieldIsDeprecated ? "==== [.line-through]#${field.name}#\n\n" : "==== ${field.name}\n\n")
            if (fieldDeprecationReason) {
                asciidoc << ".Deprecated\n"
                asciidoc << "WARNING: ${fieldDeprecationReason}\n\n"
            }
            asciidoc << (field.type?.ofType ? "*Type:* `${field.type.kind}` of type ${ref(ANCHOR_PREFIX_TYPES, field.type.ofType.name)}\n\n" : "*Type:* ${ref(ANCHOR_PREFIX_TYPES, field.type.name)}\n\n")
            asciidoc << (fieldIsDeprecated ? "_DEPRECATED_ ${field.description ?: ""}\n\n" : "${field.description ?: ""}\n\n")
        }
    }

    private static String anchor(String prefix, String id) {
        return "[[${prefix}-${id.hashCode().abs()}]]"
    }

    private static String ref(String prefix, String id, String label = null) {
        return "<<${prefix}-${id.hashCode().abs()},${label ?: id}>>"
    }
}
