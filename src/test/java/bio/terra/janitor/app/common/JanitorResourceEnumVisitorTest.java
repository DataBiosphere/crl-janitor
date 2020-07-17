package bio.terra.janitor.app.common;

import static bio.terra.janitor.common.JanitorResourceTypeEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.generated.model.*;
import bio.terra.janitor.app.Main;
import bio.terra.janitor.common.JanitorResourceEnumVisitor;
import bio.terra.janitor.common.exception.InvalidResourceUidException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Tag("unit")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Main.class)
@SpringBootTest
public class JanitorResourceEnumVisitorTest {
  @Autowired private JanitorResourceEnumVisitor visitor;

  @Test
  public void acceptGoogleProject() {
    assertEquals(
        GOOGLE_PROJECT,
        visitor.accept(
            new CloudResourceUid()
                .googleProjectUid(new GoogleProjectUid().projectId("my-project"))));
  }

  @Test
  public void acceptGoogleBigQueryDataset() {
    assertEquals(
        GOOGLE_BIG_QUERY_DATASET,
        visitor.accept(
            new CloudResourceUid()
                .googleBigQueryDatasetUid(
                    new GoogleBigQueryDatasetUid()
                        .projectId("my-project")
                        .datasetId("my-dataset"))));
  }

  @Test
  public void acceptGoogleBigQueryTable() {
    assertEquals(
        GOOGLE_BIG_QUERY_TABLE,
        visitor.accept(
            new CloudResourceUid()
                .googleBigQueryTableUid(
                    new GoogleBigQueryTableUid()
                        .projectId("my-project")
                        .datasetId("my-dataset")
                        .tableId("my-table"))));
  }

  @Test
  public void acceptGoogleBlob() {
    assertEquals(
        GOOGLE_BLOB,
        visitor.accept(
            new CloudResourceUid()
                .googleBlobUid(new GoogleBlobUid().bucketName("my-bucket").blobName("my-blob"))));
  }

  @Test
  public void acceptGoogleBucket() {
    assertEquals(
        GOOGLE_BUCKET,
        visitor.accept(
            new CloudResourceUid().googleBucketUid(new GoogleBucketUid().bucketName("my-bucket"))));
  }

  @Test
  public void acceptEmpty() {
    assertThrows(InvalidResourceUidException.class, () -> visitor.accept(new CloudResourceUid()));
  }
}