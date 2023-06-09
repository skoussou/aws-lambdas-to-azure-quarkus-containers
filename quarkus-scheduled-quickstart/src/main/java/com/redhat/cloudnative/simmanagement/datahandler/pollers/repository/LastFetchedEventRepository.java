package com.redhat.cloudnative.simmanagement.datahandler.pollers.repository;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.redhat.cloudnative.simmanagement.datahandler.pollers.function.ChangeId;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@ApplicationScoped
public class LastFetchedEventRepository {

  @Inject
  CosmosConnection connection;

  CosmosContainer table;

  private static final String DEVICE_CHANGE_ID_KEY_VALUE = "DEVICE";
  private static final String DMD_CHANGE_TIMESTAMP_KEY = "DMD_EVENT_POLL_TIMESTAMP";
  private static final Logger logger = LoggerFactory.getLogger(LastFetchedEventRepository.class);

  public LastFetchedEventRepository() {}

  @PostConstruct
  public void initialise() {
    this.table = connection.getContainer();
  }

  public String getLastDeviceChangeIdReturnsNull(String tenant) {
    return getLastChangeId(DEVICE_CHANGE_ID_KEY_VALUE, tenant);
  }

  public void putLastDeviceChangeId(String lastChangeId, String tenant) {
    putNewLastEntry(DEVICE_CHANGE_ID_KEY_VALUE, lastChangeId, tenant);
  }

  public void putNewLastEntry(String keyValue, String lastChangeId, String tenant) {
    table.upsertItem(new ChangeId(keyValue, keyValue, lastChangeId));
    logger.debug("Saved {} key with value {} to the table.", keyValue, lastChangeId);
  }

  private String getLastChangeId(String keyValue, String tenant) {
    CosmosItemResponse<ChangeId> response= null;
    try{
      response = table.readItem(keyValue, new PartitionKey(keyValue), ChangeId.class);
    }
    catch(ResourceNotFoundException rnfe) {
      logger.info("keyValue : "+keyValue+", not found in table : "+table.getId());
    }
    catch (RuntimeException re) {
      logger.error(re.getMessage());
    }
    return response.getItem().getValue();
  }

  public void saveLastDmdEventTimestamp(String timestamp, String tenant) {
    table.upsertItem(new ChangeId(DMD_CHANGE_TIMESTAMP_KEY, DMD_CHANGE_TIMESTAMP_KEY, timestamp));
    logger.debug("Saved DMD Timestamp {} to table {}", timestamp);
  }

  public String getLastDmdEventTimestamp(String tenant) {
    CosmosItemResponse<ChangeId> response = null;
    try{
      response = table.readItem(DMD_CHANGE_TIMESTAMP_KEY, new PartitionKey(DMD_CHANGE_TIMESTAMP_KEY), ChangeId.class);
    }
    catch(ResourceNotFoundException rnfe) {
      logger.info("DMD_CHANGE_TIMESTAMP_KEY : "+DMD_CHANGE_TIMESTAMP_KEY+", not found in table : "+table.getId());
    }
    catch (RuntimeException re) {
      logger.error(re.getMessage());
    }     
    return response.getItem().getValue();
  }

}
