package one.leftshift.asteria.deploy.replace

/**
 * Replaces tokens in a given {@link ReplaceRequest#filePath} with the tokens specified in {@link ReplaceRequest#tokens}
 */
interface TokenReplacer {
    Writable replace(ReplaceRequest replaceDefinition)
}