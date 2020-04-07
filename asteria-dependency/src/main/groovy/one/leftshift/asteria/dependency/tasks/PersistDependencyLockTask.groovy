package one.leftshift.asteria.dependency.tasks

import one.leftshift.asteria.dependency.AsteriaDependencyExtension
import one.leftshift.asteria.dependency.AsteriaDependencyPlugin
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException
import org.eclipse.jgit.errors.NoRemoteRepositoryException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class PersistDependencyLockTask extends DefaultTask {

    PersistDependencyLockTask() {
        group = AsteriaDependencyPlugin.GROUP
        description = "Commit and push dependency lock file."
    }

    @TaskAction
    def persistDependencyLock() {
        def extension = project.extensions.getByType(AsteriaDependencyExtension)

        Grgit git = Grgit.open(dir: project.rootProject.projectDir.absolutePath)
        logger.debug("Staging files dependencies.lock")
        def status = git.status()
        def changes = status.unstaged.added + status.unstaged.modified
        changes = changes.findAll { it.contains("dependencies.lock") }
        if (changes) {
            git.add(patterns: changes.findAll { it.contains("dependencies.lock") })
            logger.debug("Committing changes")
            git.commit(message: extension.commitMessage)
        }

        try {
            logger.debug("Pulling changes from remote")
            git.pull()
            logger.debug("Pushing changes to remote")
            git.push()
        } catch (NoRemoteRepositoryException ex) {
            logger.debug("No remote repository available (${ex.message})")
            logger.warn("Unable to push to remote repository because no remote repository is available")
        } catch (GrgitException ex) {
            if (ex.cause) {
                logger.error("Error pushing to remote: ${ex.cause.message}")
            } else {
                logger.error("Error pushing to remote: ${ex.message}")
            }
        } finally {
            git.close()
        }
    }
}
