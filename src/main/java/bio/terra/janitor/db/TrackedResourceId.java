package bio.terra.janitor.db;

import com.google.auto.value.AutoValue;
import java.util.UUID;

/** Wraps the tracked_resource_id in db tracked_resource table. */
@AutoValue
public abstract class TrackedResourceId {
  public abstract UUID id();

  public static TrackedResourceId create(UUID id) {
    return new AutoValue_TrackedResourceId.Builder().setId(id).build();
  }

  @Override
  public String toString() {
    return id().toString();
  }

  /** Builder for {@link TrackedResourceId}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(UUID value);

    public abstract TrackedResourceId build();
  }
}