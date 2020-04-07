package one.leftshift.asteria.deploy.replace

import groovy.text.SimpleTemplateEngine
import groovy.text.TemplateEngine
import one.leftshift.asteria.util.Assert

/**
 * Replaces tokens using the {@link groovy.text.SimpleTemplateEngine}
 */
class SimpleTokenReplacer implements TokenReplacer {

    @Override
    Writable replace(ReplaceRequest replaceDefinition) {
        Assert.notNull(replaceDefinition, replaceDefinition.filePath, replaceDefinition.tokens)
        final TemplateEngine engine = new SimpleTemplateEngine()
        return engine
                .createTemplate(replaceDefinition.filePath.toFile())
                .make(replaceDefinition.tokens)
    }
}
