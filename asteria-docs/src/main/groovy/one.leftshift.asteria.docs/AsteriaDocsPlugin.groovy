package one.leftshift.asteria.docs

import one.leftshift.asteria.docs.tasks.GraphQlSchemaToAsciiDocTask
import org.asciidoctor.gradle.AsciidoctorPlugin
import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar

import java.nio.file.Files

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

class AsteriaDocsPlugin implements Plugin<Project> {
    static final String GROUP = "Asteria Docs"
    static final String EXTENSION_NAME = "asteriaDocs"
    static final String DOCS_XML_TASK_NAME = "docsXml"
    static final String DOCS_HTML_TASK_NAME = "docsHtml"
    static final String DOCS_PDF_TASK_NAME = "docsPdf"
    static final String DOCS_TASK_NAME = "docs"
    static final String DOCS_JAR_TASK_NAME = "docsJar"
    static final String DOCS_ZIP_TASK_NAME = "docsZip"
    static final String DOCS_GRAPHQL_TO_ASCIIDOC_TASK_NAME = "graphqlToAsciidoc"

    File asciidocBuildDir

    @Override
    void apply(Project project) {
        asciidocBuildDir = project.file("${project.buildDir}/asciidoc")
        if (!asciidocBuildDir.exists()) {
            asciidocBuildDir.mkdirs()
        }
        def extension = project.extensions.create(EXTENSION_NAME, AsteriaDocsExtension)

        project.logger.debug("Applying asciidoc plugin")
        project.pluginManager.apply AsciidoctorPlugin

        project.logger.debug("Configuring asciidoc plugin")
        project.afterEvaluate {
            extension.asciidocAttributes.put("version", project.version as String)
            extension.asciidocAttributes.put("revnumber", project.version as String)
        }

        project.logger.debug("Adding docs xml task")
        def docsXmlTask = project.tasks.create(DOCS_XML_TASK_NAME, AsciidoctorTask)
        docsXmlTask.group = GROUP
        docsXmlTask.description = "Renders XML documentation (https://docbook.org)"
        docsXmlTask.outputs.upToDateWhen { false }
        project.afterEvaluate {
            docsXmlTask.doFirst {
                def attributes = extension.asciidocAttributes + [imagesdir: "./images"]
                project.logger.info("Using attributes ${attributes}")
            }
            docsXmlTask.configure {
                if (extension.documents) {
                    sources {
                        extension.documents.forEach { include it }
                    }
                }
                outputDir = project.file("${project.buildDir}/docs")
                backends "docbook"
                attributes extension.asciidocAttributes + [imagesdir: "./images"]
                resources {
                    from("src/docs/resources") {
                        include "images/**"
                        include "attachments/**"
                    }
                    into "./resources"
                }
            }
        }

        project.logger.debug("Adding docs html task")
        def docsHtmlTask = project.tasks.create(DOCS_HTML_TASK_NAME, AsciidoctorTask)
        docsHtmlTask.group = GROUP
        docsHtmlTask.description = "Renders HTML documentation"
        docsHtmlTask.outputs.upToDateWhen { false }
        project.afterEvaluate {
            docsHtmlTask.doFirst {
                def attributes = extension.asciidocAttributes + [
                        imagesdir : "./images",
                        stylesdir : asciidocBuildDir.absolutePath,
                        stylesheet: "leftshiftone-theme.css"
                ]
                project.logger.info("Using attributes ${attributes}")
                def styleSheet
                if (extension.htmlStyleSheet && extension.htmlStyleSheet.exists()) {
                    styleSheet = extension.htmlStyleSheet.newInputStream()
                } else {
                    styleSheet = getClass().getResourceAsStream("/styles/html/leftshiftone-theme.css")
                }
                Files.copy(styleSheet, new File(asciidocBuildDir, "leftshiftone-theme.css").toPath(), REPLACE_EXISTING)
            }
            docsHtmlTask.configure {
                if (extension.documents) {
                    sources {
                        extension.documents.forEach { include it }
                    }
                }
                outputDir = project.file("${project.buildDir}/docs")
                backends "html5"
                attributes extension.asciidocAttributes + [
                        imagesdir : "./images",
                        stylesdir : asciidocBuildDir.absolutePath,
                        stylesheet: "leftshiftone-theme.css"
                ]
                resources {
                    from("src/docs/resources") {
                        include "images/**"
                        include "attachments/**"
                    }
                    into "./resources"
                }
            }
        }

        project.logger.debug("Adding docs pdf task")
        def docsPdfTask = project.tasks.create(DOCS_PDF_TASK_NAME, AsciidoctorTask)
        docsPdfTask.group = GROUP
        docsPdfTask.description = "Renders PDF documentation"
        docsPdfTask.outputs.upToDateWhen { false }
        project.afterEvaluate {
            docsPdfTask.doFirst {
                def attributes = extension.asciidocAttributes + [
                        "pdf-stylesdir": asciidocBuildDir.absolutePath,
                        "pdf-style"    : "leftshiftone-theme.yml"
                ]
                project.logger.info("Using attributes ${attributes}")
                def pdfStyle
                if (extension.pdfStyle && extension.pdfStyle.exists()) {
                    pdfStyle = extension.pdfStyle.newInputStream()
                } else {
                    pdfStyle = getClass().getResourceAsStream("/styles/pdf/leftshiftone-theme.yml")
                }
                Files.copy(pdfStyle, new File(asciidocBuildDir, "leftshiftone-theme.yml").toPath(), REPLACE_EXISTING)
            }
            docsPdfTask.configure {
                if (extension.documents) {
                    sources {
                        extension.documents.forEach { include it }
                    }
                }
                outputDir = project.file("${project.buildDir}/docs")
                backends "pdf"
                attributes extension.asciidocAttributes + [
                        "pdf-stylesdir": asciidocBuildDir.absolutePath,
                        "pdf-style"    : "leftshiftone-theme.yml"
                ]
                resources {
                    from("src/docs/resources") {
                        include "attachments/**"
                    }
                    into "./resources"
                }
            }
        }

        def docsTask = project.tasks.create(DOCS_TASK_NAME)
        docsTask.group = GROUP
        docsTask.description = "Renders documentation"
        docsTask.dependsOn DOCS_XML_TASK_NAME, DOCS_HTML_TASK_NAME, DOCS_PDF_TASK_NAME
        docsTask.outputs.upToDateWhen { false }

        project.logger.debug("Adding docsJar task")
        def docsJarTask = project.tasks.create(DOCS_JAR_TASK_NAME, Jar)
        docsJarTask.group = GROUP
        docsJarTask.description = "Creates a jar file with docs sources"
        project.afterEvaluate {
            docsJarTask.configure {
                from project.files(project.sourceSets.docs.allSource)
                classifier "docs"
            }
        }

        project.logger.debug("Adding docsZip task")
        def docsZipTask = project.tasks.create(DOCS_ZIP_TASK_NAME, Zip)
        docsZipTask.group = GROUP
        docsZipTask.description = "Creates a zip file with rendered docs"
        docsZipTask.dependsOn DOCS_TASK_NAME
        project.afterEvaluate {
            docsZipTask.configure {
                from project.files("${project.buildDir}/docs")
            }
        }

        project.logger.debug("Adding graphqlToAsciidoc task")
        def graphQlToAsciiDocTask = project.task(DOCS_GRAPHQL_TO_ASCIIDOC_TASK_NAME, type: GraphQlSchemaToAsciiDocTask)
    }
}
