package one.leftshift.asteria.report

import org.ajoberstar.grgit.Grgit
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore("This test is just for testing the capabilities of Grgit")
class GrgitTest extends Specification {

    @Shared
    Grgit git

    def setupSpec() {
        git = Grgit.open(dir: "../")
    }

    def "Grgit retrieves current branch and repository"() {
        expect:
            git.branch.current.name == "master"
            String originUrl = git.remote.list().find { it.name == "origin" }.url
            originUrl.replace(".git", "").split(":").last().split("/").last() == "asteria"
    }
}
