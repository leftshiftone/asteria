package one.leftshift.asteria.dependency.tasks

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.AmazonS3URI
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion
import one.leftshift.asteria.common.branch.BranchResolver
import one.leftshift.asteria.dependency.AsteriaDependencyExtension
import one.leftshift.asteria.dependency.AsteriaDependencyPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * Clean branch snapshots artifacts.
 *
 * @author Michael Mair
 */
class CleanBranchSnapshotArtifactsTask extends DefaultTask {

    private String branchName = null

    CleanBranchSnapshotArtifactsTask() {
        group = AsteriaDependencyPlugin.GROUP
        description = "Delete S3 Maven artifacts from branch snapshot repository."
    }

    @Input
    String getBranchName() {
        return branchName
    }

    @Option(option = "branchName", description = "Branch name (e.g. feature/GAIA-1000-foo)")
    void setBranchName(String branchName) {
        this.branchName = branchName
    }

    @TaskAction
    def clean() {
        AsteriaDependencyExtension extension = project.extensions.getByType(AsteriaDependencyExtension)
        AmazonS3URI snapshotUri = new AmazonS3URI(extension.snapshotRepositoryUrl)

        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(snapshotUri.region).build()
        if (!s3) {
            throw new RuntimeException("S3 client could not be initialized.")
        }

        String ticketId = BranchResolver.getTicketIdBasedOnBranch(branchName)
        if (!ticketId) {
            logger.quiet("No ticket id found in branch ${branchName}")
            return
        }

        String uri = "${extension.snapshotRepositoryUrl}-${ticketId}"
        String group = project.rootProject.group.toString().replace(".", "/")
        String artifactsUri = "${uri}/${group}"
        AmazonS3URI branchSnapshotUri = new AmazonS3URI(artifactsUri)
        logger.quiet("Deleting snapshots in ${artifactsUri}")

        List<KeyVersion> keysToDelete = s3.listObjects(branchSnapshotUri.bucket, branchSnapshotUri.key)
                .getObjectSummaries()
                .collect { summary -> new KeyVersion(summary.key) }

        def deleteRequest = new DeleteObjectsRequest()
                .withBucketName(branchSnapshotUri.bucket)
        deleteRequest.setKeys(keysToDelete)
        def deleteResult = s3.deleteObjects(deleteRequest)
        logger.quiet("${deleteResult.deletedObjects.size()} objects deleted")
    }

}
