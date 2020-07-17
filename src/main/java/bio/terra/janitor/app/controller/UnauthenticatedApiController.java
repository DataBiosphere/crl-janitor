package bio.terra.janitor.app.controller;

import bio.terra.generated.controller.UnauthenticatedApi;
import bio.terra.generated.model.SystemStatus;
import bio.terra.generated.model.SystemStatusSystems;
import bio.terra.janitor.app.configuration.JanitorJdbcConfiguration;
import bio.terra.janitor.service.stairway.StairwayComponent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class UnauthenticatedApiController implements UnauthenticatedApi {
  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final StairwayComponent stairwayComponent;

  @Autowired
  UnauthenticatedApiController(
      JanitorJdbcConfiguration jdbcConfiguration, StairwayComponent stairwayComponent) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcConfiguration.getDataSource());
    this.stairwayComponent = stairwayComponent;
  }

  @Override
  public ResponseEntity<SystemStatus> serviceStatus() {
    SystemStatus systemStatus = new SystemStatus();

    final boolean postgresOk =
        jdbcTemplate.getJdbcTemplate().execute((Connection connection) -> connection.isValid(0));
    systemStatus.putSystemsItem("postgres", new SystemStatusSystems().ok(postgresOk));

    StairwayComponent.Status stairwayStatus = stairwayComponent.getStatus();
    final boolean stairwayOk = stairwayStatus.equals(StairwayComponent.Status.OK);
    systemStatus.putSystemsItem(
        "stairway",
        new SystemStatusSystems().ok(stairwayOk).addMessagesItem(stairwayStatus.toString()));

    systemStatus.ok(postgresOk && stairwayOk);
    if (systemStatus.isOk()) {
      return new ResponseEntity<>(systemStatus, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(systemStatus, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /** The service will shutdown soon. Halt anything we'd rather not interrupt. */
  @Override
  public ResponseEntity<Void> shutdownRequest() {
    try {
      if (!stairwayComponent.shutdown()) {
        // Stairway shutdown did not complete. Return an error so the caller knows that.
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (InterruptedException ex) {
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /** Required if using Swagger-CodeGen, but actually we don't need this. */
  @Override
  public Optional<ObjectMapper> getObjectMapper() {
    return Optional.empty();
  }

  /** Required if using Swagger-CodeGen, but actually we don't need this. */
  @Override
  public Optional<HttpServletRequest> getRequest() {
    return Optional.empty();
  }
}
