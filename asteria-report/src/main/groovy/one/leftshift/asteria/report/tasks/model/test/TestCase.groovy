package one.leftshift.asteria.report.tasks.model.test

import groovy.transform.ToString

import static java.time.Duration.ZERO

@ToString(includeNames = true, includeFields = true, includePackage = false, ignoreNulls = true)
class TestCase {
    String name
    String duration = ZERO.toString()

    String testClassName
    Boolean passed = false
    Boolean skipped = false
    String failureMessage

    String getFullName() {
        return [testClassName, name].join(".")
    }
}
