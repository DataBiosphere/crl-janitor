package bio.terra.janitor.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@Tag("unit")
@ActiveProfiles("unit")
public class TrackedResourceIdTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void serialize() throws Exception {
    UUID id = UUID.randomUUID();
    TrackedResourceId trackedResourceId = TrackedResourceId.create(id);
    assertEquals(
        "[\"bio.terra.janitor.db.AutoValue_TrackedResourceId\",{\"uuid\":\""
            + id.toString()
            + "\"}]",
        objectMapper.writeValueAsString(trackedResourceId));
  }

  @Test
  public void deserialize() throws Exception {
    UUID id = UUID.randomUUID();
    TrackedResourceId trackedResourceId = TrackedResourceId.create(id);
    String serialized = objectMapper.writeValueAsString(trackedResourceId);
    TrackedResourceId deserialized = objectMapper.readValue(serialized, TrackedResourceId.class);
    assertEquals(trackedResourceId, deserialized);
  }
}
