package one.leftshift.asteria.docs

class AsteriaDocsExtension {

    public static final String GRAPHQL_INTROSPECTION_QUERY = """query IntrospectionQuery {
      __schema {
        queryType { name }
        mutationType { name }
        subscriptionType { name }
        types {
          ...FullType
        }
        directives {
          name
          description
          locations
          args {
            ...InputValue
          }
        }
      }
    }
    fragment FullType on __Type {
      kind
      name
      description
      fields(includeDeprecated: true) {
        name
        description
        args {
          ...InputValue
        }
        type {
          ...TypeRef
        }
        isDeprecated
        deprecationReason
      }
      inputFields {
        ...InputValue
      }
      interfaces {
        ...TypeRef
      }
      enumValues(includeDeprecated: true) {
        name
        description
        isDeprecated
        deprecationReason
      }
      possibleTypes {
        ...TypeRef
      }
    }
    fragment InputValue on __InputValue {
      name
      description
      type { ...TypeRef }
      defaultValue
    }
    fragment TypeRef on __Type {
      kind
      name
      ofType {
        kind
        name
        ofType {
          kind
          name
          ofType {
            kind
            name
            ofType {
              kind
              name
              ofType {
                kind
                name
                ofType {
                  kind
                  name
                  ofType {
                    kind
                    name
                  }
                }
              }
            }
          }
        }
      }
    }""".stripIndent()

    /**
     * Root documents where for each one a separate documentation artifact is created.
     */
    List<String> documents = []

    /**
     * Asciidoc attributes for configuring asciidoc rendering. For more information see https://asciidoctor.org/docs/asciidoc-syntax-quick-reference/#attributes-and-substitutions.
     */
    Map<String, Object> asciidocAttributes = [
            "doctype"           : "book",
            "source-highlighter": "coderay",
            "toc"               : "",
            "numbered"          : "true",
            "xrefstyle"         : "short",
            "icons"             : "font",
            "setanchors"        : "true",
            "idprefix"          : "",
            "idseparator"       : "-"
    ]

    /**
     * Style sheet for HTML rendering. Default is the stylesheet provided by this plugin (/styles/html/leftshiftone-theme.css).
     */
    File htmlStyleSheet

    /**
     * Style sheet for PDF rendering. Default is the stylesheet provided by this plugin (/styles/pdf/leftshiftone-theme.yml).
     */
    File pdfStyle

    /**
     * GraphQL query to get the schema from a GraphQL endpoint (taken from https://github.com/graphql/graphql-js/blob/master/src/utilities/introspectionQuery.js).
     */
    String graphQlIntrospectionQuery = GRAPHQL_INTROSPECTION_QUERY
}
