package one.leftshift.asteria.docs.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class DocsTasksTest extends Specification {

    def "xml doc is available"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "docsXml", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def docResult = new File("${project.projectDir}/project-docs/build/docs/docbook/index.xml")
            def imageFileResult = new File("${project.projectDir}/project-docs/build/docs/docbook/resources/images/infrastructure-overview.svg")
            def attachmentFileResult = new File("${project.projectDir}/project-docs/build/docs/docbook/resources/attachments/schema.xsd")

        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Using attributes [doctype:book, source-highlighter:coderay, toc:, numbered:true, xrefstyle:short, icons:font, setanchors:true, idprefix:, idseparator:-, foo:bar, version:0.0.1-SNAPSHOT, revnumber:0.0.1-SNAPSHOT, imagesdir:./images]"
            docResult.exists()
            docResult.text.contains("<revnumber>0.0.1-SNAPSHOT</revnumber>")
            docResult.text.contains("<imagedata fileref=\"./resources/images/infrastructure-overview.svg\" contentwidth=\"100%\"/>")
            docResult.text.contains("<anchor xml:id=\"figure-infrastructure-overview\" xreflabel=\"[figure-infrastructure-overview]\"/>")
            docResult.text.contains("Dumm ist der, der Dummes tut.")
            imageFileResult.exists()
            attachmentFileResult.exists()
    }

    def "html doc is available"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }

        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "docsHtml", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def docResult = new File("${project.projectDir}/project-docs/build/docs/html5/index.html")
            def imageFileResult = new File("${project.projectDir}/project-docs/build/docs/html5/resources/images/infrastructure-overview.svg")
            def attachmentFileResult = new File("${project.projectDir}/project-docs/build/docs/html5/resources/attachments/schema.xsd")


        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Using attributes [doctype:book, source-highlighter:coderay, toc:, numbered:true, xrefstyle:short, icons:font, setanchors:true, idprefix:, idseparator:-, foo:bar, version:0.0.1-SNAPSHOT, revnumber:0.0.1-SNAPSHOT, imagesdir:./images, stylesdir:${project.projectDir.absolutePath}${File.separator}project-docs${File.separator}build${File.separator}asciidoc, stylesheet:leftshiftone-theme.css]"
            docResult.exists()
            docResult.text.contains("<span id=\"revnumber\">version 0.0.1-SNAPSHOT,</span>")
            docResult.text.contains("<img src=\"./resources/images/infrastructure-overview.svg\" alt=\"infrastructure overview\" width=\"100%\">")
            docResult.text.contains("<div class=\"title\">Figure 1. <a id=\"figure-infrastructure-overview\"></a>Infrastructure Overview</div>")
            docResult.text.contains("Dumm ist der, der Dummes tut.")
            docResult.text.contains("color: #010622")
            docResult.text.contains("color: #455156")
            imageFileResult.exists()
            attachmentFileResult.exists()

    }

    def "pdf doc is available"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "docsPdf", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def docResult = new File("${project.projectDir}/project-docs/build/docs/pdf/index.pdf")
            def attachmentFileResult = new File("${project.projectDir}/project-docs/build/docs/pdf/resources/attachments/schema.xsd")
        then:
            result.output.contains "BUILD SUCCESSFUL"
            result.output.contains "Using attributes [doctype:book, source-highlighter:coderay, toc:, numbered:true, xrefstyle:short, icons:font, setanchors:true, idprefix:, idseparator:-, foo:bar, version:0.0.1-SNAPSHOT, revnumber:0.0.1-SNAPSHOT, pdf-stylesdir:${project.projectDir}/project-docs/build/asciidoc, pdf-style:leftshiftone-theme.yml]"
            !result.output.contains("WARNING: image to embed not found or not readable")
            docResult.exists()
            docResult.text.contains("/Title (AsciiDoc Article Title)")
            docResult.text.contains("(fifth-level-heading) 33 0 R (figure-infrastructure-overview)")
            docResult.text.contains("/URI (mailto:support@leftshift.one)")
            attachmentFileResult.exists()
    }

    def "all doc is available"() {
        given:
            Project project = ProjectBuilder.builder().build()
            new AntBuilder().copy(todir: project.projectDir.absolutePath) {
                fileset(dir: "src/test/resources/testProject")
            }
        when:
            def result = GradleRunner.create()
                    .withProjectDir(project.projectDir)
                    .withArguments("build", "docs", "--debug", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            println result.output
            def xmlDocResult = new File("${project.projectDir}/project-docs/build/docs/docbook/index.xml")
            def htmlDocResult = new File("${project.projectDir}/project-docs/build/docs/html5/index.html")
            def pdfDocResult = new File("${project.projectDir}/project-docs/build/docs/pdf/index.pdf")
        then:
            result.output.contains "BUILD SUCCESSFUL"
            xmlDocResult.exists()
            htmlDocResult.exists()
            pdfDocResult.exists()
    }

}
