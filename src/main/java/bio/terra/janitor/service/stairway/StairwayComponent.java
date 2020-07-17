package bio.terra.janitor.service.stairway;

import bio.terra.janitor.app.configuration.StairwayConfiguration;
import bio.terra.janitor.app.configuration.StairwayJdbcConfiguration;
import bio.terra.stairway.Stairway;
import bio.terra.stairway.exception.StairwayException;
import com.google.common.collect.ImmutableList;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/** A Spring Component for exposing an initialized {@link Stairway}. */
@Component
public class StairwayComponent {
  private final Logger logger = LoggerFactory.getLogger(StairwayComponent.class);

  private final StairwayConfiguration stairwayConfiguration;
  private final StairwayJdbcConfiguration stairwayJdbcConfiguration;
  private final Stairway stairway;

  public enum Status {
    INITIALIZING,
    OK,
    ERROR,
    SHUTDOWN,
  };

  private Status status = Status.INITIALIZING;

  @Autowired
  public StairwayComponent(
      ApplicationContext applicationContext,
      StairwayConfiguration stairwayConfiguration,
      StairwayJdbcConfiguration stairwayJdbcConfiguration) {
    this.stairwayConfiguration = stairwayConfiguration;
    this.stairwayJdbcConfiguration = stairwayJdbcConfiguration;

    logger.info(
        "Creating Stairway: name: [{}]  cluster name: [{}]",
        stairwayConfiguration.getClusterName(),
        stairwayConfiguration.getClusterName());
    // TODO(CA-941): Add projectId for Stairway to make a pubsub work queue.
    stairway =
        Stairway.newBuilder()
            .maxParallelFlights(stairwayConfiguration.getMaxParallelFlights())
            .applicationContext(applicationContext)
            .stairwayName(stairwayConfiguration.getName())
            .stairwayClusterName(stairwayConfiguration.getClusterName())
            .build();
  }

  public void initialize() {
    logger.warn("stairway username {}", stairwayJdbcConfiguration.getUsername());
    try {
      // TODO(CA-941): Determine if Stairway and Janitor database migrations need to be coordinated.
      stairway.initialize(
          stairwayJdbcConfiguration.getDataSource(),
          stairwayConfiguration.isForceCleanStart(),
          stairwayConfiguration.isMigrateUpgrade());
      // TODO(CA-941): Get obsolete Stairway instances from k8s for multi-instance stairway.
      stairway.recoverAndStart(ImmutableList.of());
    } catch (StairwayException | InterruptedException e) {
      status = Status.ERROR;
      throw new RuntimeException("Error starting Stairway", e);
    }
    status = Status.OK;
  }

  /** Stop accepting jobs and shutdown stairway. Returns true if successful. */
  public boolean shutdown() throws InterruptedException {
    status = Status.SHUTDOWN;
    logger.info("Request Stairway shutdown");
    boolean shutdownSuccess =
        stairway.quietDown(
            stairwayConfiguration.getQuietDownTimeout().toMillis(), TimeUnit.MILLISECONDS);
    if (!shutdownSuccess) {
      logger.info("Request Stairway terminate");
      shutdownSuccess =
          stairway.terminate(
              stairwayConfiguration.getTerminateTimeout().toMillis(), TimeUnit.MILLISECONDS);
    }
    logger.info("Finished Stairway shutdown?: {}", shutdownSuccess);
    return shutdownSuccess;
  }

  public Stairway get() {
    return stairway;
  }

  public StairwayComponent.Status getStatus() {
    return status;
  }
}