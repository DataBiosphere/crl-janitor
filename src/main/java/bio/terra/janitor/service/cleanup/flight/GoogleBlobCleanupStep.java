package bio.terra.janitor.service.cleanup.flight;

import bio.terra.cloudres.google.storage.StorageCow;
import bio.terra.janitor.db.JanitorDao;
import bio.terra.janitor.db.ResourceMetadata;
import bio.terra.janitor.generated.model.CloudResourceUid;
import bio.terra.stairway.StepResult;
import bio.terra.stairway.StepStatus;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Step to cleanup Google Blob resource. */
public class GoogleBlobCleanupStep extends ResourceCleanupStep {
  private final Logger logger = LoggerFactory.getLogger(GoogleBlobCleanupStep.class);
  private final StorageCow storageCow;

  public GoogleBlobCleanupStep(StorageCow storageCow, JanitorDao janitorDao) {
    super(janitorDao);
    this.storageCow = storageCow;
  }

  @Override
  protected StepResult cleanUp(CloudResourceUid resourceUid, ResourceMetadata metadata) {
    try {
      BlobId blobId =
          BlobId.of(
              resourceUid.getGoogleBlobUid().getBucketName(),
              resourceUid.getGoogleBlobUid().getBlobName());
      storageCow.delete(blobId);
      return StepResult.getStepResultSuccess();
    } catch (StorageException e) {
      logger.warn("Google StorageException occurs during Blob Cleanup", e);
      // Catch all exceptions from GOOGLE and consider this retryable error.
      return new StepResult(StepStatus.STEP_RESULT_FAILURE_RETRY, e);
    }
  }
}
