package one.leftshift.asteria.deploy.replace

import groovy.transform.TupleConstructor

import java.nio.file.Path

@TupleConstructor
class ReplaceRequest {
    final Path filePath
    final Map<String, String> tokens
}
